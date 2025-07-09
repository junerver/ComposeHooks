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

/**
 * A state class for managing undo/redo functionality.
 *
 * This class maintains the history of values with past, present, and future states,
 * allowing for undo and redo operations.
 *
 * @property past The list of previous values
 * @property present The current value
 * @property future The list of future values (after undo)
 */
data class UndoState<T>(
    var past: PersistentList<T> = persistentListOf(),
    var present: T,
    var future: PersistentList<T> = persistentListOf(),
)

/** Sealed interface for undo/redo actions */
private sealed interface UndoAction

/** Action to perform an undo operation */
private data object Undo : UndoAction

/** Action to perform a redo operation */
private data object Redo : UndoAction

/** Action to set a new value */
private data class Set<S>(val payload: S) : UndoAction

/** Action to reset the state to a new value */
private data class Reset<S>(val payload: S) : UndoAction

/**
 * Reducer function for handling undo/redo actions.
 *
 * This function manages the state transitions for undo, redo, set, and reset operations.
 *
 * @param preState The current state before the action
 * @param action The action to perform
 * @return The new state after the action
 */
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
                future = persistentListOf(present) + future,
            )
        }

        Redo -> {
            if (future.isEmpty()) return preState
            preState.copy(
                past = past + present,
                present = future[0],
                future = future.mutate {
                    it.removeFirst()
                },
            )
        }

        is Set<*> -> {
            val (payload) = action as Set<T>
            if (present === payload) return preState
            preState.copy(
                past = past + present,
                present = payload!!,
                future = persistentListOf(),
            )
        }

        is Reset<*> -> {
            val (payload) = action as Reset<T>
            preState.copy(
                past = persistentListOf(),
                present = payload!!,
                future = persistentListOf(),
            )
        }
    }
}

/**
 * A hook for implementing undo/redo functionality.
 *
 * This hook provides a way to manage a value with undo and redo capabilities.
 * It's useful for implementing features like text editing history or form state management.
 *
 * @param initialPresent The initial value
 * @return An [UndoHolder] containing the state and control functions
 *
 * @example
 * ```kotlin
 * val (state, setValue, resetValue, undo, redo, canUndo, canRedo) = useUndo("")
 *
 * // Update value
 * setValue("New value")
 *
 * // Undo last change
 * if (canUndo()) {
 *     undo()
 * }
 *
 * // Redo last undone change
 * if (canRedo()) {
 *     redo()
 * }
 *
 * // Reset to initial state
 * resetValue("")
 *
 * // Display current value
 * Text(state.value.present)
 * ```
 */
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

/**
 * A holder class for undo/redo state and control functions.
 *
 * This class provides access to the undo/redo state and functions for controlling
 * the undo/redo functionality.
 *
 * @param undoState The current undo/redo state
 * @param setValue Function to set a new value
 * @param resetValue Function to reset the state
 * @param undo Function to perform an undo operation
 * @param redo Function to perform a redo operation
 * @param canUndo Function to check if undo is available
 * @param canRedo Function to check if redo is available
 */
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
