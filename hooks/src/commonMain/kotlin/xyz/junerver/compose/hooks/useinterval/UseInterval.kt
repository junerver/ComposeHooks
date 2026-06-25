package xyz.junerver.compose.hooks.useinterval

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
import xyz.junerver.compose.hooks.IsActive
import xyz.junerver.compose.hooks.Options
import xyz.junerver.compose.hooks.PauseFn
import xyz.junerver.compose.hooks.useref.Ref
import xyz.junerver.compose.hooks.ResumeFn
import xyz.junerver.compose.hooks.SuspendAsyncFn
import xyz.junerver.compose.hooks.useeffect.useEffectImpl
import xyz.junerver.compose.hooks.useDynamicOptions
import xyz.junerver.compose.hooks.uselatest.useLatestRefImpl
import xyz.junerver.compose.hooks.useunmount.useUnmountImpl
import xyz.junerver.compose.hooks.usestate.useStateImpl

/*
  Description: 一个间隔固定时间执行的interval函数。
  效果与参数类似 RxJava 的`Observable.interval(0, 3, TimeUnit.SECONDS)`

  Author: Junerver
  Date: 2024/2/1-10:53
  Email: junerver@gmail.com
  Version: v1.0
  Version: v1.1  2024/5/17
*/

@Stable
data class UseIntervalOptions internal constructor(
    var initialDelay: Duration = Duration.ZERO,
    var period: Duration = 5.seconds,
) {
    companion object : Options<UseIntervalOptions>(::UseIntervalOptions)
}

@Stable
private class Interval(var options: UseIntervalOptions) {
    var ready = true
    var scope: CoroutineScope by Delegates.notNull()
    var isActiveState: MutableState<Boolean>? = null
    lateinit var intervalFn: Ref<SuspendAsyncFn>
    private var intervalJob: Job? = null

    fun isRunning() = intervalJob?.isActive == true

    fun resume() {
        if (ready) {
            if (isRunning()) return
            scope.launch {
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

    fun pause() {
        if (intervalJob?.isActive == true) {
            intervalJob?.cancel()
            isActiveState?.value = false
        }
    }
}

@Stable
data class IntervalHolder(
    val resume: ResumeFn,
    val pause: PauseFn,
    val isActive: IsActive,
)

@Composable
fun useIntervalImpl(optionsOf: UseIntervalOptions.() -> Unit = {}, block: SuspendAsyncFn): IntervalHolder = useIntervalImpl(
    options = useDynamicOptions(optionsOf),
    block = block,
)

@Composable
fun useIntervalImpl(optionsOf: UseIntervalOptions.() -> Unit = {}, ready: Boolean, block: SuspendAsyncFn): Unit = useIntervalImpl(
    useDynamicOptions(optionsOf),
    ready = ready,
    block = block,
)

@Composable
private fun useIntervalImpl(options: UseIntervalOptions, block: SuspendAsyncFn): IntervalHolder {
    val latestFn = useLatestRefImpl(value = block)
    val isActiveState = useStateImpl(default = false)
    val scope = rememberCoroutineScope()
    val interval = remember {
        Interval(options).apply {
            this.isActiveState = isActiveState
            this.intervalFn = latestFn
            this.scope = scope
        }
    }.apply {
        this.options = options
        this.intervalFn = latestFn
        this.scope = scope
    }
    useUnmountImpl {
        interval.pause()
    }
    return remember {
        IntervalHolder(
            resume = interval::resume,
            pause = interval::pause,
            isActive = isActiveState,
        )
    }
}

@Composable
private fun useIntervalImpl(options: UseIntervalOptions, ready: Boolean, block: SuspendAsyncFn) {
    val latestFn = useLatestRefImpl(value = block)
    val scope = rememberCoroutineScope()
    val interval = remember {
        Interval(options).apply {
            this.intervalFn = latestFn
            this.scope = scope
        }
    }.apply {
        this.options = options
        this.intervalFn = latestFn
        this.scope = scope
        this.ready = ready
    }
    useEffectImpl(ready) {
        if (ready && !interval.isRunning()) {
            interval.resume()
        }
        if (!ready && interval.isRunning()) {
            interval.pause()
        }
    }
    useUnmountImpl {
        interval.pause()
    }
}
