package xyz.junerver.composehooks

import com.ctrip.flight.mmkv.defaultMMKV

actual fun getKVDelegate(): KeyValueStoreDelegate {
    val mmkv = defaultMMKV()
    return object : KeyValueStoreDelegate {
        override fun <T> saveData(key: String, data: T) {
            when (data) {
                is Int -> mmkv[key] = data
                is Long -> mmkv[key] = data
                is Double -> mmkv[key] = data
                is Float -> mmkv[key] = data
                is Boolean -> mmkv[key] = data
                is String -> mmkv[key] = data
            }
        }

        override fun <T> readData(key: String, default: T): T = when (default) {
            is Int -> mmkv.takeInt(key, default)
            is Long -> mmkv.takeLong(key, default)
            is Double -> mmkv.takeDouble(key, default)
            is Float -> mmkv.takeFloat(key, default)
            is Boolean -> mmkv.takeBoolean(key, default)
            is String -> mmkv.takeString(key, default)
            else -> error("wrong type of default value！")
        } as T

        override fun remove(key: String) {
            mmkv.removeValueForKey(key)
        }
    }
}
