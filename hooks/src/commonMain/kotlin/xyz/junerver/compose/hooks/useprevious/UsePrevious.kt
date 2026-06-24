package xyz.junerver.compose.hooks.useprevious

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import xyz.junerver.compose.hooks._useState
import xyz.junerver.compose.hooks.useeffect.useEffectImpl
import xyz.junerver.compose.hooks.useRef

/*
  Description: Hook that saves the previous state.
  Author: Junerver
  Date: 2024/2/1-14:55
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun <T> usePreviousImpl(present: T): State<T?> {
    val previousRef = useRef<T?>(default = null)
    val previousState = _useState<T?>(default = null)
    useEffectImpl(present) {
        previousState.value = previousRef.current
        previousRef.current = present
    }
    return previousState
}
