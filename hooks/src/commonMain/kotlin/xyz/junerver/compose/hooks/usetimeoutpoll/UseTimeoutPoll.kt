package xyz.junerver.compose.hooks.usetimeoutpoll
import xyz.junerver.compose.hooks.useasync.useAsyncImpl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import xyz.junerver.compose.hooks.Options
import xyz.junerver.compose.hooks.SuspendAsyncFn
import xyz.junerver.compose.hooks.useref.getValue
import xyz.junerver.compose.hooks.usemount.useMountImpl
import xyz.junerver.compose.hooks.useunmount.useUnmountImpl
import xyz.junerver.compose.hooks.useDynamicOptions
import xyz.junerver.compose.hooks.uselatest.useLatestRefImpl
import xyz.junerver.compose.hooks.usetimeoutfn.useTimeoutFnImpl
import xyz.junerver.compose.hooks.useref.useRefImpl
import xyz.junerver.compose.hooks.usestate.useStateImpl

/*
  Description: Use timeout to poll for content. Triggers the callback after the last task is completed.
  Author: Junerver
  Date: 2025/6/25-19:26
  Email: junerver@gmail.com
  Version: v1.0

  Update: 2025/6/26-9:23 by Junerver
  Version: v1.1
  Description: base on useTimeoutFn
*/

@Stable
data class UseTimeoutPollOptions internal constructor(
    var immediate: Boolean = true,
    var immediateCallback: Boolean = false,
) {
    companion object : Options<UseTimeoutPollOptions>(::UseTimeoutPollOptions)
}

@Stable
data class TimeoutPollHolder(
    val isActive: MutableState<Boolean>,
    val pause: () -> Unit,
    val resume: () -> Unit,
)

@Composable
fun useTimeoutPollImpl(
    fn: SuspendAsyncFn,
    interval: Duration = 1.seconds,
    optionsOf: UseTimeoutPollOptions.() -> Unit,
): TimeoutPollHolder {
    val options = useDynamicOptions(optionsOf)
    val latestFn = useLatestRefImpl(value = fn)
    val isActiveState = useStateImpl(default = false)
    val asyncRun = useAsyncImpl()

    val startRef = useRefImpl(default = {})
    val stopRef = useRefImpl(default = {})

    val internalLoop: SuspendAsyncFn = internalLoop@{
        if (!isActiveState.value) {
            return@internalLoop
        }
        latestFn.current(this)
        startRef.current()
    }

    val (_, startTimeoutFn, stopTimeoutFn) = useTimeoutFnImpl(
        fn = internalLoop,
        interval = interval,
        optionsOf = {
            immediate = false
            immediateCallback = false
        },
    )

    startRef.current = startTimeoutFn
    stopRef.current = stopTimeoutFn

    val pause = {
        isActiveState.value = false
        stopTimeoutFn()
    }

    val resume = {
        if (!isActiveState.value) {
            isActiveState.value = true
            if (options.immediateCallback) {
                asyncRun(latestFn.current)
            }
            startTimeoutFn()
        }
    }

    useMountImpl {
        if (options.immediate) {
            resume()
        }
    }
    useUnmountImpl {
        pause()
    }

    return remember {
        TimeoutPollHolder(
            isActive = isActiveState,
            pause = pause,
            resume = resume,
        )
    }
}

@Composable
fun useTimeoutPollImpl(fn: SuspendAsyncFn, interval: Duration = 1.seconds, immediate: Boolean = true) = useTimeoutPollImpl(
    fn = fn,
    interval = interval,
    optionsOf = { this.immediate = immediate },
)
