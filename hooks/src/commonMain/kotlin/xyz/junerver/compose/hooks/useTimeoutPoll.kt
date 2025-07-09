package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

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

/**
 * UseTimeoutPoll options interface
 */
@Stable
data class UseTimeoutPollOptions internal constructor(
    /**
     * Start the timer immediately
     *
     * @default true
     */
    var immediate: Boolean = true,
    /**
     * Execute the callback immediately after calling `resume`
     *
     * @default false
     */
    var immediateCallback: Boolean = false,
) {
    companion object : Options<UseTimeoutPollOptions>(::UseTimeoutPollOptions)
}

/**
 * TimeoutPoll holder, provides control functions
 */
@Stable
data class TimeoutPollHolder(
    val isActive: MutableState<Boolean>,
    val pause: () -> Unit,
    val resume: () -> Unit,
)

/**
 * Use timeout to poll for content. Triggers the callback after the last task is completed.
 *
 * @param fn Function to execute
 * @param interval Time interval
 * @param optionsOf Options configuration
 * @return TimeoutPollHolder containing isActive, pause, resume
 */
@Composable
fun useTimeoutPoll(
    fn: SuspendAsyncFn,
    interval: Duration = 1.seconds,
    optionsOf: UseTimeoutPollOptions.() -> Unit = {},
): TimeoutPollHolder {
    val options = remember { UseTimeoutPollOptions.optionOf(optionsOf) }
    val latestFn = useLatestRef(value = fn)
    val isActiveState = useState(default = false)
    val asyncRun = useAsync()

    val startRef = useRef(default = {})
    val stopRef = useRef(default = {})

    val internalLoop: SuspendAsyncFn = internalLoop@{
        if (!isActiveState.value) {
            return@internalLoop
        }
        latestFn.current(this)
        startRef.current()
    }

    val (_, startTimeoutFn, stopTimeoutFn) = useTimeoutFn(
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

    // If configured to start immediately, start when component is mounted
    useMount {
        if (options.immediate) {
            resume()
        }
    }
    useUnmount {
        // Clean up when component is unmounted
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

/**
 * Use timeout to poll for content, simplified version, does not return control functions.
 *
 * @param fn Function to execute
 * @param interval Time interval
 * @param immediate Whether to start immediately
 */
@Composable
fun useTimeoutPoll(fn: SuspendAsyncFn, interval: Duration = 1.seconds, immediate: Boolean = true) {
    useTimeoutPoll(
        fn = fn,
        interval = interval,
        optionsOf = { this.immediate = immediate },
    )
}
