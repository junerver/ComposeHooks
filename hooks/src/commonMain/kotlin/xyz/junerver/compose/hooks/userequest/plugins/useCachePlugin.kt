package xyz.junerver.compose.hooks.userequest.plugins

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import xyz.junerver.compose.hooks.MutableRef
import xyz.junerver.compose.hooks.Tuple4
import xyz.junerver.compose.hooks.tuple
import xyz.junerver.compose.hooks.useRef
import xyz.junerver.compose.hooks.useUnmount
import xyz.junerver.compose.hooks.useUpdateEffect
import xyz.junerver.compose.hooks.userequest.Fetch
import xyz.junerver.compose.hooks.userequest.GenPluginLifecycleFn
import xyz.junerver.compose.hooks.userequest.Keys
import xyz.junerver.compose.hooks.userequest.OnBeforeReturn
import xyz.junerver.compose.hooks.userequest.OnRequestReturn
import xyz.junerver.compose.hooks.userequest.Plugin
import xyz.junerver.compose.hooks.userequest.PluginLifecycle
import xyz.junerver.compose.hooks.userequest.PluginOnBefore
import xyz.junerver.compose.hooks.userequest.PluginOnError
import xyz.junerver.compose.hooks.userequest.PluginOnMutate
import xyz.junerver.compose.hooks.userequest.PluginOnRequest
import xyz.junerver.compose.hooks.userequest.PluginOnSuccess
import xyz.junerver.compose.hooks.userequest.RequestOptions
import xyz.junerver.compose.hooks.userequest.useEmptyPlugin
import xyz.junerver.compose.hooks.userequest.utils.CachedData
import xyz.junerver.compose.hooks.userequest.utils.RestoreFetchStateData
import xyz.junerver.compose.hooks.userequest.utils.getCacheDeferred
import xyz.junerver.compose.hooks.userequest.utils.setCacheDeferred
import xyz.junerver.compose.hooks.userequest.utils.subscribe
import xyz.junerver.compose.hooks.userequest.utils.trigger
import xyz.junerver.compose.hooks.utils.CacheManager
import xyz.junerver.compose.hooks.utils.asBoolean
import xyz.junerver.compose.hooks.utils.currentTime
import xyz.junerver.compose.hooks.utils.isNotNull

/*
  Description:
  Author: Junerver
  Date: 2024/2/23-8:38
  Email: junerver@gmail.com
  Version: v1.0
*/
private class CachePlugin<TParams, TData : Any> : Plugin<TParams, TData>() {
    lateinit var unSubscribeRef: MutableRef<(() -> Unit)?>
    lateinit var currentPromiseRef: MutableRef<Deferred<*>?>

    private var staleTime: Duration = Duration.ZERO
    lateinit var setCache: (key: String, cachedData: CachedData<TData>) -> Unit
    lateinit var getCache: (String, TParams) -> CachedData<TData>?

    override val invoke: GenPluginLifecycleFn<TParams, TData>
        get() = { fetch: Fetch<TParams, TData>, requestOptions: RequestOptions<TParams, TData> ->
            initFetch(fetch, requestOptions)
            val (cacheKey, staleTimeOp) = with(requestOptions) { tuple(cacheKey, staleTime) }
            staleTime = staleTimeOp

            object : PluginLifecycle<TParams, TData>() {
                override val onBefore: PluginOnBefore<TParams, TData>
                    get() = onBefore@{
                        val cacheData = getCache(cacheKey, it)
                        // 正在请求，啥也不做
                        if (!cacheData.asBoolean()) return@onBefore null
                        if (staleTime == (-1).seconds || currentTime - cacheData.time <= staleTime) {
                            OnBeforeReturn<TParams, TData>(
                                loading = false,
                                data = cacheData.data,
                                error = null,
                                returnNow = true, // 未过期直接返回
                            ).apply {
                                copyMap = buildMap {
                                    putAll(asNotNullMap())
                                    this[Keys.error] = null
                                }
                            }
                        } else {
                            // 过期继续请求
                            OnBeforeReturn<TParams, TData>(
                                data = cacheData.data,
                                error = null,
                            ).apply {
                                copyMap = buildMap {
                                    putAll(asNotNullMap())
                                    this[Keys.error] = null
                                }
                            }
                        }
                    }

                override val onRequest: PluginOnRequest<TParams, TData>
                    get() = onRequest@{ requestFn, param ->
                        var servicePromise: Deferred<TData>? = getCacheDeferred(cacheKey)
                        trigger(cacheKey, RestoreFetchStateData(loading = true))

                        // 如果已经发起过请求，则直接从缓存中返回之前请求的 Deferred
                        // 这样可以让同时发出的两个请求，共用同一个
                        if (servicePromise != null && servicePromise != currentPromiseRef.current) {
                            return@onRequest OnRequestReturn(servicePromise)
                        }
                        // 发起异步请求，将Deferred存入 ref、缓存，并且返回 包装后的OnRequestReturn
                        servicePromise = async(SupervisorJob()) { requestFn(param) }
                        currentPromiseRef.current = servicePromise
                        setCacheDeferred(cacheKey, servicePromise)
                        OnRequestReturn(servicePromise)
                    }

                override val onSuccess: PluginOnSuccess<TParams, TData>
                    get() = { data: TData, params: TParams ->
                        if (cacheKey.asBoolean()) {
                            // 取消订阅，保存
                            unSubscribeRef.current?.invoke()
                            setCache(
                                cacheKey,
                                CachedData(
                                    data,
                                    params as Any?,
                                ),
                            )
                            unSubscribeRef.current = subscribe(cacheKey, ::setFetchState)
                        }
                    }

                override val onError: PluginOnError<TParams>
                    get() = { e: Throwable, _ ->
                        if (cacheKey.asBoolean()) {
                            unSubscribeRef.current?.invoke()
                            trigger(
                                cacheKey,
                                RestoreFetchStateData(
                                    error = e,
                                    loading = false,
                                ),
                            )
                            unSubscribeRef.current = subscribe(cacheKey, ::setFetchState)
                        }
                    }

                override val onMutate: PluginOnMutate<TData>
                    get() = {
                        if (cacheKey.asBoolean()) {
                            unSubscribeRef.current?.invoke()
                            setCache(
                                cacheKey,
                                CachedData(
                                    it,
                                    fetchInstance.fetchState.params as Any?,
                                ),
                            )
                            unSubscribeRef.current = subscribe(cacheKey, ::setFetchState)
                        }
                    }
            }
        }

    /**
     * 初始化 fetch 的状态
     */
    fun initFetchStateWithCachedData(cacheData: CachedData<TData>) {
        with(fetchInstance.fetchState) {
            data = cacheData.data
            @Suppress("UNCHECKED_CAST")
            params = cacheData.params as TParams
            if (staleTime == (-1).seconds || currentTime - cacheData.time <= staleTime) {
                loading = false
            }
        }
    }

    fun setFetchState(data: RestoreFetchStateData) {
        fetchInstance.setState(
            buildMap {
                if (data.loading.isNotNull) this[Keys.loading] = data.loading
                if (data.data.isNotNull) this[Keys.data] = data.data
                if (data.error.isNotNull) this[Keys.error] = data.error
            },
        )
    }
}

@Composable
internal fun <TParams, TData : Any> useCachePlugin(options: RequestOptions<TParams, TData>): Plugin<TParams, TData> {
    val (cacheKey, cacheTime, customSetCache, customGetCache) = with(options) {
        Tuple4(cacheKey, cacheTime, setCache, getCache)
    }
    if (cacheKey.isEmpty()) {
        return useEmptyPlugin()
    }

    fun setCache(key: String, cachedData: CachedData<TData>) {
        if (customSetCache.asBoolean()) {
            customSetCache(cachedData)
        } else {
            CacheManager.saveCache(key, cacheTime, cachedData)
        }
        trigger(
            key,
            RestoreFetchStateData(
                data = cachedData.data,
                loading = false,
                error = null,
            ),
        )
    }

    fun getCache(key: String, params: TParams? = null): CachedData<TData>? = if (customGetCache.asBoolean()) {
        params?.let { customGetCache(params) }
    } else {
        CacheManager.getCache(key)
    }

    // 反订阅函数
    val unSubscribeRef = useRef<(() -> Unit)?>(null)
    val currentPromiseRef = useRef<Deferred<*>?>(null)

    val cachePlugin = remember {
        CachePlugin<TParams, TData>().apply {
            this.setCache = ::setCache
            this.getCache = ::getCache
            this.unSubscribeRef = unSubscribeRef
            this.currentPromiseRef = currentPromiseRef
        }
    }

    useUpdateEffect {
        val cacheData = getCache(cacheKey)
        cacheData?.let {
            // 使用缓存初始化
            cachePlugin.initFetchStateWithCachedData(it)
        }

        /**
         * 订阅数据更新，当数据更新时，通知变更状态，这样可以实现全局的请求统一
         */
        unSubscribeRef.current = subscribe(cacheKey) {
            cachePlugin.setFetchState(it)
        }
    }

    useUnmount {
        unSubscribeRef.current?.invoke()
    }

    return cachePlugin
}
