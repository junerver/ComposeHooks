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
 * A hook that provides an enhanced version of [useState] with better state management capabilities.
 *
 * This hook solves two common issues with [useState]:
 * 1. Closure problems when using destructuring declarations
 * 2. State loss when calling set functions rapidly (millisecond level)
 *
 * [useGetState] provides a getter function to access the latest state value and
 * a setter function that supports both direct value updates and functional updates.
 *
 * @param default The initial value of the state
 * @return A [GetStateHolder] containing the state and control functions
 *
 * @example
 * ```kotlin
 * val (state, setValue, getValue) = useGetState(0)
 *
 * // Direct value update
 * setValue(5.left())
 *
 * // Functional update
 * setValue { current -> current + 1 }.right()
 *
 * // Get latest value
 * val currentValue = getValue()
 * ```
 */
@Composable
fun <T> useGetState(default: T & Any): GetStateHolder<T & Any> {
    val state = useState(default)
    return remember {
        GetStateHolder(
            state = state,
            setValue = { value: SetterEither<T & Any> ->
                val newValue = value.fold({ it }, { it(state.value) })
                state.value = newValue
            },
            getValue = { state.value }
        )
    }
}

/**
 * A nullable version of [useGetState].
 *
 * This version allows the state to hold null values, which is useful when
 * dealing with optional data or uninitialized states.
 *
 * @param default The initial value of the state (can be null)
 * @return A [GetStateHolder] containing the nullable state and control functions
 */
@Composable
fun <T> _useGetState(default: T): GetStateHolder<T> {
    val state = _useState(default)
    return remember {
        GetStateHolder(
            state = state,
            setValue = { value: SetterEither<T> ->
                val newValue = value.fold({ it }, { it(state.value) })
                state.value = newValue
            },
            getValue = { state.value }
        )
    }
}

/**
 * Holder class for state management with getter and setter functions.
 *
 * @property state The current state value as a [State]
 * @property setValue Function to update the state (supports both direct and functional updates)
 * @property getValue Function to get the latest state value
 */
@Stable
data class GetStateHolder<T>(
    val state: State<T>,
    val setValue: SetValueFn<SetterEither<T>>,
    val getValue: GetValueFn<T>,
)
