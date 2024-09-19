package xyz.junerver.compose.hooks.userequest

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlin.reflect.KFunction0
import kotlin.reflect.KFunction1
import xyz.junerver.compose.hooks.SuspendNormalFunction
import xyz.junerver.compose.hooks.VoidFunction
import xyz.junerver.compose.hooks._useGetState
import xyz.junerver.compose.hooks.useCreation
import xyz.junerver.compose.hooks.useRef
import xyz.junerver.compose.hooks.useUnmount
import xyz.junerver.compose.hooks.userequest.plugins.*
import xyz.junerver.kotlin.Tuple7

typealias ReqFn = VoidFunction
typealias MutateFn<TData> = KFunction1<(TData?) -> TData, Unit>
typealias RefreshFn = KFunction0<Unit>
typealias CancelFn = KFunction0<Unit>
internal typealias ComposablePluginGenFn<TData> = @Composable (RequestOptions<TData>) -> Plugin<TData>
/*
  Description:
  Author: Junerver
  Date: 2024/1/25-8:11
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Description: 一个用来管理网络状态的Hook，它可以非常方便的接入到传统的 retrofit 网络请求模式中。
 * 你几乎不需要做任何额外工作，就可以简单高效的在 Compose 中使用网络请求，并将请求数据作为状态，直接驱动UI。
 *
 * [SuspendNormalFunction]
 * 是所有函数的抽象，我们最终通过函数拿到的手动执行函数也是[SuspendNormalFunction]类型的，
 * 调用时要传递的是[arrayOf]的参数。
 *
 * 我还额外提供了两个方便的转换函数
 * [asNoopFn]、[asSuspendNoopFn]，这两个函数可以把任意的Kotlin函数转换成[useRequest]需要的函数。
 * 需要注意区分，如果是挂起函数就需要调用[asSuspendNoopFn]，否则就使用
 * [asNoopFn]，通过这个函数我们可以简化普通函数到[SuspendNormalFunction]的包装过程。
 *
 * 示例代码：
 *
 * ```kotlin
 * @Parcelize
 * data class Resp<T : Parcelable?>(
 *     val message: String,
 *     val status: Int,
 *     val data: T?
 * ) : Parcelable
 *
 * @Parcelize
 * data class LoginSucc(
 *     val expire: String,
 *     val token: String
 * ) : Parcelable
 *
 * interface WebService {
 *     //登录
 *     @Headers("Content-Type:application/json;charset=UTF-8")
 *     @POST("api/cas/login/restful")
 *     suspend fun login(@Body body: RequestBody): Resp<LoginSucc>
 * }
 *
 * // 自定义一个传递Retrofit接口实例的扩展函数，省去调用 `asSuspendNoopFn` 每次都要传递实例的步骤
 * fun <T : Any> KFunction<T?>.asRequestFn()
 *      = this.asSuspendNoopFn(NetFetchManager.INSTANCE)
 *
 * // 现在你可以放心的使用状态了，通过解构赋值拿到的 data 可以直接应用在 Compose UI 中
 * val (data, loading, err, run) = useRequest(
 *      WebService::login.asRequestFn(),
 *      optionsOf {defaultParams = arrayOf(bodyreq)}
 *   )
 * ```
 *
 * 是的，它可以简单到只有一行代码，通过[RequestOptions]选项配置，你可以设置：手动请求、Ready、错误重试、
 * 生命周期回调、轮询、防抖、节流、依赖刷新等待功能。
 *
 * Tips: 强烈建议开启Android Studio中类型镶嵌提示，位于：Editor - Inlay Hints - Types -
 * Kotlin，它可以 更高效的提示我们解构赋值后拿到的相关状态、函数的类型。
 *
 * @param requestFn 经过抽象后的请求函数：suspend (TParams) ->
 *    TData，如果你不喜欢使用[asSuspendNoopFn]，也可以使用匿名 [suspend]闭包。
 * @param options
 *    请求的配置项，参考[RequestOptions]，以及[ahooks-useRequest](https://ahooks.gitee.io/zh-CN/hooks/use-request/index).
 * @param plugins 自定义的插件，这是一个数组，请通过arrayOf传入
 */
@Deprecated(
    "Please use the performance-optimized version. Do not pass the Options instance directly. You can simply switch by adding `=` after the `optionsOf` function. If you need to use an older version, you need to explicitly declare the parameters as `options`"
)
@Composable
fun <TData : Any> useRequest(
    requestFn: SuspendNormalFunction<TData>,
    options: RequestOptions<TData> = remember { RequestOptions() },
    plugins: Array<ComposablePluginGenFn<TData>> = emptyArray(),
): Tuple7<TData?, Boolean, Throwable?, ReqFn, MutateFn<TData>, RefreshFn, CancelFn> {
    val customPluginsRef = useRef<Array<Plugin<TData>>>(emptyArray())
    if (customPluginsRef.current.size != plugins.size) {
        customPluginsRef.current = plugins.map {
            it(options)
        }.toTypedArray()
    }
    val buildInDebouncePlugin = useDebouncePlugin(options)
    val buildInLoadingDelayPlugin = useLoadingDelayPlugin(options)
    val buildInPollingPlugin = usePollingPlugin(options)
    val buildInThrottlePlugin = useThrottlePlugin(options)
    val buildInAutoRunPlugin = useAutoRunPlugin(options)
    val buildInCachePlugin = useCachePlugin(options)
    val buildInRetryPlugin = useRetryPlugin(options)
    val allPlugins = useCreation(*plugins) {
        customPluginsRef.current + arrayOf(
            buildInDebouncePlugin,
            buildInLoadingDelayPlugin,
            buildInPollingPlugin,
            buildInThrottlePlugin,
            buildInAutoRunPlugin,
            buildInCachePlugin,
            buildInRetryPlugin
        )
    }
    val fetch = useRequestPluginsImpl(
        requestFn,
        options,
        allPlugins.current
    )

    return with(fetch) {
        Tuple7(
            first = dataState.value,
            second = loadingState.value,
            third = errorState.value,
            fourth = run,
            fifth = ::mutate,
            sixth = ::refresh,
            seventh = ::cancel
        )
    }
}

/**
 * 性能优化版本，[optionsOf] 是一个普通的内联函数，他会在每次组件重组时调用，这回带来一些性能上的损耗，我们可以简单呢的通过 [remember]，进行优化。
 * 在未来版本将会把原始的直接传递对象这类api转变为私有，只允许通过闭包方式使用。
 */
@Composable
fun <TData : Any> useRequest(
    requestFn: SuspendNormalFunction<TData>,
    optionsOf: RequestOptions<TData>.() -> Unit = {},
    plugins: Array<ComposablePluginGenFn<TData>> = emptyArray(),
): Tuple7<TData?, Boolean, Throwable?, ReqFn, MutateFn<TData>, RefreshFn, CancelFn> = useRequest(
    requestFn,
    remember { RequestOptions.optionOf(optionsOf) }.apply(optionsOf),
    plugins
)

@Composable
private fun <TData : Any> useRequestPluginsImpl(
    requestFn: SuspendNormalFunction<TData>,
    options: RequestOptions<TData> = RequestOptions(),
    plugins: Array<Plugin<TData>> = emptyArray(),
): Fetch<TData> {
    val (dataState, setData) = _useGetState<TData?>(null)
    val (loadingState, setLoading) = _useGetState(false)
    val (errorState, setError) = _useGetState<Throwable?>(null)

    val fetch = remember {
        Fetch(options).apply {
            this.dataState = dataState
            this.setData = setData
            this.loadingState = loadingState
            this.setLoading = setLoading
            this.errorState = errorState
            this.setError = setError
            this.requestFn = requestFn

            this.fetchState = plugins.mapNotNull {
                it.onInit?.invoke(options)
            }.cover() ?: FetchState()
            this.pluginImpls = plugins.map { it.invoke(this, options) }.toTypedArray()
        }
    }.apply {
        this.scope = rememberCoroutineScope()
    }

    useUnmount {
        fetch.cancel()
    }

    return fetch
}
