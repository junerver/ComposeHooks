package xyz.junerver.compose.hooks.userequest.plugins

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import xyz.junerver.compose.hooks.TParams
import xyz.junerver.compose.hooks.useRef
import xyz.junerver.compose.hooks.userequest.Fetch
import xyz.junerver.compose.hooks.userequest.FetchState
import xyz.junerver.compose.hooks.userequest.GenPluginLifecycleFn
import xyz.junerver.compose.hooks.userequest.OnBeforeReturn
import xyz.junerver.compose.hooks.userequest.Plugin
import xyz.junerver.compose.hooks.userequest.PluginLifecycle
import xyz.junerver.compose.hooks.userequest.RequestOptions
import xyz.junerver.kotlin.asBoolean
import xyz.junerver.kotlin.plus
import xyz.junerver.kotlin.runIf

/**
 * Description:
 * @author Junerver
 * date: 2024/2/6-10:35
 * Email: junerver@gmail.com
 * Version: v1.0
 */

class AutoRunPlugin<TData : Any> : Plugin<TData>() {
    /**
     * [ready]是动态值，可以通过外部副作用修改传递
     */
    var ready = true


    override val onInit: (RequestOptions<TData>) -> FetchState<TData> = {
        // 如果是手动模式 则不loading，自动模式则loading
        FetchState(loading = it.manual.not() && ready)
    }

    override val invoke: GenPluginLifecycleFn<TData>
        get() = { fetch: Fetch<TData>, requestOptions: RequestOptions<TData> ->
            initFetch(fetch, requestOptions)
            object : PluginLifecycle<TData>() {
                override val onBefore: (TParams) -> OnBeforeReturn<TData>?
                    get() = {
                        runIf(!ready) {
                            OnBeforeReturn(
                                stopNow = true
                            )
                        }
                    }
            }
        }

    //region 间接调用 fetch 实例

    /**
     * 因为只有[use]函数能感知组件的状态，
     * 但是在use函数中拿不到fetch实例，
     * 所以只能通过插件对象实现同名方法，
     * 间接实现对fetch实例的调用
     */
    override fun refresh() {
        fetchInstance.refresh()
    }

    override fun _run(params: TParams) {
        fetchInstance.run(params)
    }
    //endregion
}


/**
 * 钩子应该返回两个值，一个是plugin自身，方便调用init函数，另一个是pluginreturn，用来调用周期得methods
 */
@Composable
fun <T : Any> useAutoRunPlugin(options: RequestOptions<T>): Plugin<T> {
    val (manual, ready, defaultParams, refreshDeps, refreshDepsAction) = with(options) {
        (manual to ready) + defaultParams + refreshDeps + refreshDepsAction
    }
    val hasAutoRun = useRef(default = false)
    hasAutoRun.current = false
    val autoRunPlugin = rememberSaveable {
        AutoRunPlugin<T>()
    }.apply {
        this.ready = ready
    }
    /**
     * 这里会因为旋转屏幕重组时再次调用[Fetch.run]函数
     */
    LaunchedEffect(key1 = ready, block = {
        if (!manual && ready) {
            hasAutoRun.current = true
            autoRunPlugin._run(defaultParams)
        }
    })
    LaunchedEffect(*refreshDeps) block@{
        if (hasAutoRun.current) return@block
        if (!manual) {
            hasAutoRun.current = true
            if (refreshDepsAction.asBoolean()) {
                refreshDepsAction.invoke()
            } else {
                //自动状态 && !ready 时，会调用此处，但是实际最终驱动run的时候
                //由于 !ready 会阻止请求发出。
                autoRunPlugin.refresh()
            }
        }
    }

    return autoRunPlugin
}