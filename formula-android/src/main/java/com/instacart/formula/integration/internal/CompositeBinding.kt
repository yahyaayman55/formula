package com.instacart.formula.integration.internal

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.FormulaContext
import com.instacart.formula.Stream
import com.instacart.formula.integration.Binding
import com.instacart.formula.integration.ComponentFactory
import com.instacart.formula.integration.DisposableScope

/**
 * Defines how a group of keys should be bound to their integrations.
 *
 * @param Key A key type associated with this binding.
 * @param ParentComponent A component associated with the parent. Often this will map to the parent dagger component.
 * @param ScopedComponent A component that is initialized when user enters this flow and is shared between
 *                  all the screens within the flow. Component will be destroyed when user exists the flow.
 */
internal class CompositeBinding<Key: Any, ParentComponent, ScopedComponent>(
    private val scopeFactory: ComponentFactory<ParentComponent, ScopedComponent>,
    private val bindings: List<Binding<ScopedComponent, Key>>
) : Binding<ParentComponent, Key>(),
    Formula<Binding.Input<ParentComponent, Key>, CompositeBinding.State<ScopedComponent>, Unit> {

    data class State<ScopedComponent>(
        val component: DisposableScope<ScopedComponent>?
    )

    override fun binds(key: Any): Boolean {
        bindings.forEachIndices {
            if (it.binds(key)) return true
        }
        return false
    }

    override fun bind(context: FormulaContext<*>, input: Input<ParentComponent, Key>) {
        context.child(this, this).input(input)
    }

    private fun resolveComponent(
        input: Input<ParentComponent, Key>,
        state: State<ScopedComponent>
    ): State<ScopedComponent> {
        val isInScope = input.activeKeys.any { binds(it) }
        val component = state.component
        return if (isInScope && component == null) {
            State(component = scopeFactory.invoke(input.component))
        } else if (!isInScope && component != null) {
            component.dispose()
            State(null)
        } else {
            state
        }
    }

    override fun initialState(input: Input<ParentComponent, Key>): State<ScopedComponent> {
        return resolveComponent(input, State(null))
    }

    override fun onInputChanged(
        oldInput: Input<ParentComponent, Key>,
        input: Input<ParentComponent, Key>,
        state: State<ScopedComponent>
    ): State<ScopedComponent> {
        return resolveComponent(input, state)
    }

    override fun evaluate(
        input: Input<ParentComponent, Key>,
        state: State<ScopedComponent>,
        context: FormulaContext<State<ScopedComponent>>
    ): Evaluation<Unit> {
        val component = state.component
        if (component != null) {
            val childInput = Input(component.component, input.activeKeys, input.onStateChanged)
            bindings.forEachIndices {
                it.bind(context, childInput)
            }
        }
        return Evaluation(
            renderModel = Unit,
            updates = context.updates {
                events(Stream.onTerminate()) {
                    transition {
                        component?.dispose()
                    }
                }
            }
        )
    }
}
