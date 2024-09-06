package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import kotlinx.coroutines.CoroutineScope
import xyz.junerver.compose.hooks.useredux.DispatchAsync
import xyz.junerver.kotlin.Tuple2
import xyz.junerver.kotlin.Tuple3

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

typealias Reducer<S, A> = (prevState: S, action: A) -> S

typealias Dispatch<A> = (A) -> Unit

typealias Middleware<S, A> = (dispatch: Dispatch<A>, state: S) -> Dispatch<A>

@Composable
fun <S, A> useReducer(
    reducer: Reducer<S, A>,
    initialState: S,
    middlewares: Array<Middleware<S, A>> = emptyArray(),
): Tuple3<S, Dispatch<A>, DispatchAsync<A>> {
    val asyncRun = useAsync()
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
    val enhancedDispatchAsync: DispatchAsync<A> = { block ->
        asyncRun {
            enhancedDispatch(block(enhancedDispatch))
        }
    }
    return Tuple3(state, enhancedDispatch, enhancedDispatchAsync)
}

typealias ThunkAction<A> = Either<A, suspend CoroutineScope.(Dispatch<A>) -> A>

typealias ThunkDispatch<A> = (ThunkAction<A>) -> Unit

/**
 * 类似使用redux-thunk插件的效果，现在dispatch可以直接接收一个[Either]作为参数，这样它可以传递Action
 * ，或者传递一个返回Action的异步函数
 *
 * @param reducer
 * @param initialState
 * @param middlewares
 * @param S
 * @param A
 * @return
 */
@Composable
fun <S, A> useReducerThunk(
    reducer: Reducer<S, A>,
    initialState: S,
    middlewares: Array<Middleware<S, A>> = emptyArray(),
): Tuple2<S, ThunkDispatch<A>> {
    val asyncRun = useAsync()
    val (state, dispatch) = useReducer(reducer = reducer, initialState = initialState, middlewares)
    val thunkDispatch = { action: ThunkAction<A> ->
        action.fold(
            ifLeft = {
                dispatch(it)
            },
            ifRight = {
                asyncRun {
                    val thunkAction = it(dispatch)
                    dispatch(thunkAction)
                }
            }
        )
    }

    return Tuple2(state, thunkDispatch)
}

operator fun <A> ThunkDispatch<A>.invoke(leftValue: A) = this(leftValue.left())
operator fun <A> ThunkDispatch<A>.invoke(rightValue: suspend CoroutineScope.(Dispatch<A>) -> A) =
    this(rightValue.right())
