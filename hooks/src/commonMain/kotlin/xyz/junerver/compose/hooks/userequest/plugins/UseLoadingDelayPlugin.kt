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
import xyz.junerver.compose.hooks.userequest.useEmptyPlugin
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

private class LoadingDelayPlugin<TParams, TData : Any> : Plugin<TParams, TData>() {
    /**
     * [ready]是动态值，可以通过外部副作用修改传递
     */
    var ready = true
    private val loadingDelayTimer = LoadingDelayTimer()

    override val invoke: GenPluginLifecycleFn<TParams, TData>
        get() = { fetch: Fetch<TParams, TData>, useRequestOptions: UseRequestOptions<TParams, TData> ->
            initFetch(fetch, useRequestOptions)
            val loadingDelay = useRequestOptions.loadingDelay
            val cacheKey = useRequestOptions.cacheKey
            object : PluginLifecycle<TParams, TData>() {
                override val onBefore: PluginOnBefore<TParams, TData>
                    get() = {
                        loadingDelayTimer.cancel()
                        OnBeforeReturn(loading = false)
                    }

                override val onRequest: PluginOnRequest<TParams, TData>
                    get() = { _, _ ->
                        if (ready) {
                            loadingDelayTimer.schedule(fetch.scope, loadingDelay) {
                                if (cacheKey.asBoolean()) {
                                    trigger(cacheKey, RestoreFetchStateData(loading = true))
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
    if (!loadingDelay.asBoolean()) {
        return useEmptyPlugin()
    }

    val loadingDelayPlugin = remember(loadingDelay, cacheKey) {
        LoadingDelayPlugin<TParams, TData>()
    }.apply {
        this.ready = ready
    }
    return loadingDelayPlugin
}
