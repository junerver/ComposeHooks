package xyz.junerver.compose.hooks.useidle

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import xyz.junerver.compose.hooks.useState
import xyz.junerver.kotlin.Tuple2

/*
  Description: Tracks whether the user is being inactive.
  Author: Junerver
  Date: 2024/7/9-9:00
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun useIdle(timeout: Duration = 5.seconds): Tuple2<Boolean, Instant> {
    val window = (LocalContext.current as Activity).window
    var idle by useState(default = false)
    var lastActive by useState(default = Clock.System.now())
    val originalCallback = remember { window.callback }
    val scope = rememberCoroutineScope()
    DisposableEffect(key1 = Unit) {
        val inactivityCallback =
            InactivityWindowCallback(originalCallback, scope, timeout) { i, i2 ->
                idle = i
                lastActive = i2
            }
        window.callback = inactivityCallback
        onDispose {
            window.callback = originalCallback
        }
    }
    return Tuple2(
        idle,
        lastActive
    )
}
