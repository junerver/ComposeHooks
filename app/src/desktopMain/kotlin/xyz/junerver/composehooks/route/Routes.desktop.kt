package xyz.junerver.composehooks.route

import androidx.compose.runtime.Composable
import xyz.junerver.composehooks.example.AsNoopFn
import xyz.junerver.composehooks.example.UseKeyPressExample

actual fun getPlatformSpecialRoutes(): Map<String, @Composable () -> Unit> = mapOf(
    "useKeyPress" to { UseKeyPressExample() },
)

actual fun getSubRequestRoutes(): Map<String, @Composable () -> Unit> = mapOf(
    "ext: asSuspendNoopFn" to { AsNoopFn() },
)
