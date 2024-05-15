@file:Suppress("FunctionName")

package xyz.junerver.compose.hooks.userequest

import androidx.compose.runtime.Composable
import java.io.Serializable
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import xyz.junerver.compose.hooks.SuspendNormalFunction
import xyz.junerver.compose.hooks.TParams
import xyz.junerver.compose.hooks.useCreation
import xyz.junerver.compose.hooks.userequest.plugins.useAutoRunPlugin
import xyz.junerver.compose.hooks.userequest.plugins.useDebouncePlugin
import xyz.junerver.kotlin.isNotNull

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
 * @author Junerver
 * date: 2024/2/6-9:05
 * Email: junerver@gmail.com
 * Version: v1.0
 */
internal sealed interface Copyable<Self> {
    fun copy(that: Self?): Self
    operator fun plus(that: Self?) = this.copy(that)
}

/**
 * 可覆盖对象列表的覆盖实现
 */
internal fun <T : Copyable<T>> List<T>.cover(): T? {
    return this.takeIf { it.isNotEmpty() }?.reduce { acc, fetchState -> acc + fetchState }
}

/***
 * map中如果有这个key就取值，无论结果是否为null；
 * 没有这个key则取旧值
 */
@Suppress("UNCHECKED_CAST")
internal fun <T> Map<String, Any?>.getOrElse(key: String, default: T?) = if (this.containsKey(key)) {
    this[key] as? T
} else {
    default
}

/**
 * 使用密封类或者密封接口，可以避免外部继承实现，但是不影响使用接口声明。
 * @param loading 当前请求是否正在loading
 * @param params 发起请求使用的参数
 * @param data 请求的响应值
 * @param error 请求出错后的错误信息
 */
sealed class IFetchStata<out TData>(
    open val loading: Boolean? = null,
    open val params: TParams? = null,
    open val data: TData? = null,
    open val error: Throwable? = null,
) {
    /**
     * [copyMap] 当前对象用于覆盖时的等价map
     */
    var copyMap: Map<String, Any?> = emptyMap()
    abstract fun asNotNullMap(): Map<String, Any?>
    abstract fun copy(needCopyMap: Map<String, Any?>?): IFetchStata<TData>
}

//region [Fetch] 类持有数据的内部状态
/**
 * [Fetch] 类持有数据的内部状态
 */
data class FetchState<TData>(
    override var loading: Boolean? = null,
    override var params: TParams? = null,
    override var data: TData? = null,
    override var error: Throwable? = null,
) : IFetchStata<TData>(), Copyable<FetchState<TData>> {

    override fun copy(needCopyMap: Map<String, Any?>?): FetchState<TData> {
        needCopyMap ?: return this
        if (needCopyMap.entries.isEmpty()) return this
        return with(needCopyMap) {
            FetchState(
                loading = getOrElse(Keys.loading, loading),
                params = getOrElse(Keys.params, params),
                data = getOrElse(Keys.data, data),
                error = getOrElse(Keys.error, error)
            )
        }
    }

    /**
     * 只产生不是空的map
     */
    override fun asNotNullMap(): Map<String, Any?> {
        // 如果手动设置了map则采用手动设置的，否则用默认的
        if (copyMap.entries.isNotEmpty()) return copyMap
        return buildMap {
            if (loading.isNotNull) this[Keys.loading] = loading
            if (params.isNotNull) this[Keys.params] = params
            if (data.isNotNull) this[Keys.data] = data
            if (error.isNotNull) this[Keys.error] = error
        }
    }

    /**
     * 完整的map
     */
    fun asMap() = mapOf(
        Keys.loading to loading,
        Keys.params to params,
        Keys.data to data,
        Keys.error to error
    )

    override fun copy(that: FetchState<TData>?): FetchState<TData> {
        that ?: return this
        return this.copy(that.asNotNullMap())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FetchState<*>

        if (loading != other.loading) return false
        if (params != null) {
            if (other.params == null) return false
            if (!params.contentEquals(other.params)) return false
        } else if (other.params != null) return false
        if (data != other.data) return false
        return error == other.error
    }

    override fun hashCode(): Int {
        var result = loading?.hashCode() ?: 0
        result = 31 * result + (params?.contentHashCode() ?: 0)
        result = 31 * result + (data?.hashCode() ?: 0)
        result = 31 * result + error.hashCode()
        return result
    }
}
//endregion

//region 插件化后所有插件的[onBefore]函数执行的返回值
/**
 * 插件生命周期[PluginLifecycle.onBefore]的返回值类型，
 * [Fetch._runAsync]会在请求发生前回调所有插件的[PluginLifecycle.onBefore]函数，
 * 他们可以彼此覆盖。
 * [stopNow]可以阻止请求发出，不改变状态；
 * [returnNow]可以将返回的状态作为[Fetch.fetchState]的状态改变ui；
 */
data class OnBeforeReturn<TData>(
    val stopNow: Boolean? = null,
    val returnNow: Boolean? = null,
    override val loading: Boolean? = null,
    override val params: TParams? = null,
    override val data: TData? = null,
    override val error: Throwable? = null,
) : IFetchStata<TData>(), Copyable<OnBeforeReturn<TData>> {

    fun asFetchStateMap(): Map<String, Any?> = this.asNotNullMap().filter {
        it.key in Keys.FetchStateKeys
    }

    override fun copy(needCopyMap: Map<String, Any?>?): OnBeforeReturn<TData> {
        needCopyMap ?: return this
        if (needCopyMap.entries.isEmpty()) return this
        return with(needCopyMap) {
            OnBeforeReturn(
                stopNow = getOrElse(Keys.stopNow, stopNow),
                returnNow = getOrElse(Keys.returnNow, returnNow),
                loading = getOrElse(Keys.loading, loading),
                params = getOrElse(Keys.params, params),
                data = getOrElse(Keys.data, data),
                error = getOrElse(Keys.error, error)
            )
        }
    }

    override fun asNotNullMap(): Map<String, Any?> {
        // 如果手动设置了map则采用手动设置的，否则用默认的
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

    override fun copy(that: OnBeforeReturn<TData>?): OnBeforeReturn<TData> {
        that ?: return this
        return this.copy(that.asNotNullMap())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OnBeforeReturn<*>

        if (stopNow != other.stopNow) return false
        if (returnNow != other.returnNow) return false
        if (loading != other.loading) return false
        if (params != null) {
            if (other.params == null) return false
            if (!params.contentEquals(other.params)) return false
        } else if (other.params != null) return false
        if (data != other.data) return false
        return error == other.error
    }

    override fun hashCode(): Int {
        var result = stopNow?.hashCode() ?: 0
        result = 31 * result + (returnNow?.hashCode() ?: 0)
        result = 31 * result + (loading?.hashCode() ?: 0)
        result = 31 * result + (params?.contentHashCode() ?: 0)
        result = 31 * result + (data?.hashCode() ?: 0)
        result = 31 * result + (error?.hashCode() ?: 0)
        return result
    }
}

//endregion

//region 插件化后所有插件的[OnRequest]函数执行的返回值
/**
 * 插件生命周期[PluginLifecycle.onRequest]的返回值类型，
 * [Fetch._runAsync]会在调用[Fetch.requestFn]请求发生前回调所有插件的[PluginLifecycle.onRequest]函数。
 * 插件可以通过[Fetch.requestFn]拿到原始的请求函数，通过返回值[requestDeferred]来`async`闭包的`await`，
 * 来改变实际请求。
 */
data class OnRequestReturn<TData>(val requestDeferred: Deferred<TData>? = null) :
    Copyable<OnRequestReturn<TData>> {
    override fun copy(that: OnRequestReturn<TData>?): OnRequestReturn<TData> {
        that ?: return this
        return OnRequestReturn(
            requestDeferred = that.requestDeferred ?: this.requestDeferred
        )
    }
}
//endregion

/**
 * 因为[Fetch]的相关操作要同样暴露给插件实例，所以创建一个接口，
 * 这样避免插件实例命名出错，对应调用更直白。
 */
internal sealed interface IFetch<TData> {

    /**
     * 异步请求函数，调用者需要自己提供作用域，需要注意，取消也需要自己处理
     */
    suspend fun _runAsync(params: TParams) {}

    /**
     * 发起请求
     */
    fun _run(params: TParams) {}

    /**
     * 取消请求
     */
    fun cancel() {}

    /**
     * 刷新
     */
    fun refresh() {}

    suspend fun refreshAsync() {}

    /**
     * [mutate]直接修改
     */
    fun mutate(mutateFn: (TData?) -> TData) {}
}

/**
 * 插件的生命周期对象：这个对象是插件[Plugin.invoke]方法执行后的返回值，
 * 用来让插件监听请求的生命周期，当执行异步请求时，会在不同的阶段调用插件的生命周期；
 */
abstract class PluginLifecycle<TData> {
    // 插件 onBefore 之后会返回fetch的状态，并且扩展了两个新的字段
    open val onBefore: ((TParams) -> OnBeforeReturn<TData>?)? = null

    /**
     * 传递原本用来请求的函数、参数，返回新的函数
     * 例如原来我们要请求的是 ::run，传递给他参数，现在我们将这两个参数传递给
     * [OnRequest]，如果他返回结果（一个新的函数），我们则调用这个函数，传递给他参数
     * 就像 debounce，原本我们传递的是 ::run ，现在我们传递参数的是 debounce
     */
    open val onRequest: ((requestFn: SuspendNormalFunction<TData>, params: TParams) -> OnRequestReturn<TData>?)? =
        null
    open val onSuccess: ((data: TData, params: TParams) -> Unit)? = null
    open val onError: ((e: Throwable, params: TParams) -> Unit)? = null
    open val onFinally: ((params: TParams, data: TData?, e: Throwable?) -> Unit)? = null
    open val onCancel: (() -> Unit)? = null
    open val onMutate: ((data: TData) -> Unit)? = null
}

/**
 * [Fetch.pluginImpls] 本质是调用[GenPluginLifecycleFn]函数后保存的[PluginLifecycle]列表
 * ，这个函数的入参是[Fetch]实例与[RequestOptions]配置。
 */
typealias GenPluginLifecycleFn<TData> = (Fetch<TData>, RequestOptions<TData>) -> PluginLifecycle<TData>

/**
 * 插件函数 `useXXXPlugin` 的返回值是真实的插件[Plugin]对象，
 * 可以通过在[useRequestPluginsImpl]中调用[onInit]函数，用来初始化 [Fetch.fetchState]状态。
 * 插件对象自身实现了协程作用域[CoroutineScope]，持有[Fetch]的实例、请求[RequestOptions]配置等内容。
 * 按需实现[IFetch]对应[Fetch]中的各个函数调用，就可以在插件函数`useXXXPlugin`中需要使用副作用函数时，间接回调[Fetch]实例。
 * 具体用例可以参考：[useAutoRunPlugin]
 */
abstract class Plugin<TData : Any> : IFetch<TData>, Serializable, CoroutineScope {

    private var pluginJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + pluginJob

    /**
     * 因为我们无法像js那样直接定义一个函数类型的同时声明一个函数属性，而且因为序列化的考虑，
     * 我们必须用一个实例对象来存储插件函数、初始化函数。故而[Fetch]的实例我们必须保存在插件对象中。
     * 对外暴露的副作用也不能再直接通过[Fetch]实例调用，必须要再通过插件报装一手。
     */
    lateinit var fetchInstance: Fetch<TData>
    lateinit var options: RequestOptions<TData>

    /**
     * 插件在被[invoke]执行调用时初始化[Fetch]与[RequestOptions]
     */
    fun initFetch(fetchInstance: Fetch<TData>, options: RequestOptions<TData>) {
        if (!this::fetchInstance.isInitialized) {
            this.fetchInstance = fetchInstance
        }
        if (!this::options.isInitialized) {
            this.options = options
        }
    }

    /**
     * 必须实现的[invoke]属性，该属性执行后返回[PluginLifecycle]，
     * 它被调用的时机是实例化[Fetch]时，调用后存入[Fetch.pluginImpls]。
     * 调用时应该执行[initFetch]拿到相应实例，返回值是[PluginLifecycle]
     */
    abstract val invoke: GenPluginLifecycleFn<TData>

    // 用来初始化[FetchState]，目前看主要是自动运行时设置loading状态为true
    open val onInit: ((RequestOptions<TData>) -> FetchState<TData>)? = null

    /**
     * 插件默认有一个作用域，可通过
     */
    override fun cancel() {
        pluginJob.cancelChildren()
    }
}

/**
 * 一个空插件，部分插件例如[useDebouncePlugin]是根据相应参数决定是否启用的，
 * 为了避免在插件实例中处理最终返回生命周期对象的问题，我们直接在入口判断返回。
 */
internal class EmptyPlugin<TData : Any> : Plugin<TData>() {
    override val invoke: GenPluginLifecycleFn<TData>
        get() = { _, _ ->
            object : PluginLifecycle<TData>() {}
        }
}

/**
 * 返回一个空插件，避免直接使用[EmptyPlugin]实例
 */
@Composable
fun <T : Any> useEmptyPlugin(): Plugin<T> {
    val emptyPluginRef = useCreation {
        EmptyPlugin<T>()
    }
    return emptyPluginRef.current
}

/**
 * 用于判断处理动作
 */
internal sealed interface PluginLifecycleMethods
internal data object OnBefore : PluginLifecycleMethods
internal data object OnRequest : PluginLifecycleMethods
internal data object OnSuccess : PluginLifecycleMethods
internal data object OnError : PluginLifecycleMethods
internal data object OnFinally : PluginLifecycleMethods
internal data object OnCancel : PluginLifecycleMethods
internal data object OnMutate : PluginLifecycleMethods
