package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import arrow.core.Tuple4

/*
  Description: [useGetState] that provides [reset]
  Author: Junerver
  Date: 2024/7/9-14:19
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun<T> useResetState(default: T & Any): Tuple4<T, SetValueFn<T & Any>, GetValueFn<T>, ResetFn> {
    val (state, setState, getState) = useGetState(default)
    fun reset() {
        setState(default)
    }
    return Tuple4(
        state,
        setState,
        getState,
        ::reset
    )
}
