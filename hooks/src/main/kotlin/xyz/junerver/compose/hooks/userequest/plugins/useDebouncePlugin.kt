package xyz.junerver.compose.hooks.userequest.plugins

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.time.Duration.Companion.seconds
import xyz.junerver.compose.hooks.Debounce
import xyz.junerver.compose.hooks.userequest.Fetch
import xyz.junerver.compose.hooks.userequest.GenPluginLifecycleFn
import xyz.junerver.compose.hooks.userequest.Plugin
import xyz.junerver.compose.hooks.userequest.PluginLifecycle
import xyz.junerver.compose.hooks.userequest.RequestOptions
import xyz.junerver.compose.hooks.userequest.useEmptyPlugin

/**
 * Description:
 * @author Junerver
 * date: 2024/2/19-13:38
 * Email: junerver@gmail.com
 * Version: v1.0
 */
class DebouncePlugin<TData : Any> : Plugin<TData>() {
    override val invoke: GenPluginLifecycleFn<TData>
        get() = { fetch: Fetch<TData>, requestOptions: RequestOptions<TData> ->
            if (requestOptions.debounceOptions.wait > 0.seconds) {
                val debounce = Debounce(
                    fn = { params -> fetch._run(params) },
                    scope = this,
                    requestOptions.debounceOptions
                )
                fetch.runAsync = {
                    debounce(it)
                }
                fetch.run = {
                    debounce(it)
                }
            }
            object : PluginLifecycle<TData>() {
                override val onCancel: (() -> Unit)
                    get() = {
                        cancel()
                    }
            }
        }
}

@Composable
fun <T : Any> useDebouncePlugin(options: RequestOptions<T>): Plugin<T> {
    if (options.debounceOptions.wait == 0.seconds) {
        return useEmptyPlugin()
    }
    val debouncePlugin = remember {
        DebouncePlugin<T>()
    }
    return debouncePlugin
}
