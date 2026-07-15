package xyz.junerver.compose.hooks.userequest.plugins

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlin.time.Duration
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
import xyz.junerver.compose.hooks.userequest.PluginOnBefore
import xyz.junerver.compose.hooks.userequest.PluginOnCancel
import xyz.junerver.compose.hooks.userequest.PluginOnError
import xyz.junerver.compose.hooks.userequest.PluginOnFinally
import xyz.junerver.compose.hooks.userequest.PluginOnSuccess
import xyz.junerver.compose.hooks.userequest.UseRequestOptions
import xyz.junerver.compose.hooks.userequest.useEmptyPlugin

/*
  Description:
  Author: Junerver
  Date: 2024/2/20-13:07
  Email: junerver@gmail.com
  Version: v1.0
*/
internal class PollingTimer {
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

private class PollingPlugin<TParams, TData : Any>(initiallyInBackground: Boolean) : Plugin<TParams, TData>() {
    private val stateLock = SynchronizedObject()
    private val pollingTimer = PollingTimer()
    private var currentRetryCount = 0
    private var inBackground = initiallyInBackground
    private var pollingEpoch = 0L

    /** 轮询任务是一个单独 Job，新请求开始前必须停止旧任务，避免形成多个轮询链。 */
    private fun stopPolling(resetErrors: Boolean = false) {
        synchronized(stateLock) {
            pollingEpoch += 1L
            if (resetErrors) currentRetryCount = 0
        }
        pollingTimer.cancel()
    }

    fun enterBackground() {
        synchronized(stateLock) {
            inBackground = true
            pollingEpoch += 1L
        }
        pollingTimer.cancel()
    }

    fun enterForegroundAndRefresh() {
        val shouldRefresh = synchronized(stateLock) {
            if (!inBackground) {
                false
            } else {
                inBackground = false
                pollingEpoch += 1L
                true
            }
        }
        if (shouldRefresh) refresh()
    }

    override val invoke: GenPluginLifecycleFn<TParams, TData>
        get() = { fetch: Fetch<TParams, TData>, useRequestOptions: UseRequestOptions<TParams, TData> ->
            initFetch(fetch, useRequestOptions)
            val (pollingInterval, pollingWhenHidden, pollingErrorRetryCount) = with(useRequestOptions) {
                tuple(pollingInterval, pollingWhenHidden, pollingErrorRetryCount)
            }
            val pluginScope = this

            object : PluginLifecycle<TParams, TData>() {
                override val onBefore: PluginOnBefore<TParams, TData>
                    get() = {
                        stopPolling()
                        null
                    }

                override val onError: PluginOnError<TParams>
                    get() = { _, _ ->
                        synchronized(stateLock) {
                            currentRetryCount += 1
                        }
                    }

                override val onSuccess: PluginOnSuccess<TParams, TData>
                    get() = { _, _ ->
                        synchronized(stateLock) {
                            currentRetryCount = 0
                        }
                    }

                override val onFinally: PluginOnFinally<TParams, TData>
                    get() = onFinally@{ params, _, _ ->
                        val usedScope = if (pollingWhenHidden) pluginScope else fetch.scope
                        synchronized(stateLock) {
                            if (!pollingWhenHidden && inBackground) return@onFinally
                            if (pollingErrorRetryCount != -1 && currentRetryCount > pollingErrorRetryCount) {
                                currentRetryCount = 0
                                return@onFinally
                            }
                            val scheduledEpoch = pollingEpoch
                            val dispatchToken = fetch.requestDispatchToken()
                            pollingTimer.schedule(usedScope, pollingInterval) {
                                synchronized(stateLock) {
                                    if (
                                        scheduledEpoch == pollingEpoch &&
                                        (pollingWhenHidden || !inBackground)
                                    ) {
                                        fetch.runIfDispatchTokenCurrent(dispatchToken, params)
                                    }
                                }
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
        stopPolling(resetErrors = true)
        super.cancel()
    }

    override fun refresh() {
        fetchInstance.refresh()
    }
}

@Composable
internal fun <TParams, TData : Any> usePollingPlugin(options: UseRequestOptions<TParams, TData>): Plugin<TParams, TData> {
    val pollingInterval = options.pollingInterval
    val pollingWhenHidden = options.pollingWhenHidden
    val pollingErrorRetryCount = options.pollingErrorRetryCount
    if (pollingInterval <= Duration.ZERO) {
        return useEmptyPlugin()
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    val initiallyInBackground = !lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
    val pollingPlugin = remember(pollingInterval, pollingWhenHidden, pollingErrorRetryCount, lifecycleOwner) {
        PollingPlugin<TParams, TData>(initiallyInBackground)
    }
    if (!pollingWhenHidden) {
        LifecycleResumeEffect(pollingPlugin, lifecycleOwner = lifecycleOwner) {
            pollingPlugin.enterForegroundAndRefresh()
            onPauseOrDispose {
                pollingPlugin.enterBackground()
            }
        }
    }

    return pollingPlugin
}
