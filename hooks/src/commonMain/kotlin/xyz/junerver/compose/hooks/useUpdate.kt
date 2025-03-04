package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/*
  Description:
  Author: Junerver
  Date: 2024/3/8-11:28
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * A hook for forcing a component to re-render.
 *
 * This hook provides a function that, when called, will trigger a recomposition
 * of the component. It's useful when you need to force a UI update without
 * changing any state values, such as after completing an operation that doesn't
 * affect state directly.
 *
 * The hook works by maintaining an internal counter state that, when incremented,
 * forces a recomposition due to state change.
 *
 * @return A function that, when called, forces a component update
 *
 * @example
 * ```kotlin
 * // Basic usage
 * val forceUpdate = useUpdate()
 * 
 * // Force a re-render
 * Button(onClick = forceUpdate) {
 *     Text("Refresh UI")
 * }
 * 
 * // Use in callbacks
 * LaunchedEffect(Unit) {
 *     someExternalOperation()
 *     // Force UI update after operation
 *     forceUpdate()
 * }
 * 
 * // Use in event handlers
 * Button(
 *     onClick = {
 *         performSideEffect()
 *         // Refresh UI after side effect
 *         forceUpdate()
 *     }
 * ) {
 *     Text("Perform Action")
 * }
 * 
 * // Use with manual DOM manipulations
 * DisposableEffect(Unit) {
 *     val observer = MutationObserver { _, _ ->
 *         // Update UI when DOM changes
 *         forceUpdate()
 *     }
 *     onDispose {
 *         observer.disconnect()
 *     }
 * }
 * ```
 */
@Suppress("UNUSED_VARIABLE")
@Composable
inline fun useUpdate(): () -> Unit {
    var state by useState(0.0)
    val (single) = Single(state) // core logic
    return {
        state += 1
    }
}
