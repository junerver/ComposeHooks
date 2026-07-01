/*
 * Copyright (c) 2024. ComposeHooks project
 *
 * Description: wasmJs persistent storage actual backed by browser localStorage.
 * Author: Junerver
 * Date: 2026/07/01
 * Email: junerver@gmail.com
 */
package xyz.junerver.composehooks

import kotlinx.browser.localStorage

/**
 * wasmJs actual for [getKVDelegate].
 *
 * The browser sandbox has no process-wide filesystem, so the file/properties-backed delegate
 * used on desktop (and the MMKV delegate on android/ios) is unavailable. We fall back to the
 * browser's `localStorage` — a synchronous string key/value store that survives page reloads
 * within the same origin. Values are stored as their [Any.toString] form (matching the desktop
 * implementation's behaviour) and coerced back to the default value's type on read.
 */
actual fun getKVDelegate(): KeyValueStoreDelegate = LocalStorageDelegate

private object LocalStorageDelegate : KeyValueStoreDelegate {
    override fun <T> saveData(key: String, data: T) {
        localStorage.setItem(key, data?.toString().orEmpty())
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> readData(key: String, default: T): T {
        val value = localStorage.getItem(key) ?: return default
        return when (default) {
            is Int -> value.toIntOrNull() ?: default
            is Long -> value.toLongOrNull() ?: default
            is Double -> value.toDoubleOrNull() ?: default
            is Float -> value.toFloatOrNull() ?: default
            is Boolean -> value.toBooleanStrictOrNull() ?: default
            is String -> value
            else -> error("wrong type of default value！")
        } as T
    }

    override fun remove(key: String) {
        localStorage.removeItem(key)
    }
}
