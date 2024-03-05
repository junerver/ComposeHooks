package xyz.junerver.compose.hooks.userequest

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import xyz.junerver.compose.hooks.NoParamsVoidFunction
import xyz.junerver.compose.hooks.SuspendNormalFunction
import xyz.junerver.compose.hooks.VoidFunction
import xyz.junerver.compose.hooks.asNoopFn
import xyz.junerver.compose.hooks.asSuspendNoopFn
import xyz.junerver.compose.hooks.defaultOption
import xyz.junerver.compose.hooks.useUnmount
import xyz.junerver.compose.hooks.userequest.plugins.useAutoRunPlugin
import xyz.junerver.compose.hooks.userequest.plugins.useCachePlugin
import xyz.junerver.compose.hooks.userequest.plugins.useDebouncePlugin
import xyz.junerver.compose.hooks.userequest.plugins.useLoadingDelayPlugin
import xyz.junerver.compose.hooks.userequest.plugins.usePollingPlugin
import xyz.junerver.compose.hooks.userequest.plugins.useRetryPlugin
import xyz.junerver.compose.hooks.userequest.plugins.useThrottlePlugin
import xyz.junerver.kotlin.Tuple7
import xyz.junerver.kotlin.tuple
import kotlin.reflect.KFunction1

/**
 * Description: 一个用来管理网络状态的Hook，它可以非常方便的接入到传统的 retrofit 网络请求模式中。
 * 你几乎不需要做任何额外工作，就可以简单高效的在 Compose 中使用网络请求，并将请求数据作为状态，直接驱动UI。
 *
 * 重要：如果你使用例如 [GsonConverterFactory] 来反序列化你的响应，必须将反序列化后的对象设置成[Parcelable]!

 * [noop] 是所有函数的抽象，我们最终通过函数拿到的手动执行函数也是[noop]类型的，调用时要传递的是[arrayOf]的参数。
 *
 * 我还额外提供了两个方便的转换函数 [asNoopFn]、[asSuspendNoopFn]，这两个函数可以把任意的Kotlin函数转换成[useRequest]需要的函数。
 * 需要注意区分，如果是挂起函数就需要调用[asSuspendNoopFn]，否则就使用 [asNoopFn]，通过这个函数我们可以简化普通函数到[noop]的包装过程。
 *
 *
 * 示例代码：
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
 * 是的，它可以简单到只有一行代码，通过[RequestOptions]选项配置，你可以设置：手动请求、Ready、错误重试、
 * 生命周期回调、轮询、防抖、节流、依赖刷新等待功能，唯一需要注意的也仅仅是上面说的设置数据类为：[Parcelable]。
 *
 * Tips: 强烈建议开启Android Studio中类型镶嵌提示，位于：Editor - Inlay Hints - Types - Kotlin，它可以
 * 更高效的提示我们解构赋值后拿到的相关状态、函数的类型。
 *
 * @param requestFn 经过抽象后的请求函数：suspend (TParams) -> TData，如果你不喜欢使用[asSuspendNoopFn]，也可以使用匿名 [suspend]闭包。
 * @param options 请求的配置项，参考[RequestOptions]，以及[ahooks-useRequest](https://ahooks.gitee.io/zh-CN/hooks/use-request/index).
 * @param plugins 自定义的插件，这是一个数组，请通过arrayOf传入，事实上这个参数使用了面向未来的，因为在当前版本被[Composable]注解标注的函数是无法使用函数引用方式传入的。
 *  想传入自定义的插件函数尽可以通过匿名lambda闭包来进行传递，这样无法实现复用，意义不大。
 *
 * @author Junerver
 * date: 2024/1/25-8:11
 * Email: junerver@gmail.com
 * Version: v1.0
 */
@Composable
inline fun <reified TData : Any> useRequest(
    noinline requestFn: SuspendNormalFunction<TData>,
    options: RequestOptions<TData> = defaultOption(),
    plugins: Array<@Composable (RequestOptions<TData>) -> Plugin<TData>> = emptyArray(),
): Tuple7<TData?, Boolean, Throwable?, VoidFunction, KFunction1<(TData?) -> TData, Unit>, NoParamsVoidFunction, NoParamsVoidFunction> {

    val fetch = useRequestPluginsImpl(
        requestFn,
        options,
        buildList {
            addAll(plugins.map {
                it(options)
            })
            addAll(
                arrayOf(
                    useDebouncePlugin(options),
                    useLoadingDelayPlugin(options),
                    usePollingPlugin(options),
                    useThrottlePlugin(options),
                    useAutoRunPlugin(options),
                    useCachePlugin(options),
                    useRetryPlugin(options),
                )
            )
        }.toTypedArray()
    )

    /**
     * 这样做的好处是方便添加与变更次序，而不用每次覆写componentN函数
     */
    return with(fetch) {
        tuple(
            /**
             * 直接将[dataState.value]返回，避免拆包
             */
            first = dataState.value,
            /**
             * 返回[loadingState]
             */
            second = loadingState.value,
            /**
             * 返回原函数执行出错的异常[errorState]
             */
            third = errorState.value,
            /**
             * 如果函数手动执行，则通过返回的[run]函数，进行执行。
             * 如果配置了防抖、节流会按照优先防抖、其次节流的策略返回对应的函数。
             */
            fourth = run,
            /**
             * [mutate] 函数，用于直接修改当前状态值，目前缺少回溯
             */
            fifth = ::mutate,
            /**
             * [refresh] 函数
             */
            sixth = ::refresh,
            /**
             * [cancel] 函数
             */
            seventh = ::cancel
        )
    }
}

@Composable
fun <TData : Any> useRequestPluginsImpl(
    requestFn: SuspendNormalFunction<TData>,
    options: RequestOptions<TData> = defaultOption(),
    plugins: Array<Plugin<TData>> = emptyArray(),
): Fetch<TData> {
    // 为了驱动组件更新，必须要持有真实的状态
    // 请求的数据状态
    val dataState = rememberSaveable { mutableStateOf<TData?>(null) }
    val (_, setData) = dataState
    // 是否处于首次请求中的状态
    val loadingState = rememberSaveable { mutableStateOf(false) }
    val (_, setLoading) = loadingState
    // 请求出错后的保存错误的状态
    val errorState = rememberSaveable { mutableStateOf<Throwable?>(null) }
    val (_, setError) = errorState

    // Fetch 对象用于持有请求的状态
    val fetch = rememberSaveable {
        Fetch(options).apply {
            this.dataState = dataState
            this.setData = setData
            this.loadingState = loadingState
            this.setLoading = setLoading
            this.errorState = errorState
            this.setError = setError
            this.requestFn = requestFn

            // initState，主要负责初始的loading状态
            this.fetchState = plugins.mapNotNull {
                it.onInit?.invoke(options)
            }.cover() ?: FetchState()
            //此时插件被装在，并且获得相应实例
            this.pluginImpls = plugins.map { it.invoke(this, options) }.toTypedArray()
        }
    }.apply {
        // 作用域必须在每次重组后更新，否则会导致请求无法发出
        this.scope = rememberCoroutineScope()
    }

    useUnmount {
        fetch.cancel()
    }

    return fetch
}