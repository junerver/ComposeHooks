package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import org.jetbrains.annotations.Nullable
import xyz.junerver.kotlin.Tuple3

/**
 * Description: Using destructuring declarations on [useState] can cause closure problems.
 * Using [useLatestRef] is a solution, but if you call the set function quickly(millisecond level),
 * there will be a problem of state loss.
 *
 * Now you can use [useGetState] to solve these problems and get the latest value
 * through [getter] to avoid closure problems. The [setter] function also supports fast update.
 *
 * @author Junerver
 * date: 2024/5/10-9:31
 * Email: junerver@gmail.com
 * Version: v1.0
 */
@Composable
fun <T> useGetState(default: T & Any): Tuple3<T, (T & Any) -> Unit, () -> T> {
    var state: T & Any by useState(default)
    return Tuple3(
        first = state, // state
        second = { state = it }, // setter
        third = { state } // getter
    )
}

/**
 * A nullable version of [useGetState]
 *
 * @param T
 * @param default
 * @return
 */
@Composable
fun <T> _useGetState(@Nullable default: T): Tuple3<T, (T) -> Unit, () -> T> {
    var state: T by _useState(default)
    return Tuple3(
        first = state,
        second = { state = it },
        third = { state }
    )
}
