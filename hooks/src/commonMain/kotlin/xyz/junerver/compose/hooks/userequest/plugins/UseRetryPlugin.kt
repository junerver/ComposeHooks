package xyz.junerver.compose.hooks.userequest.plugins

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.tuple
import xyz.junerver.compose.hooks.userequest.Fetch
import xyz.junerver.compose.hooks.userequest.GenPluginLifecycleFn
import xyz.junerver.compose.hooks.userequest.Plugin
import xyz.junerver.compose.hooks.userequest.PluginLifecycle
import xyz.junerver.compose.hooks.userequest.PluginOnCancel
import xyz.junerver.compose.hooks.userequest.PluginOnError
import xyz.junerver.compose.hooks.userequest.PluginOnSuccess
import xyz.junerver.compose.hooks.userequest.UseRequestOptions
import xyz.junerver.compose.hooks.userequest.useEmptyPlugin

/*
  Description:
  Author: Junerver
  Date: 2024/2/20-10:26
  Email: junerver@gmail.com
  Version: v1.0
*/
internal class RetryTimer {
    private class ScheduledTask {
        lateinit var job: Job
    }

    private val scheduledTask = MutableStateFlow<ScheduledTask?>(null)

    fun schedule(scope: CoroutineScope, interval: Duration, block: suspend () -> Unit) {
        val task = ScheduledTask()
        task.job = scope.launch(start = CoroutineStart.LAZY) {
            delay(interval)
            if (!scheduledTask.compareAndSet(task, null)) return@launch
            block()
        }
        scheduledTask.getAndUpdate { task }?.job?.cancel()
        task.job.start()
        if (scheduledTask.value !== task) task.job.cancel()
    }

    fun cancel() {
        scheduledTask.getAndUpdate { null }?.job?.cancel()
    }
}

internal fun retryDelay(configuredInterval: Duration, retryAttempt: Int): Duration = when {
    configuredInterval > Duration.ZERO -> configuredInterval
    retryAttempt >= 5 -> 30.seconds
    retryAttempt > 0 -> 1.seconds * (1 shl retryAttempt)
    else -> 1.seconds
}

private class RetryPlugin<TParams, TData : Any> : Plugin<TParams, TData>() {
    private data class RetryChain<TParams>(
        val epoch: Long = 0L,
        val dispatchToken: Long = Long.MIN_VALUE,
        val count: Int = 0,
        val failedParams: TParams? = null,
    )

    private val stateLock = SynchronizedObject()
    private val retryTimer = RetryTimer()
    private var retryChain = RetryChain<TParams>()

    override val invoke: GenPluginLifecycleFn<TParams, TData>
        get() = { fetch: Fetch<TParams, TData>, useRequestOptions: UseRequestOptions<TParams, TData> ->
            val (retryInterval, retryCount) = with(useRequestOptions) {
                tuple(retryInterval, retryCount)
            }

            object : PluginLifecycle<TParams, TData>() {
                override val onSuccess: PluginOnSuccess<TParams, TData>
                    get() = { _, _ ->
                        resetChain(fetch.requestDispatchToken())
                    }

                override val onError: PluginOnError<TParams>
                    get() = { _, params ->
                        synchronized(stateLock) {
                            val dispatchToken = fetch.requestDispatchToken()
                            val previousChain = retryChain
                            val continuesCurrentChain = previousChain.dispatchToken == dispatchToken
                            val nextCount = if (continuesCurrentChain) previousChain.count + 1 else 1
                            val nextEpoch = if (continuesCurrentChain) previousChain.epoch else previousChain.epoch + 1L

                            if (retryCount == -1 || nextCount <= retryCount) {
                                val scheduledChain = RetryChain(
                                    epoch = nextEpoch,
                                    dispatchToken = dispatchToken,
                                    count = nextCount,
                                    failedParams = params,
                                )
                                retryChain = scheduledChain
                                retryTimer.schedule(this@RetryPlugin, retryDelay(retryInterval, nextCount)) {
                                    dispatchRetry(fetch, scheduledChain)
                                }
                            } else {
                                retryChain = RetryChain(
                                    epoch = nextEpoch + 1L,
                                    dispatchToken = dispatchToken,
                                )
                                retryTimer.cancel()
                            }
                        }
                    }
                override val onCancel: PluginOnCancel
                    get() = {
                        cancel()
                    }
            }
        }

    override fun cancel() {
        resetChain()
        super.cancel()
    }

    private fun resetChain(dispatchToken: Long = retryChain.dispatchToken) {
        synchronized(stateLock) {
            retryChain = RetryChain(
                epoch = retryChain.epoch + 1L,
                dispatchToken = dispatchToken,
            )
            retryTimer.cancel()
        }
    }

    private fun dispatchRetry(fetch: Fetch<TParams, TData>, scheduledChain: RetryChain<TParams>) {
        synchronized(stateLock) {
            if (
                retryChain.epoch != scheduledChain.epoch ||
                retryChain.dispatchToken != scheduledChain.dispatchToken ||
                retryChain.count != scheduledChain.count
            ) {
                return
            }
            if (!fetch.runIfDispatchTokenCurrent(scheduledChain.dispatchToken, scheduledChain.failedParams)) {
                retryChain = RetryChain(
                    epoch = retryChain.epoch + 1L,
                    dispatchToken = fetch.requestDispatchToken(),
                )
            }
        }
    }
}

@Composable
internal fun <TParams, TData : Any> useRetryPlugin(options: UseRequestOptions<TParams, TData>): Plugin<TParams, TData> {
    val retryCount = options.retryCount
    val retryInterval = options.retryInterval
    if (retryCount == 0 || retryCount < -1) {
        return useEmptyPlugin()
    }
    val retryPlugin = remember(retryCount, retryInterval) {
        RetryPlugin<TParams, TData>()
    }
    return retryPlugin
}
