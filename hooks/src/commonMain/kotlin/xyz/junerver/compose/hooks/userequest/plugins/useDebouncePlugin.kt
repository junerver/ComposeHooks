package xyz.junerver.compose.hooks.userequest.plugins

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.time.Duration
import xyz.junerver.compose.hooks.Debounce
import xyz.junerver.compose.hooks.TParams
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
private class DebouncePlugin<TData : Any> : Plugin<TData>() {
    override val invoke: GenPluginLifecycleFn<TData>
        get() = { fetch: Fetch<TData>, requestOptions: RequestOptions<TData> ->
            if (requestOptions.debounceOptions.wait > Duration.ZERO) {
                val debounce = Debounce<TParams>(
                    fn = { params -> fetch._run(params) },
                    scope = this,
                    requestOptions.debounceOptions,
                )
                fetch.runAsync = {
                    debounce.invoke(it)
                }
                fetch.run = {
                    debounce.invoke(it)
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
internal fun <T : Any> useDebouncePlugin(options: RequestOptions<T>): Plugin<T> {
    if (options.debounceOptions.wait == Duration.ZERO) {
        return useEmptyPlugin()
    }
    val debouncePlugin = remember {
        DebouncePlugin<T>()
    }
    return debouncePlugin
}
