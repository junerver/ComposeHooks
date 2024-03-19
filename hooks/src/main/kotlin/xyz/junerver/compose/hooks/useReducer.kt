package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import xyz.junerver.kotlin.Tuple2

/**
 * Description:
 * @author Junerver
 * date: 2024/1/26-14:21
 * Email: junerver@gmail.com
 * Version: v1.1
 * update: 2024/3/11 10:57
 * 删除了模板代码，Action 使用 sealed 似乎是更好的决定
 * update: 2024/3/19 13:48
 * action类型从 Any 修改为泛型 A
 */
// reducer 函数类型抽象
typealias Reducer<S, A> = (prevState: S, action: A) -> S

typealias Dispatch<A> = (A) -> Unit

@Composable
fun <S, A> useReducer(
    reducer: Reducer<S, A>,
    initialState: S,
): Tuple2<S, Dispatch<A>> {
    val (state, setState) = _useState(initialState)
    return Pair(
        first = state, // state状态值
        second = { action: A -> setState(reducer(state, action)) } // dispatch函数
    )
}
