package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState

/**
 * Description: Hook that returns the latest value can avoid closure problems when using destructuring.
 *
 * @author Junerver
 * date: 2024/2/21-8:45
 * Email: junerver@gmail.com
 * Version: v1.0
 */
@Composable
fun <T> useLatestRef(value: T): Ref<T> = useRef(default = value).apply { current = value }

/**
 * Description: Alias for [rememberUpdatedState]
 *
 * @param T
 * @param value
 * @return
 */
@Composable
fun <T> useLatestState(value: T): State<T> = rememberUpdatedState(value)
