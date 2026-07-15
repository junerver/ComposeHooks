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

private fun registerCompletionCleanup(cacheKey: String, promise: Deferred<*>) {
    promise.invokeOnCompletion {
        cacheDeferred.update { currentCache ->
            if (currentCache[cacheKey] === promise) currentCache - cacheKey else currentCache
        }
    }
}

internal fun setCacheDeferred(cacheKey: String, promise: Deferred<*>) {
    cacheDeferred.update { currentCache -> currentCache + (cacheKey to promise) }
    registerCompletionCleanup(cacheKey, promise)
}

/**
 * 原子获取或注册同一 cacheKey 的 Deferred。
 *
 * 当已注册的 Deferred 不属于当前 Holder 时直接复用；当它就是当前 Holder 的请求时，允许候选请求原子替换，
 * 保留同一 Holder 主动再次请求的既有语义。候选 Deferred 必须以 LAZY 模式创建，调用方只启动最终注册成功的实例。
 */
@Suppress("UNCHECKED_CAST")
internal fun <T> acquireCacheDeferred(
    cacheKey: String,
    currentPromise: Deferred<*>?,
    candidate: Deferred<T>,
): Deferred<T> {
    while (true) {
        val currentCache = cacheDeferred.value
        val existing = currentCache[cacheKey] as? Deferred<T>
        if (existing != null && existing !== currentPromise) {
            return existing
        }
        if (cacheDeferred.compareAndSet(currentCache, currentCache + (cacheKey to candidate))) {
            registerCompletionCleanup(cacheKey, candidate)
            return candidate
        }
    }
}

/**
 * 等待共享 Deferred。全局注册项由 Deferred 自身完成时清理，取消单个等待者不会影响其他等待者。
 */
internal suspend fun <T> Deferred<T>.awaitPlus(): T = await()
