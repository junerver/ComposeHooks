package xyz.junerver.compose.hooks.usecontrollable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import xyz.junerver.compose.hooks.GetValueFn
import xyz.junerver.compose.hooks.SetValueFn
import xyz.junerver.compose.hooks.left
import xyz.junerver.compose.hooks.usegetstate._useGetStateImpl
import xyz.junerver.compose.hooks.usegetstate.useGetStateImpl

/*
  Description:
  Author: Junerver
  Date: 2025/7/15-13:36
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun <T> useControllableImpl(default: T & Any): ControllableHolder<T & Any> {
    val (state, setState, getState) = useGetStateImpl(default)
    return remember(state, setState, getState) {
        ControllableHolder(state, setState.left(), getState)
    }
}

@Composable
fun <T> _useControllableImpl(default: T): ControllableHolder<T> {
    val (state, setState, getState) = _useGetStateImpl(default)
    return remember(state, setState, getState) {
        ControllableHolder(state, setState.left(), getState)
    }
}

@Stable
data class ControllableHolder<T>(
    val state: State<T>,
    val setValue: SetValueFn<T>,
    val getValue: GetValueFn<T>,
)
