package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State

/*
  Description:
  Author: Junerver
  Date: 2025/7/15-13:36
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun <T> useControllable(default: T & Any): ControllableHolder<T & Any> {
    val (state, setState, getState) = useGetState(default)
    return ControllableHolder(state, setState.left(), getState)
}

@Composable
fun <T> _useControllable(default: T): ControllableHolder<T> {
    val (state, setState, getState) = _useGetState(default)
    return ControllableHolder(state, setState.left(), getState)
}

@Stable
data class ControllableHolder<T>(
    val state: State<T>,
    val setValue: SetValueFn<T>,
    val getValue: GetValueFn<T>,
)
