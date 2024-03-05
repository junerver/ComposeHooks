package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import xyz.junerver.kotlin.Tuple7
import xyz.junerver.kotlin.tuple
import java.io.Serializable

/**
 * Description:
 * @author Junerver
 * date: 2024/1/26-14:21
 * Email: junerver@gmail.com
 * Version: v1.0
 */

data class UndoState<T>(
    //过去的状态
    var past: List<T> = emptyList(),
    var present: T,
    var future: List<T> = emptyList(),
) : Serializable

enum class UndoActionType { Undo, Redo, Set, Reset, }
data class UndoAction<S>(override val type: UndoActionType, override val payload: S? = null) :
    Action(type, payload) {
    companion object {
        fun undo() = UndoAction<Nothing>(UndoActionType.Undo)
        fun redo() = UndoAction<Nothing>(UndoActionType.Redo)

        fun <T> set(payload: T) = UndoAction(UndoActionType.Set, payload)
        fun <T> reset(payload: T) = UndoAction(UndoActionType.Reset, payload)
    }

}

@Suppress("UNCHECKED_CAST")
fun <T> undoReducer(preState: UndoState<T>, action: Action): UndoState<T> {
    val (past, present, future) = preState
    // 此处明确Action为其子类型[UndoAction]
    val (type, payload) = action as UndoAction<T>
    return when (type) {
        // 撤销
        UndoActionType.Undo -> {
            if (past.isEmpty()) return preState
            // 取出过去操作中的最后一个操作作为新的present
            val newPresent = past[past.size - 1]
            // 将最后一个操作从past中移除
            val newPast = past.dropLast(1)
            preState.copy(
                // 将新的present和新的past设置到state中
                past = newPast,
                present = newPresent,
                //将旧的present放入到future数组的头部
                future = listOf(present) + future
            )
        }
        // 重做
        UndoActionType.Redo -> {
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

        UndoActionType.Set -> {
            if (present === payload) return preState
            preState.copy(
                past = past + listOf(present),
                present = payload!!,
                future = emptyList()
            )
        }

        UndoActionType.Reset -> {
            preState.copy(
                past = emptyList(),
                present = payload!!,
                future = emptyList()
            )
        }
    }
}

@Composable
fun <T> useUndo(initialPresent: T): Tuple7<UndoState<T>, (T) -> Unit, (T) -> Unit, () -> Unit, () -> Unit, Boolean, Boolean> {
    val (state, dispatch) = useReducer(::undoReducer, UndoState(present = initialPresent))
    //是否可以撤销
    val canUndo = state.past.isNotEmpty()
    //是否可以重做
    val canRedo = state.future.isNotEmpty()
    // undo相关的四个函数
    val undo = { dispatch(UndoAction.undo()) }
    val redo = { dispatch(UndoAction.redo()) }
    val set = { newPresent: T -> dispatch(UndoAction.set(newPresent)) }
    val reset = { newPresent: T -> dispatch(UndoAction.reset(newPresent)) }

    return tuple(
        first = state,
        second = set,
        third = reset,
        fourth = undo,
        fifth = redo,
        sixth = canUndo,
        seventh = canRedo,
    )
}
