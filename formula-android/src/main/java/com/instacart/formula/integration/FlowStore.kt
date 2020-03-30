package com.instacart.formula.integration

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.FormulaContext
import com.instacart.formula.integration.internal.BackStackUtils
import com.instacart.formula.start
import io.reactivex.Observable

/**
 * A store that manages render model changes for each entry in the [BackStack].
 *
 * A simple example of how to initialize a store.
 * ```
 * val backstack = BackStackStore<Key>()
 * FlowStore.init(backstack.changes()) {
 *     bind(TaskListIntegration())
 *     bind(TaskDetailIntegration())
 *     bind(SettingsIntegration())
 * }
 * ```
 */
class FlowStore<Key : Any> constructor(
    private val keyState: Observable<BackStack<Key>>,
    private val root: Binding<Unit, Key>
) : Formula<Unit, FlowState<Key>, FlowState<Key>> {
    companion object {
        inline fun <Key : Any> init(
            state: Observable<BackStack<Key>>,
            crossinline init: BindingBuilder<Unit, Key>.() -> Unit
        ): FlowStore<Key> {
            return init(Unit, state, init)
        }

        inline fun <Component, Key : Any> init(
            rootComponent: Component,
            state: Observable<BackStack<Key>>,
            crossinline init: BindingBuilder<Component, Key>.() -> Unit
        ): FlowStore<Key> {
            val factory: (Unit) -> DisposableScope<Component> = {
                DisposableScope(component = rootComponent, onDispose = {})
            }

            val root = BindingBuilder<Component, Key>()
                .apply(init)
                .build()

            val rootBinding = Binding.composite(factory, root)
            return FlowStore(state, rootBinding)
        }
    }

    override fun initialState(input: Unit): FlowState<Key> = FlowState()

    override fun evaluate(
        input: Unit,
        state: FlowState<Key>,
        context: FormulaContext<FlowState<Key>>
    ): Evaluation<FlowState<Key>> {
        val rootInput = Binding.Input(
            component = Unit,
            activeKeys = state.backStack.keys,
            onStateChanged = context.eventCallback {
                transition(state.copy(states = state.states.plus(it.key to it)))
            }
        )
        root.bind(context, rootInput)

        return Evaluation(
            renderModel = state,
            updates = context.updates {
                events(keyState) { keys ->
                    val attachedKeys = BackStackUtils.findAttachedKeys(
                        lastActive = state.backStack.keys,
                        currentlyActive = keys.keys
                    )

                    val detached = BackStackUtils.findDetachedKeys(
                        lastActive = state.backStack.keys,
                        currentlyActive = keys.keys
                    )

                    // We want to emit an empty state update if key is not handled.
                    val notHandled = attachedKeys
                        .filter { !root.binds(it) }
                        .map { Pair(it, KeyState(it, "missing-registration")) }

                    val updated = state.copy(
                        backStack = keys,
                        states = state.states.minus(detached).plus(notHandled)
                    )
                    transition(updated)
                }
            }
        )
    }

    fun state(): Observable<FlowState<Key>> {
        return start()
    }
}
