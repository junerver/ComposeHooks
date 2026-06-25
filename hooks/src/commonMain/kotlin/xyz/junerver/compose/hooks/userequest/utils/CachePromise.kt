package xyz.junerver.compose.hooks.userequest.utils

import kotlinx.coroutines.Deferred

/*
  Description:
  Author: Junerver
  Date: 2024/2/23-10:09
  Email: junerver@gmail.com
  Version: v1.0
*/

private val cacheDeferred: MutableMap<String, Deferred<*>> = mutableMapOf()

@Suppress("UNCHECKED_CAST")
internal fun <T> getCacheDeferred(cacheKey: String) = cacheDeferred[cacheKey] as? Deferred<T>

internal fun setCacheDeferred(cacheKey: String, promise: Deferred<*>) {
    cacheDeferred[cacheKey] = promise
}

/**
 * 如果是添加到缓存中的Deferred则移除该缓存
 */
internal suspend fun <T> Deferred<T>.awaitPlus(): T {
    cacheDeferred.entries.removeAll { it.value == this }
    return this.await()
}
