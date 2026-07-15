package xyz.junerver.compose.hooks.userequest.plugins

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import xyz.junerver.compose.hooks.Tuple5
import xyz.junerver.compose.hooks.useref.getValue
import xyz.junerver.compose.hooks.useref.setValue
import xyz.junerver.compose.hooks.useEffect
import xyz.junerver.compose.hooks.useUpdateEffect
import xyz.junerver.compose.hooks.useref.useRefImpl
import xyz.junerver.compose.hooks.userequest.Fetch
import xyz.junerver.compose.hooks.userequest.FetchState
import xyz.junerver.compose.hooks.userequest.GenPluginLifecycleFn
import xyz.junerver.compose.hooks.userequest.Keys
import xyz.junerver.compose.hooks.userequest.OnBeforeReturn
import xyz.junerver.compose.hooks.userequest.Plugin
import xyz.junerver.compose.hooks.userequest.PluginLifecycle
import xyz.junerver.compose.hooks.userequest.PluginOnBefore
import xyz.junerver.compose.hooks.userequest.PluginOnCancel
import xyz.junerver.compose.hooks.userequest.PluginOnError
import xyz.junerver.compose.hooks.userequest.UseRequestOptions
import xyz.junerver.compose.hooks.utils.asBoolean

/*
  Description:
  Author: Junerver
  Date: 2024/2/6-10:35
  Email: junerver@gmail.com
  Version: v1.0
*/

private class AutoRunPlugin<TParams, TData : Any> : Plugin<TParams, TData>() {
    /**
     * [ready]是动态值，可以通过外部副作用修改传递
     */
    var ready = true

    private var pendingAutoRun = false

    override val onInit: (UseRequestOptions<TParams, TData>) -> FetchState<TParams, TData> = {
        // 如果是手动模式 则不loading，自动模式则loading
        val shouldAutoRun = it.manual.not() && ready
        pendingAutoRun = shouldAutoRun
        FetchState(loading = shouldAutoRun)
    }

    override val invoke: GenPluginLifecycleFn<TParams, TData>
        get() = { fetch: Fetch<TParams, TData>, useRequestOptions: UseRequestOptions<TParams, TData> ->
            initFetch(fetch, useRequestOptions)
            object : PluginLifecycle<TParams, TData>() {
                override val onBefore: PluginOnBefore<TParams, TData>
                    get() = {
                        pendingAutoRun = false
                        if (!ready) {
                            fetch.setState(Keys.loading to false)
                            OnBeforeReturn(
                                stopNow = true,
                            )
                        } else {
                            null
                        }
                    }

                override val onError: PluginOnError<TParams>
                    get() = { _, _ -> pendingAutoRun = false }

                override val onCancel: PluginOnCancel
                    get() = { pendingAutoRun = false }
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

    override fun _run(params: TParams?) {
        pendingAutoRun = true
        fetchInstance.run(params)
    }

    fun cancelPendingAutoRun() {
        if (!pendingAutoRun) return
        pendingAutoRun = false
        fetchInstance.cancel()
    }
    //endregion
}

/**
 * 钩子应该返回两个值，一个是plugin自身，方便调用init函数，另一个是pluginreturn，用来调用周期得methods
 */
@Composable
internal fun <TParams, TData : Any> useAutoRunPlugin(options: UseRequestOptions<TParams, TData>): Plugin<TParams, TData> {
    val (manual, ready, defaultParams, refreshDeps, refreshDepsAction) = with(options) {
        Tuple5(manual, ready, defaultParams, refreshDeps, refreshDepsAction)
    }
    var hasAutoRun by useRefImpl(default = false)
    hasAutoRun = false
    val autoRunPlugin = remember {
        AutoRunPlugin<TParams, TData>()
    }.apply {
        this.ready = ready
    }
    /**
     * 这里会因为旋转屏幕重组时再次调用[Fetch.run]函数
     */
    useEffect(ready) {
        if (!manual && ready) {
            hasAutoRun = true
            autoRunPlugin._run(defaultParams)
        } else if (!ready) {
            autoRunPlugin.cancelPendingAutoRun()
        }
    }
    useUpdateEffect(*refreshDeps) block@{
        if (hasAutoRun) return@block
        if (!manual) {
            hasAutoRun = true
            if (refreshDepsAction.asBoolean()) {
                refreshDepsAction.invoke()
            } else {
                // 自动状态 && !ready 时，会调用此处，但是实际最终驱动run的时候
                // 由于 !ready 会阻止请求发出。
                autoRunPlugin.refresh()
            }
        }
    }

    return autoRunPlugin
}
