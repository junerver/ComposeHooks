package xyz.junerver.compose.hooks.useidle

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/*
  Description: Tracks whether the user is being inactive.
  Author: Junerver
  Date: 2024/7/9-9:00
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun useIdle(timeout: Duration = 5.seconds): State<IdleInfo> {
    val window = (LocalContext.current as Activity).window
    val originalCallback = remember { window.callback }
    val scope = rememberCoroutineScope()
    return produceState(initialValue = IdleInfo()) {
        val inactivityCallback =
            InactivityWindowCallback(originalCallback, scope, timeout) { i, i2 ->
                value = IdleInfo(i, i2)
            }
        window.callback = inactivityCallback
        awaitDispose {
            window.callback = originalCallback
        }
    }
}

@Stable
data class IdleInfo(
    val idle: Boolean = false,
    val lastActive: Instant = Instant.DISTANT_PAST,
)
