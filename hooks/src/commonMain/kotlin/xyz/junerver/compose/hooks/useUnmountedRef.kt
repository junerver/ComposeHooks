package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable

/*
  Description: 获取当前组件是否已经卸载的 Hook
  A Hook can be used to get whether the component is unmounted.
  Author: Junerver
  Date: 2024/1/26-13:29
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * A hook for tracking whether a composable is unmounted.
 *
 * This hook provides a reference that indicates whether the current component has been
 * unmounted. It's useful for preventing operations on unmounted components, which can
 * lead to memory leaks or unexpected behavior.
 *
 * The reference value is:
 * - `false` when the component is mounted
 * - `true` when the component has been unmounted
 *
 * @return A [Ref] containing the unmounted state
 *
 * @example
 * ```kotlin
 * // Basic usage
 * val unmountedRef = useUnmountedRef()
 *
 * // Use in async operations
 * LaunchedEffect(Unit) {
 *     delay(1000)
 *     if (!unmountedRef.current) {
 *         // Only update state if component is still mounted
 *         updateState()
 *     }
 * }
 *
 * // Use in callbacks
 * Button(
 *     onClick = {
 *         scope.launch {
 *             val result = fetchData()
 *             if (!unmountedRef.current) {
 *                 // Safe to update UI
 *                 updateUI(result)
 *             }
 *         }
 *     }
 * ) {
 *     Text("Fetch Data")
 * }
 *
 * // Use in network requests
 * suspend fun safeApiCall() {
 *     try {
 *         val response = api.fetchData()
 *         if (!unmountedRef.current) {
 *             // Component is still mounted, safe to use response
 *             handleResponse(response)
 *         }
 *     } catch (e: Exception) {
 *         if (!unmountedRef.current) {
 *             // Component is still mounted, safe to handle error
 *             handleError(e)
 *         }
 *     }
 * }
 * ```
 */
@Composable
fun useUnmountedRef(): Ref<Boolean> {
    val unmountedRef = useRef(default = false)
    useMount {
        unmountedRef.current = false
    }
    useUnmount {
        unmountedRef.current = true
    }
    return unmountedRef
}
