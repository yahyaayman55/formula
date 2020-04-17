package com.instacart.formula.stopwatch

import com.instacart.formula.Formula

interface Stopwatch<State> : Formula<Unit, State, Stopwatch.Output> {
    data class Output(
        val timePassedInMillis: Long,
        val isRunning: Boolean,
        val toggle: () -> Unit,
        val reset: () -> Unit
    )
}