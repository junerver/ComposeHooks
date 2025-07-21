package xyz.junerver.compose.hooks.userequest.plugins

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.time.Duration
import xyz.junerver.compose.hooks.Throttle
import xyz.junerver.compose.hooks.userequest.Fetch
import xyz.junerver.compose.hooks.userequest.GenPluginLifecycleFn
import xyz.junerver.compose.hooks.userequest.Plugin
import xyz.junerver.compose.hooks.userequest.PluginLifecycle
import xyz.junerver.compose.hooks.userequest.PluginOnCancel
import xyz.junerver.compose.hooks.userequest.UseRequestOptions
import xyz.junerver.compose.hooks.userequest.useEmptyPlugin

/*
  Description:
  Author: Junerver
  Date: 2024/2/19-13:38
  Email: junerver@gmail.com
  Version: v1.0
*/
private class ThrottlePlugin<TParams, TData : Any> : Plugin<TParams, TData>() {
    override val invoke: GenPluginLifecycleFn<TParams, TData>
        get() = { fetch: Fetch<TParams, TData>, useRequestOptions: UseRequestOptions<TParams, TData> ->
            initFetch(fetch, useRequestOptions)
            if (useRequestOptions.throttleOptions.wait > Duration.ZERO) {
                val throttle = Throttle<TParams?>(
                    fn = { params -> fetch._run(params) },
                    scope = this,
                    useRequestOptions.throttleOptions,
                )
                fetch.run = {
                    throttle.invoke(it)
                }
                fetch.runAsync = {
                    throttle.invoke(it)
                }
            }
            object : PluginLifecycle<TParams, TData>() {
                override val onCancel: PluginOnCancel
                    get() = {
                        cancel()
                    }
            }
        }
}

@Composable
internal fun <TParams, TData : Any> useThrottlePlugin(options: UseRequestOptions<TParams, TData>): Plugin<TParams, TData> {
    if (options.throttleOptions.wait == Duration.ZERO) {
        return useEmptyPlugin()
    }
    val throttlePlugin = remember {
        ThrottlePlugin<TParams, TData>()
    }
    return throttlePlugin
}
