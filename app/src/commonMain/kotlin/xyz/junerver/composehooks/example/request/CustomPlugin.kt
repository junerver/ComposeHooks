package xyz.junerver.composehooks.example.request

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import xyz.junerver.compose.hooks.MutableRef
import xyz.junerver.compose.hooks.useRef
import xyz.junerver.compose.hooks.userequest.CancelFn
import xyz.junerver.compose.hooks.userequest.Fetch
import xyz.junerver.compose.hooks.userequest.FetchState
import xyz.junerver.compose.hooks.userequest.GenPluginLifecycleFn
import xyz.junerver.compose.hooks.userequest.MutateFn
import xyz.junerver.compose.hooks.userequest.Plugin
import xyz.junerver.compose.hooks.userequest.PluginLifecycle
import xyz.junerver.compose.hooks.userequest.PluginOnMutate
import xyz.junerver.compose.hooks.userequest.RefreshFn
import xyz.junerver.compose.hooks.userequest.ReqFn
import xyz.junerver.compose.hooks.userequest.RequestOptions
import xyz.junerver.compose.hooks.userequest.useRequest
import xyz.junerver.kotlin.Tuple8
import xyz.junerver.kotlin.plus

/**
 * Description:
 * 这个例子演示了如何自定义插件来对[useRequest]进行扩展，通过在[PluginLifecycle.onMutate]时记录之前的[FetchState]，通过[useRef]
 * 保存 `rollback` 函数，并最终暴露给调用者，那么只要执行过 [Fetch.mutate]，就可以随时通过 `rollback`
 * 恢复状态。
 *
 * This example demonstrates how to customize the plug-in to
 * extend [useRequest], record the previous [FetchState] during
 * [PluginLifecycle.onMutate], save the `rollback` function through
 * [useRef], and finally expose it to the caller, then after executing
 * [Fetch.mutate], you can restore the state at any time through
 * `rollback`.
 */

// You can copy the aliases in the source code to be consistent with me, or you can use the original type directly
typealias TParams = Array<Any?>
typealias RollbackFn = () -> Unit

@Composable
fun <TData : Any> useCustomPluginRequest(
    requestFn: suspend (TParams) -> TData,
    options: RequestOptions<TData>,
): Tuple8<TData?, Boolean, Throwable?, ReqFn, MutateFn<TData>, RefreshFn, CancelFn, RollbackFn> {
    val rollbackRef = useRef(default = { })
    val tuple = useRequest(
        requestFn = requestFn,
        options = options,
        arrayOf({
            useRollbackPlugin(ref = rollbackRef)
        })
    )
    return tuple + {
        rollbackRef.current.invoke()
    }
}

@Composable
private fun <TData : Any> useRollbackPlugin(ref: MutableRef<() -> Unit>) = remember {
    object : Plugin<TData>() {
        var pervState: FetchState<TData>? = null

        fun rollback() {
            pervState?.let { fetchInstance.setState(it.asMap()) }
        }

        override val invoke: GenPluginLifecycleFn<TData>
            get() = { fetch: Fetch<TData>, options: RequestOptions<TData> ->
                initFetch(fetch, options)
                object : PluginLifecycle<TData>() {
                    override val onMutate: PluginOnMutate<TData>
                        get() = {
                            pervState = fetch.fetchState
                        }
                }
            }
    }.also { ref.current = it::rollback }
}
