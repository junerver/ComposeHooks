package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import xyz.junerver.compose.hooks.utils.unwrap

/*
  Description: Alias for [LaunchedEffect]
  Author: Junerver
  Date: 2024/3/4-8:20
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun useEffect(vararg deps: Any?, block: SuspendAsyncFn) {
    LaunchedEffect(keys = unwrap(deps), block = block)
}
