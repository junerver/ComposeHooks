package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import arrow.core.left
import xyz.junerver.compose.hooks.optionsOf
import xyz.junerver.compose.hooks.useCounter
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description:
  Author: Junerver
  Date: 2024/7/8-13:53
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun UseCounterExample() {
    val (current, inc, dec, set, reset) = useCounter(
        initialValue = 100,
        options = optionsOf {
            min = 1
            max = 10
        }
    )

    Surface {
        Column {
            Text(text = "$current [max: 10; min: 1;]")
            Row {
                TButton(text = "inc()") {
                    inc(1)
                }
                TButton(text = "dec()") {
                    dec(1)
                }
                TButton(text = "set(3)") {
                    set(3.left())
                }
                TButton(text = "reset()") {
                    reset()
                }
            }
        }
    }
}
