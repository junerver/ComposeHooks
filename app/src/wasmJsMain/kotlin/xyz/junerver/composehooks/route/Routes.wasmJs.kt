/*
 * Copyright (c) 2024. ComposeHooks project
 *
 * Description: wasmJs route actuals — no platform-specific hooks on the web target.
 * Author: Junerver
 * Date: 2026/07/01
 * Email: junerver@gmail.com
 */
package xyz.junerver.composehooks.route

import androidx.compose.runtime.Composable

// Platform-specific hooks (useBiometric, useNetwork, useKeyPress, asSuspendNoopFn, …) are
// bound to android/desktop capabilities that the browser sandbox does not expose, so the wasmJs
// build registers no extra demo routes. All commonMain hooks remain available via `routes`.
actual fun getPlatformSpecialRoutes(): Map<String, @Composable () -> Unit> = mapOf()

actual fun getSubRequestRoutes(): Map<String, @Composable () -> Unit> = mapOf()
