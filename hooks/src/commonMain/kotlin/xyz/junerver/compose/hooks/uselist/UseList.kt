package xyz.junerver.compose.hooks.uselist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import xyz.junerver.compose.hooks.usestate.useStateImpl

/*
  Description: More convenient to use dynamic List state
  Author: Junerver
  Date: 2024/3/7-15:02
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun <T> useListImpl(elements: Collection<T>): SnapshotStateList<T> = remember {
    elements.toMutableStateList()
}

@Composable
fun <T> useListImpl(vararg elements: T): SnapshotStateList<T> = remember {
    elements.asList().toMutableStateList()
}

/**
 * Reactive List.reduce.
 */
@Composable
fun <S, T : S> useListReduceImpl(list: List<T>, operation: (acc: S, T) -> S): State<S?> {
    val currentList = useIdentityState(list)
    val currentOperation = useIdentityState(operation)
    return useStateImpl {
        currentList.value.reduceOrNull(currentOperation.value)
    }
}

@Composable
private fun <T> useIdentityState(value: T): State<T> {
    val state = remember { mutableStateOf(value, referentialEqualityPolicy()) }
    state.value = value
    return state
}
