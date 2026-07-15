package xyz.junerver.compose.hooks.userequest.plugins
import xyz.junerver.compose.hooks.usebackfront.useFrontToBackEffectImpl
import xyz.junerver.compose.hooks.usebackfront.useBackToFrontEffectImpl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.time.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.tuple
import xyz.junerver.compose.hooks.useBackToFrontEffect
import xyz.junerver.compose.hooks.useFrontToBackEffect
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
        var job: Job? = null
    }

    private var scheduledTask: ScheduledTask? = null

    fun schedule(scope: CoroutineScope, interval: Duration, block: suspend () -> Unit) {
        cancel()
        val task = ScheduledTask()
        scheduledTask = task
        task.job = scope.launch {
            delay(interval)
            if (scheduledTask !== task) return@launch
            scheduledTask = null
            block()
        }
    }

    fun cancel() {
        val task = scheduledTask
        scheduledTask = null
        task?.job?.cancel()
    }
}

private class PollingPlugin<TParams, TData : Any> : Plugin<TParams, TData>() {
    // 已经重试计数
    var currentRetryCount = 0

    var inBackground = false

    private val pollingTimer = PollingTimer()

    /** 轮询任务是一个单独 Job，新请求开始前必须停止旧任务，避免形成多个轮询链。 */
    fun stopPolling() {
        pollingTimer.cancel()
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
                        currentRetryCount += 1
                    }

                override val onSuccess: PluginOnSuccess<TParams, TData>
                    get() = { _, _ ->
                        currentRetryCount = 0
                    }

                override val onFinally: PluginOnFinally<TParams, TData>
                    get() = onFinally@{ _, _, _ ->
                        val usedScope = if (pollingWhenHidden) pluginScope else fetch.scope
                        if (!pollingWhenHidden && inBackground) return@onFinally
                        if (pollingErrorRetryCount == -1 || currentRetryCount <= pollingErrorRetryCount) {
                            pollingTimer.schedule(usedScope, pollingInterval) {
                                if (pollingWhenHidden) fetch.refreshAsync() else fetch.refresh()
                            }
                        } else {
                            currentRetryCount = 0
                        }
                    }

                override val onCancel: PluginOnCancel
                    get() = {
                        cancel()
                    }
            }
        }

    override fun cancel() {
        stopPolling()
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
    if (pollingInterval == Duration.ZERO) {
        return useEmptyPlugin()
    }
    val pollingPlugin = remember(pollingInterval, pollingWhenHidden, pollingErrorRetryCount) {
        PollingPlugin<TParams, TData>()
    }
    if (!pollingWhenHidden) {
        useBackToFrontEffect {
            pollingPlugin.refresh()
            pollingPlugin.inBackground = false
        }
        useFrontToBackEffect {
            pollingPlugin.stopPolling()
            pollingPlugin.inBackground = true
        }
    }

    return pollingPlugin
}
