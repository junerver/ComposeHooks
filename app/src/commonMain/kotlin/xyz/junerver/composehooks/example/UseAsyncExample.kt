package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.useAsync
import xyz.junerver.compose.hooks.useCancelableAsync
import xyz.junerver.compose.hooks.useGetState
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description:
  Author: Junerver
  Date: 2024/4/2-8:19
  email:junerver@gmail.com
  version: v1.0
*/

@Composable
fun UseAsyncExample() {
    val (_, setState, getState) = useGetState(0)

    /** 如果你向[useAsync]传递一个闭包作为参数，那么返回值是 `()->Unit` */
    val async = useAsync {
        delay(1.seconds)
        setState { it + 1 }
    }

    /** 如果不传递参数，则使用另一个重载，返回值是[xyz.junerver.compose.hooks.AsyncRunFn] */
    val asyncRun = useAsync()

    val (cancelableAsyncRun, cancel, isRunning) = useCancelableAsync()

    Surface {
        Column {
            Text(text = "count:${getState()}")
            Spacer(modifier = Modifier.height(20.dp))
            Text("The asynchronous closure is passed as an argument to `useAsync`")
            TButton(text = "delay  +1") {
                async()
            }
            Text("equivalent to `rememberCoroutineScope`")
            TButton(text = "delay +1") {
                asyncRun {
                    delay(1.seconds)
                    setState { it + 1 }
                }
            }
            Text("useCancelableAsync")
            Row {
                TButton(text = "delay +1") {
                    cancelableAsyncRun {
                        delay(1.seconds)
                        setState { it + 1 }
                    }
                }
                TButton(text = "cancel", enabled = isRunning.value) {
                    cancel()
                }
            }
        }
    }
}
