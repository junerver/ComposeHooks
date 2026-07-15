package xyz.junerver.compose.hooks.userequest

import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import kotlin.properties.Delegates
import kotlin.reflect.KFunction
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import xyz.junerver.compose.hooks.SuspendNormalFunction
import xyz.junerver.compose.hooks.SuspendVoidFunction
import xyz.junerver.compose.hooks.Tuple
import xyz.junerver.compose.hooks.VoidFunction
import xyz.junerver.compose.hooks.userequest.utils.awaitPlus

/*
  Description: 插件化的 Fetch
  Author: Junerver
  Date: 2024/2/6-11:45
  Email: junerver@gmail.com
  Version: v1.0
*/

@Suppress("unused")
@Stable
class Fetch<TParams, TData : Any>(private val options: UseRequestOptions<TParams, TData> = UseRequestOptions()) :
    IFetch<TParams, TData> {
    // 请求的代次；所有读写均通过 StateFlow 的原子操作完成。
    private val generation = MutableStateFlow(0L)
    private val cancellationGeneration = MutableStateFlow(0L)
    private val transitionLock = SynchronizedObject()

    lateinit var fetchState: FetchState<TParams, TData>

    // 插件实现
    lateinit var pluginImpls: Array<PluginLifecycle<TParams, TData>>

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

    // 缓存尚未完成的请求 Job，处理并发登记、取消与完成清理。
    private val requestJobs = MutableStateFlow<Set<Job>>(emptySet())

    internal val activeRequestJobCount: Int
        get() = requestJobs.value.size

    private fun nextGeneration(): Long = generation.updateAndGet { it + 1L }

    private fun isCurrentGeneration(target: Long): Boolean = generation.value == target

    private inline fun <T> withTransitionLock(block: () -> T): T = synchronized(transitionLock, block)

    private fun registerRequestJob(job: Job) {
        requestJobs.update { currentJobs -> currentJobs + job }
        job.invokeOnCompletion {
            requestJobs.update { currentJobs ->
                currentJobs.filterNot { currentJob -> currentJob === job }.toSet()
            }
        }
    }

    private fun detachRequestJobs(): Set<Job> = requestJobs.getAndUpdate { emptySet() }

    internal fun requestDispatchToken(): Long = cancellationGeneration.value

    private fun Throwable.rethrowIfCancellation() {
        if (this is CancellationException) throw this
    }

    /**
     * 真实的retrofit请求函数 - 的 - 包装函数
     */
    lateinit var requestFn: SuspendNormalFunction<TParams, TData>

    /**
     * 由于js非常灵活，可以动态的替换实例的方法，这一点在kotlin时无法完成的，所以我们通过
     * 实例属性来达成这一效果，所有的原始方法通过`_`前缀来标识，对外方法通过属性来达成。
     */
    var runAsync: SuspendVoidFunction<TParams?> = ::_runAsync
    val originRunAsync: KFunction<Any> = ::_runAsync
    var run: VoidFunction<TParams?> = ::_run
    val originRun: KFunction<Any> = ::_run

    private fun commitState(map: Map<String, Any?>) {
        // 传入map，按需替换
        fetchState = fetchState.copy(map) // 更新本地状态
        with(fetchState) {
            loading?.let(setLoading)
            data.let(setData)
            error.let(setError)
        }
    }

    fun setState(map: Map<String, Any?>) {
        withTransitionLock { commitState(map) }
    }

    fun setState(vararg pairs: Pair<String, Any?>) {
        setState(mapOf(pairs = pairs))
    }

    private fun beginTransition(state: Map<String, Any?>): Long = withTransitionLock {
        nextGeneration().also { commitState(state) }
    }

    private fun beginTransitionUnlessCanceled(
        expectedCancellationGeneration: Long,
        state: Map<String, Any?>,
    ): Long? = withTransitionLock {
        if (cancellationGeneration.value != expectedCancellationGeneration) {
            null
        } else {
            nextGeneration().also { commitState(state) }
        }
    }

    private fun commitTerminalState(requestGeneration: Long, state: Map<String, Any?>): Boolean = withTransitionLock {
        if (!isCurrentGeneration(requestGeneration)) {
            false
        } else {
            commitState(state)
            true
        }
    }

    /**
     * 异步请求函数，需要注意调用取消时将无法自动取消请求，
     * 因为请求发生在调用者自己的作用域，而非Fetch实例所在的作用域。
     */
    @Suppress("UNCHECKED_CAST")
    override suspend fun _runAsync(params: TParams?) = coroutineScope {
        // ready=false 必须在参数校验和 generation 推进前短路，不能淘汰仍在执行的请求。
        if (!options.ready) return@coroutineScope

        // 每次调用都持有自己的参数快照，避免并发请求互相覆盖。
        val resolvedParams = when {
            params is Array<*> && params.isEmpty() -> options.defaultParams
            params is Tuple && params.isEmpty() -> options.defaultParams
            params == null -> options.defaultParams
            else -> params
        }
        val lifecycleSnapshot = pluginImpls
        val requestFnSnapshot = requestFn
        if (resolvedParams == null) {
            val error = IllegalStateException("useRequest: params is null and defaultParams is null")
            val requestGeneration = beginTransition(
                mapOf(
                    Keys.loading to false,
                    Keys.params to null,
                    Keys.data to null, // 清除旧数据
                    Keys.error to error,
                ),
            )
            if (isCurrentGeneration(requestGeneration)) {
                try {
                    options.onError.invoke(error, null)
                } catch (callbackError: Throwable) {
                    callbackError.rethrowIfCancellation()
                }
            }
            if (isCurrentGeneration(requestGeneration)) {
                runPluginHandler(Methods.OnError(error, null), lifecycleSnapshot)
            }
            try {
                options.onFinally.invoke(null, null, error)
            } catch (callbackError: Throwable) {
                callbackError.rethrowIfCancellation()
            }
            if (isCurrentGeneration(requestGeneration)) {
                runPluginHandler(Methods.OnFinally(null, null, error), lifecycleSnapshot)
            }
            return@coroutineScope
        }

        // 这里等同于runPluginHandler
        val cancellationSnapshot = cancellationGeneration.value
        val onBeforeReturn = OnBeforeReturn<TParams, TData>(
            stopNow = false,
            returnNow = false,
        ).copy(
            // 如果列表不为空，则说明onBefore有返回，返回的对象必须实现copy函数，来达成用后一个的值覆盖前一个
            (runPluginHandler(Methods.OnBefore(resolvedParams), lifecycleSnapshot) as List<OnBeforeReturn<TParams, TData>>).cover()
                ?.asNotNullMap(),
        )
        val (stopNow, returnNow) = onBeforeReturn
        val state = onBeforeReturn.asFetchStateMap()
        /**
         * [xyz.junerver.compose.hooks.userequest.plugins.AutoRunPlugin] 中的 [xyz.junerver.compose.hooks.userequest.plugins.AutoRunPlugin.ready] 可以阻止请求
         */
        if (stopNow!!) return@coroutineScope
        // 使用缓存插件可以直接返回缓存的内容，而不需要发起真实请求
        val requestGeneration = beginTransitionUnlessCanceled(
            cancellationSnapshot,
            mapOf(
                Keys.loading to true,
                Keys.params to resolvedParams,
            ) + state,
        ) ?: return@coroutineScope
        if (returnNow!!) return@coroutineScope

        var requestResult: TData? = null
        var requestError: Throwable? = null
        try {
            // 调用选项配置的生命周期函数
            options.onBefore.invoke(resolvedParams)
            if (!isCurrentGeneration(requestGeneration)) return@coroutineScope
            var (serviceDeferred) = OnRequestReturn<TData>().copy(
                (
                    runPluginHandler(
                        Methods.OnRequest(requestFnSnapshot, resolvedParams),
                        lifecycleSnapshot,
                    ) as List<OnRequestReturn<TData>>
                ).cover(),
            )
            val result = serviceDeferred?.awaitPlus() ?: supervisorScope {
                async { requestFnSnapshot(resolvedParams) }.await()
            }
            requestResult = result
            if (
                !commitTerminalState(
                    requestGeneration,
                    mapOf(
                        Keys.loading to false,
                        Keys.data to result,
                        Keys.error to null,
                    ),
                )
            ) {
                return@coroutineScope
            }
            try {
                options.onSuccess.invoke(result, resolvedParams)
            } catch (e: Throwable) {
                e.rethrowIfCancellation()
                // 捕获 onSuccess 回调异常，防止影响后续流程
            }
            if (isCurrentGeneration(requestGeneration)) {
                runPluginHandler(Methods.OnSuccess(result, resolvedParams), lifecycleSnapshot)
            }
        } catch (error: Throwable) {
            error.rethrowIfCancellation()
            requestError = error
            if (
                !commitTerminalState(
                    requestGeneration,
                    mapOf(
                        Keys.loading to false,
                        Keys.error to error,
                    ),
                )
            ) {
                return@coroutineScope
            }
            try {
                options.onError.invoke(error, resolvedParams)
            } catch (e: Throwable) {
                e.rethrowIfCancellation()
                // 捕获 onError 回调异常，保留原始错误
            }
            if (isCurrentGeneration(requestGeneration)) {
                runPluginHandler(Methods.OnError(error, resolvedParams), lifecycleSnapshot)
            }
        } finally {
            // onFinally 在 finally 块中执行，确保总是被调用
            try {
                options.onFinally.invoke(resolvedParams, requestResult, requestError)
            } catch (e: Throwable) {
                e.rethrowIfCancellation()
                // 捕获 onFinally 回调异常
            }
            // 已取消或已被更新请求取代的旧生命周期不能再修改当前插件状态；
            // 取消清理由 onCancel 负责，公开的 options.onFinally 仍保持总会调用。
            if (isCurrentGeneration(requestGeneration)) {
                runPluginHandler(Methods.OnFinally(resolvedParams, requestResult, requestError), lifecycleSnapshot)
            }
        }
    }

    /**
     * 使用自身作用域的同步请求函数
     */
    private fun launchRequest(params: TParams?, expectedDispatchToken: Long?): Boolean {
        val requestJob = this.scope.launch(start = CoroutineStart.LAZY) {
            _runAsync(params)
        }
        val registered = if (expectedDispatchToken == null) {
            registerRequestJob(requestJob)
            true
        } else {
            withTransitionLock {
                if (cancellationGeneration.value != expectedDispatchToken) {
                    false
                } else {
                    registerRequestJob(requestJob)
                    true
                }
            }
        }
        if (!registered) {
            requestJob.cancel()
            return false
        }
        requestJob.start()
        return true
    }

    internal fun runIfDispatchTokenCurrent(dispatchToken: Long, params: TParams?): Boolean =
        launchRequest(params, expectedDispatchToken = dispatchToken)

    override fun _run(params: TParams?) {
        launchRequest(params, expectedDispatchToken = null)
    }

    private fun cancelRequestLifecycle(clearError: Boolean) {
        val lifecycleSnapshot = pluginImpls
        val jobsToCancel = withTransitionLock {
            cancellationGeneration.update { it + 1L }
            nextGeneration()
            commitState(
                buildMap {
                    put(Keys.loading, false)
                    if (clearError) put(Keys.error, null)
                },
            )
            detachRequestJobs()
        }
        runPluginHandler(Methods.OnCancel, lifecycleSnapshot)
        jobsToCancel.forEach { it.cancel() }
    }

    internal fun cancelForPluginReplacement() {
        cancelRequestLifecycle(clearError = false)
    }

    /**
     * 取消请求
     */
    override fun cancel() {
        cancelRequestLifecycle(clearError = true)
    }

    /**
     * 刷新请求
     */
    override fun refresh() {
        this.fetchState.params?.let { this._run(it) }
    }

    /**
     * 异步刷新
     */
    override suspend fun refreshAsync() {
        this.fetchState.params?.let { this._runAsync(it) }
    }

    /**
     * 直接修改状态
     */
    override fun mutate(mutateFn: (TData?) -> TData) {
        try {
            val targetData = mutateFn(fetchState.data)
            runPluginHandler(Methods.OnMutate(targetData))
            setState(Keys.data to targetData)
        } catch (e: Throwable) {
            e.rethrowIfCancellation()
            // 捕获 mutate 函数异常，保持状态一致性
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun runPluginHandler(
        method: Methods<*, *>,
        lifecycleSnapshot: Array<PluginLifecycle<TParams, TData>> = pluginImpls,
    ): List<*> = lifecycleSnapshot.mapNotNull {
        try {
            when (method) {
                is Methods.OnBefore -> {
                    @Suppress("UNCHECKED_CAST")
                    it.onBefore?.invoke(method.params as TParams)
                }

                is Methods.OnRequest -> {
                    it.onRequest?.invoke(
                        method.requestFn as SuspendNormalFunction<TParams, TData>,
                        method.params as TParams,
                    )
                }
                /**
                 * 参数1：请求的返回值，参数2：请求使用的参数
                 */
                is Methods.OnSuccess -> {
                    @Suppress("UNCHECKED_CAST")
                    it.onSuccess?.invoke(
                        method.result as TData,
                        method.params as TParams,
                    )
                }
                /**
                 * 参数1：错误，参数2：请求使用的参数
                 */
                is Methods.OnError -> {
                    @Suppress("UNCHECKED_CAST")
                    it.onError?.invoke(
                        method.error,
                        method.params as TParams,
                    )
                }
                /**
                 * 参数1：请求使用的参数，参数2：请求的返回值，参数3：错误
                 */
                is Methods.OnFinally -> {
                    @Suppress("UNCHECKED_CAST")
                    it.onFinally?.invoke(
                        method.params as TParams,
                        method.result as TData?,
                        method.error,
                    )
                }

                Methods.OnCancel -> {
                    it.onCancel?.invoke()
                }
                /**
                 * 参数1：要修改的目标数据
                 */
                is Methods.OnMutate -> {
                    it.onMutate?.invoke(method.result as TData)
                }
            }
        } catch (e: Throwable) {
            e.rethrowIfCancellation()
            // 捕获插件异常，防止中断其他插件执行
            null
        }
    }
}
