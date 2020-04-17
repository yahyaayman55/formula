package com.instacart.formula.stopwatch

import com.instacart.formula.Evaluation
import com.instacart.formula.FormulaContext
import com.instacart.formula.StatelessFormula
import java.util.concurrent.TimeUnit

class StopwatchUI(
    private val stopwatch: Stopwatch<*>
) : StatelessFormula<Unit, StopwatchRenderModel>() {

    data class State(
        val timePassedInMillis: Long,
        val isRunning: Boolean
    )

    override fun evaluate(input: Unit, context: FormulaContext<Unit>): Evaluation<StopwatchRenderModel> {
        val stopwatch = context.child(stopwatch).input(Unit)
        return Evaluation(
            renderModel = StopwatchRenderModel(
                timePassed = formatTimePassed(stopwatch.timePassedInMillis),
                startStopButton = startStopButton(stopwatch),
                resetButton = resetButton(stopwatch)
            )
        )
    }

    private fun formatTimePassed(timePassedInMillis: Long): String {
        return buildString {
            val minutesPassed = TimeUnit.MILLISECONDS.toMinutes(timePassedInMillis)
            if (minutesPassed > 0) {
                append(minutesPassed)
                append('m')
                append(' ')
            }

            val secondsPassed = TimeUnit.MILLISECONDS.toSeconds(timePassedInMillis) % 60
            append(secondsPassed)
            append('s')
            append(' ')

            // Always show millis as two digits
            val millisPassed = (timePassedInMillis % 1000) / 10
            if (millisPassed < 10) {
                append('0')
            }
            append(millisPassed)
        }
    }

    private fun startStopButton(stopwatch: Stopwatch.Output): ButtonRenderModel {
        return ButtonRenderModel(
            text = if (stopwatch.isRunning) "Stop" else "Start",
            onSelected = stopwatch.toggle
        )
    }

    private fun resetButton(stopwatch: Stopwatch.Output): ButtonRenderModel {
        return ButtonRenderModel(
            text = "Reset",
            onSelected = stopwatch.reset
        )
    }
}
