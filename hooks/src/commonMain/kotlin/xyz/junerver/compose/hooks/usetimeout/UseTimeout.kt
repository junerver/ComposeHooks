package xyz.junerver.compose.hooks.usetimeout

import androidx.compose.runtime.Composable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import xyz.junerver.compose.hooks.usetimeoutfn.useTimeoutFnImpl

/*
  Description: 定时一段时间后执行的任务
  Author: Junerver
  Date: 2024/2/1-15:08
  Email: junerver@gmail.com
  Version: v1.0
*/

@Deprecated(message = "useTimeout with delay and block is deprecated. Use useTimeoutFn instead.")
@Composable
fun useTimeoutImpl(delay: Duration = 1.seconds, block: () -> Unit) {
    useTimeoutFnImpl(fn = { block() }, interval = delay)
}
