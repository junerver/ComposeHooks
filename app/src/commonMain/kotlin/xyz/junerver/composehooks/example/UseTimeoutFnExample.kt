package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kotlin.time.Duration.Companion.seconds
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.useResetState
import xyz.junerver.compose.hooks.useTimeoutFn
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description:
  Author: Junerver
  Date: 2025/6/25-10:10
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun UseTimeoutFnExample() {
    val (text, setText, _, reset) = useResetState("Please wait for 3 seconds")
    val (isPending, start, stop) = useTimeoutFn(fn = {
        setText("Done")
    }, 3.seconds)
    Surface {
        Column {
            Text(text = text.value)
            Row {
                TButton(text = "Restart", enabled = !isPending.value) {
                    reset()
                    start()
                }
                TButton(text = "Stop", enabled = isPending.value) {
                    stop()
                }
            }
        }
    }
}
