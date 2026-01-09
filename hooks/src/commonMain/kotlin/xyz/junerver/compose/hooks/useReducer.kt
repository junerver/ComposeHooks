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

/**
 * A hook for managing complex state logic using the reducer pattern.
 *
 * This hook implements a Redux-like state management pattern, allowing you to handle
 * complex state updates through actions and reducers. It supports middleware for
 * extending functionality and async actions.
 *
 * @param reducer The reducer function that determines how state changes based on actions
 * @param initialState The initial state value
 * @param middlewares Optional array of middleware functions to enhance dispatch behavior
 * @return A [ReducerHolder] containing the state and dispatch functions
 *
 * @example
 * ```kotlin
 * // Define actions
 * sealed class CounterAction {
 *     object Increment : CounterAction()
 *     object Decrement : CounterAction()
 *     data class SetValue(val value: Int) : CounterAction()
 * }
 *
 * // Define reducer
 * val counterReducer: Reducer<Int, CounterAction> = { state, action ->
 *     when (action) {
 *         is CounterAction.Increment -> state + 1
 *         is CounterAction.Decrement -> state - 1
 *         is CounterAction.SetValue -> action.value
 *     }
 * }
 *
 * // Use the hook
 * val (state, dispatch, dispatchAsync) = useReducer(counterReducer, 0)
 *
 * // Dispatch actions
 * dispatch(CounterAction.Increment)
 *
 * // Async action
 * dispatchAsync { dispatch ->
 *     delay(1000)
 *     dispatch(CounterAction.SetValue(10))
 * }
 * ```
 */
@Composable
fun <S, A> useReducer(
    reducer: Reducer<S, A>,
    initialState: S,
    middlewares: Array<Middleware<S, A>> = emptyArray(),
): ReducerHolder<S, A> {
    val asyncRun = useAsync()
    val state = _useState(initialState)

    val reducerRef = useLatestRef(reducer)
    val middlewaresRef = useLatestRef(middlewares)

    val baseDispatch: Dispatch<A> = remember(state, reducerRef) {
        { action ->
            state.value = reducerRef.current(state.value, action)
        }
    }

    val enhancedDispatch: Dispatch<A> = remember(state, baseDispatch, middlewaresRef) {
        { action ->
            val currentMiddlewares = middlewaresRef.current
            var nextDispatch: Dispatch<A> = baseDispatch

            if (currentMiddlewares.isNotEmpty()) {
                for (index in currentMiddlewares.size - 1 downTo 0) {
                    val middleware = currentMiddlewares[index]
                    nextDispatch = middleware(nextDispatch, state.value)
                }
            }

            nextDispatch(action)
        }
    }

    val enhancedDispatchRef = useLatestRef(enhancedDispatch)

    val enhancedDispatchAsync: DispatchAsync<A> = remember(asyncRun, enhancedDispatchRef) {
        { block ->
            asyncRun {
                val currentDispatch = enhancedDispatchRef.current
                currentDispatch(block(currentDispatch))
            }
        }
    }

    return remember {
        ReducerHolder(state, enhancedDispatch, enhancedDispatchAsync)
    }
}

/**
 * A holder class for the reducer state and dispatch functions.
 *
 * This class provides access to the current state and dispatch functions for
 * updating the state through actions.
 *
 * @param state The current state value
 * @param dispatch Function to dispatch synchronous actions
 * @param dispatchAsync Function to dispatch asynchronous actions
 *
 * @example
 * ```kotlin
 * val (state, dispatch, dispatchAsync) = useReducer(reducer, initialState)
 *
 * // Access state
 * val currentValue = state.value
 *
 * // Dispatch sync action
 * dispatch(MyAction.DoSomething)
 *
 * // Dispatch async action
 * dispatchAsync { dispatch ->
 *     // Perform async operation
 *     dispatch(MyAction.UpdateFromAsync)
 * }
 * ```
 */
@Stable
data class ReducerHolder<S, A>(
    val state: State<S>,
    val dispatch: Dispatch<A>,
    val dispatchAsync: DispatchAsync<A>,
)
