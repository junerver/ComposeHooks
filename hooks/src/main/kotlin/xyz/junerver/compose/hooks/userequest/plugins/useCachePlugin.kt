package xyz.junerver.compose.hooks.userequest.plugins

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import xyz.junerver.compose.hooks.Ref
import xyz.junerver.compose.hooks.SuspendNormalFunction
import xyz.junerver.compose.hooks.TParams
import xyz.junerver.compose.hooks.useCreation
import xyz.junerver.compose.hooks.useRef
import xyz.junerver.compose.hooks.useUnmount
import xyz.junerver.compose.hooks.userequest.Fetch
import xyz.junerver.compose.hooks.userequest.FetchCacheManager
import xyz.junerver.compose.hooks.userequest.GenPluginLifecycleFn
import xyz.junerver.compose.hooks.userequest.Keys
import xyz.junerver.compose.hooks.userequest.OnBeforeReturn
import xyz.junerver.compose.hooks.userequest.OnRequestReturn
import xyz.junerver.compose.hooks.userequest.Plugin
import xyz.junerver.compose.hooks.userequest.PluginLifecycle
import xyz.junerver.compose.hooks.userequest.RequestOptions
import xyz.junerver.compose.hooks.userequest.useEmptyPlugin
import xyz.junerver.compose.hooks.userequest.utils.CachedData
import xyz.junerver.compose.hooks.userequest.utils.Data
import xyz.junerver.compose.hooks.userequest.utils.getCachePromise
import xyz.junerver.compose.hooks.userequest.utils.setCachePromise
import xyz.junerver.compose.hooks.userequest.utils.subscribe
import xyz.junerver.compose.hooks.userequest.utils.trigger
import xyz.junerver.kotlin.asBoolean
import xyz.junerver.kotlin.isNotNull
import xyz.junerver.kotlin.tuple

/**
 * Description:
 * @author Junerver
 * date: 2024/2/23-8:38
 * Email: junerver@gmail.com
 * Version: v1.0
 */
class CachePlugin<TData : Any> : Plugin<TData>() {

    lateinit var unSubscribeRef: Ref<(() -> Unit)?>
    lateinit var currentPromiseRef: Ref<Deferred<*>?>

    private var staleTime: Long = 0
    val currentTime: Long
        get() = System.currentTimeMillis()
    lateinit var setCache: (key: String, cachedData: CachedData<TData>) -> Unit
    lateinit var getCache: (String, TParams) -> CachedData<TData>?

    override val invoke: GenPluginLifecycleFn<TData>
        get() = { fetch: Fetch<TData>, requestOptions: RequestOptions<TData> ->
            initFetch(fetch, requestOptions)
            val (cacheKey, staleTimeOp) = with(requestOptions) { tuple(cacheKey, staleTime) }
            staleTime = staleTimeOp

            object : PluginLifecycle<TData>() {
                override val onBefore: ((TParams) -> OnBeforeReturn<TData>?)
                    get() = onBefore@{
                        val cacheData = getCache(cacheKey, it)
                        // 正在请求，啥也不做
                        if (!cacheData.asBoolean()) return@onBefore null
                        if (staleTime == -1L || currentTime - cacheData.time <= staleTime) {
                            OnBeforeReturn(
                                loading = false,
                                data = cacheData.data,
                                error = null,
                                returnNow = true // 未过期直接返回
                            ).apply {
                                copyMap = buildMap {
                                    putAll(asNotNullMap())
                                    Keys.error to null
                                }
                            }
                        } else {
                            // 过期继续请求
                            OnBeforeReturn(
                                data = cacheData.data,
                                error = null
                            ).apply {
                                copyMap = buildMap {
                                    putAll(asNotNullMap())
                                    Keys.error to null
                                }
                            }
                        }
                    }

                override val onRequest: ((requestFn: SuspendNormalFunction<TData>, params: TParams) -> OnRequestReturn<TData>?)
                    get() = onRequest@{ requestFn, param ->
                        var servicePromise: Deferred<TData>? = getCachePromise(cacheKey)
                        trigger(cacheKey, Data(loading = true))

                        // 如果已经发起过请求，则直接从缓存中返回之前请求的 Deferred
                        // 这样可以让同时发出的两个请求，共用同一个
                        if (servicePromise != null && servicePromise != currentPromiseRef.current) {
                            return@onRequest OnRequestReturn(servicePromise)
                        }
                        // 发起异步请求，将Deferred存入 ref、缓存，并且返回 包装后的OnRequestReturn
                        servicePromise = async(SupervisorJob()) { requestFn(param) }
                        currentPromiseRef.current = servicePromise
                        setCachePromise(cacheKey, servicePromise)
                        OnRequestReturn(servicePromise)
                    }

                override val onSuccess: ((data: TData, params: TParams) -> Unit)
                    get() = { data: TData, params: TParams ->
                        if (cacheKey.asBoolean()) {
                            // 取消订阅，保存
                            unSubscribeRef.current?.invoke()
                            setCache(
                                cacheKey,
                                CachedData(
                                    data,
                                    params,
                                    currentTime
                                )
                            )
                            unSubscribeRef.current = subscribe(cacheKey, ::setFetchState)
                        }
                    }

                override val onError: ((e: Throwable, params: TParams) -> Unit)
                    get() = { e: Throwable, _ ->
                        if (cacheKey.asBoolean()) {
                            unSubscribeRef.current?.invoke()
                            trigger(
                                cacheKey,
                                Data(
                                    error = e,
                                    loading = false
                                )
                            )
                            unSubscribeRef.current = subscribe(cacheKey, ::setFetchState)
                        }
                    }

                override val onMutate: ((data: TData) -> Unit)
                    get() = {
                        if (cacheKey.asBoolean()) {
                            unSubscribeRef.current?.invoke()
                            setCache(
                                cacheKey,
                                CachedData(
                                    it,
                                    fetchInstance.fetchState.params ?: emptyArray(),
                                    currentTime
                                )
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
            params = cacheData.params
            if (staleTime == -1L || currentTime - cacheData.time <= staleTime) {
                loading = false
            }
        }
    }

    fun setFetchState(data: Data) {
        fetchInstance.setState(
            buildMap {
                if (data.loading.isNotNull) this[Keys.loading] = data.loading
                if (data.data.isNotNull) this[Keys.data] = data.data
                if (data.error.isNotNull) this[Keys.error] = data.error
            }
        )
    }
}

@Composable
fun <T : Any> useCachePlugin(options: RequestOptions<T>): Plugin<T> {
    val (cacheKey, cacheTime, customSetCache, customGetCache) = with(options) {
        tuple(cacheKey, cacheTime, setCache, getCache)
    }
    if (cacheKey.isEmpty()) {
        return useEmptyPlugin()
    }
    fun setCache(key: String, cachedData: CachedData<T>) {
        if (customSetCache.asBoolean()) {
            customSetCache(cachedData)
        } else {
            FetchCacheManager.saveCache(key, cacheTime, cachedData)
        }
        trigger(
            key,
            Data(
                data = cachedData.data,
                loading = false,
                error = null
            )
        )
    }

    fun getCache(key: String, params: TParams = emptyArray()): CachedData<T>? {
        return if (customGetCache.asBoolean()) {
            customGetCache(params)
        } else {
            FetchCacheManager.getCache(key)
        }
    }

    // 反订阅函数
    val unSubscribeRef = useRef<(() -> Unit)?>(null)
    val currentPromiseRef = useRef<Deferred<*>?>(null)

    val cachePlugin = remember {
        CachePlugin<T>().apply {
            this.setCache = ::setCache
            this.getCache = ::getCache
            this.unSubscribeRef = unSubscribeRef
            this.currentPromiseRef = currentPromiseRef
        }
    }

    useCreation {
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
