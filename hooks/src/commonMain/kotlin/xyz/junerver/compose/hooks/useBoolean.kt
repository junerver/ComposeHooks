package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember

/*
  Description: A hook to conveniently manage Boolean state
  Author: Junerver
  Date: 2024/1/26-13:38
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * A composable function that provides a state holder for a boolean value
 * with various utility functions to manipulate the state.
 *
 * This function allows you to easily manage a boolean state with operations
 * like toggling, setting it to `true`, or setting it to `false`. It is useful
 * for scenarios where you need to track and manipulate a boolean state in a
 * composable function.
 *
 * @param default The default boolean value for the state. Default is `false`.
 *
 * @return A [BooleanHolder] object that contains:
 * - `state`: The current boolean state value.
 * - `toggle`: A function that toggles the state between `true` and `false`.
 * - `setValue`: A function to set the state to a specified boolean value.
 * - `setTrue`: A function to set the state to `true`.
 * - `setFalse`: A function to set the state to `false`.
 *
 * Example usage:
 * ```kotlin
 * val (booleanState, toggle) = useBoolean(true)
 * toggle()  // Will change the state to false.
 * ```
 */
@Composable
fun useBoolean(default: Boolean = false): BooleanHolder {
    val (state, setState, getState) = useGetState(default)
    return remember {
        BooleanHolder(
            state = state, // boolean state
            toggle = { setState(!getState()) }, // toggle fun
            setValue = { b: Boolean -> setState(b) }, // set fun
            setTrue = { setState(true) }, // setTrue
            setFalse = { setState(false) }, // setFalse
        )
    }
}

@Stable
data class BooleanHolder(
    val state: State<Boolean>,
    val toggle: ToggleFn,
    val setValue: SetValueFn<Boolean>,
    val setTrue: SetTrueFn,
    val setFalse: SetFalseFn,
)
