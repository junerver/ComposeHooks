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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/*
  Description: 一个间隔固定时间执行的interval函数。
  效果与参数类似 RxJava 的`Observable.interval(0, 3, TimeUnit.SECONDS)`

  Author: Junerver
  Date: 2024/2/1-10:53
  Email: junerver@gmail.com
  Version: v1.0
  Version: v1.1  2024/5/17
*/

/**
 * Interval options for configuring the interval behavior.
 *
 * @constructor Create empty Interval options
 * @property initialDelay The delay before the first execution
 * @property period The time between subsequent executions
 */
@Stable
data class IntervalOptions internal constructor(
    var initialDelay: Duration = 0.seconds,
    var period: Duration = 5.seconds,
) {
    companion object : Options<IntervalOptions>(::IntervalOptions)
}

/**
 * Internal class to manage interval execution.
 *
 * @property ready Whether the interval is ready to execute
 * @property scope The coroutine scope for interval execution
 * @property isActiveState The state tracking if the interval is currently active
 * @property intervalFn The function to be executed in each interval
 * @property intervalJob The job managing the interval execution
 */
@Stable
private class Interval(private val options: IntervalOptions) {
    var ready = true
    var scope: CoroutineScope by Delegates.notNull()
    var isActiveState: MutableState<Boolean>? = null
    lateinit var intervalFn: Ref<SuspendAsyncFn>
    private lateinit var intervalJob: Job

    fun isRunning() = this::intervalJob.isInitialized && intervalJob.isActive

    fun resume() {
        if (ready) {
            scope.launch {
                if (isRunning()) return@launch
                launch {
                    delay(options.initialDelay)
                    while (isActive) {
                        intervalFn.current(this)
                        delay(options.period)
                    }
                }.also {
                    intervalJob = it
                    isActiveState?.value = true
                }
            }
        }
    }

    fun pause() {
        if (this::intervalJob.isInitialized && intervalJob.isActive) {
            intervalJob.cancel()
            isActiveState?.value = false
        }
    }
}

/**
 * A hook for executing code at regular intervals.
 *
 * This hook provides a way to create and manage intervals that execute code periodically,
 * similar to RxJava's `Observable.interval()`. It supports configurable initial delay,
 * period, and provides control functions to resume and pause the interval.
 *
 * @param optionsOf A lambda to configure the interval options
 * @param block The suspend function to be executed in each interval
 * @return An [IntervalHolder] containing control functions for the interval
 *
 * @example
 * ```kotlin
 * val (resume, pause, isActive) = useInterval {
 *     initialDelay = 1.seconds
 *     period = 2.seconds
 * } {
 *     // This block will be executed every 2 seconds
 *     println("Interval executed")
 * }
 * 
 * // Start the interval
 * resume()
 * 
 * // Pause the interval
 * pause()
 * 
 * // Check if interval is running
 * if (isActive.value) {
 *     // Interval is currently running
 * }
 * ```
 */
@Composable
fun useInterval(optionsOf: IntervalOptions.() -> Unit = {}, block: SuspendAsyncFn): IntervalHolder = useInterval(
    options = remember { IntervalOptions.optionOf(optionsOf) },
    block = block
)

/**
 * A hook for executing code at regular intervals with a ready state.
 *
 * This version of the interval hook allows you to control the interval execution
 * based on a ready state. The interval will automatically start when ready becomes
 * true and stop when it becomes false.
 *
 * @param optionsOf A lambda to configure the interval options
 * @param ready A boolean state that controls whether the interval should run
 * @param block The suspend function to be executed in each interval
 *
 * @example
 * ```kotlin
 * var isReady by remember { mutableStateOf(false) }
 * 
 * useInterval(
 *     optionsOf = {
 *         period = 1.seconds
 *     },
 *     ready = isReady
 * ) {
 *     // This block will be executed every second when isReady is true
 *     println("Interval executed")
 * }
 * ```
 */
@Composable
fun useInterval(optionsOf: IntervalOptions.() -> Unit = {}, ready: Boolean, block: SuspendAsyncFn): Unit = useInterval(
    remember { IntervalOptions.optionOf(optionsOf) },
    ready = ready,
    block = block
)

/**
 * Holder class for interval control functions.
 *
 * @property resume Function to start or resume the interval
 * @property pause Function to pause the interval
 * @property isActive State indicating whether the interval is currently running
 */
@Stable
data class IntervalHolder(
    val resume: ResumeFn,
    val pause: PauseFn,
    val isActive: IsActive,
)

/**
 * Internal implementation of the interval hook.
 *
 * @param options The interval options
 * @param block The suspend function to be executed in each interval
 * @return An [IntervalHolder] containing control functions for the interval
 */
@Composable
private fun useInterval(options: IntervalOptions = remember { IntervalOptions() }, block: SuspendAsyncFn): IntervalHolder {
    val latestFn = useLatestRef(value = block)
    val isActiveState = useState(default = false)
    val scope = rememberCoroutineScope()
    val interval = remember {
        Interval(options).apply {
            this.isActiveState = isActiveState
            this.intervalFn = latestFn
            this.scope = scope
        }
    }
    return remember {
        IntervalHolder(
            resume = interval::resume,
            pause = interval::pause,
            isActive = isActiveState
        )
    }
}

/**
 * Internal implementation of the interval hook with ready state.
 *
 * @param options The interval options
 * @param ready A boolean state that controls whether the interval should run
 * @param block The suspend function to be executed in each interval
 */
@Composable
private fun useInterval(options: IntervalOptions = remember { IntervalOptions() }, ready: Boolean, block: SuspendAsyncFn) {
    val latestFn = useLatestRef(value = block)
    val scope = rememberCoroutineScope()
    val interval = remember {
        Interval(options).apply {
            this.intervalFn = latestFn
            this.scope = scope
        }
    }.apply {
        this.ready = ready
    }
    useEffect(ready) {
        if (ready && !interval.isRunning()) {
            interval.resume()
        }
        if (!ready && interval.isRunning()) {
            interval.pause()
        }
    }
}
