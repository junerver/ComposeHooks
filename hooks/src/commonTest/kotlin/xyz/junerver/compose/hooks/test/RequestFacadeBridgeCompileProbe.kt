@file:Suppress("unused", "ComposableNaming")

package xyz.junerver.compose.hooks.test

import androidx.compose.runtime.Composable
import xyz.junerver.compose.hooks.RequestHolder
import xyz.junerver.compose.hooks.SuspendNormalFunction
import xyz.junerver.compose.hooks.rememberRequest
import xyz.junerver.compose.hooks.useRequest

/*
  Description: 根包请求 facade 的源码兼容性编译探针
  Author: Junerver
  Date: 2026/7/15
  Email: junerver@gmail.com
  Version: v1.0
*/
@Composable
private fun requestFacadeBridgeCompileProbe() {
    val canonical: SuspendNormalFunction<String, Int> = { it.length }
    val legacy: SuspendNormalFunction<String?, Int> = { it?.length ?: 0 }

    val canonicalUseRequest: RequestHolder<String, Int> = useRequest(canonical)
    val legacyUseRequest: RequestHolder<String, Int> = useRequest(legacy)
    val canonicalRememberRequest: RequestHolder<String, Int> = rememberRequest(canonical)
    val legacyRememberRequest: RequestHolder<String, Int> = rememberRequest(legacy)
}
