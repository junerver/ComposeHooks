package xyz.junerver.compose.hooks.utils

import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.cacheKey
import xyz.junerver.compose.hooks.tuple
import xyz.junerver.compose.hooks.userequest.utils.CachedData

/*
  Description:
  Author: Junerver
  Date: 2024/2/4-10:28
  Email: junerver@gmail.com
  Version: v1.0
*/

/** first: cacheData, seconds: expiration */
private typealias DataCache = Pair<CachedData<*>, Instant>

internal object CacheManager : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + SupervisorJob()

    private val cache: MutableMap<String, DataCache> = mutableMapOf()

    init {
        launch {
            delay(1.seconds)
            while (isActive) {
                cache.entries.removeAll { it.value.second <= currentTime }
                delay(30.seconds)
            }
        }
    }

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
                cache[key.cacheKey] = Pair(
                    cachedData,
                    if (duration == (-1).seconds) Instant.DISTANT_FUTURE else cachedData.time + duration,
                )
            }
        }

    @Suppress("UNCHECKED_CAST")
    fun <T> getCache(key: String): CachedData<T>? = cache[key.cacheKey]?.takeIf {
        currentTime < it.second
    }?.first as? CachedData<T> ?: run {
        cache.remove(key.cacheKey)
        null
    }
    //endregion

    //region longTermCache & without wrap
    fun <T> saveCache(key: String, data: T) {
        cache[key] = tuple(
            CachedData(data),
            Instant.DISTANT_FUTURE,
        )
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getCache(key: String, default: T): T = (cache[key]?.first as CachedData<T>?)?.data ?: default

    fun clearCache(vararg keys: String) {
        keys.forEach {
            cache.remove(it)
        }
    }
    //endregion
}
