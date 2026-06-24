package xyz.junerver.compose.hooks.usemount

import androidx.compose.runtime.Composable
import xyz.junerver.compose.hooks.SuspendAsyncFn
import xyz.junerver.compose.hooks.useeffect.useEffectImpl

/*
  Description:Execute `fn` function when component is mounted
  Author: Junerver
  Date: 2024/1/25-8:26
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun useMountImpl(block: SuspendAsyncFn) = useEffectImpl(Unit) { block() }
