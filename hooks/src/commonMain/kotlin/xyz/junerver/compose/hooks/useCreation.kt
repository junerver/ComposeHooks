package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/*
  Description:
  Author: Junerver
  Date: 2024/2/7-14:20
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * A hook for creating memoized values with better performance than [useRef].
 *
 * This hook is an alternative to [useRef] for creating constant values. While both hooks
 * can be used to store persistent values, [useCreation] is optimized for creating complex
 * constants by ensuring they are only created when dependencies change.
 *
 * Key differences from [useRef]:
 * - Values are memoized based on dependencies
 * - Creation only occurs when dependencies change
 * - Better performance for complex object creation
 * - Prevents unnecessary recreations
 *
 * @param keys Dependencies that trigger recreation when changed
 * @param factory Function that creates the value
 * @return A [Ref] containing the memoized value
 *
 * @example
 * ```kotlin
 * // Basic usage - create a complex object
 * val expensiveObject = useCreation {
 *     ComplexObject().apply {
 *         // Expensive initialization
 *         initialize()
 *     }
 * }
 * 
 * // With dependencies
 * val userId = useState("")
 * val userProfile = useCreation(userId.value) {
 *     // Only recreated when userId changes
 *     UserProfile(userId.value)
 * }
 * 
 * // Creating a service instance
 * val apiService = useCreation {
 *     ApiService(
 *         baseUrl = "https://api.example.com",
 *         timeout = 30.seconds
 *     )
 * }
 * 
 * // With multiple dependencies
 * val (width, height) = useState(0) to useState(0)
 * val canvas = useCreation(width.value, height.value) {
 *     // Recreate canvas only when dimensions change
 *     Canvas(width.value, height.value)
 * }
 * 
 * // Creating a memoized callback
 * val callback = useCreation(someState.value) {
 *     // Callback recreated only when someState changes
 *     { data -> processData(data, someState.value) }
 * }
 * 
 * // Creating a shared resource
 * val sharedResource = useCreation {
 *     // Created once and shared across recompositions
 *     SharedResource().apply {
 *         addShutdownHook { cleanup() }
 *     }
 * }
 * ```
 */
@Composable
fun <T> useCreation(vararg keys: Any?, factory: () -> T): Ref<T> = remember(keys = keys) {
    MutableRef(factory())
}
