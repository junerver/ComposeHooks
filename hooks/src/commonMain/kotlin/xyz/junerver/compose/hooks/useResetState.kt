package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember

/*
  Description: [useGetState] that provides [reset]
  Author: Junerver
  Date: 2024/7/9-14:19
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun <T> useResetState(default: T & Any): ResetStateHolder<T & Any> {
    val (state, setState, getState) = useGetState(default)

    fun reset() {
        setState(default)
    }
    return remember { ResetStateHolder(state, setState, getState, ::reset) }
}

@Stable
data class ResetStateHolder<T>(
    val state: State<T>,
    val setValue: SetValueFn<SetterEither<T>>,
    val getValue: GetValueFn<T>,
    val reset: ResetFn,
)
