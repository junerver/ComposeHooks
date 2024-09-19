package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus

/*
  Description:
  Author: Junerver
  Date: 2024/1/26-14:21
  Email: junerver@gmail.com
  Version: v1.0
*/

data class UndoState<T>(
    var past: PersistentList<T> = persistentListOf(),
    var present: T,
    var future: PersistentList<T> = persistentListOf(),
)

private sealed interface UndoAction

private data object Undo : UndoAction

private data object Redo : UndoAction

private data class Set<S>(val payload: S) : UndoAction

private data class Reset<S>(val payload: S) : UndoAction

@Suppress("UNCHECKED_CAST")
private fun <T> undoReducer(preState: UndoState<T>, action: UndoAction): UndoState<T> {
    val (past, present, future) = preState
    return when (action) {
        Undo -> {
            if (past.isEmpty()) return preState
            preState.copy(
                past = past.mutate {
                    it.removeLast()
                },
                present = past[past.size - 1],
                future = persistentListOf(present) + future
            )
        }

        Redo -> {
            if (future.isEmpty()) return preState
            preState.copy(
                past = past + present,
                present = future[0],
                future = future.mutate {
                    it.removeFirst()
                }
            )
        }

        is Set<*> -> {
            val (payload) = action as Set<T>
            if (present === payload) return preState
            preState.copy(
                past = past + present,
                present = payload!!,
                future = persistentListOf()
            )
        }

        is Reset<*> -> {
            val (payload) = action as Reset<T>
            preState.copy(
                past = persistentListOf(),
                present = payload!!,
                future = persistentListOf()
            )
        }
    }
}

@Composable
fun <T> useUndo(initialPresent: T): UndoHolder<T> {
    val (state, dispatch) = useReducer(::undoReducer, UndoState(present = initialPresent))
    val canUndo = useState { state.value.past.isNotEmpty() }
    val canRedo = useState { state.value.future.isNotEmpty() }
    val undo = { dispatch(Undo) }
    val redo = { dispatch(Redo) }
    val set = { newPresent: T -> dispatch(Set(newPresent)) }
    val reset = { newPresent: T -> dispatch(Reset(newPresent)) }

    return remember { UndoHolder(state, set, reset, undo, redo, canUndo, canRedo) }
}

@Stable
data class UndoHolder<T>(
    val undoState: State<UndoState<T>>,
    val setValue: SetValueFn<T>,
    val resetValue: ResetValueFn<T>,
    val undo: UndoFn,
    val redo: RedoFn,
    val canUndo: CanUndo,
    val canRedo: CanRedo,
)
