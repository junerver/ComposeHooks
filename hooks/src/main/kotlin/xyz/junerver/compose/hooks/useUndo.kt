package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import java.io.Serializable
import xyz.junerver.kotlin.Tuple7
import xyz.junerver.kotlin.tuple

/**
 * Description:
 * @author Junerver
 * date: 2024/1/26-14:21
 * Email: junerver@gmail.com
 * Version: v1.0
 */

data class UndoState<T>(
    // 过去的状态
    var past: List<T> = emptyList(),
    var present: T,
    var future: List<T> = emptyList(),
) : Serializable

sealed interface UndoAction<S>
internal data object Undo : UndoAction<Nothing>
internal data object Redo : UndoAction<Nothing>
internal data class Set<S>(val payload: S) : UndoAction<S>
internal data class Reset<S>(val payload: S) : UndoAction<S>

@Suppress("UNCHECKED_CAST")
fun <T> undoReducer(preState: UndoState<T>, action: Any): UndoState<T> {
    val (past, present, future) = preState
    return when (action) {
        // 撤销
        Undo -> {
            if (past.isEmpty()) return preState
            // 取出过去操作中的最后一个操作作为新的present
            val newPresent = past[past.size - 1]
            // 将最后一个操作从past中移除
            val newPast = past.dropLast(1)
            preState.copy(
                // 将新的present和新的past设置到state中
                past = newPast,
                present = newPresent,
                // 将旧的present放入到future数组的头部
                future = listOf(present) + future
            )
        }
        // 重做
        Redo -> {
            if (future.isEmpty()) return preState
            // 取出第一个future
            val newPresent = future[0]
            // 将第一个future从future中删除 slice 函数可以用于剪裁\复制数组
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

        else -> preState
    }
}

@Composable
fun <T> useUndo(initialPresent: T): Tuple7<UndoState<T>, (T) -> Unit, (T) -> Unit, () -> Unit, () -> Unit, Boolean, Boolean> {
    val (state, dispatch) = useReducer(::undoReducer, UndoState(present = initialPresent))
    // 是否可以撤销
    val canUndo = state.past.isNotEmpty()
    // 是否可以重做
    val canRedo = state.future.isNotEmpty()
    // undo相关的四个函数
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
