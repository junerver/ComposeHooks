package xyz.junerver.compose.hooks.useidle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.activity.compose.LocalActivity
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import xyz.junerver.compose.hooks.useCreation

/*
  Description: Tracks whether the user is being inactive.
  Author: Junerver
  Date: 2024/7/9-9:00
  Email: junerver@gmail.com
  Version: v1.0
*/

private val idleWindowCallbackRegistry = IdleWindowCallbackRegistry()

@Composable
fun useIdle(timeout: Duration = 5.seconds): State<IdleInfo> {
    val window = LocalActivity.current?.window
    val callbackHost = useCreation(window) {
        window?.let(::AndroidIdleCallbackHost)
    }.current
    return produceState(initialValue = IdleInfo(), callbackHost, timeout) {
        if (callbackHost == null) return@produceState
        val subscription = idleWindowCallbackRegistry.acquire(callbackHost)
        try {
            subscription.activities.collectLatest { activity ->
                value = IdleInfo(idle = false, lastActive = activity.lastActive)
                val remaining = remainingIdleDuration(
                    nowNanos = subscription.now().elapsedRealtimeNanos,
                    activeAtNanos = activity.activeAtNanos,
                    timeout = timeout,
                )
                when {
                    remaining == Duration.INFINITE -> awaitCancellation()
                    remaining > Duration.ZERO -> delay(remaining)
                }
                value = IdleInfo(idle = true, lastActive = activity.lastActive)
            }
        } finally {
            subscription.close()
        }
    }
}

@Stable
data class IdleInfo(
    val idle: Boolean = false,
    val lastActive: Instant = Instant.DISTANT_PAST,
)
