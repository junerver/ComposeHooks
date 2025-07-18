package xyz.junerver.compose.hooks.userequest.utils

import kotlin.time.Instant
import xyz.junerver.compose.hooks.cacheKey
import xyz.junerver.compose.hooks.utils.CacheManager
import xyz.junerver.compose.hooks.utils.currentTime

/*
  Description:
  Author: Junerver
  Date: 2024/2/23-8:46
  Email: junerver@gmail.com
  Version: v1.0
*/
data class CachedData<TData>(
    val data: TData,
    val params: Any? = null,
) {
    val time: Instant = currentTime

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CachedData<*>

        if (data != other.data) return false
        if (params != other.params) return false
        return time == other.time
    }

    override fun hashCode(): Int {
        var result = data?.hashCode() ?: 0
        result = 31 * result + params.hashCode()
        result = 31 * result + time.hashCode()
        return result
    }
}

fun clearCache(vararg keys: String) {
    CacheManager.clearCache(*keys.map { it.cacheKey }.toTypedArray())
}
