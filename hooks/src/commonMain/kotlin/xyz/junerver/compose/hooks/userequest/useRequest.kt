package xyz.junerver.compose.hooks.userequest

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlin.reflect.KFunction0
import kotlin.reflect.KFunction1
import xyz.junerver.compose.hooks.SuspendNormalFunction
import xyz.junerver.compose.hooks.VoidFunction
import xyz.junerver.compose.hooks._useControllable
import xyz.junerver.compose.hooks.getValue
import xyz.junerver.compose.hooks.setValue
import xyz.junerver.compose.hooks.useCreation
import xyz.junerver.compose.hooks.useDynamicOptions
import xyz.junerver.compose.hooks.useRef
import xyz.junerver.compose.hooks.useUnmount
import xyz.junerver.compose.hooks.userequest.plugins.useAutoRunPlugin
import xyz.junerver.compose.hooks.userequest.plugins.useCachePlugin
import xyz.junerver.compose.hooks.userequest.plugins.useDebouncePlugin
import xyz.junerver.compose.hooks.userequest.plugins.useLoadingDelayPlugin
import xyz.junerver.compose.hooks.userequest.plugins.usePollingPlugin
import xyz.junerver.compose.hooks.userequest.plugins.useRetryPlugin
import xyz.junerver.compose.hooks.userequest.plugins.useThrottlePlugin

typealias ReqFn<TParams> = VoidFunction<TParams>
typealias MutateFn<TData> = KFunction1<(TData?) -> TData, Unit>
typealias RefreshFn = KFunction0<Unit>
typealias CancelFn = KFunction0<Unit>
typealias ComposablePluginGenFn<TParams, TData> = @Composable (UseRequestOptions<TParams, TData>) -> Plugin<TParams, TData>
/*
  Description:
  Author: Junerver
  Date: 2024/1/25-8:11
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * A hook for managing network request state that can be easily integrated with traditional Retrofit network request patterns.
 * You need almost no additional work to use network requests simply and efficiently in Compose,
 * and use request data as state to directly drive the UI.
 *
 * [SuspendNormalFunction] is an abstraction of all functions. The manual execution function we finally get through the function
 * is also of type [SuspendNormalFunction], and the parameters passed when calling are [arrayOf].
 *
 * Two convenient conversion functions [asNoopFn] and [asSuspendNoopFn] are also provided,
 * which can convert any Kotlin function to the function required by [useRequest].
 * Note the distinction: if it is a suspend function, you need to call [asSuspendNoopFn], otherwise use [asNoopFn].
 * Through this function, we can simplify the wrapping process from ordinary functions to [SuspendNormalFunction].
 *
 * Example usage:
 *
 * ```kotlin
 * // Auto request with default parameters
 * val (userInfo, loading, error) = useRequest(
 *     requestFn = { NetApi.userInfo(it) },
 *     optionsOf = {
 *         defaultParams = "junerver" // Auto requests must set default parameters
 *     }
 * )
 *
 * // Manual request
 * val (repoInfo, loading, error, request) = useRequest(
 *     requestFn = { params: Tuple2<String, String> ->
 *         NetApi.repoInfo(params.first, params.second)
 *     },
 *     optionsOf = {
 *         manual = true
 *         defaultParams = tuple("junerver", "ComposeHooks")
 *     }
 * )
 *
 * // Using asSuspendNoopFn for direct function reference
 * val (userInfo, loading, error) = useRequest(
 *     requestFn = NetApi::userInfo.asSuspendNoopFn(),
 *     optionsOf = {
 *         defaultParams = tuple("junerver")
 *     }
 * )
 * ```
 *
 * Through [UseRequestOptions] configuration, you can set: manual request, ready state, error retry,
 * lifecycle callbacks, polling, debounce, throttle, dependency refresh and other functions.
 *
 * Tips: It is strongly recommended to enable type inlay hints in Android Studio, located at:
 * Editor - Inlay Hints - Types - Kotlin, which can more efficiently prompt us about the types of
 * related states and functions obtained after destructuring assignment.
 *
 * @param requestFn The abstracted request function: suspend (TParams) -> TData.
 *   If you don't like using [asSuspendNoopFn], you can also use anonymous [suspend] closures.
 * @param optionsOf Configuration factory function for request options, see [UseRequestOptions]
 * @param plugins Custom plugins array, pass through arrayOf
 * @return [RequestHolder] containing data state, loading state, error state, and control functions
 */
@Composable
fun <TParams, TData : Any> useRequest(
    requestFn: SuspendNormalFunction<TParams, TData>,
    optionsOf: UseRequestOptions<TParams, TData>.() -> Unit = {},
    plugins: Array<ComposablePluginGenFn<TParams, TData>> = emptyArray(),
): RequestHolder<TParams, TData> = useRequestPrivate(
    requestFn,
    useDynamicOptions(optionsOf),
    plugins,
)

/**
 * Internal implementation of useRequest hook with full plugin support.
 * This function handles the core logic of request management including plugin initialization,
 * state management, and request execution.
 *
 * @param requestFn The abstracted request function: suspend (TParams) -> TData
 * @param options Request configuration options, see [UseRequestOptions]
 * @param plugins Custom plugins array, pass through arrayOf
 */
@Composable
private fun <TParams, TData : Any> useRequestPrivate(
    requestFn: SuspendNormalFunction<TParams, TData>,
    options: UseRequestOptions<TParams, TData>,
    plugins: Array<ComposablePluginGenFn<TParams, TData>> = emptyArray(),
): RequestHolder<TParams, TData> {
    var customPluginsRef by useRef<Array<Plugin<TParams, TData>>>(emptyArray())
    if (customPluginsRef.size != plugins.size) {
        customPluginsRef = plugins.map {
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
    val allPlugins by useCreation(*plugins) {
        customPluginsRef + arrayOf(
            buildInDebouncePlugin,
            buildInLoadingDelayPlugin,
            buildInPollingPlugin,
            buildInThrottlePlugin,
            buildInAutoRunPlugin,
            buildInCachePlugin,
            buildInRetryPlugin,
        )
    }
    val fetch = useRequestPluginsImpl(
        requestFn,
        options,
        allPlugins,
    )

    return with(fetch) {
        RequestHolder(
            data = dataState,
            isLoading = loadingState,
            error = errorState,
            request = run,
            mutate = ::mutate,
            refresh = ::refresh,
            cancel = ::cancel,
        )
    }
}

/**
 * Internal implementation function that creates and configures the Fetch instance with plugins.
 * This function handles the low-level details of state management, plugin initialization,
 * and coroutine scope management for the request lifecycle.
 *
 * @param requestFn The request function to be executed
 * @param options Configuration options for the request behavior
 * @param plugins Array of plugins to be applied to the request
 * @return Configured Fetch instance ready for use
 */
@Composable
private fun <TParams, TData : Any> useRequestPluginsImpl(
    requestFn: SuspendNormalFunction<TParams, TData>,
    options: UseRequestOptions<TParams, TData> = UseRequestOptions(),
    plugins: Array<Plugin<TParams, TData>> = emptyArray(),
): Fetch<TParams, TData> {
    val (dataState, setData) = _useControllable<TData?>(null)
    val (loadingState, setLoading) = _useControllable(false)
    val (errorState, setError) = _useControllable<Throwable?>(null)

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

/**
 * Holder class that encapsulates all the state and control functions returned by useRequest hook.
 * This class provides a structured way to access request data, loading state, error state,
 * and various control functions for managing the request lifecycle.
 *
 * @param TParams The type of parameters passed to the request function
 * @param TData The type of data returned by the request function
 * @property data State containing the response data, null if no data has been loaded yet
 * @property isLoading State indicating whether a request is currently in progress
 * @property error State containing any error that occurred during the request, null if no error
 * @property request Function to manually trigger a request with optional parameters
 * @property mutate Function to manually update the data state without making a request
 * @property refresh Function to refresh the request using the last used parameters
 * @property cancel Function to cancel any ongoing request
 */
@Stable
data class RequestHolder<TParams, TData>(
    val data: State<TData?>,
    val isLoading: State<Boolean>,
    val error: State<Throwable?>,
    val request: ReqFn<TParams>,
    val mutate: MutateFn<TData>,
    val refresh: RefreshFn,
    val cancel: CancelFn,
)
