package com.instacart.formula

/**
 * This interface provides ability to [Formula] to trigger transitions, instantiate updates and create
 * child formulas.
 */
abstract class FormulaContext<State, Output> {

    /**
     * Creates a callback to be used for handling UI event transitions.
     *
     * NOTE: this uses positional index to determine the key.
     */
    abstract fun callback(transition: Transition.Factory.() -> Transition<State, Output>): () -> Unit

    /**
     * Creates a callback if [condition] is true.
     */
    abstract fun optionalCallback(
        condition: Boolean,
        transition: Transition.Factory.() -> Transition<State, Output>
    ): (() -> Unit)?

    /**
     * Creates a callback to be used for handling UI event transitions.
     *
     * @param key Unique identifier that describes this callback
     */
    abstract fun callback(key: String, transition: Transition.Factory.() -> Transition<State, Output>): () -> Unit

    /**
     * Creates a callback that takes a [UIEvent] and performs a [Transition].
     *
     * NOTE: this uses positional index to determine the key.
     */
    abstract fun <UIEvent> eventCallback(transition: Transition.Factory.(UIEvent) -> Transition<State, Output>): (UIEvent) -> Unit

    /**
     * If [condition] is met, creates a callback that takes a [UIEvent] and performs a [Transition].
     *
     * NOTE: this uses positional index to determine the key.
     */
    abstract fun <UIEvent> optionalEventCallback(
        condition: Boolean,
        transition: Transition.Factory.(UIEvent) -> Transition<State, Output>
    ): ((UIEvent) -> Unit)?

    /**
     * Creates a callback that takes a [UIEvent] and performs a [Transition].
     *
     * @param key Unique identifier that describes this callback
     */
    abstract fun <UIEvent> eventCallback(
        key: String,
        transition: Transition.Factory.(UIEvent) -> Transition<State, Output>
    ): (UIEvent) -> Unit

    /**
     * Starts building a child [Formula]. The state management of child [Formula]
     * will be managed by the runtime. Call [Child.input] to finish declaring the child
     * and receive the [ChildRenderModel].
     */
    fun <ChildInput, ChildState, ChildOutput, ChildRenderModel> child(
        formula: Formula<ChildInput, ChildState, ChildOutput, ChildRenderModel>
    ): Child<State, Output, ChildInput, ChildOutput, ChildRenderModel> {
        return child("", formula)
    }

    /**
     * Starts building a child [Formula]. The state management of child [Formula]
     * will be managed by the runtime. Call [Child.input] to finish declaring the child
     * and receive the [ChildRenderModel].
     *
     * @param key A unique identifier for this formula.
     */
    abstract fun <ChildInput, ChildState, ChildOutput, ChildRenderModel> child(
        key: String,
        formula: Formula<ChildInput, ChildState, ChildOutput, ChildRenderModel>
    ): Child<State, Output, ChildInput, ChildOutput, ChildRenderModel>

    /**
     * Provides an [UpdateBuilder] that enables [Formula] to declare various events and effects.
     */
    abstract fun updates(init: UpdateBuilder<State, Output>.() -> Unit): List<Update>
}
