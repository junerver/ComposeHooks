package xyz.junerver.compose.hooks.userequest.plugins

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.TParams
import xyz.junerver.compose.hooks.useBackToFrontEffect
import xyz.junerver.compose.hooks.useFrontToBackEffect
import xyz.junerver.compose.hooks.userequest.Fetch
import xyz.junerver.compose.hooks.userequest.GenPluginLifecycleFn
import xyz.junerver.compose.hooks.userequest.OnBeforeReturn
import xyz.junerver.compose.hooks.userequest.Plugin
import xyz.junerver.compose.hooks.userequest.PluginLifecycle
import xyz.junerver.compose.hooks.userequest.RequestOptions
import xyz.junerver.compose.hooks.userequest.useEmptyPlugin
import xyz.junerver.kotlin.tuple

/*
  Description:
  Author: Junerver
  Date: 2024/2/20-13:07
  Email: junerver@gmail.com
  Version: v1.0
*/
private class PollingPlugin<TData : Any> : Plugin<TData>() {
    // 已经重试计数
    var currentRetryCount = 0

    var inBackground = false

    // 保存正在轮询的job，其本质是一个延时执行刷新的协程job
    private lateinit var pollingJob: Job

    // 被使用的作用域
    private lateinit var usedScope: CoroutineScope

    /**
     * 判断是否正在轮询中
     */
    private fun isPolling(): Boolean =
        this::pollingJob.isInitialized && pollingJob.isActive

    /**
     * 轮询任务是一个单独job，应该单独停止。[force]时强制停止
     */
    fun stopPolling(force: Boolean = false) {
        // 正在轮询， 未设置隐藏时轮询
        if ((isPolling() && !options.pollingWhenHidden) || force) {
            if (this::pollingJob.isInitialized) pollingJob.cancel()
        }
    }

    override val invoke: GenPluginLifecycleFn<TData>
        get() = { fetch: Fetch<TData>, requestOptions: RequestOptions<TData> ->
            initFetch(fetch, requestOptions)
            val (pollingInterval, pollingWhenHidden, pollingErrorRetryCount) = with(requestOptions) {
                tuple(pollingInterval, pollingWhenHidden, pollingErrorRetryCount)
            }
            val pluginScope = this

            object : PluginLifecycle<TData>() {
                override val onBefore: ((TParams) -> OnBeforeReturn<TData>?)
                    get() = {
                        stopPolling()
                        null
                    }

                override val onError: ((e: Throwable, params: TParams) -> Unit)
                    get() = { _, _ ->
                        currentRetryCount += 1
                    }

                override val onSuccess: ((data: TData, params: TParams) -> Unit)
                    get() = { _, _ ->
                        currentRetryCount = 0
                    }

                override val onFinally: ((params: TParams, data: TData?, e: Throwable?) -> Unit)
                    get() = onFinally@{ _, _, _ ->
                        usedScope = if (pollingWhenHidden) pluginScope else fetch.scope
                        if (!pollingWhenHidden && inBackground) return@onFinally
                        if (pollingErrorRetryCount == -1 || currentRetryCount <= pollingErrorRetryCount) {
                            usedScope.launch(Dispatchers.IO) {
                                delay(pollingInterval)
                                if (pollingWhenHidden) fetch.refreshAsync() else fetch.refresh()
                            }.also { pollingJob = it }
                        } else {
                            currentRetryCount = 0
                        }
                    }

                override val onCancel: (() -> Unit)
                    get() = {
                        cancel()
                    }
            }
        }

    override fun cancel() {
        if (isPolling()) pollingJob.cancel()
        super.cancel()
    }

    override fun refresh() {
        fetchInstance.refresh()
    }
}

@Composable
internal fun <T : Any> usePollingPlugin(options: RequestOptions<T>): Plugin<T> {
    if (options.pollingInterval == 0.milliseconds) {
        return useEmptyPlugin()
    }
    val pollingPlugin = remember {
        PollingPlugin<T>()
    }
    if (!options.pollingWhenHidden) {
        useBackToFrontEffect {
            pollingPlugin.refresh()
            pollingPlugin.inBackground = false
        }
        useFrontToBackEffect {
            pollingPlugin.stopPolling(true)
            pollingPlugin.inBackground = true
        }
    }

    return pollingPlugin
}
