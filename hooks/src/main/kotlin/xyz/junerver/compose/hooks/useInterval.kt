package xyz.junerver.compose.hooks

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
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
import xyz.junerver.kotlin.Tuple3
import xyz.junerver.kotlin.tuple

// /
// / Description: 一个间隔固定时间执行的interval函数。
// /
// / 效果与参数类似 RxJava 的`Observable.interval(0, 3, TimeUnit.SECONDS)`
// / @author Junerver
// / date: 2024/2/1-10:53
// / Email: junerver@gmail.com
// / Version: v1.0
// / Version: v1.1  2024/5/17
// /

/**
 * Interval options
 *
 * @property initialDelay 初始调用延时
 * @property period 调用间隔
 * @constructor Create empty Interval options
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

@Composable
fun useInterval(
    options: IntervalOptions = defaultOption(),
    block: () -> Unit,
): Tuple3<ResumeFn, PauseFn, IsActive> {
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
        tuple(
            first = ::resume,
            second = ::pause,
            third = isActiveState.value
        )
    }
}

@SuppressLint("ComposableNaming")
@Composable
fun useInterval(
    options: IntervalOptions = defaultOption(),
    ready: Boolean,
    block: () -> Unit,
) {
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
