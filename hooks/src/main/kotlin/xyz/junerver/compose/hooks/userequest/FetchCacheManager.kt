package xyz.junerver.compose.hooks.userequest

import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import xyz.junerver.compose.hooks.userequest.utils.CachedData
import xyz.junerver.kotlin.Tuple2
import xyz.junerver.kotlin.tuple

/*
  Description:
  Author: Junerver
  Date: 2024/2/4-10:28
  Email: junerver@gmail.com
  Version: v1.0
*/
private typealias DataCache = Tuple2<CachedData<*>, Long>

internal object FetchCacheManager : CoroutineScope {

    // 如果你看过协程的官方文档或视频。你应该会知道Job和SupervisorJob的一个区别是，Job的子协程发生异常被取消会同时取消Job的其它子协程，而SupervisorJob不会。
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + SupervisorJob()

    private val cache: MutableMap<String, DataCache> = mutableMapOf()

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

    private val currentTime: Long
        get() = Clock.System.now().toEpochMilliseconds()

    /**
     * 缓存是否有效
     */
    private fun isCacheValid(key: String): Boolean {
        if (!cache.containsKey(key)) return false // 无缓存
        val cacheData = cache[key]!!
        return currentTime < cacheData.second // 还新鲜
    }

    /**
     * 存入缓存，key - <数据，缓存过期时间>
     */
    fun <T> saveCache(key: String, cacheTime: Long, cachedData: CachedData<T>) {
        cache[key] = tuple(cachedData, currentTime + cacheTime)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getCache(key: String): CachedData<T>? {
        return if (isCacheValid(key)) {
            cache[key]!!.first as CachedData<T>
        } else {
            null
        }
    }
}
