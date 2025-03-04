package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

/*
  Description: Hook that saves the previous state.
  Author: Junerver
  Date: 2024/2/1-14:55
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * A hook that tracks and returns the previous value of a state.
 *
 * This hook is useful when you need to compare the current value with its previous
 * value, such as detecting changes or implementing undo functionality. It internally
 * uses [useUndo] to maintain the history of values.
 *
 * @param present The current value to track
 * @return A [State] containing the previous value, or null if there is no previous value
 *
 * @example
 * ```kotlin
 * val currentCount = useCounter(0)
 * val previousCount = usePrevious(currentCount.value)
 * 
 * // Compare current and previous values
 * if (previousCount.value != null && currentCount.value > previousCount.value) {
 *     // Count has increased
 * }
 * 
 * // Display both values
 * Text("Current: ${currentCount.value}, Previous: ${previousCount.value}")
 * ```
 */
@Composable
fun <T> usePrevious(present: T): State<T?> {
    val (state, set) = useUndo(initialPresent = present)
    useEffect(present) {
        set(present)
    }
    return useState { state.value.past.lastOrNull() }
}
