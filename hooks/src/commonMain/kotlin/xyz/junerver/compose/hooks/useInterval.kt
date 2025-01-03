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
 * Interval options
 *
 * @constructor Create empty Interval options
 * @property initialDelay initial call delay
 * @property period call interval
 */
@Stable
data class IntervalOptions internal constructor(
    var initialDelay: Duration = 0.seconds,
    var period: Duration = 5.seconds,
) {
    companion object : Options<IntervalOptions>(::IntervalOptions)
}

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
 * A Composable function to periodically execute a block of code within Jetpack Compose.
 *
 * This function allows users to define a set of interval options and repeatedly execute the specified operation.
 * It returns an [IntervalHolder] that can be used to manage the interval.
 *
 * @param optionsOf A lambda expression used to configure IntervalOptions, defining the interval and animation type, etc.
 * @param block A suspend function that defines the operation to be executed in each interval cycle.
 * @return An [IntervalHolder] that manages the interval.
 */
@Composable
fun useInterval(optionsOf: IntervalOptions.() -> Unit = {}, block: SuspendAsyncFn): IntervalHolder = useInterval(
    options = remember { IntervalOptions.optionOf(optionsOf) },
    block = block
)

/**
 * A Composable function to periodically execute a block of code within Jetpack Compose.
 *
 * This function allows users to define a set of interval options and repeatedly execute the specified operation when conditions are met.
 * It is suitable for scenarios where animations need to be created or the UI needs to be updated periodically.
 *
 * @param optionsOf A lambda expression used to configure IntervalOptions, defining the interval and animation type, etc.
 * @param ready A boolean value indicating whether the interval operation should start.
 * @param block A suspend function that defines the operation to be executed in each interval cycle.
 */
@Composable
fun useInterval(optionsOf: IntervalOptions.() -> Unit = {}, ready: Boolean, block: SuspendAsyncFn): Unit = useInterval(
    remember { IntervalOptions.optionOf(optionsOf) },
    ready = ready,
    block = block
)

/**
 * @param resume A function used to resume the process.
 * @param pause A function used to pause the process.
 * @param isActive A function used to check if the process is currently active.
 */
@Stable
data class IntervalHolder(
    val resume: ResumeFn,
    val pause: PauseFn,
    val isActive: IsActive,
)

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
