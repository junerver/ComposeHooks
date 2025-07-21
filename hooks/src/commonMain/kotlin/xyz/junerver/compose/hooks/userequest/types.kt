@file:Suppress("FunctionName")

package xyz.junerver.compose.hooks.userequest

import androidx.compose.runtime.Composable
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import xyz.junerver.compose.hooks.SuspendNormalFunction
import xyz.junerver.compose.hooks.useCreation
import xyz.junerver.compose.hooks.userequest.plugins.useAutoRunPlugin
import xyz.junerver.compose.hooks.userequest.plugins.useDebouncePlugin
import xyz.junerver.compose.hooks.utils.isNotNull

/*
  Description: Types
  Author: Junerver
  Date: 2024/2/6-9:05
  Email: junerver@gmail.com
  Version: v1.0
*/

@Suppress("ConstPropertyName")
internal object Keys {
    const val loading = "loading"
    const val params = "params"
    const val data = "data"
    const val error = "error"
    const val stopNow = "stopNow"
    const val returnNow = "returnNow"

    val FetchStateKeys = arrayOf(loading, params, data, error)
}

/**
 * Description: 可拷贝覆盖对象的预期实现
 *
 * @param Self
 * @constructor Create empty Copyable
 */
internal sealed interface Copyable<Self> {
    fun copy(that: Self?): Self

    operator fun plus(that: Self?) = this.copy(that)
}

/** 可覆盖对象列表的覆盖实现 */
internal fun <T : Copyable<T>> List<T>.cover(): T? = this.takeIf { it.isNotEmpty() }?.reduce { acc, fetchState -> acc + fetchState }

/** map中如果有这个key就取值，无论结果是否为null； 没有这个key则取旧值 */
@Suppress("UNCHECKED_CAST")
internal fun <T> Map<String, Any?>.getOrElse(key: String, default: T?) = if (this.containsKey(key)) {
    this[key] as? T
} else {
    default
}

/**
 * 使用密封类或者密封接口，可以避免外部继承实现，但是不影响使用接口声明。
 *
 * @param loading 当前请求是否正在loading
 * @param params 发起请求使用的参数
 * @param data 请求的响应值
 * @param error 请求出错后的错误信息
 */
sealed class IFetchState<TParams, out TData>(
    open val loading: Boolean? = null,
    open val params: TParams? = null,
    open val data: TData? = null,
    open val error: Throwable? = null,
) {
    /** [copyMap] 当前对象用于覆盖时的等价map */
    var copyMap: Map<String, Any?> = emptyMap()

    abstract fun asNotNullMap(): Map<String, Any?>

    abstract fun copy(needCopyMap: Map<String, Any?>?): IFetchState<TParams, TData>
}

//region [Fetch] 类持有数据的内部状态
data class FetchState<TParams, TData>(
    override var loading: Boolean? = null,
    override var params: TParams? = null,
    override var data: TData? = null,
    override var error: Throwable? = null,
) : IFetchState<TParams, TData>(), Copyable<FetchState<TParams, TData>> {
    override fun copy(needCopyMap: Map<String, Any?>?): FetchState<TParams, TData> {
        needCopyMap ?: return this
        if (needCopyMap.entries.isEmpty()) return this
        return with(needCopyMap) {
            FetchState(
                loading = getOrElse(Keys.loading, loading),
                params = getOrElse(Keys.params, params),
                data = getOrElse(Keys.data, data),
                error = getOrElse(Keys.error, error),
            )
        }
    }

    override fun asNotNullMap(): Map<String, Any?> {
        if (copyMap.entries.isNotEmpty()) return copyMap
        return buildMap {
            if (loading.isNotNull) this[Keys.loading] = loading
            if (params.isNotNull) this[Keys.params] = params
            if (data.isNotNull) this[Keys.data] = data
            if (error.isNotNull) this[Keys.error] = error
        }
    }

    fun asMap() = mapOf(
        Keys.loading to loading,
        Keys.params to params,
        Keys.data to data,
        Keys.error to error,
    )

    override fun copy(that: FetchState<TParams, TData>?): FetchState<TParams, TData> {
        that ?: return this
        return this.copy(that.asNotNullMap())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as FetchState<*, *>

        if (loading != other.loading) return false
        if (params != null) {
            if (other.params == null) return false
            if (!(params as Any).equals(other.params)) return false
        } else if (other.params != null) {
            return false
        }
        if (data != other.data) return false
        if (error != other.error) return false

        return true
    }

    override fun hashCode(): Int {
        var result = loading?.hashCode() ?: 0
        result = 31 * result + (params?.hashCode() ?: 0)
        result = 31 * result + (data?.hashCode() ?: 0)
        result = 31 * result + (error?.hashCode() ?: 0)
        return result
    }
}
//endregion

//region 插件化后所有插件的[onBefore]函数执行的返回值
data class OnBeforeReturn<TParams, TData>(
    val stopNow: Boolean? = null,
    val returnNow: Boolean? = null,
    override val loading: Boolean? = null,
    override val params: TParams? = null,
    override val data: TData? = null,
    override val error: Throwable? = null,
) : IFetchState<TParams, TData>(), Copyable<OnBeforeReturn<TParams, TData>> {
    fun asFetchStateMap(): Map<String, Any?> = this.asNotNullMap().filter {
        it.key in Keys.FetchStateKeys
    }

    override fun copy(needCopyMap: Map<String, Any?>?): OnBeforeReturn<TParams, TData> {
        needCopyMap ?: return this
        if (needCopyMap.entries.isEmpty()) return this
        return with(needCopyMap) {
            OnBeforeReturn(
                stopNow = getOrElse(Keys.stopNow, stopNow),
                returnNow = getOrElse(Keys.returnNow, returnNow),
                loading = getOrElse(Keys.loading, loading),
                params = getOrElse(Keys.params, params),
                data = getOrElse(Keys.data, data),
                error = getOrElse(Keys.error, error),
            )
        }
    }

    override fun asNotNullMap(): Map<String, Any?> {
        if (copyMap.entries.isNotEmpty()) return copyMap
        return buildMap {
            if (stopNow.isNotNull) this[Keys.stopNow] = stopNow
            if (returnNow.isNotNull) this[Keys.returnNow] = returnNow
            if (loading.isNotNull) this[Keys.loading] = loading
            if (params.isNotNull) this[Keys.params] = params
            if (data.isNotNull) this[Keys.data] = data
            if (error.isNotNull) this[Keys.error] = error
        }
    }

    override fun copy(that: OnBeforeReturn<TParams, TData>?): OnBeforeReturn<TParams, TData> {
        that ?: return this
        return this.copy(that.asNotNullMap())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as OnBeforeReturn<*, *>

        if (stopNow != other.stopNow) return false
        if (returnNow != other.returnNow) return false
        if (loading != other.loading) return false
        if (params != null) {
            if (other.params == null) return false
            if (!params.equals(other.params)) return false
        } else if (other.params != null) {
            return false
        }
        if (data != other.data) return false
        if (error != other.error) return false

        return true
    }

    override fun hashCode(): Int {
        var result = stopNow?.hashCode() ?: 0
        result = 31 * result + (returnNow?.hashCode() ?: 0)
        result = 31 * result + (loading?.hashCode() ?: 0)
        result = 31 * result + (params?.hashCode() ?: 0)
        result = 31 * result + (data?.hashCode() ?: 0)
        result = 31 * result + (error?.hashCode() ?: 0)
        return result
    }
}

//endregion

//region 插件化后所有插件的[OnRequest]函数执行的返回值
data class OnRequestReturn<TData>(val requestDeferred: Deferred<TData>? = null) :
    Copyable<OnRequestReturn<TData>> {
    override fun copy(that: OnRequestReturn<TData>?): OnRequestReturn<TData> {
        that ?: return this
        return OnRequestReturn(
            requestDeferred = that.requestDeferred ?: this.requestDeferred,
        )
    }
}
//endregion

/** 因为[Fetch]的相关操作要同样暴露给插件实例，所以创建一个接口， 这样避免插件实例命名出错，对应调用更直白。 */
internal sealed interface IFetch<TParams, TData> {
    suspend fun _runAsync(params: TParams?) {}

    fun _run(params: TParams?) {}

    fun cancel() {}

    fun refresh() {}

    suspend fun refreshAsync() {}

    fun mutate(mutateFn: (TData?) -> TData) {}
}

/** 插件声明周期回调函数的类型定义 */
typealias PluginOnBefore<TParams, TData> = (TParams) -> OnBeforeReturn<TParams, TData>?
typealias PluginOnRequest<TParams, TData> = (requestFn: SuspendNormalFunction<TParams, TData>, params: TParams) -> OnRequestReturn<TData>?
typealias PluginOnSuccess<TParams, TData> = (data: TData, params: TParams) -> Unit
typealias PluginOnError<TParams> = (e: Throwable, params: TParams) -> Unit
typealias PluginOnFinally<TParams, TData> = (params: TParams, data: TData?, e: Throwable?) -> Unit
typealias PluginOnCancel = () -> Unit
typealias PluginOnMutate<TData> = (data: TData) -> Unit

/**
 * 插件的生命周期对象：这个对象是插件[Plugin.invoke]方法执行后的返回值，
 * 用来让插件监听请求的生命周期，当执行异步请求时，会在不同的阶段调用插件的生命周期；
 */
abstract class PluginLifecycle<TParams, TData> {
    // 插件 onBefore 之后会返回fetch的状态，并且扩展了两个新的字段
    open val onBefore: PluginOnBefore<TParams, TData>? = null
    open val onRequest: PluginOnRequest<TParams, TData>? = null
    open val onSuccess: PluginOnSuccess<TParams, TData>? = null
    open val onError: PluginOnError<TParams>? = null
    open val onFinally: PluginOnFinally<TParams, TData>? = null
    open val onCancel: PluginOnCancel? = null
    open val onMutate: PluginOnMutate<TData>? = null
}

/**
 * [Fetch.pluginImpls] 本质是调用[GenPluginLifecycleFn]函数后保存的[PluginLifecycle]列表
 * ，这个函数的入参是[Fetch]实例与[UseRequestOptions]配置。
 */
typealias GenPluginLifecycleFn<TParams, TData> = (Fetch<TParams, TData>, UseRequestOptions<TParams, TData>) -> PluginLifecycle<TParams, TData>

/**
 * 插件函数 `useXXXPlugin` 的返回值是真实的插件[Plugin]对象，
 * 可以通过在[useRequestPluginsImpl]中调用[onInit]函数，用来初始化 [Fetch.fetchState]状态。
 * 插件对象自身实现了协程作用域[CoroutineScope]，持有[Fetch]的实例、请求[UseRequestOptions]配置等内容。
 * 按需实现[IFetch]对应[Fetch]中的各个函数调用，就可以在插件函数`useXXXPlugin`中需要使用副作用函数时，间接回调[Fetch]实例。
 * 具体用例可以参考：[useAutoRunPlugin]
 */
abstract class Plugin<TParams, TData : Any> : IFetch<TParams, TData>, CoroutineScope {
    private var pluginJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + pluginJob

    lateinit var fetchInstance: Fetch<TParams, TData>
    lateinit var options: UseRequestOptions<TParams, TData>

    fun initFetch(fetchInstance: Fetch<TParams, TData>, options: UseRequestOptions<TParams, TData>) {
        if (!this::fetchInstance.isInitialized) {
            this.fetchInstance = fetchInstance
        }
        if (!this::options.isInitialized) {
            this.options = options
        }
    }

    abstract val invoke: GenPluginLifecycleFn<TParams, TData>

    open val onInit: ((UseRequestOptions<TParams, TData>) -> FetchState<TParams, TData>)? = null

    override fun cancel() {
        pluginJob.cancelChildren()
    }
}

/**
 * 一个空插件，部分插件例如[useDebouncePlugin]是根据相应参数决定是否启用的，
 * 为了避免在插件实例中处理最终返回生命周期对象的问题，我们直接在入口判断返回。
 */
internal class EmptyPlugin<TParams, TData : Any> : Plugin<TParams, TData>() {
    override val invoke: GenPluginLifecycleFn<TParams, TData>
        get() = { _, _ ->
            object : PluginLifecycle<TParams, TData>() {}
        }
}

/** 返回一个空插件，避免直接使用[EmptyPlugin]实例 */
@Composable
fun <TParams, TData : Any> useEmptyPlugin(): Plugin<TParams, TData> {
    val emptyPluginRef = useCreation {
        EmptyPlugin<TParams, TData>()
    }
    return emptyPluginRef.current
}

/** 用于判断处理动作 */
internal sealed interface Methods<TParams, out TData> {
    class OnBefore<TParams>(val params: TParams?) : Methods<TParams, Nothing>

    class OnRequest<TParams, TData>(var requestFn: SuspendNormalFunction<TParams, TData>, val params: TParams?) : Methods<TParams, TData>

    class OnSuccess<TParams, TData>(val result: TData, val params: TParams?) : Methods<TParams, TData>

    class OnError<TParams>(var error: Throwable, val params: TParams?) : Methods<TParams, Nothing>

    class OnFinally<TParams, TData>(val params: TParams?, val result: TData?, val error: Throwable?) : Methods<TParams, TData>

    data object OnCancel : Methods<Nothing, Nothing>

    class OnMutate<TData>(val result: TData) : Methods<Nothing, TData>
}
