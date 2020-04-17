package com.instacart.formula.stopwatch

import com.instacart.formula.Evaluation
import com.instacart.formula.FormulaContext
import com.instacart.formula.RxStream
import com.instacart.formula.stopwatch.Stopwatch.Output
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class StopwatchImpl : Stopwatch<StopwatchImpl.State> {

    data class State(
        val timePassedInMillis: Long,
        val isRunning: Boolean
    )

    override fun initialState(input: Unit): State = State(
        timePassedInMillis = 0,
        isRunning = false
    )

    override fun evaluate(
        input: Unit,
        state: State,
        context: FormulaContext<State>
    ): Evaluation<Output> {
        return Evaluation(
            renderModel = Output(
                timePassedInMillis = state.timePassedInMillis,
                isRunning = state.isRunning,
                toggle = context.callback {
                    transition(state.copy(isRunning = !state.isRunning))
                },
                reset = context.callback {
                    transition(state.copy(timePassedInMillis = 0, isRunning = false))
                }
            ),
            updates = context.updates {
                if (state.isRunning) {
                    val incrementTimePassed = RxStream.fromObservable {
                        Observable.interval(1, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread())
                    }

                    events(incrementTimePassed) {
                        transition(state.copy(timePassedInMillis = state.timePassedInMillis + 1))
                    }
                }
            }
        )
    }
}
