package com.instacart.formula.integration

/**
 * Representation of the [BackStack] and the state associated with each of the entries.
 *
 * @param Key type representing the entry in the [BackStack]
 */
data class FlowState<Key>(
    val backStack: BackStack<Key> = BackStack.empty(),
    val states: Map<Key, KeyState<Key>> = emptyMap()
) {
    // TODO: this method should be driven by visible fragments
    fun lastEntry(): KeyState<Key>? {
        val currentKey = backStack.keys.lastOrNull()
        return currentKey?.let { states[it] }
    }
}
