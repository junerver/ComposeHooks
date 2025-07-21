package xyz.junerver.composehooks.route

import androidx.compose.runtime.Composable
import xyz.junerver.composehooks.example.AsNoopFn

actual fun getAndroidRoutes(): Map<String, @Composable () -> Unit> = mapOf()

actual fun getSubRequestRoutes(): Map<String, @Composable () -> Unit> = mapOf(
    "ext: asSuspendNoopFn" to { AsNoopFn() },
)
