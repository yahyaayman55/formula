package com.instacart.formula.internal

class EventCallback<T>: (T) -> Unit {
    @PublishedApi internal var callback: ((T) -> Unit)? = null

    override fun invoke(p1: T) {
        callback?.invoke(p1)
        // TODO: log if null callback
    }
}
