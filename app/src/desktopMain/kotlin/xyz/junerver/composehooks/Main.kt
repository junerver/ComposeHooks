package xyz.junerver.composehooks

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import xyz.junerver.compose.hooks.KeyPressDelegate

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        alwaysOnTop = true,
        title = "ComposeHooks",
        onKeyEvent = { KeyPressDelegate.onKeyEvent(it) },
    ) {
        App()
    }
}
