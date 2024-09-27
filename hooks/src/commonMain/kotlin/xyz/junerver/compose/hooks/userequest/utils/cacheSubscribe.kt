package xyz.junerver.compose.hooks.userequest.utils

import xyz.junerver.compose.hooks.cacheKey
import xyz.junerver.compose.hooks.utils.HooksEventManager

/*
  Description:
  Author: Junerver
  Date: 2024/2/23-9:36
  Email: junerver@gmail.com
  Version: v1.0
*/

private typealias CachedDataChangeListener = (data: RestoreFetchStateData) -> Unit

/**
 * 用于触发缓存回调的网络请求数据缓存，同时缓存了 [loading]、[error]、[data]
 */
internal data class RestoreFetchStateData(
    val loading: Boolean? = null,
    val error: Throwable? = null,
    val data: Any? = null,
)

/**
 * 触发初测在缓存回调map中的回调函数
 */
internal fun trigger(key: String, data: RestoreFetchStateData) {
    HooksEventManager.post(key.cacheKey, data)
}

/**
 * 将 cacheKey-回调函数，注册到监听器列表，返回值是反订阅函数，只要指定返回函数就可以取消订阅
 */
internal fun subscribe(key: String, listener: CachedDataChangeListener): () -> Unit = HooksEventManager.register(key.cacheKey, listener)
