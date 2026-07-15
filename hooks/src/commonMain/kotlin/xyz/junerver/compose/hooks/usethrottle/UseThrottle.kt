package xyz.junerver.compose.hooks.usethrottle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.None
import xyz.junerver.compose.hooks.Options
import xyz.junerver.compose.hooks.SuspendAsyncFn
import xyz.junerver.compose.hooks.VoidFunction
import xyz.junerver.compose.hooks.useeffect.useEffectImpl
import xyz.junerver.compose.hooks.useDynamicOptions
import xyz.junerver.compose.hooks.uselatest.useLatestStateImpl
import xyz.junerver.compose.hooks.usegetstate._useGetStateImpl
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.utils.currentInstant

/*
  Description:
  Author: Junerver
  date: 2024/1/30-11:02
  Email: junerver@gmail.com
  Version: v1.0

  Update: 2025/7/16-10:00 by Junerver
  Version: v1.1
  Description: fix leading
 */

@Stable
data class UseThrottleOptions internal constructor(
    var wait: Duration = 1.seconds,
    var leading: Boolean = true,
    var trailing: Boolean = true,
) {
    companion object : Options<UseThrottleOptions>(::UseThrottleOptions)
}

@Stable
internal class Throttle<TParams>(
    fn: VoidFunction<TParams>,
    private val scope: CoroutineScope,
    options: UseThrottleOptions = UseThrottleOptions(),
    private val now: () -> Instant = { currentInstant },
) {
    private class PendingArgs<T>(val value: T)

    private class ClaimedCallback<T>(
        val fn: VoidFunction<T>,
        val params: T,
    )

    private class Transition<T>(
        val timeoutToCancel: Job? = null,
        val timeoutToStart: Job? = null,
        val callback: ClaimedCallback<T>? = null,
    )

    private val stateLock = SynchronizedObject()
    private var currentFn = fn
    private var currentOptions = options.copy()
    private var timeoutJob: Job? = null
    private var lastArgs: PendingArgs<TParams>? = null
    private var latestInvokedTime = Instant.DISTANT_PAST

    fun update(fn: VoidFunction<TParams>, options: UseThrottleOptions) {
        val transition = synchronized(stateLock) {
            currentFn = fn
            if (currentOptions == options) {
                null
            } else {
                currentOptions = options.copy()
                val timeoutToCancel = timeoutJob
                timeoutJob = null
                lastArgs = null
                latestInvokedTime = Instant.DISTANT_PAST
                Transition<TParams>(timeoutToCancel = timeoutToCancel)
            }
        }
        completeTransition(transition)
    }

    private fun createTimeoutJobLocked(wait: Duration, trailing: Boolean): Job {
        lateinit var job: Job
        job = scope.launch(start = CoroutineStart.LAZY) {
            delay(wait)
            fireTimeout(job, trailing)
        }
        timeoutJob = job
        return job
    }

    private fun fireTimeout(job: Job, trailing: Boolean) {
        val invokedAt = now()
        val transition = synchronized(stateLock) {
            if (timeoutJob !== job) {
                null
            } else {
                timeoutJob = null
                val pendingArgs = lastArgs
                lastArgs = null
                val callback = if (trailing && pendingArgs != null) {
                    latestInvokedTime = invokedAt
                    ClaimedCallback(currentFn, pendingArgs.value)
                } else {
                    null
                }
                Transition<TParams>(callback = callback)
            }
        }
        completeTransition(transition)
    }

    private fun completeTransition(transition: Transition<TParams>?) {
        transition ?: return
        transition.timeoutToCancel?.cancel()
        transition.timeoutToStart?.start()
        transition.callback?.let { it.fn(it.params) }
    }

    fun cancel() {
        val transition = synchronized(stateLock) {
            val timeoutToCancel = timeoutJob
            timeoutJob = null
            lastArgs = null
            latestInvokedTime = Instant.DISTANT_PAST
            Transition<TParams>(timeoutToCancel = timeoutToCancel)
        }
        completeTransition(transition)
    }

    fun invoke(p1: TParams) {
        if (!scope.isActive) return
        val nowInstant = now()
        val transition = synchronized(stateLock) {
            val (wait, leading, trailing) = currentOptions
            val pendingArgs = PendingArgs(p1)
            lastArgs = pendingArgs

            if (latestInvokedTime == Instant.DISTANT_PAST && !leading) {
                latestInvokedTime = nowInstant
            }

            val remaining = wait - (nowInstant - latestInvokedTime)
            var timeoutToCancel: Job? = null
            var timeoutToStart: Job? = null
            var callback: ClaimedCallback<TParams>? = null

            if (remaining <= Duration.ZERO || remaining > wait) {
                timeoutToCancel = timeoutJob
                timeoutJob = null
                latestInvokedTime = nowInstant
                if (leading) {
                    if (lastArgs === pendingArgs) lastArgs = null
                    callback = ClaimedCallback(currentFn, pendingArgs.value)
                } else if (trailing) {
                    timeoutToStart = createTimeoutJobLocked(wait, trailing)
                } else if (lastArgs === pendingArgs) {
                    lastArgs = null
                }
            } else if (timeoutJob == null && trailing) {
                timeoutToStart = createTimeoutJobLocked(remaining, trailing)
            } else if (!trailing && lastArgs === pendingArgs) {
                lastArgs = null
            }

            Transition<TParams>(
                timeoutToCancel = timeoutToCancel,
                timeoutToStart = timeoutToStart,
                callback = callback,
            )
        }
        completeTransition(transition)
    }
}

@Composable
fun <S> useThrottleImpl(value: S, optionsOf: UseThrottleOptions.() -> Unit = {}): State<S> =
    useThrottleImpl(value, useDynamicOptions(optionsOf))

@Composable
fun <TParams> useThrottleFnImpl(fn: VoidFunction<TParams>, optionsOf: UseThrottleOptions.() -> Unit = {}): VoidFunction<TParams> =
    useThrottleFnImpl(fn, useDynamicOptions(optionsOf))

@Composable
fun useThrottleEffectImpl(vararg keys: Any?, optionsOf: UseThrottleOptions.() -> Unit = {}, block: SuspendAsyncFn) = useThrottleEffectImpl(
    keys = keys,
    useDynamicOptions(optionsOf),
    block = block,
)

@Composable
private fun <S> useThrottleImpl(value: S, options: UseThrottleOptions): State<S> {
    val (throttled, setThrottled) = _useGetStateImpl(value)
    val throttledSet = useThrottleFnImpl<None>(
        fn = {
            setThrottled(value)
        },
        options,
    )
    useEffectImpl(value) {
        throttledSet()
    }
    return throttled
}

@Composable
private fun <TParams> useThrottleFnImpl(fn: VoidFunction<TParams>, options: UseThrottleOptions): VoidFunction<TParams> {
    val latestFn by useLatestStateImpl(value = fn)
    val scope = rememberCoroutineScope()
    val throttled = remember {
        Throttle(latestFn, scope, options)
    }.apply {
        update(latestFn, options)
    }
    return remember { { p1: TParams -> throttled.invoke(p1) } }
}

@Composable
private fun useThrottleEffectImpl(vararg keys: Any?, options: UseThrottleOptions, block: SuspendAsyncFn) {
    val throttledBlock = useThrottleFnImpl<CoroutineScope>(
        fn = { coroutineScope ->
            coroutineScope.launch {
                this.block()
            }
        },
        options,
    )
    val scope = rememberCoroutineScope()
    useEffectImpl(*keys) {
        throttledBlock(scope)
    }
}
