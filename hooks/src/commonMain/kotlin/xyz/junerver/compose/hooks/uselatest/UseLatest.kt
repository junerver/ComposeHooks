package xyz.junerver.compose.hooks.uselatest

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import xyz.junerver.compose.hooks.useref.Ref
import xyz.junerver.compose.hooks.useref.useRefImpl

/*
  Description: Hook that returns the latest value can avoid closure problems when using destructuring.
  Author: Junerver
  Date: 2024/2/21-8:45
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun <T> useLatestRefImpl(value: T): Ref<T> = useRefImpl(default = value).apply { current = value }

@Composable
fun <T> useLatestStateImpl(value: T): State<T> = rememberUpdatedState(value)
