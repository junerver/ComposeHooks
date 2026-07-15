package xyz.junerver.compose.hooks.userequest.plugins

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.time.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.userequest.Fetch
import xyz.junerver.compose.hooks.userequest.GenPluginLifecycleFn
import xyz.junerver.compose.hooks.userequest.Keys
import xyz.junerver.compose.hooks.userequest.OnBeforeReturn
import xyz.junerver.compose.hooks.userequest.Plugin
import xyz.junerver.compose.hooks.userequest.PluginLifecycle
import xyz.junerver.compose.hooks.userequest.PluginOnBefore
import xyz.junerver.compose.hooks.userequest.PluginOnCancel
import xyz.junerver.compose.hooks.userequest.PluginOnFinally
import xyz.junerver.compose.hooks.userequest.PluginOnRequest
import xyz.junerver.compose.hooks.userequest.UseRequestOptions
import xyz.junerver.compose.hooks.userequest.utils.RestoreFetchStateData
import xyz.junerver.compose.hooks.userequest.utils.trigger
import xyz.junerver.compose.hooks.utils.asBoolean

/*
  Description:
  Author: Junerver
  Date: 2024/2/19-15:46
  Email: junerver@gmail.com
  Version: v1.0
*/
internal class LoadingDelayTimer {
    private class ScheduledTask {
        var job: Job? = null
    }

    private val scheduledTask = MutableStateFlow<ScheduledTask?>(null)

    fun schedule(scope: CoroutineScope, delayDuration: Duration, block: suspend () -> Unit) {
        val task = ScheduledTask()
        scheduledTask.getAndUpdate { task }?.job?.cancel()
        val job = scope.launch {
            delay(delayDuration)
            if (!scheduledTask.compareAndSet(task, null)) return@launch
            block()
        }
        task.job = job
        if (scheduledTask.value !== task) job.cancel()
    }

    fun cancel() {
        scheduledTask.getAndUpdate { null }?.job?.cancel()
    }
}

private data class LoadingDelayConfig(
    val loadingDelay: Duration = Duration.ZERO,
    val ready: Boolean = true,
    val cacheKey: String = "",
)

private class LoadingDelayPlugin<TParams, TData : Any> : Plugin<TParams, TData>() {
    private val loadingDelayTimer = LoadingDelayTimer()
    private val config = MutableStateFlow(LoadingDelayConfig())

    fun updateConfig(loadingDelay: Duration, ready: Boolean, cacheKey: String) {
        val updatedConfig = LoadingDelayConfig(loadingDelay, ready, cacheKey)
        if (config.getAndUpdate { updatedConfig } != updatedConfig) {
            loadingDelayTimer.cancel()
        }
    }

    override val invoke: GenPluginLifecycleFn<TParams, TData>
        get() = { fetch: Fetch<TParams, TData>, useRequestOptions: UseRequestOptions<TParams, TData> ->
            initFetch(fetch, useRequestOptions)
            object : PluginLifecycle<TParams, TData>() {
                override val onBefore: PluginOnBefore<TParams, TData>
                    get() = {
                        loadingDelayTimer.cancel()
                        if (config.value.loadingDelay.asBoolean()) {
                            OnBeforeReturn(loading = false)
                        } else {
                            null
                        }
                    }

                override val onRequest: PluginOnRequest<TParams, TData>
                    get() = { _, _ ->
                        val configSnapshot = config.value
                        if (configSnapshot.ready && configSnapshot.loadingDelay.asBoolean()) {
                            loadingDelayTimer.schedule(fetch.scope, configSnapshot.loadingDelay) {
                                if (configSnapshot.cacheKey.asBoolean()) {
                                    trigger(configSnapshot.cacheKey, RestoreFetchStateData(loading = true))
                                } else {
                                    fetch.setState(Keys.loading to true)
                                }
                            }
                        }
                        null
                    }

                override val onFinally: PluginOnFinally<TParams, TData>
                    get() = { _, _, _ -> loadingDelayTimer.cancel() }

                override val onCancel: PluginOnCancel
                    get() = { cancel() }
            }
        }

    override fun cancel() {
        loadingDelayTimer.cancel()
        super.cancel()
    }
}

@Composable
internal fun <TParams, TData : Any> useLoadingDelayPlugin(options: UseRequestOptions<TParams, TData>): Plugin<TParams, TData> {
    val loadingDelay = options.loadingDelay
    val ready = options.ready
    val cacheKey = options.cacheKey
    val loadingDelayPlugin = remember {
        LoadingDelayPlugin<TParams, TData>()
    }.apply {
        updateConfig(loadingDelay, ready, cacheKey)
    }
    return loadingDelayPlugin
}
