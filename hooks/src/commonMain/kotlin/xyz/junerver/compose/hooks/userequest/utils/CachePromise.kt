package xyz.junerver.compose.hooks.userequest.utils

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/*
  Description:
  Author: Junerver
  Date: 2024/2/23-10:09
  Email: junerver@gmail.com
  Version: v1.0
*/

private val cacheDeferred = MutableStateFlow<Map<String, Deferred<*>>>(emptyMap())

@Suppress("UNCHECKED_CAST")
internal fun <T> getCacheDeferred(cacheKey: String) = cacheDeferred.value[cacheKey] as? Deferred<T>

internal fun setCacheDeferred(cacheKey: String, promise: Deferred<*>) {
    cacheDeferred.update { currentCache -> currentCache + (cacheKey to promise) }
}

/**
 * 如果是添加到缓存中的Deferred则移除该缓存
 */
internal suspend fun <T> Deferred<T>.awaitPlus(): T = try {
    await()
} finally {
    cacheDeferred.update { currentCache ->
        currentCache.filterValues { it !== this }
    }
}
