package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import arrow.core.right
import kotlinx.coroutines.delay
import xyz.junerver.compose.hooks.useAsync
import xyz.junerver.compose.hooks.useGetState
import xyz.junerver.composehooks.ui.component.TButton
import kotlin.time.Duration.Companion.seconds

/*
  Description:
  Author: Junerver
  Date: 2024/4/2-8:19
  email:junerver@gmail.com
  version: v1.0
*/

@Composable
fun UseAsyncExample() {
    val (state, setState) = useGetState(0)

    /** 如果你向[useAsync]传递一个闭包作为参数，那么返回值是 `()->Unit` */
    val async = useAsync {
        delay(1.seconds)
        setState({ it: Int -> it + 1 }.right())
    }

    /** 如果不传递参数，则使用另一个重载，返回值是[xyz.junerver.compose.hooks.AsyncRunFn] */
    val asyncRun = useAsync()

    Surface {
        Column {
            Text(text = "count:${state.value}")
            TButton(text = "delay +1") {
                async()
            }
            TButton(text = "delay +1") {
                asyncRun {
                    delay(1.seconds)
                    setState({ it: Int -> it + 1 }.right())
                }
            }
        }
    }
}
