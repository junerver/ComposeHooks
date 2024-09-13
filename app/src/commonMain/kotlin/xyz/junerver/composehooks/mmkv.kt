package xyz.junerver.composehooks

// import com.ctrip.flight.mmkv.defaultMMKV

/*
  Description:
  Author: Junerver
  Date: 2024/9/11-11:02
  Email: junerver@gmail.com
  Version: v1.0
*/

val storeDelegate by lazy {
    getKVDelegate()
}

fun mmkvSave(key: String, value: Any?) {
    storeDelegate.saveData(key, value)
}

fun mmkvGet(key: String, value: Any): Any = storeDelegate.readData(key, value)

fun mmkvClear(key: String) {
    storeDelegate.remove(key)
}

expect fun getKVDelegate(): KeyValueStoreDelegate

interface KeyValueStoreDelegate {
    fun <T> saveData(key: String, data: T)

    fun <T> readData(key: String, default: T): T

    fun remove(key: String)
}
