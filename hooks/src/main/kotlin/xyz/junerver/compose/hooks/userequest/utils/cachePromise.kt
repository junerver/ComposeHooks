package xyz.junerver.compose.hooks.userequest.utils

import kotlinx.coroutines.Deferred

/**
 * Description:
 * @author Junerver
 * date: 2024/2/23-10:09
 * Email: junerver@gmail.com
 * Version: v1.0
 */

private val cachePromise: MutableMap<String, Deferred<*>> = mutableMapOf()

@Suppress("UNCHECKED_CAST")
internal fun <T> getCachePromise(cacheKey: String) = cachePromise[cacheKey] as? Deferred<T>

internal fun setCachePromise(cacheKey: String, promise: Deferred<*>) {
    cachePromise[cacheKey] = promise
}

/**
 * 如果是添加到缓存中的Deferred则移除该缓存
 */
internal suspend fun <T> Deferred<T>.awaitPlus(): T {
    cachePromise.entries.removeIf { it.value == this }
    return this.await()
}
