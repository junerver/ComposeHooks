package xyz.junerver.compose.hooks.userequest.plugins

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.TParams
import xyz.junerver.compose.hooks.userequest.Fetch
import xyz.junerver.compose.hooks.userequest.GenPluginLifecycleFn
import xyz.junerver.compose.hooks.userequest.OnBeforeReturn
import xyz.junerver.compose.hooks.userequest.Plugin
import xyz.junerver.compose.hooks.userequest.PluginLifecycle
import xyz.junerver.compose.hooks.userequest.RequestOptions
import xyz.junerver.compose.hooks.userequest.useEmptyPlugin
import xyz.junerver.kotlin.tuple
import kotlin.math.pow
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Description:
 * @author Junerver
 * date: 2024/2/20-10:26
 * Email: junerver@gmail.com
 * Version: v1.0
 */
class RetryPlugin<TData : Any> : Plugin<TData>() {
    var count = 0
    var triggerByRetry = false //触发retry标志

    override val invoke: GenPluginLifecycleFn<TData>
        get() = { fetch: Fetch<TData>, requestOptions: RequestOptions<TData> ->
            val (retryInterval, retryCount) = with(requestOptions) {
                tuple(
                    retryInterval,
                    retryCount
                )
            }

            object : PluginLifecycle<TData>() {
                override val onBefore: ((TParams) -> OnBeforeReturn<TData>?)
                    get() = {
                        //未触发retry时，
                        if (!triggerByRetry) {
                            count = 0
                        }
                        triggerByRetry = false
                        null
                    }

                override val onSuccess: ((data: TData, params: TParams) -> Unit)
                    get() = { _, _ ->
                        count = 0
                    }

                override val onError: ((e: Throwable, params: TParams) -> Unit)
                    get() = { _, _ ->
                        count++
                        if (retryCount == -1 || count <= retryCount) {
                            launch(Dispatchers.IO) {
                                delay(
                                    if (retryInterval > 0.milliseconds)
                                        retryInterval
                                    else
                                        (1.seconds * 2f.pow(count).toInt()).coerceAtMost(30.seconds)

                                )
                                triggerByRetry = true
                                fetch.refreshAsync()
                            }
                        } else {
                            count = 0
                        }
                    }
                override val onCancel: (() -> Unit)
                    get() = {
                        cancel()
                        count = 0
                    }
            }
        }
}

@Composable
fun <T : Any> useRetryPlugin(options: RequestOptions<T>): Plugin<T> {
    if (options.retryCount == 0) {
        return useEmptyPlugin()
    }
    val retryPlugin = rememberSaveable {
        RetryPlugin<T>()
    }
    return retryPlugin
}