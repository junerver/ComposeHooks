package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

/*
  Description: Alias for [LaunchedEffect]
  Author: Junerver
  Date: 2024/3/4-8:20
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun useEffect(vararg deps: Any?, block: SuspendAsyncFn) =
    LaunchedEffect(keys = deps, block = block)
