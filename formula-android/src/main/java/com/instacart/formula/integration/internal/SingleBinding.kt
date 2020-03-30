package com.instacart.formula.integration.internal

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.FormulaContext
import com.instacart.formula.RxStream
import com.instacart.formula.integration.Binding
import com.instacart.formula.integration.Integration
import com.instacart.formula.integration.KeyState

/**
 * Defines how a specific key should be bound to it's [Integration],
 */
internal class SingleBinding<Component, Key : Any, State : Any>(
    private val type: Class<Key>,
    private val integration: Integration<Component, Key, State>
) : Binding<Component, Key>(), Formula<Binding.Input<Component, Key>, Unit, Unit> {

    override fun binds(key: Any): Boolean {
        return type.isInstance(key)
    }

    override fun bind(context: FormulaContext<*>, input: Input<Component, Key>) {
        context.child(type, this).input(input)
    }

    override fun initialState(input: Input<Component, Key>) =  Unit

    override fun evaluate(
        input: Input<Component, Key>,
        state: Unit,
        context: FormulaContext<Unit>
    ): Evaluation<Unit> {
        return Evaluation(
            renderModel = state,
            updates = context.updates {
                input.activeKeys.forEachIndices { key ->
                    if (binds(key)) {
                        val stream = RxStream.fromObservable(key) {
                            integration.create(input.component, key)
                        }
                        events(stream) { update ->
                            transition {
                                input.onStateChanged(KeyState(key, update))
                            }
                        }
                    }
                }

            }
        )
    }
}
