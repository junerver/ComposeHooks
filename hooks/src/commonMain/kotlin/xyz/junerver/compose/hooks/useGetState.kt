package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember

/*
  Description: Better `useState`
  Author: Junerver
  Date: 2024/5/10-9:31
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Description: Using destructuring declarations on [useState] can cause
 * closure problems. Using [useLatestRef] is a solution, but if you call
 * the set function quickly(millisecond level), there will be a problem of
 * state loss.
 *
 * Now you can use [useGetState] to solve these problems and get the latest
 * value through `getter` to avoid closure problems. The `setter` function
 * also supports fast update.
 */
@Composable
fun <T> useGetState(default: T & Any): GetStateHolder<T & Any> {
    val state = useState(default)
    return remember {
        GetStateHolder(
            state = state,
            setValue = { state.value = it },
            getValue = { state.value }
        )
    }
}

/**
 * A nullable version of [useGetState]
 *
 * @param default
 * @param T
 * @return
 */
@Composable
fun <T> _useGetState(default: T): GetStateHolder<T> {
    val state = _useState(default)
    return remember {
        GetStateHolder(
            state = state,
            setValue = { state.value = it },
            getValue = { state.value }
        )
    }
}

@Stable
data class GetStateHolder<T>(
    val state: State<T>,
    val setValue: SetValueFn<T>,
    val getValue: GetValueFn<T>,
)
