package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable

/*
  Description:Execute `fn` function when component is mounted
  Author: Junerver
  Date: 2024/1/25-8:26
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun useMount(fn: SuspendAsyncFn) = useEffect(Unit) { fn() }
