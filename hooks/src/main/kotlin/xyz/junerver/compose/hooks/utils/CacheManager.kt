package xyz.junerver.compose.hooks.utils

import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.datetime.Instant
import xyz.junerver.compose.hooks.cacheKey
import xyz.junerver.compose.hooks.userequest.utils.CachedData
import xyz.junerver.kotlin.Tuple2
import xyz.junerver.kotlin.asBoolean
import xyz.junerver.kotlin.runIf
import xyz.junerver.kotlin.tuple

/*
  Description:
  Author: Junerver
  Date: 2024/2/4-10:28
  Email: junerver@gmail.com
  Version: v1.0
*/

/** first: cacheData, seconds: expiration */
private typealias DataCache = Tuple2<CachedData<*>, Instant>

internal object CacheManager {

    private val cache: MutableMap<String, DataCache> = ConcurrentHashMap()

    //region for useCachePlugin
    /**
     * Save cache
     *
     * @param key
     * @param duration
     * @param cachedData
     * @param T
     * @return true: saveCache, false: param duration error
     */
    fun <T> saveCache(key: String, duration: Duration, cachedData: CachedData<T>): Boolean =
        (duration.asBoolean() || duration == (-1).seconds).also {
            runIf(it) {
                cache[key.cacheKey] = tuple(
                    cachedData,
                    cachedData.time + if (duration == (-1).seconds) Long.MAX_VALUE.seconds else duration
                )
            }
        }

    @Suppress("UNCHECKED_CAST")
    fun <T> getCache(key: String): CachedData<T>? {
        return cache[key.cacheKey]?.takeIf {
            currentTime < it.second
        }?.first as? CachedData<T> ?: run {
            cache.remove(key.cacheKey)
            null
        }
    }
    //endregion

    //region longTermCache & without wrap
    fun <T> saveCache(key: String, data: T) {
        cache[key] = tuple(
            CachedData(data),
            currentTime + Long.MAX_VALUE.seconds
        )
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getCache(key: String, default: T): T =
        (cache[key]?.first as CachedData<T>?)?.data ?: default

    fun clearCache(vararg keys: String) {
        keys.forEach {
            cache.remove(it)
        }
    }
    //endregion
}
