package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember

/*
  Description:
  Author: Junerver
  Date: 2024/1/26-14:21
  Email: junerver@gmail.com
  Version: v1.2

  update: 2024/3/11 10:57
  删除了模板代码，Action 使用 sealed 似乎是更好的决定

  update: 2024/3/19 13:48
  action类型从 Any 修改为泛型 A
*/

@Composable
fun <S, A> useReducer(reducer: Reducer<S, A>, initialState: S, middlewares: Array<Middleware<S, A>> = emptyArray()): ReducerHolder<S, A> {
    val asyncRun = useAsync()
    val state = _useState(initialState)
    val dispatch = { action: A -> state.value = reducer(state.value, action) }
    val enhancedDispatch: Dispatch<A> = if (middlewares.isNotEmpty()) {
        { action ->
            var nextDispatch: Dispatch<A> = dispatch
            for (middleware in middlewares) {
                nextDispatch = middleware(nextDispatch, state.value)
            }
            nextDispatch(action)
        }
    } else {
        dispatch
    }
    val enhancedDispatchAsync: DispatchAsync<A> = { block ->
        asyncRun {
            enhancedDispatch(block(enhancedDispatch))
        }
    }
    return remember { ReducerHolder(state, enhancedDispatch, enhancedDispatchAsync) }
}

@Stable
data class ReducerHolder<S, A>(
    val state: State<S>,
    val dispatch: Dispatch<A>,
    val dispatchAsync: DispatchAsync<A>,
)
