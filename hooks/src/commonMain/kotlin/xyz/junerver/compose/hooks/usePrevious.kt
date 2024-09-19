package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

/*
  Description: Hook that saves the previous state.
  Author: Junerver
  Date: 2024/2/1-14:55
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun <T> usePrevious(present: T): State<T?> {
    val (state, set) = useUndo(initialPresent = present)
    useEffect(present) {
        set(present)
    }
    return useState { state.value.past.lastOrNull() }
}
