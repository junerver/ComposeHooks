package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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

typealias Reducer<S, A> = (prevState: S, action: A) -> S

typealias Dispatch<A> = (A) -> Unit

typealias Middleware<S, A> = (dispatch: Dispatch<A>, state: S) -> Dispatch<A>

@Composable
fun <S, A> useReducer(
    reducer: Reducer<S, A>,
    initialState: S,
    middlewares: Array<Middleware<S, A>> = emptyArray(),
): Tuple2<S, Dispatch<A>> {
    var state by _useState(initialState)
    val dispatch = { action: A -> state = reducer(state, action) }
    val enhancedDispatch: Dispatch<A> = if (middlewares.isNotEmpty()) {
        { action ->
            var nextDispatch: Dispatch<A> = dispatch
            for (middleware in middlewares) {
                nextDispatch = middleware(nextDispatch, state)
            }
            nextDispatch(action)
        }
    } else {
        dispatch
    }
    return Tuple2(state, enhancedDispatch)
}
