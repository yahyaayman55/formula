package com.instacart.formula.internal

class Callback(): () -> Unit {
    @PublishedApi internal var callback: (() -> Unit)? = null

    override fun invoke() {
        callback?.invoke()
        // TODO: log if null callback (it might be due to formula removal or due to callback removal)
    }
}
