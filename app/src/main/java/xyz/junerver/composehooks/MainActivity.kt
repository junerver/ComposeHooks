package xyz.junerver.composehooks

import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.tencent.mmkv.MMKV
import xyz.junerver.compose.hooks.notifyDefaultPersistentObserver

val mmkv = MMKV.defaultMMKV()

fun mmkvSave(key: String, value: Any?) {
    when (value) {
        is Int -> mmkv.encode(key, value)
        is Long -> mmkv.encode(key, value)
        is Double -> mmkv.encode(key, value)
        is Float -> mmkv.encode(key, value)
        is Boolean -> mmkv.encode(key, value)
        is String -> mmkv.encode(key, value)
        is ByteArray -> mmkv.encode(key, value)
        is Parcelable -> mmkv.encode(key, value)
    }
    notifyDefaultPersistentObserver(key)
}

@Suppress("IMPLICIT_CAST_TO_ANY")
fun mmkvGet(key: String, value: Any): Any {
    return when (value) {
        is Int -> mmkv.decodeInt(key, value)
        is Long -> mmkv.decodeLong(key, value)
        is Double -> mmkv.decodeDouble(key, value)
        is Float -> mmkv.decodeFloat(key, value)
        is Boolean -> mmkv.decodeBool(key, value)
        is String -> mmkv.decodeString(key, value)
        is ByteArray -> mmkv.decodeBytes(key, value)
        is Parcelable -> mmkv.decodeParcelable(key, value.javaClass)
        else -> error("wrong type of default value！")
    } as Any
}

fun mmkvClear(key: String) {
    mmkv.remove(key)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}

