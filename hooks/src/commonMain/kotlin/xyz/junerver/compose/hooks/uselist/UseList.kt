package xyz.junerver.compose.hooks.uselist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import xyz.junerver.compose.hooks.useState

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
    val state = remember { mutableStateOf<S?>(null) }
    useState(list) {
        state.value = if (list.isEmpty()) null else list.reduce(operation)
    }
    return state
}
