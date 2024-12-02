package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import arrow.core.left
import xyz.junerver.compose.hooks.useResetState
import xyz.junerver.composehooks.ui.component.TButton
import xyz.junerver.composehooks.utils.Timestamp

/*
  Description:
  Author: Junerver
  Date: 2024/7/9-14:24
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun UseResetStateExample() {
    val (state, setState, _, reset) = useResetState("default value")
    Surface {
        Column {
            Text(text = state.value)
            TButton(text = "set value") {
                setState("has set new value${Timestamp.now()}".left())
            }
            TButton(text = "reset") {
                reset()
            }
        }
    }
}
