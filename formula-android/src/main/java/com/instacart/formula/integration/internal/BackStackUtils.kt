package com.instacart.formula.integration.internal

internal object BackStackUtils {
    fun <Key> findAttachedKeys(lastActive: List<Key>, currentlyActive: List<Key>): List<Key> {
        return currentlyActive.filter { !lastActive.contains(it) }
    }

    fun <Key> findDetachedKeys(lastActive: List<Key>, currentlyActive: List<Key>): List<Key> {
        return lastActive.filter { !currentlyActive.contains(it) }
    }
}
