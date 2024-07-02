package xyz.junerver.compose.hooks

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

/*
  Description: 定时一段时间后执行的任务
  Author: Junerver
  Date: 2024/2/1-15:08
  Email: junerver@gmail.com
  Version: v1.0
*/

@SuppressLint("ComposableNaming")
@Composable
fun useTimeout(delay: Duration = 1.seconds, block: () -> Unit) {
    useEffect {
        delay(delay)
        block()
    }
}
