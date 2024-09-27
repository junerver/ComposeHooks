package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentListOf

/*
  Description: More convenient to use dynamic List state
  Author: Junerver
  Date: 2024/3/7-15:02
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun <T> useList(elements: Collection<T>): SnapshotStateList<T> = remember {
    elements.toMutableStateList()
}

@Composable
fun <T> useList(vararg elements: T): SnapshotStateList<T> = remember {
    mutableStateListOf(*elements)
}

private sealed interface ImmutableListAction

private data class Add<T>(val payload: T) : ImmutableListAction
private data class Remove<T>(val payload: T) : ImmutableListAction


private fun <T> immutableListReducer(preState: PersistentList<T>, action: ImmutableListAction): PersistentList<T> {
    return when (action) {
        is Add<*> -> preState.mutate {
            it.add(action.payload as T)
        }

        is Remove<*> -> preState.mutate {
            it.remove(action.payload as T)
        }
    }
}

@Composable
fun <T> useImmutableList(vararg elements: T): ImmutableListHolder<T> {
    val (state, dispatch) = useReducer(::immutableListReducer, persistentListOf(*elements))
    val add = { payload: T -> dispatch(Add(payload)) }
    val remove = { payload: T -> dispatch(Remove(payload)) }
    return remember(state, add, remove) {
        ImmutableListHolder(state, add, remove)
    }
}

@Stable
data class ImmutableListHolder<T>(
    val list: State<PersistentList<T>>,
    val add: (T) -> Unit,
    val remove: (T) -> Unit,
)
