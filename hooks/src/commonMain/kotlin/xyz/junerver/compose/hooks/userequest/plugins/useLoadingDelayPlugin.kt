package xyz.junerver.compose.hooks.userequest.plugins

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.userequest.*
import xyz.junerver.kotlin.asBoolean

/*
  Description:
  Author: Junerver
  Date: 2024/2/19-15:46
  Email: junerver@gmail.com
  Version: v1.0
*/
private class LoadingDelayPlugin<TData : Any> : Plugin<TData>() {
    /**
     * [ready]是动态值，可以通过外部副作用修改传递
     */
    var ready = true
    private var timeoutJob: Job? = null

    private fun cancelTimeout() = timeoutJob?.let {
        it.cancel()
        timeoutJob = null
    } ?: Unit

    override val invoke: GenPluginLifecycleFn<TData>
        get() = { fetch: Fetch<TData>, requestOptions: RequestOptions<TData> ->
            val (loadingDelay, staleTime) = with(requestOptions) { Pair(loadingDelay, staleTime) }
            object : PluginLifecycle<TData>() {
                override val onBefore: PluginOnBefore<TData>
                    get() = {
                        // 清空并创建一个新的定时器对象
                        cancelTimeout()
                        if (ready && staleTime != (-1).seconds && loadingDelay > staleTime) {
                            // 如果已经ready则添加一个定时任务
                            launch {
                                delay(loadingDelay)
                                fetch.setState(Keys.loading to true)
                            }.also { timeoutJob = it }
                        }
                        OnBeforeReturn(loading = false)
                    }

                override val onFinally: PluginOnFinally<TData>
                    get() = { _, _, _ -> cancelTimeout() }

                override val onCancel: PluginOnCancel
                    get() = { cancel() }
            }
        }

    override fun cancel() {
        cancelTimeout()
        super.cancel()
    }
}

@Composable
internal fun <T : Any> useLoadingDelayPlugin(options: RequestOptions<T>): Plugin<T> {
    val (loadingDelay, ready) = with(options) {
        loadingDelay to ready
    }
    if (!loadingDelay.asBoolean()) {
        return useEmptyPlugin()
    }

    val loadingDelayPlugin = remember {
        LoadingDelayPlugin<T>()
    }.apply {
        this.ready = ready
    }
    return loadingDelayPlugin
}
