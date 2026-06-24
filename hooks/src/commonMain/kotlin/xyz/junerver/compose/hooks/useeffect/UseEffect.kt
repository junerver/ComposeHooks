package xyz.junerver.compose.hooks.useeffect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import xyz.junerver.compose.hooks.SuspendAsyncFn
import xyz.junerver.compose.hooks.utils.unwrap

/*
  Description: Alias for [LaunchedEffect]
  Author: Junerver
  Date: 2024/3/4-8:20
  Email: junerver@gmail.com
  Version: v1.0

  Update: 2024/9/18 10:50
  增加了解包装函数，对State、Ref进行解包，方便直接使用其实例作为依赖
*/

@Composable
fun useEffectImpl(vararg deps: Any?, block: SuspendAsyncFn) {
    LaunchedEffect(keys = unwrap(deps), block = block)
}
