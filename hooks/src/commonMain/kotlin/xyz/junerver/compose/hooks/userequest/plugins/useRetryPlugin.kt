package xyz.junerver.compose.hooks.userequest.plugins

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.math.pow
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.userequest.*

/*
  Description:
  Author: Junerver
  Date: 2024/2/20-10:26
  Email: junerver@gmail.com
  Version: v1.0
*/
private class RetryPlugin<TData : Any> : Plugin<TData>() {
    var count = 0
    var triggerByRetry = false // 触发retry标志

    override val invoke: GenPluginLifecycleFn<TData>
        get() = { fetch: Fetch<TData>, requestOptions: RequestOptions<TData> ->
            val (retryInterval, retryCount) = with(requestOptions) {
                Pair(retryInterval, retryCount)
            }

            object : PluginLifecycle<TData>() {
                override val onBefore: PluginOnBefore<TData>
                    get() = {
                        // 未触发retry时，
                        if (!triggerByRetry) {
                            count = 0
                        }
                        triggerByRetry = false
                        null
                    }

                override val onSuccess: PluginOnSuccess<TData>
                    get() = { _, _ ->
                        count = 0
                    }

                override val onError: PluginOnError
                    get() = { _, _ ->
                        count++
                        if (retryCount == -1 || count <= retryCount) {
                            launch {
                                delay(
                                    if (retryInterval > 0.milliseconds) {
                                        retryInterval
                                    } else {
                                        (1.seconds * 2f.pow(count).toInt()).coerceAtMost(30.seconds)
                                    }
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
internal fun <T : Any> useRetryPlugin(options: RequestOptions<T>): Plugin<T> {
    if (options.retryCount == 0) {
        return useEmptyPlugin()
    }
    val retryPlugin = remember {
        RetryPlugin<T>()
    }
    return retryPlugin
}
