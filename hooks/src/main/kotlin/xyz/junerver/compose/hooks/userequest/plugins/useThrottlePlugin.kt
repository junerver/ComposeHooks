package xyz.junerver.compose.hooks.userequest.plugins

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.time.Duration.Companion.seconds
import xyz.junerver.compose.hooks.Throttle
import xyz.junerver.compose.hooks.userequest.Fetch
import xyz.junerver.compose.hooks.userequest.GenPluginLifecycleFn
import xyz.junerver.compose.hooks.userequest.Plugin
import xyz.junerver.compose.hooks.userequest.PluginLifecycle
import xyz.junerver.compose.hooks.userequest.PluginOnCancel
import xyz.junerver.compose.hooks.userequest.RequestOptions
import xyz.junerver.compose.hooks.userequest.useEmptyPlugin

/*
  Description:
  Author: Junerver
  Date: 2024/2/19-13:38
  Email: junerver@gmail.com
  Version: v1.0
*/
private class ThrottlePlugin<TData : Any> : Plugin<TData>() {

    override val invoke: GenPluginLifecycleFn<TData>
        get() = { fetch: Fetch<TData>, requestOptions: RequestOptions<TData> ->
            initFetch(fetch, requestOptions)
            if (requestOptions.throttleOptions.wait > 0.seconds) {
                val throttle = Throttle(
                    fn = { params -> fetch._run(params) },
                    scope = this,
                    requestOptions.throttleOptions
                )
                fetch.run = {
                    throttle(it)
                }
                fetch.runAsync = {
                    throttle(it)
                }
            }
            object : PluginLifecycle<TData>() {
                override val onCancel: PluginOnCancel
                    get() = {
                        cancel()
                    }
            }
        }
}

@Composable
internal fun <T : Any> useThrottlePlugin(options: RequestOptions<T>): Plugin<T> {
    if (options.throttleOptions.wait == 0.seconds) {
        return useEmptyPlugin()
    }
    val throttlePlugin = remember {
        ThrottlePlugin<T>()
    }
    return throttlePlugin
}
