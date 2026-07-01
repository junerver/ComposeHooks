/*
 * Copyright (c) 2024. ComposeHooks project
 *
 * Description: WASM entry point for the :app web target.
 * Author: Junerver
 * Date: 2026/07/01
 * Email: junerver@gmail.com
 */
package xyz.junerver.composehooks

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

/**
 * WASM (browser) entry point for the :app module.
 *
 * Renders the **same [App] the android, desktop, and ios builds use** — the full hooks demo
 * with the sidebar, theme, and every hook showcase. This makes :app a four-platform sample
 * (android + desktop + ios + web) sharing one codebase.
 *
 * [WithWebFont] loads Noto Sans SC so CJK glyphs render in the browser (Skiko has no system
 * font fallback on wasmJs).
 *
 * Uses [ComposeViewport] (the Compose 1.9+ web entry point; `CanvasBasedWindow` is deprecated
 * and removed in Compose 1.11).
 *
 * @see <a href="https://github.com/JetBrains/compose-multiplatform/blob/master/examples/nav_cupcake/composeApp/src/wasmJsMain/kotlin/main.kt">Official entry-point pattern</a>
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport("ComposeTarget") {
        WithWebFont {
            App()
        }
    }
}
