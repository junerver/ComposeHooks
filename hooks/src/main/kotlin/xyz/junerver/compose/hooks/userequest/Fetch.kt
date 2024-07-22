package xyz.junerver.compose.hooks.userequest

import androidx.compose.runtime.State
import java.io.Serializable
import kotlin.properties.Delegates
import kotlin.reflect.KFunction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.SuspendNormalFunction
import xyz.junerver.compose.hooks.SuspendVoidFunction
import xyz.junerver.compose.hooks.TParams
import xyz.junerver.compose.hooks.VoidFunction
import xyz.junerver.compose.hooks.defaultOption
import xyz.junerver.compose.hooks.userequest.utils.awaitPlus

/*
  Description: 插件化的 Fetch
  Author: Junerver
  Date: 2024/2/6-11:45
  Email: junerver@gmail.com
  Version: v1.0
*/

@Suppress("unused")
class Fetch<TData : Any>(private val options: RequestOptions<TData> = defaultOption()) :
    IFetch<TData>, Serializable {
    // 请求的计数器
    private var count: Int = 0

    lateinit var fetchState: FetchState<TData>

    /**
     * 最后一次调用时的参数，该数据用于[refresh]函数发起请求
     */
    private var latestParams: TParams = emptyArray()

    // 插件实现
    lateinit var pluginImpls: Array<PluginLifecycle<TData>>

    /**
     * 请求结果的封装，最终类型会通过泛型[TData]对外暴露成正确的类型
     */
    lateinit var dataState: State<TData?>
    lateinit var setData: (TData?) -> Unit

    /**
     * 请求是否正在发起尚未响应结果。
     */
    lateinit var loadingState: State<Boolean>
    lateinit var setLoading: (Boolean) -> Unit

    /**
     * 对[useRequest]中的[requestFn]函数进行try-catch
     */
    lateinit var errorState: State<Throwable?>
    lateinit var setError: (Throwable?) -> Unit

    /**
     * 调用[useRequest]的组件所在协程作用域
     */
    internal var scope: CoroutineScope by Delegates.notNull()

    // 缓存jobs，处理竞态取消
    private var requestJobs: MutableList<Job> = arrayListOf()

    @Synchronized
    fun cancelRequest() {
        requestJobs.forEach {
            it.cancel()
        }
        requestJobs.clear()
    }

    /**
     * 真实的retrofit请求函数 - 的 - 包装函数
     */
    lateinit var requestFn: SuspendNormalFunction<TData>

    /**
     * 由于js非常灵活，可以动态的替换实例的方法，这一点在kotlin时无法完成的，所以我们通过
     * 实例属性来达成这一效果，所有的原始方法通过`_`前缀来标识，对外方法通过属性来达成。
     */
    var runAsync: SuspendVoidFunction = ::_runAsync
    val originRunAsync: KFunction<Any> = ::_runAsync
    var run: VoidFunction = ::_run
    val originRun: KFunction<Any> = ::_run

    fun setState(map: Map<String, Any?>) {
        // 传入map，按需替换
        fetchState = fetchState.copy(map) // 更新本地状态
        with(fetchState) {
            loading?.let(setLoading)
            data.let(setData)
            error.let(setError)
        }
    }

    fun setState(vararg pairs: Pair<String, Any?>) {
        setState(mapOf(pairs = pairs))
    }

    /**
     * 异步请求函数，需要注意调用取消时将无法自动取消请求，
     * 因为请求发生在调用者自己的作用域，而非Fetch实例所在的作用域。
     */
    @Suppress("UNCHECKED_CAST")
    override suspend fun _runAsync(params: TParams) = coroutineScope {
        // 有手动参数但是与默认参数不匹配，报错
        if (params.isNotEmpty() && options.defaultParams.isNotEmpty()) {
            check(params.size == options.defaultParams.size) { "pass parameters length mismatch defaultParams" }
        }
        // 请求参数，如果为空则使用默认参数
        latestParams = if (params.isNotEmpty()) params else options.defaultParams
        count += 1
        val currentCount = count
        // 这里等同于runPluginHandler
        val onBeforeReturn = OnBeforeReturn<TData>(
            stopNow = false,
            returnNow = false
        ).copy(
            // 如果列表不为空，则说明onBefore有返回，返回的对象必须实现copy函数，来达成用后一个的值覆盖前一个
            (runPluginHandler(OnBefore, latestParams) as List<OnBeforeReturn<TData>>).cover()
                ?.asNotNullMap()
        )
        val (stopNow, returnNow) = onBeforeReturn
        val state = onBeforeReturn.asFetchStateMap()
        /**
         * [xyz.junerver.compose.hooks.userequest.plugins.AutoRunPlugin] 中的 [xyz.junerver.compose.hooks.userequest.plugins.AutoRunPlugin.ready] 可以阻止请求
         */
        if (stopNow!!) return@coroutineScope
        // 使用缓存插件可以直接返回缓存的内容，而不需要发起真实请求
        setState(
            mapOf(
                Keys.loading to true,
                Keys.params to latestParams
            ) + state
        )
        if (returnNow!!) return@coroutineScope
        // 调用选项配置的生命周期函数
        options.onBefore.invoke(latestParams)

        try {
            var (serviceDeferred) = OnRequestReturn<TData>().copy(
                (
                    runPluginHandler(
                        OnRequest,
                        requestFn,
                        latestParams
                    ) as List<OnRequestReturn<TData>>
                    ).cover()
            )
            // 此处要明确声明async所在的job，避免异常传递
            serviceDeferred = serviceDeferred ?: async(SupervisorJob()) { requestFn(latestParams) }
            val result = serviceDeferred.awaitPlus()
            if (currentCount != count) return@coroutineScope
            setState(
                Keys.loading to false,
                Keys.data to result,
                Keys.error to null
            )
            options.onSuccess.invoke(result, latestParams)
            runPluginHandler(OnSuccess, result, latestParams)
            // 回调finally
            options.onFinally.invoke(latestParams, result, null)
            if (currentCount == count) {
                runPluginHandler(OnFinally, latestParams, result, null)
            }
        } catch (error: Throwable) {
            if (currentCount != count) return@coroutineScope
            setState(
                Keys.loading to false,
                Keys.error to error
            )
            options.onError.invoke(error, latestParams)
            runPluginHandler(OnError, error, latestParams)
            options.onFinally.invoke(latestParams, null, error)
            if (currentCount == count) {
                runPluginHandler(OnFinally, latestParams, null, error)
            }
        }
    }

    /**
     * 使用自身作用域的同步请求函数
     */
    override fun _run(params: TParams) {
        this.scope.launch {
            _runAsync(params)
        }.also { requestJobs.add(it) }
    }

    /**
     * 取消请求
     */
    override fun cancel() {
        this.count += 1
        this.setState(Keys.loading to false)
        cancelRequest()
        runPluginHandler(OnCancel)
    }

    /**
     * 刷新请求
     */
    override fun refresh() {
        this._run(this.fetchState.params ?: emptyArray())
    }

    /**
     * 异步刷新
     */
    override suspend fun refreshAsync() {
        this._runAsync(this.fetchState.params ?: emptyArray())
    }

    /**
     * 直接修改状态
     */
    override fun mutate(mutateFn: (TData?) -> TData) {
        val targetData = mutateFn(fetchState.data)
        runPluginHandler(OnMutate, targetData)
        setState(Keys.data to targetData)
    }

    @Suppress("UNCHECKED_CAST")
    private fun runPluginHandler(method: PluginLifecycleMethods, vararg rest: Any?): List<*> {
        return pluginImpls.mapNotNull {
            when (method) {
                OnBefore -> {
                    it.onBefore?.invoke(rest[0] as TParams)
                }

                OnRequest -> {
                    it.onRequest?.invoke(
                        rest[0] as SuspendNormalFunction<TData>,
                        rest[1] as TParams
                    )
                }
                /**
                 * 参数1：请求的返回值，参数2：请求使用的参数
                 */
                OnSuccess -> {
                    it.onSuccess?.invoke(
                        rest[0] as TData,
                        rest[1] as TParams
                    )
                }
                /**
                 * 参数1：错误，参数2：请求使用的参数
                 */
                OnError -> {
                    it.onError?.invoke(
                        rest[0] as Throwable,
                        rest[1] as TParams
                    )
                }
                /**
                 * 参数1：请求使用的参数，参数2：请求的返回值，参数3：错误
                 */
                OnFinally -> {
                    it.onFinally?.invoke(
                        rest[0] as TParams,
                        rest[1] as TData?,
                        rest[2] as Throwable?
                    )
                }

                OnCancel -> {
                    it.onCancel?.invoke()
                }
                /**
                 * 参数1：要修改的目标数据
                 */
                OnMutate -> {
                    it.onMutate?.invoke(rest[0] as TData)
                }
            }
        }
    }
}
