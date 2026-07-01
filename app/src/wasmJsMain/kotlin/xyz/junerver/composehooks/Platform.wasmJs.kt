/*
 * Copyright (c) 2024. ComposeHooks project
 *
 * Description: wasmJs platform actual for getPlatform().
 * Author: Junerver
 * Date: 2026/07/01
 * Email: junerver@gmail.com
 */
package xyz.junerver.composehooks

class WasmJsPlatform : Platform {
    override val name: String = "Web (Kotlin/WasmJs)"
}

actual fun getPlatform(): Platform = WasmJsPlatform()
