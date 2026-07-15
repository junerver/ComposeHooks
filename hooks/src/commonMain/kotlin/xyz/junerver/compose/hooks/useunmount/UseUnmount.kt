package xyz.junerver.compose.hooks.useunmount

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import xyz.junerver.compose.hooks.uselatest.useLatestRefImpl

/*
  Description: 组件卸载时执行
  Author: Junerver
  Date: 2024/1/26-13:29
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun useUnmountImpl(block: () -> Unit) {
    val blockRef = useLatestRefImpl(block)
    DisposableEffect(Unit) {
        onDispose { blockRef.current() }
    }
}
