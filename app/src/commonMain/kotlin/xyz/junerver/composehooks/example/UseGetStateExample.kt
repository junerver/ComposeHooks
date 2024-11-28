package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.useGetState

/*
  Description:
  Author: Junerver
  Date: 2024/5/10-10:10
  Email: junerver@gmail.com
  Version: v1.0
*/
@Composable
fun UseGetStateExample() {
    Surface {
        Column {
            Text(text = "Resolve two issues that arise when using [useState] via destructuring declarations:")
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Question1. Closure problems")
            // in pass, you need to use [useLatestRef] to get latest value of state
            val (state, setState) = useGetState("getState")
            LaunchedEffect(key1 = Unit) {
                repeat(10) {
                    delay(1.seconds)
                    // Now there is no need to use [useLatestRef] to get the latest value directly through [getter]
                    setState { "$it." }
                }
            }
            Text(text = state.value)

            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Question2. modify state very quickly")
            val (state2, setState2) = useGetState("getState2")
            LaunchedEffect(key1 = Unit) {
                repeat(20) {
                    setState2 { "$it." }
                }
            }
            Text(text = state2.value)
        }
    }
}
