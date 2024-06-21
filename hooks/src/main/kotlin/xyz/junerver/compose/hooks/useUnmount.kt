package xyz.junerver.compose.hooks

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

/*
  Description: 组件卸载时执行
  @author Junerver
  date: 2024/1/26-13:29
  Email: junerver@gmail.com
  Version: v1.0
*/
@SuppressLint("ComposableNaming")
@Composable
fun useUnmount(block: () -> Unit) = DisposableEffect(Unit) {
    onDispose {
        block()
    }
}
