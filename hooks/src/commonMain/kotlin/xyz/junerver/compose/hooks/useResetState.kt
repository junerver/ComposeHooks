package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember

/*
  Description: [useGetState] that provides [reset]
  Author: Junerver
  Date: 2024/7/9-14:19
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * A hook for managing state with reset functionality.
 *
 * This hook extends [useGetState] by adding a reset capability that allows you to
 * restore the state to its initial value. It's useful for forms, settings, or any
 * state that needs to be reset to its default value.
 *
 * @param default The initial value of the state
 * @return A [ResetStateHolder] containing the state and state management functions
 *
 * @example
 * ```kotlin
 * // Create a state with reset capability
 * val (state, setValue, getValue, reset) = useResetState(0)
 *
 * // Update state
 * setValue(42)
 *
 * // Get current value
 * val current = getValue()
 *
 * // Reset to initial value
 * reset()
 *
 * // Use in UI
 * Column {
 *     Text("Value: ${state.value}")
 *     Button(onClick = { setValue(100) }) {
 *         Text("Set to 100")
 *     }
 *     Button(onClick = reset) {
 *         Text("Reset")
 *     }
 * }
 * ```
 */
@Composable
fun <T> useResetState(default: T & Any): ResetStateHolder<T & Any> {
    val (state, setState, getState) = useGetState(default)

    fun reset() {
        setState(default)
    }
    return remember { ResetStateHolder(state, setState, getState, ::reset) }
}

/**
 * A holder class for state management with reset capability.
 *
 * This class provides access to the state value and functions for managing it,
 * including the ability to reset the state to its initial value.
 *
 * @param state The current state value
 * @param setValue Function to update the state value
 * @param getValue Function to get the current state value
 * @param reset Function to reset the state to its initial value
 *
 * @example
 * ```kotlin
 * val holder = useResetState("initial")
 *
 * // Access state
 * val currentValue = holder.state.value
 *
 * // Update state
 * holder.setValue("new value")
 *
 * // Get current value
 * val value = holder.getValue()
 *
 * // Reset state
 * holder.reset()
 * ```
 */
@Stable
data class ResetStateHolder<T>(
    val state: State<T>,
    val setValue: SetValueFn<SetterEither<T>>,
    val getValue: GetValueFn<T>,
    val reset: ResetFn,
)
