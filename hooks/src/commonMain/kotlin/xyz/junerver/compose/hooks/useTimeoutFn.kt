package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlin.properties.Delegates
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/*
  Description: A wrapper for delayed function execution with controls.
  Author: Junerver
  Date: 2025/6/25-9:06
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Options for configuring the timeout function behavior.
 *
 * @property immediate Whether to start the timer immediately
 * @property immediateCallback Whether to execute the callback immediately after calling start
 */
@Stable
data class TimeoutFnOptions internal constructor(
    var immediate: Boolean = true,
    var immediateCallback: Boolean = false,
) {
    companion object : Options<TimeoutFnOptions>(::TimeoutFnOptions)
}

/**
 * Holder class for timeout function control functions and state.
 *
 * @property isPending State indicating whether the timeout is currently pending
 * @property start Function to start or restart the timeout
 * @property stop Function to stop the timeout
 */
@Stable
data class TimeoutFnHolder(
    val isPending: IsActive,
    val start: StartFn,
    val stop: StopFn,
)

/**
 * Function type for starting a timeout with optional parameters.
 */
typealias StartFn = NoParamsVoidFunction

/**
 * Function type for stopping a timeout.
 */
typealias StopFn = () -> Unit

/**
 * Internal class to manage timeout execution.
 *
 * @property options The timeout options
 */
@Stable
private class TimeoutFn(private val options: TimeoutFnOptions) {
    var scope: CoroutineScope by Delegates.notNull()
    var isPendingState: MutableState<Boolean>? = null
    lateinit var timeoutFn: Ref<SuspendAsyncFn>
    var interval: Duration by Delegates.notNull()
    private var timeoutJob: Job? = null

    fun isRunning() = timeoutJob?.isActive == true

    fun start() {
        if (isRunning()) {
            stop()
        }

        scope.launch {
            isPendingState?.value = true
            if (options.immediateCallback) {
                timeoutFn.current(this)
            } else {
                delay(interval)
                timeoutFn.current(this)
            }
            isPendingState?.value = false
        }.also { timeoutJob = it }
    }

    fun stop() {
        timeoutJob?.cancel()
        timeoutJob = null
        isPendingState?.value = false
    }
}

/**
 * A hook for executing a function after a specified delay with controls.
 *
 * This hook provides a way to create and manage a delayed function execution,
 * similar to setTimeout in JavaScript. It supports configurable options and
 * provides control functions to start, stop, and check the status of the timeout.
 *
 * @param fn The function to be executed after the delay
 * @param interval The delay before executing the function
 * @param optionsOf A lambda to configure the timeout options
 * @return A [TimeoutFnHolder] containing control functions and state for the timeout
 *
 * @example
 * ```kotlin
 * val (isPending, start, stop) = useTimeoutFn(
 *     fn = { println("Timeout executed!") },
 *     interval = 2.seconds,
 *     optionsOf = {
 *         immediate = true
 *         immediateCallback = false
 *     }
 * )
 *
 * // Start the timeout
 * start()
 *
 * // Stop the timeout
 * stop()
 *
 * // Check if timeout is pending
 * if (isPending.value) {
 *     // Timeout is currently pending
 * }
 * ```
 */
@Composable
fun useTimeoutFn(fn: SuspendAsyncFn, interval: Duration = 1.seconds, optionsOf: TimeoutFnOptions.() -> Unit = {}): TimeoutFnHolder {
    val options by useCreation { TimeoutFnOptions.optionOf(optionsOf) }
    val latestFn = useLatestRef(value = fn)
    val isPendingState = useState(default = false)
    val scope = rememberCoroutineScope()

    val timeoutFn = remember {
        TimeoutFn(options).apply {
            this.isPendingState = isPendingState
            this.timeoutFn = latestFn
            this.scope = scope
            this.interval = interval
        }
    }

    // Start immediately if configured
    useMount {
        if (options.immediate) {
            timeoutFn.start()
        }
    }

    useUnmount {
        timeoutFn.stop()
    }

    return remember {
        TimeoutFnHolder(
            isPending = isPendingState,
            start = timeoutFn::start,
            stop = timeoutFn::stop
        )
    }
}
