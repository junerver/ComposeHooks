package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
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
 * @property initialDelay 初始调用延时
 * @property period 调用间隔
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
    lateinit var intervalFn: Ref<() -> Unit>
    private lateinit var intervalJob: Job

    fun isRunning() = this::intervalJob.isInitialized && intervalJob.isActive

    fun resume() {
        if (ready) {
            scope.launch {
                if (isRunning()) return@launch
                launch {
                    delay(options.initialDelay)
                    while (isActive) {
                        intervalFn.current()
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
fun useInterval(options: IntervalOptions = remember { IntervalOptions() }, block: () -> Unit): IntervalHolder {
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
fun useInterval(optionsOf: IntervalOptions.() -> Unit, block: () -> Unit): IntervalHolder = useInterval(
    options = remember { IntervalOptions.optionOf(optionsOf) },
    block = block
)

@Deprecated(
    "Please use the performance-optimized version. Do not pass the Options instance directly. You can simply switch by adding `=` after the `optionsOf` function. If you need to use an older version, you need to explicitly declare the parameters as `options`"
)
@Composable
fun useInterval(options: IntervalOptions = remember { IntervalOptions() }, ready: Boolean, block: () -> Unit) {
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

@Composable
fun useInterval(optionsOf: IntervalOptions.() -> Unit, ready: Boolean, block: () -> Unit) = useInterval(
    remember { IntervalOptions.optionOf(optionsOf) },
    ready = ready,
    block = block
)

@Stable
data class IntervalHolder(
    val resume: ResumeFn,
    val pause: PauseFn,
    val isActive: State<IsActive>,
)
