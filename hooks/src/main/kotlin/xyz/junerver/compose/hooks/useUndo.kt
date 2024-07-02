package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import xyz.junerver.kotlin.Tuple7
import xyz.junerver.kotlin.tuple

/*
   Description:
   @author Junerver
   date: 2024/1/26-14:21
   Email: junerver@gmail.com
   Version: v1.0
*/

data class UndoState<T>(
    var past: List<T> = emptyList(),
    var present: T,
    var future: List<T> = emptyList(),
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
            val newPresent = past[past.size - 1]
            val newPast = past.dropLast(1)
            preState.copy(
                past = newPast,
                present = newPresent,
                future = listOf(present) + future
            )
        }
        Redo -> {
            if (future.isEmpty()) return preState
            val newPresent = future[0]
            val newFuture = future.drop(1)
            preState.copy(
                past = past + listOf(present),
                present = newPresent,
                future = newFuture
            )
        }

        is Set<*> -> {
            val (payload) = action as Set<T>
            if (present === payload) return preState
            preState.copy(
                past = past + listOf(present),
                present = payload!!,
                future = emptyList()
            )
        }

        is Reset<*> -> {
            val (payload) = action as Reset<T>
            preState.copy(
                past = emptyList(),
                present = payload!!,
                future = emptyList()
            )
        }
    }
}

internal typealias ResetValueFn<T> = (T) -> Unit
internal typealias RedoFn = () -> Unit
internal typealias UndoFn = () -> Unit
internal typealias CanUndo = Boolean
internal typealias CanRedo = Boolean

@Composable
fun <T> useUndo(initialPresent: T): Tuple7<UndoState<T>, SetValueFn<T>, ResetValueFn<T>, UndoFn, RedoFn, CanUndo, CanRedo> {
    val (state, dispatch) = useReducer(::undoReducer, UndoState(present = initialPresent))
    val canUndo = state.past.isNotEmpty()
    val canRedo = state.future.isNotEmpty()
    val undo = { dispatch(Undo) }
    val redo = { dispatch(Redo) }
    val set = { newPresent: T -> dispatch(Set(newPresent)) }
    val reset = { newPresent: T -> dispatch(Reset(newPresent)) }

    return tuple(
        first = state,
        second = set,
        third = reset,
        fourth = undo,
        fifth = redo,
        sixth = canUndo,
        seventh = canRedo
    )
}
