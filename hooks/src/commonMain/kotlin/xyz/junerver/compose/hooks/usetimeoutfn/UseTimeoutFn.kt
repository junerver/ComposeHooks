package xyz.junerver.compose.hooks.usetimeoutfn

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
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.IsActive
import xyz.junerver.compose.hooks.NoParamsVoidFunction
import xyz.junerver.compose.hooks.Options
import xyz.junerver.compose.hooks.SuspendAsyncFn
import xyz.junerver.compose.hooks.useDynamicOptions
import xyz.junerver.compose.hooks.usemount.useMountImpl
import xyz.junerver.compose.hooks.useunmount.useUnmountImpl
import xyz.junerver.compose.hooks.usestate.useStateImpl

/*
  Description: A wrapper for delayed function execution with controls.
  Author: Junerver
  Date: 2025/6/25-9:06
  Email: junerver@gmail.com
  Version: v1.0
*/

@Stable
data class UseTimeoutFnOptions internal constructor(
    var immediate: Boolean = true,
    var immediateCallback: Boolean = false,
) {
    companion object : Options<UseTimeoutFnOptions>(::UseTimeoutFnOptions)
}

@Stable
data class TimeoutFnHolder(
    val isPending: IsActive,
    val start: StartFn,
    val stop: StopFn,
)

typealias StartFn = NoParamsVoidFunction

typealias StopFn = () -> Unit

@Stable
private class TimeoutFn(
    private val scope: CoroutineScope,
    private val isPendingState: MutableState<Boolean>,
    options: UseTimeoutFnOptions,
    interval: Duration,
    fn: SuspendAsyncFn,
) {
    private val lock = SynchronizedObject()
    private var immediateCallback = options.immediateCallback
    private var interval = interval
    private var timeoutFn = fn
    private var timeoutJob: Job? = null

    fun update(options: UseTimeoutFnOptions, interval: Duration, fn: SuspendAsyncFn) {
        synchronized(lock) {
            immediateCallback = options.immediateCallback
            this.interval = interval
            timeoutFn = fn
        }
    }

    fun start() {
        lateinit var candidate: Job
        val previousJob = synchronized(lock) {
            val scheduledDelay = if (immediateCallback) Duration.ZERO else interval
            candidate = scope.launch(start = CoroutineStart.LAZY) {
                delay(scheduledDelay)
                val callback = synchronized(lock) {
                    timeoutFn.takeIf { timeoutJob === candidate }
                } ?: return@launch
                callback(this)
            }
            timeoutJob.also {
                timeoutJob = candidate
                isPendingState.value = true
            }
        }

        candidate.invokeOnCompletion {
            synchronized(lock) {
                if (timeoutJob === candidate) {
                    timeoutJob = null
                    isPendingState.value = false
                }
            }
        }
        previousJob?.cancel()
        candidate.start()
    }

    fun stop() {
        val jobToCancel = synchronized(lock) {
            val currentJob = timeoutJob
            timeoutJob = null
            isPendingState.value = false
            currentJob
        }
        jobToCancel?.cancel()
    }
}

@Composable
fun useTimeoutFnImpl(
    fn: SuspendAsyncFn,
    interval: Duration = 1.seconds,
    optionsOf: UseTimeoutFnOptions.() -> Unit = {},
): TimeoutFnHolder {
    require(interval >= Duration.ZERO) { "useTimeoutFn interval must not be negative" }
    val options = useDynamicOptions(optionsOf)
    val isPendingState = useStateImpl(default = false)
    val scope = rememberCoroutineScope()

    val timeoutFn = remember {
        TimeoutFn(
            scope = scope,
            isPendingState = isPendingState,
            options = options,
            interval = interval,
            fn = fn,
        )
    }.also { it.update(options = options, interval = interval, fn = fn) }

    useMountImpl {
        if (options.immediate) {
            timeoutFn.start()
        }
    }

    useUnmountImpl {
        timeoutFn.stop()
    }

    return remember {
        TimeoutFnHolder(
            isPending = isPendingState,
            start = timeoutFn::start,
            stop = timeoutFn::stop,
        )
    }
}
