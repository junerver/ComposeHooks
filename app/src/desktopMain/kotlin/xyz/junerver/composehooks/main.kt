package xyz.junerver.composehooks

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        alwaysOnTop = true,
        title = "ComposeHooks",
    ) {
        App()
    }
}
