package com.instacart.formula.compose.stopwatch

import android.os.Bundle
import androidx.compose.Composable
import androidx.fragment.app.FragmentActivity
import androidx.ui.core.Modifier
import androidx.ui.core.setContent
import androidx.ui.foundation.Box
import androidx.ui.foundation.ContentGravity
import androidx.ui.foundation.Text
import androidx.ui.graphics.Color
import androidx.ui.layout.Column
import androidx.ui.layout.Row
import androidx.ui.layout.Spacer
import androidx.ui.layout.fillMaxSize
import androidx.ui.material.Button
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Surface
import androidx.ui.rxjava2.subscribeAsState
import androidx.ui.tooling.preview.Preview
import com.instacart.formula.start
import io.reactivex.Observable

class StopwatchActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { render(StopwatchFormula().start()) }
    }
}

@Composable
fun render(state: Observable<StopwatchRenderModel>) {
    val model = state.subscribeAsState().value
    if (model != null) {
        MaterialTheme {
            Surface {
                Column(Modifier.fillMaxSize()) {
                    Box(gravity = ContentGravity.Center) {
                        Column {
                            Text(text = model.timePassed, color = Color.Black)
                            Row {
                                render(model.startStopButton)
                                render(model.resetButton)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun render(model: ButtonRenderModel) {
    Button(
        onClick = model.onSelected,
        backgroundColor = Color.Gray,
        text = {
            Text(text = model.text, color = Color.White)
        }
    )
}
}