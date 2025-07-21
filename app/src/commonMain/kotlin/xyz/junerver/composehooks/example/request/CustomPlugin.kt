package xyz.junerver.composehooks.example.request

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import xyz.junerver.compose.hooks.ArrayParams
import xyz.junerver.compose.hooks.MutableRef
import xyz.junerver.compose.hooks.Tuple8
import xyz.junerver.compose.hooks.tuple
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

typealias RollbackFn = () -> Unit

@Composable
fun <TData : Any> useCustomPluginRequest(
    requestFn: suspend (ArrayParams) -> TData,
    optionsOf: RequestOptions<ArrayParams, TData>.() -> Unit = {},
): Tuple8<State<TData?>, State<Boolean>, State<Throwable?>, ReqFn<ArrayParams>, MutateFn<TData>, RefreshFn, CancelFn, RollbackFn> {
    val rollbackRef = useRef(default = { })
    val requestHolder = useRequest(
        requestFn = requestFn,
        optionsOf = optionsOf,
        plugins = arrayOf({
            useRollbackPlugin(ref = rollbackRef)
        }),
    )
    return with(requestHolder) {
        tuple(
            data,
            isLoading,
            error,
            request,
            mutate,
            refresh,
            cancel,
            eighth = { rollbackRef.current.invoke() },
        )
    }
}

@Composable
private fun <TData : Any> useRollbackPlugin(ref: MutableRef<() -> Unit>): Plugin<ArrayParams, TData> = remember {
    object : Plugin<ArrayParams, TData>() {
        var pervState: FetchState<ArrayParams, TData>? = null

        fun rollback() {
            pervState?.let { fetchInstance.setState(it.asMap()) }
        }

        override val invoke: GenPluginLifecycleFn<ArrayParams, TData>
            get() = { fetch: Fetch<ArrayParams, TData>, options: RequestOptions<ArrayParams, TData> ->
                initFetch(fetch, options)
                object : PluginLifecycle<ArrayParams, TData>() {
                    override val onMutate: PluginOnMutate<TData>
                        get() = {
                            pervState = fetch.fetchState
                        }
                }
            }
    }.also { ref.current = it::rollback }
}
