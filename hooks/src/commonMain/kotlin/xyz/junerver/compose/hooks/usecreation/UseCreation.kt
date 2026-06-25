package xyz.junerver.compose.hooks.usecreation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import xyz.junerver.compose.hooks.useref.MutableRef
import xyz.junerver.compose.hooks.useref.Ref

/*
  Description:
  Author: Junerver
  Date: 2024/2/7-14:20
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun <T> useCreationImpl(vararg keys: Any?, factory: () -> T): Ref<T> = remember(keys = keys) {
    MutableRef(factory())
}
