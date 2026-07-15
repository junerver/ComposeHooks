package xyz.junerver.compose.hooks.useinterval

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.IsActive
import xyz.junerver.compose.hooks.Options
import xyz.junerver.compose.hooks.PauseFn
import xyz.junerver.compose.hooks.ResumeFn
import xyz.junerver.compose.hooks.SuspendAsyncFn
import xyz.junerver.compose.hooks.useeffect.useEffectImpl
import xyz.junerver.compose.hooks.useDynamicOptions
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
private class Interval(
    private val scope: CoroutineScope,
    private val isActiveState: MutableState<Boolean>?,
    options: UseIntervalOptions,
    block: SuspendAsyncFn,
) {
    private val lock = SynchronizedObject()
    private var initialDelay = options.initialDelay
    private var period = options.period
    private var intervalFn = block
    private var ready = true
    private var intervalJob: Job? = null

    fun update(options: UseIntervalOptions, ready: Boolean = true, block: SuspendAsyncFn) {
        synchronized(lock) {
            initialDelay = options.initialDelay
            period = options.period
            intervalFn = block
            this.ready = ready
        }
    }

    fun isRunning(): Boolean = synchronized(lock) { intervalJob != null }

    fun resume() {
        lateinit var candidate: Job
        val shouldStart = synchronized(lock) {
            if (!ready || intervalJob != null) {
                false
            } else {
                val scheduledInitialDelay = initialDelay
                candidate = scope.launch(start = CoroutineStart.LAZY) {
                    delay(scheduledInitialDelay)
                    while (isActive) {
                        val callback = synchronized(lock) {
                            intervalFn.takeIf { intervalJob === candidate && ready }
                        } ?: return@launch
                        callback(this)
                        val nextDelay = synchronized(lock) {
                            period.takeIf { intervalJob === candidate && ready }
                        } ?: return@launch
                        delay(nextDelay)
                    }
                }
                intervalJob = candidate
                isActiveState?.value = true
                true
            }
        }
        if (!shouldStart) return

        candidate.invokeOnCompletion {
            synchronized(lock) {
                if (intervalJob === candidate) {
                    intervalJob = null
                    isActiveState?.value = false
                }
            }
        }
        candidate.start()
    }

    fun pause() {
        val jobToCancel = synchronized(lock) {
            val currentJob = intervalJob
            intervalJob = null
            isActiveState?.value = false
            currentJob
        }
        jobToCancel?.cancel()
    }
}

@Stable
data class IntervalHolder(
    val resume: ResumeFn,
    val pause: PauseFn,
    val isActive: IsActive,
)

@Composable
fun useIntervalImpl(optionsOf: UseIntervalOptions.() -> Unit = {}, block: SuspendAsyncFn): IntervalHolder {
    val options = useDynamicOptions(optionsOf).validated()
    return useIntervalImpl(options = options, block = block)
}

@Composable
fun useIntervalImpl(optionsOf: UseIntervalOptions.() -> Unit = {}, ready: Boolean, block: SuspendAsyncFn) {
    val options = useDynamicOptions(optionsOf).validated()
    useIntervalImpl(options, ready = ready, block = block)
}

private fun UseIntervalOptions.validated(): UseIntervalOptions = apply {
    require(initialDelay >= Duration.ZERO) { "useInterval initialDelay must not be negative" }
    require(period > Duration.ZERO) { "useInterval period must be positive" }
}

@Composable
private fun useIntervalImpl(options: UseIntervalOptions, block: SuspendAsyncFn): IntervalHolder {
    val isActiveState = useStateImpl(default = false)
    val scope = rememberCoroutineScope()
    val interval = remember {
        Interval(
            scope = scope,
            isActiveState = isActiveState,
            options = options,
            block = block,
        )
    }.also { it.update(options = options, block = block) }
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
    val scope = rememberCoroutineScope()
    val interval = remember {
        Interval(
            scope = scope,
            isActiveState = null,
            options = options,
            block = block,
        )
    }.also { it.update(options = options, ready = ready, block = block) }
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
