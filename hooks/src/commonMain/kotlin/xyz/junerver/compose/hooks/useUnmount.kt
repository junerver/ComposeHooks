package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

/*
  Description: 组件卸载时执行
  Author: Junerver
  Date: 2024/1/26-13:29
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun useUnmount(block: () -> Unit) = DisposableEffect(Unit) {
    onDispose(block)
}
