package xyz.junerver.composehooks

import com.ctrip.flight.mmkv.defaultMMKV
import xyz.junerver.compose.hooks.notifyDefaultPersistentObserver

val mmkv = defaultMMKV()

actual fun mmkvSave(key: String, value: Any?) {
    when (value) {
        is Int -> mmkv.set(key, value)
        is Long -> mmkv.set(key, value)
        is Double -> mmkv.set(key, value)
        is Float -> mmkv.set(key, value)
        is Boolean -> mmkv.set(key, value)
        is String -> mmkv.set(key, value)
        is ByteArray -> mmkv.set(key, value)
    }
    notifyDefaultPersistentObserver(key)
}

actual fun mmkvGet(key: String, value: Any): Any {
    return when (value) {
        is Int -> mmkv.takeInt(key, value)
        is Long -> mmkv.takeLong(key, value)
        is Double -> mmkv.takeDouble(key, value)
        is Float -> mmkv.takeFloat(key, value)
        is Boolean -> mmkv.takeBoolean(key, value)
        is String -> mmkv.takeString(key, value)
        is ByteArray -> mmkv.takeByteArray(key, value)
        else -> error("wrong type of default value！")
    } as Any
}

actual fun mmkvClear(key: String) {
    mmkv.removeValueForKey(key)
}
