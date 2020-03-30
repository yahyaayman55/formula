package com.instacart.formula.integration

import com.instacart.formula.FormulaContext
import com.instacart.formula.integration.internal.CompositeBinding
import com.instacart.formula.integration.internal.SingleBinding
import io.reactivex.Observable
import kotlin.reflect.KClass

/**
 * Defines how specific keys bind to the state management associated
 */
abstract class Binding<ParentComponent, Key : Any> {
    companion object {
        fun <Component, Key : Any, State : Any> single(
            type: KClass<Key>,
            stateInit: (Component, Key) -> Observable<State>
        ): Binding<Component, Key> {
            val integration = object : Integration<Component, Key, State>() {
                override fun create(component: Component, key: Key): Observable<State> {
                    return stateInit(component, key)
                }
            }

            return SingleBinding(type.java, integration)
        }

        fun <ParentComponent, Component, Key : Any> composite(
            scopeFactory: ComponentFactory<ParentComponent, Component>,
            bindings: List<Binding<Component, Key>>
        ): Binding<ParentComponent, Key> {
            return CompositeBinding(scopeFactory, bindings)
        }
    }

    data class Input<Component, Key : Any>(
        val component: Component,
        val activeKeys: List<Key>,
        val onStateChanged: (KeyState<Key>) -> Unit
    )

    /**
     * Returns true if this binding handles this [key]
     */
    internal abstract fun binds(key: Any): Boolean

    /**
     * Listens for active key changes and triggers [Input.onStateChanged] events.
     */
    internal abstract fun bind(context: FormulaContext<*>, input: Input<ParentComponent, Key>)
}
