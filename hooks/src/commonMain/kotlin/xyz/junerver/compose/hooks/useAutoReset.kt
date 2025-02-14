package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import kotlin.time.Duration
import kotlinx.coroutines.delay

/*
  Description: A hook which will reset state to the default value after some time.
  Author: Junerver
  Date: 2024/7/8-15:52
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * A composable function that automatically resets a state value to its default
 * value after a specified duration (interval).
 *
 * This function can be used to reset a value back to its default state after a given
 * time period has passed, making it useful for situations where you want to restore
 * the state to its initial condition after a certain amount of time.
 *
 * @param default The default value of type `T` that the state will reset to.
 * @param interval The `Duration` indicating the time period after which the state
 *                 will be reset to the `default` value.
 *
 * @return A `MutableState<T>` that holds the current state value, which will be
 *         automatically reset to `default` after the specified interval.
 *
 * Example usage:
 * ```kotlin
 * var autoResetState by useAutoReset(0, 5.seconds)
 * autoResetState = 1
 * ```
 */
@Composable
fun <T> useAutoReset(default: T & Any, interval: Duration): MutableState<T & Any> {
    val state = useState(default = default)
    val defaultValue = useCreation { default }
    useEffect(state) {
        delay(interval)
        state.value = defaultValue.current
    }
    return state
}
