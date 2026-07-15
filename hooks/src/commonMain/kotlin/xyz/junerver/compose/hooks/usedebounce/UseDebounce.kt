package xyz.junerver.compose.hooks.usedebounce

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
  Description: Debounce hooks for Compose
  Author: Junerver
  Date: 2024/1/29-14:46
  Email: junerver@gmail.com
  Version: v1.0

  Update: 2025/7/15-18:54 by Junerver
  Version: v1.1
  Description: fix leading
*/

@Stable
data class UseDebounceOptions internal constructor(
    var wait: Duration = 1.seconds,
    var leading: Boolean = false,
    var trailing: Boolean = true,
    var maxWait: Duration = Duration.ZERO,
) {
    companion object : Options<UseDebounceOptions>(::UseDebounceOptions)
}

@Stable
internal class Debounce<TParams>(
    fn: VoidFunction<TParams>,
    private val scope: CoroutineScope,
    options: UseDebounceOptions = UseDebounceOptions(),
    private val now: () -> Instant = { currentInstant },
) {
    private class PendingArgs<T>(val value: T)

    private class ClaimedCallback<T>(
        val fn: VoidFunction<T>,
        val params: T,
    )

    private class Transition<T>(
        val timeoutToCancel: Job? = null,
        val maxWaitToCancel: Job? = null,
        val timeoutToStart: Job? = null,
        val maxWaitToStart: Job? = null,
        val callback: ClaimedCallback<T>? = null,
    )

    private val stateLock = SynchronizedObject()
    private var currentFn = fn
    private var currentOptions = options
    private var timeoutJob: Job? = null
    private var maxWaitJob: Job? = null
    private var latestInvokedTime = Instant.DISTANT_PAST
    private var latestCalledTime = latestInvokedTime
    private var lastArgs: PendingArgs<TParams>? = null

    private var isAwaitingNextDebounce: Boolean = true

    fun update(fn: VoidFunction<TParams>, options: UseDebounceOptions) {
        synchronized(stateLock) {
            currentFn = fn
            currentOptions = options
        }
    }

    private fun claimCallbackLocked(params: PendingArgs<TParams>?, invokedAt: Instant): ClaimedCallback<TParams>? {
        params ?: return null
        if (lastArgs === params) lastArgs = null
        latestInvokedTime = invokedAt
        return ClaimedCallback(currentFn, params.value)
    }

    private fun resetDebounceStateLocked(): Job? {
        isAwaitingNextDebounce = true
        timeoutJob = null
        val maxWaitToCancel = maxWaitJob
        maxWaitJob = null
        lastArgs = null
        return maxWaitToCancel
    }

    private fun createMaxWaitJobLocked(maxWait: Duration, trailing: Boolean): Job {
        lateinit var job: Job
        job = scope.launch(start = CoroutineStart.LAZY) {
            delay(maxWait)
            fireMaxWait(job, trailing)
        }
        maxWaitJob = job
        return job
    }

    private fun createResetTimeoutJobLocked(wait: Duration): Job {
        lateinit var job: Job
        job = scope.launch(start = CoroutineStart.LAZY) {
            delay(wait)
            fireResetTimeout(job)
        }
        timeoutJob = job
        return job
    }

    private fun createTrailingTimeoutJobLocked(
        wait: Duration,
        trailing: Boolean,
        isMaxWaitExceeded: Boolean,
    ): Job {
        lateinit var job: Job
        job = scope.launch(start = CoroutineStart.LAZY) {
            delay(wait)
            fireTrailingTimeout(job, wait, trailing, isMaxWaitExceeded)
        }
        timeoutJob = job
        return job
    }

    private fun fireMaxWait(job: Job, trailing: Boolean) {
        val invokedAt = now()
        val transition = synchronized(stateLock) {
            if (maxWaitJob !== job) {
                null
            } else {
                maxWaitJob = null
                val callback = if (trailing) claimCallbackLocked(lastArgs, invokedAt) else null
                Transition<TParams>(callback = callback)
            }
        }
        completeTransition(transition)
    }

    private fun fireResetTimeout(job: Job) {
        val transition = synchronized(stateLock) {
            if (timeoutJob !== job) {
                null
            } else {
                Transition<TParams>(maxWaitToCancel = resetDebounceStateLocked())
            }
        }
        completeTransition(transition)
    }

    private fun fireTrailingTimeout(
        job: Job,
        wait: Duration,
        trailing: Boolean,
        isMaxWaitExceeded: Boolean,
    ) {
        val invokedAt = now()
        val transition = synchronized(stateLock) {
            if (timeoutJob !== job) {
                null
            } else {
                val trailingArgs = lastArgs.takeIf {
                    (invokedAt - latestCalledTime >= wait || isMaxWaitExceeded) && trailing
                }
                val maxWaitToCancel = resetDebounceStateLocked()
                Transition<TParams>(
                    maxWaitToCancel = maxWaitToCancel,
                    callback = claimCallbackLocked(trailingArgs, invokedAt),
                )
            }
        }
        completeTransition(transition)
    }

    private fun completeTransition(transition: Transition<TParams>?) {
        transition ?: return
        transition.timeoutToCancel?.cancel()
        transition.maxWaitToCancel?.cancel()
        transition.maxWaitToStart?.start()
        transition.timeoutToStart?.start()
        transition.callback?.let { it.fn(it.params) }
    }

    fun cancel() {
        val transition = synchronized(stateLock) {
            val timeoutToCancel = timeoutJob
            val maxWaitToCancel = maxWaitJob
            timeoutJob = null
            maxWaitJob = null
            latestInvokedTime = Instant.DISTANT_PAST
            latestCalledTime = latestInvokedTime
            lastArgs = null
            isAwaitingNextDebounce = true
            Transition<TParams>(
                timeoutToCancel = timeoutToCancel,
                maxWaitToCancel = maxWaitToCancel,
            )
        }
        completeTransition(transition)
    }

    fun invoke(p1: TParams) {
        if (!scope.isActive) return
        val currentTimeStamp = now()
        val transition = synchronized(stateLock) {
            val (wait, leading, trailing, maxWait) = currentOptions
            val pendingArgs = PendingArgs(p1)
            lastArgs = pendingArgs
            if (latestInvokedTime == Instant.DISTANT_PAST) {
                latestInvokedTime = currentTimeStamp
            }
            val waitTime = currentTimeStamp - latestInvokedTime
            val isMaxWaitExceeded = maxWait > Duration.ZERO && waitTime >= maxWait
            val shouldInvokeImmediately = leading && isAwaitingNextDebounce
            latestCalledTime = currentTimeStamp

            val timeoutToCancel = timeoutJob
            timeoutJob = null
            var maxWaitToCancel: Job? = null
            var maxWaitToStart: Job? = null
            val timeoutToStart: Job
            val callback: ClaimedCallback<TParams>?

            if (shouldInvokeImmediately) {
                if (trailing && maxWait > Duration.ZERO && maxWaitJob == null) {
                    maxWaitToStart = createMaxWaitJobLocked(maxWait, trailing)
                }
                isAwaitingNextDebounce = false
                timeoutToStart = createResetTimeoutJobLocked(wait)
                callback = claimCallbackLocked(pendingArgs, currentTimeStamp)
            } else if (isMaxWaitExceeded) {
                maxWaitToCancel = maxWaitJob
                maxWaitJob = null
                isAwaitingNextDebounce = false
                timeoutToStart = createResetTimeoutJobLocked(wait)
                callback = claimCallbackLocked(lastArgs, currentTimeStamp)
            } else {
                if (trailing && maxWait > Duration.ZERO && maxWaitJob == null) {
                    maxWaitToStart = createMaxWaitJobLocked(maxWait, trailing)
                }
                timeoutToStart = createTrailingTimeoutJobLocked(wait, trailing, isMaxWaitExceeded)
                callback = null
            }

            Transition<TParams>(
                timeoutToCancel = timeoutToCancel,
                maxWaitToCancel = maxWaitToCancel,
                timeoutToStart = timeoutToStart,
                maxWaitToStart = maxWaitToStart,
                callback = callback,
            )
        }
        completeTransition(transition)
    }
}

@Composable
fun <S> useDebounceImpl(value: S, optionsOf: UseDebounceOptions.() -> Unit = {}): State<S> =
    useDebounceImpl(value, useDynamicOptions(optionsOf))

@Composable
fun <TParams> useDebounceFnImpl(fn: VoidFunction<TParams>, optionsOf: UseDebounceOptions.() -> Unit = {}): VoidFunction<TParams> =
    useDebounceFnImpl(fn, useDynamicOptions(optionsOf))

@Composable
fun useDebounceEffectImpl(vararg keys: Any?, optionsOf: UseDebounceOptions.() -> Unit = {}, block: SuspendAsyncFn) = useDebounceEffectImpl(
    keys = keys,
    useDynamicOptions(optionsOf),
    block,
)

@Composable
private fun <S> useDebounceImpl(value: S, options: UseDebounceOptions): State<S> {
    val (debounced, setDebounced) = _useGetStateImpl(value)
    val debouncedSet = useDebounceFnImpl<None>(
        fn = {
            setDebounced(value)
        },
        options,
    )
    useEffectImpl(value) {
        debouncedSet()
    }
    return debounced
}

@Composable
private fun <TParams> useDebounceFnImpl(fn: VoidFunction<TParams>, options: UseDebounceOptions): VoidFunction<TParams> {
    val latestFn by useLatestStateImpl(value = fn)
    val scope = rememberCoroutineScope()
    val debounced = remember {
        Debounce(latestFn, scope, options)
    }.apply {
        update(latestFn, options)
    }
    return remember { { p1 -> debounced.invoke(p1) } }
}

@Composable
private fun useDebounceEffectImpl(vararg keys: Any?, options: UseDebounceOptions, block: SuspendAsyncFn) {
    val debouncedBlock = useDebounceFnImpl<CoroutineScope>(
        fn = { coroutineScope ->
            coroutineScope.launch {
                this.block()
            }
        },
        options,
    )
    val scope = rememberCoroutineScope()
    useEffectImpl(*keys) {
        debouncedBlock(scope)
    }
}
