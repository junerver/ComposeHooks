package xyz.junerver.compose.hooks.userequest.plugins

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.time.Duration
import xyz.junerver.compose.hooks.usedebounce.Debounce
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
private class DebouncePlugin<TParams, TData : Any> : Plugin<TParams, TData>() {
    override val invoke: GenPluginLifecycleFn<TParams, TData>
        get() = { fetch: Fetch<TParams, TData>, useRequestOptions: UseRequestOptions<TParams, TData> ->
            var debounce: Debounce<TParams?>? = null
            if (useRequestOptions.debounceOptions.wait > Duration.ZERO) {
                val debounced = Debounce<TParams?>(
                    fn = { params -> fetch._run(params) },
                    scope = this,
                    useRequestOptions.debounceOptions,
                )
                debounce = debounced
                fetch.runAsync = {
                    debounced.invoke(it)
                }
                fetch.run = {
                    debounced.invoke(it)
                }
            }
            object : PluginLifecycle<TParams, TData>() {
                override val onCancel: PluginOnCancel
                    get() = {
                        debounce?.cancel()
                        cancel()
                    }
            }
        }
}

@Composable
internal fun <TParams, TData : Any> useDebouncePlugin(options: UseRequestOptions<TParams, TData>): Plugin<TParams, TData> {
    val (wait, leading, trailing, maxWait) = options.debounceOptions
    if (wait == Duration.ZERO) {
        return useEmptyPlugin()
    }
    val debouncePlugin = remember(wait, leading, trailing, maxWait) {
        DebouncePlugin<TParams, TData>()
    }
    return debouncePlugin
}
