package xyz.junerver.compose.hooks.userequest.plugins

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.math.pow
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.tuple
import xyz.junerver.compose.hooks.userequest.Fetch
import xyz.junerver.compose.hooks.userequest.GenPluginLifecycleFn
import xyz.junerver.compose.hooks.userequest.Plugin
import xyz.junerver.compose.hooks.userequest.PluginLifecycle
import xyz.junerver.compose.hooks.userequest.PluginOnBefore
import xyz.junerver.compose.hooks.userequest.PluginOnCancel
import xyz.junerver.compose.hooks.userequest.PluginOnError
import xyz.junerver.compose.hooks.userequest.PluginOnSuccess
import xyz.junerver.compose.hooks.userequest.RequestOptions
import xyz.junerver.compose.hooks.userequest.useEmptyPlugin
import xyz.junerver.compose.hooks.utils.asBoolean

/*
  Description:
  Author: Junerver
  Date: 2024/2/20-10:26
  Email: junerver@gmail.com
  Version: v1.0
*/
private class RetryPlugin<TParams, TData : Any> : Plugin<TParams, TData>() {
    var count = 0
    var triggerByRetry = false // 触发retry标志

    override val invoke: GenPluginLifecycleFn<TParams, TData>
        get() = { fetch: Fetch<TParams, TData>, requestOptions: RequestOptions<TParams, TData> ->
            val (retryInterval, retryCount) = with(requestOptions) {
                tuple(retryInterval, retryCount)
            }

            object : PluginLifecycle<TParams, TData>() {
                override val onBefore: PluginOnBefore<TParams, TData>
                    get() = {
                        // 未触发retry时，
                        if (!triggerByRetry) {
                            count = 0
                        }
                        triggerByRetry = false
                        null
                    }

                override val onSuccess: PluginOnSuccess<TParams, TData>
                    get() = { _, _ ->
                        count = 0
                    }

                override val onError: PluginOnError<TParams>
                    get() = { _, _ ->
                        count++
                        if (retryCount == -1 || count <= retryCount) {
                            launch {
                                delay(
                                    if (retryInterval.asBoolean()) {
                                        retryInterval
                                    } else {
                                        (1.seconds * 2f.pow(count).toInt()).coerceAtMost(30.seconds)
                                    },
                                )
                                triggerByRetry = true
                                fetch.refreshAsync()
                            }
                        } else {
                            count = 0
                        }
                    }
                override val onCancel: PluginOnCancel
                    get() = {
                        cancel()
                        count = 0
                    }
            }
        }
}

@Composable
internal fun <TParams, TData : Any> useRetryPlugin(options: RequestOptions<TParams, TData>): Plugin<TParams, TData> {
    if (options.retryCount == 0) {
        return useEmptyPlugin()
    }
    val retryPlugin = remember {
        RetryPlugin<TParams, TData>()
    }
    return retryPlugin
}
