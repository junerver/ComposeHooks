package xyz.junerver.compose.hooks.useidle

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.datetime.Instant
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.utils.currentTime

/*
  Description: Tracks whether the user is being inactive.
  Author: Junerver
  Date: 2024/7/9-9:00
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun useIdle(timeout: Duration = 5.seconds): Pair<State<Boolean>, State<Instant>> {
    val window = (LocalContext.current as Activity).window
    val idle = useState(default = false)
    val lastActive = useState(default = currentTime)
    val originalCallback = remember { window.callback }
    val scope = rememberCoroutineScope()
    DisposableEffect(key1 = Unit) {
        val inactivityCallback =
            InactivityWindowCallback(originalCallback, scope, timeout) { i, i2 ->
                idle.value = i
                lastActive.value = i2
            }
        window.callback = inactivityCallback
        onDispose {
            window.callback = originalCallback
        }
    }
    return remember { Pair(idle, lastActive) }
}
