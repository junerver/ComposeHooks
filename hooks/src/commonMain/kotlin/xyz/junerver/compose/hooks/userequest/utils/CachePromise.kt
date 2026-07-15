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

private class CacheDeferredEntry(
    val owner: Any,
    val requestVersion: Long,
    val deferred: Deferred<*>,
)

private val cacheDeferred = MutableStateFlow<Map<String, CacheDeferredEntry>>(emptyMap())

@Suppress("UNCHECKED_CAST")
internal fun <T> getCacheDeferred(cacheKey: String) = cacheDeferred.value[cacheKey]?.deferred as? Deferred<T>

private fun registerCompletionCleanup(cacheKey: String, entry: CacheDeferredEntry) {
    entry.deferred.invokeOnCompletion {
        cacheDeferred.update { currentCache ->
            if (currentCache[cacheKey] === entry) currentCache - cacheKey else currentCache
        }
    }
}

internal fun setCacheDeferred(cacheKey: String, promise: Deferred<*>) {
    val entry = CacheDeferredEntry(
        owner = promise,
        requestVersion = 0,
        deferred = promise,
    )
    cacheDeferred.update { currentCache -> currentCache + (cacheKey to entry) }
    registerCompletionCleanup(cacheKey, entry)
}

/**
 * 原子获取或注册同一 cacheKey 的 Deferred。
 *
 * 当已注册的 Deferred 属于其他 owner 时直接复用。同一 owner 仅允许更高 requestVersion 原子替换，
 * 相同或更低版本必须复用已经注册的 Deferred。候选 Deferred 必须以 LAZY 模式创建，调用方只启动最终注册成功的实例。
 */
@Suppress("UNCHECKED_CAST")
internal fun <T> acquireCacheDeferred(
    cacheKey: String,
    owner: Any,
    requestVersion: Long,
    candidate: Deferred<T>,
): Deferred<T> {
    val candidateEntry = CacheDeferredEntry(owner, requestVersion, candidate)
    while (true) {
        val currentCache = cacheDeferred.value
        val existing = currentCache[cacheKey]
        if (
            existing != null &&
            (existing.owner !== owner || existing.requestVersion >= requestVersion)
        ) {
            return existing.deferred as Deferred<T>
        }
        if (cacheDeferred.compareAndSet(currentCache, currentCache + (cacheKey to candidateEntry))) {
            registerCompletionCleanup(cacheKey, candidateEntry)
            return candidate
        }
    }
}

/**
 * 等待共享 Deferred。全局注册项由 Deferred 自身完成时清理，取消单个等待者不会影响其他等待者。
 */
internal suspend fun <T> Deferred<T>.awaitPlus(): T = await()
