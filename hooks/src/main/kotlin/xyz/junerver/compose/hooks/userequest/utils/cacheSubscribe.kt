package xyz.junerver.compose.hooks.userequest.utils

/*
  Description:
  Author: Junerver
  Date: 2024/2/23-9:36
  Email: junerver@gmail.com
  Version: v1.0
*/

private typealias CachedDataChangeListener = (data: RestoreFetchStateData) -> Unit

/**
 * 缓存回调，key-listener列表，同一个 cacheKey 可以有多个回调函数。
 */
private val listeners: MutableMap<String, MutableList<CachedDataChangeListener>> = mutableMapOf()

/**
 * 用于触发缓存回调的网络请求数据缓存，同时缓存了 [loading]、[error]、[data]
 */
internal data class RestoreFetchStateData(
    val loading: Boolean? = null,
    val error: Throwable? = null,
    val data: Any? = null,
)

/**
 * 触发初测在缓存回调map[listeners]中的回调函数
 */
internal fun trigger(key: String, data: RestoreFetchStateData) {
    if (listeners.containsKey(key)) {
        listeners[key]?.forEach { item -> item(data) }
    }
}

/**
 * 将 cacheKey-回调函数，注册到监听器列表，返回值是反订阅函数，只要指定返回函数就可以取消订阅
 */
internal fun subscribe(key: String, listener: CachedDataChangeListener): () -> Unit {
    listeners[key] ?: run { listeners[key] = mutableListOf() }
    listeners[key]?.add(listener)
    return fun() {
        listeners[key]?.remove(listener)
    }
}
