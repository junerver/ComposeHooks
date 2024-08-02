package xyz.junerver.compose.hooks.userequest

import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import xyz.junerver.compose.hooks.CACHE_KEY_PREFIX
import xyz.junerver.compose.hooks.userequest.utils.CachedData
import xyz.junerver.compose.hooks.utils.currentTime
import xyz.junerver.kotlin.Tuple2
import xyz.junerver.kotlin.asBoolean
import xyz.junerver.kotlin.tuple

/*
  Description:
  Author: Junerver
  Date: 2024/2/4-10:28
  Email: junerver@gmail.com
  Version: v1.0
*/
private typealias DataCache = Tuple2<CachedData<*>, Instant>

internal object FetchCacheManager : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + SupervisorJob()

    private val cache: MutableMap<String, DataCache> = ConcurrentHashMap()

    private val _flow = flow<Nothing> {
        delay(1.seconds)
        while (isActive) {
            cache.entries.removeIf { it.value.second <= currentTime }
            delay(0.1.seconds)
        }
    }

    init {
        launch {
            _flow.collect()
        }
    }

    /** 缓存是否有效 */
    private fun isCacheValid(key: String): Boolean {
        if (!cache.containsKey("${CACHE_KEY_PREFIX}$key")) return false // 无缓存
        val cacheData = cache["${CACHE_KEY_PREFIX}$key"]!!
        return currentTime < cacheData.second // 还新鲜
    }

    /** 存入缓存，key - <数据，缓存过期时间> */
    fun <T> saveCache(key: String, cacheTime: Duration, cachedData: CachedData<T>) {
        if (cacheTime.asBoolean() || cacheTime == (-1).seconds) {
            cache["${CACHE_KEY_PREFIX}$key"] = tuple(
                cachedData,
                currentTime + if (cacheTime == (-1).seconds) Long.MAX_VALUE.seconds else cacheTime
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getCache(key: String): CachedData<T>? {
        return if (isCacheValid(key)) {
            cache["${CACHE_KEY_PREFIX}$key"]!!.first as CachedData<T>
        } else {
            null
        }
    }

    fun clearCache(vararg keys: String) {
        keys.forEach {
            cache.remove("${CACHE_KEY_PREFIX}$it")
        }
    }
}

fun clearCache(vararg keys: String) {
    FetchCacheManager.clearCache(*keys)
}
