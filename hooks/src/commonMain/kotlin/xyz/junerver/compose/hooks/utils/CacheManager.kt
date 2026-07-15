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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import xyz.junerver.compose.hooks.Tuple2
import xyz.junerver.compose.hooks.internal.cacheKey
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
private typealias DataCache = Tuple2<CachedData<*>, Instant>

internal object CacheManager : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + SupervisorJob()

    private val cache = MutableStateFlow<Map<String, DataCache>>(emptyMap())

    init {
        launch {
            delay(1.seconds)
            while (isActive) {
                val now = currentInstant
                cache.update { currentCache ->
                    currentCache.filterValues { it.second > now }
                }
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
                cache.update { currentCache ->
                    currentCache + (
                        key.cacheKey to tuple(
                            cachedData,
                            if (duration == (-1).seconds) Instant.DISTANT_FUTURE else cachedData.time + duration,
                        )
                    )
                }
            }
        }

    @Suppress("UNCHECKED_CAST")
    fun <T> getCache(key: String): CachedData<T>? {
        val internalKey = key.cacheKey
        val cachedEntry = cache.value[internalKey] ?: return null
        if (currentInstant < cachedEntry.second) return cachedEntry.first as? CachedData<T>
        cache.update { currentCache ->
            if (currentCache[internalKey] === cachedEntry) currentCache - internalKey else currentCache
        }
        return null
    }
    //endregion

    //region longTermCache & without wrap
    fun <T> saveCache(key: String, data: T) {
        cache.update { currentCache ->
            currentCache + (
                key to tuple(
                    CachedData(data),
                    Instant.DISTANT_FUTURE,
                )
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getCache(key: String, default: T): T = (cache.value[key]?.first as CachedData<T>?)?.data ?: default

    fun clearCache(vararg keys: String) {
        val keysToRemove = keys.toSet()
        cache.update { currentCache ->
            currentCache - keysToRemove
        }
    }
    //endregion
}
