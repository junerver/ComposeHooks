package xyz.junerver.composehooks.route

import androidx.compose.runtime.Composable

actual fun getAndroidRoutes(): Map<String, @Composable () -> Unit> = mapOf()

actual fun getSubRequestRoutes(): Map<String, @Composable () -> Unit> = mapOf()
