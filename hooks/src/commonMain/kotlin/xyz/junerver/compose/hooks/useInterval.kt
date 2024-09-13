package xyz.junerver.compose.hooks

import androidx.compose.runtime.*
import kotlin.properties.Delegates
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.*

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
 * @property initialDelay 初始调用延时
 * @property period 调用间隔
 */
data class IntervalOptions internal constructor(
    var initialDelay: Duration = 0.seconds,
    var period: Duration = 5.seconds,
) {
    companion object : Options<IntervalOptions>(::IntervalOptions)
}

private class Interval(private val options: IntervalOptions) {
    var ready = true
    var scope: CoroutineScope by Delegates.notNull()
    var isActiveState: MutableState<Boolean>? = null
    lateinit var intervalFn: () -> Unit
    private lateinit var intervalJob: Job

    fun isRunning() = this::intervalJob.isInitialized && intervalJob.isActive

    fun resume() {
        if (ready) {
            scope.launch {
                if (isRunning()) return@launch
                launch {
                    delay(options.initialDelay)
                    while (isActive) {
                        intervalFn()
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

@Deprecated(
    "Please use the performance-optimized version. Do not pass the Options instance directly. You can simply switch by adding `=` after the `optionsOf` function. If you need to use an older version, you need to explicitly declare the parameters as `options`"
)
@Composable
fun useInterval(options: IntervalOptions = remember { IntervalOptions() }, block: () -> Unit): Triple<ResumeFn, PauseFn, IsActive> {
    val latestFn by useLatestState(value = block)
    val isActiveState = useState(default = false)
    val interval = remember {
        Interval(options).apply {
            this.isActiveState = isActiveState
        }
    }.apply {
        this.intervalFn = latestFn
        this.scope = rememberCoroutineScope()
    }
    return with(interval) {
        Triple(
            first = ::resume,
            second = ::pause,
            third = isActiveState.value
        )
    }
}

@Composable
fun useInterval(optionsOf: IntervalOptions.() -> Unit, block: () -> Unit): Triple<ResumeFn, PauseFn, IsActive> = useInterval(
    options = remember(optionsOf) { IntervalOptions.optionOf(optionsOf) },
    block = block
)

@Deprecated(
    "Please use the performance-optimized version. Do not pass the Options instance directly. You can simply switch by adding `=` after the `optionsOf` function. If you need to use an older version, you need to explicitly declare the parameters as `options`"
)
@Composable
fun useInterval(options: IntervalOptions = remember { IntervalOptions() }, ready: Boolean, block: () -> Unit) {
    val latestFn by useLatestState(value = block)
    val interval = remember {
        Interval(options)
    }.apply {
        this.intervalFn = latestFn
        this.scope = rememberCoroutineScope()
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

@Composable
fun useInterval(optionsOf: IntervalOptions.() -> Unit, ready: Boolean, block: () -> Unit) = useInterval(
    remember(optionsOf) { IntervalOptions.optionOf(optionsOf) },
    ready = ready,
    block = block
)
