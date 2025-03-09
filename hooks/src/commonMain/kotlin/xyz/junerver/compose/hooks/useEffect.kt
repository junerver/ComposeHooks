package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import xyz.junerver.compose.hooks.utils.unwrap

/*
  Description: Alias for [LaunchedEffect]
  Author: Junerver
  Date: 2024/3/4-8:20
  Email: junerver@gmail.com
  Version: v1.0

  Update: 2024/9/18 10:50
  增加了解包装函数，对State、Ref进行解包，方便直接使用其实例作为依赖
*/

/**
 * A hook for executing side effects in response to dependency changes.
 *
 * This hook is an alias for [LaunchedEffect] with enhanced dependency handling.
 * It automatically unwraps [State] and [Ref] dependencies using [unwrap], making
 * it easier to work with wrapped values.
 *
 * @param deps The dependencies that trigger the effect when changed
 * @param block The suspend function to execute when dependencies change
 *
 * @example
 * ```kotlin
 * // Basic usage with primitive value
 * var count by useState(0)
 * useEffect(count) {
 *     println("Count is now: $count")
 * }
 *
 * // With State wrapper
 * val name: State<String> = useState("John")
 * useEffect(name) {
 *     // Automatically unwraps name.value
 *     println("Name changed to: ${name.value}")
 * }
 *
 * // With Ref wrapper
 * val countRef = useRef(0)
 * useEffect(countRef) {
 *     // Automatically unwraps countRef.current
 *     println("Count ref is: ${countRef.current}")
 * }
 *
 * // Multiple dependencies
 * val (firstName, lastName) = useState("John") to useState("Doe")
 * useEffect(firstName, lastName) {
 *     // Triggers when either name changes
 *     println("Full name: ${firstName.value} ${lastName.value}")
 * }
 *
 * // With API calls
 * val query = useState("")
 * useEffect(query) {
 *     if (query.value.isNotEmpty()) {
 *         val results = searchApi(query.value)
 *         updateResults(results)
 *     }
 * }
 *
 * // Cleanup example
 * useEffect(Unit) {
 *     val job = startBackgroundTask()
 *     // Cleanup when effect is cancelled
 *     try {
 *         // Effect body
 *     } finally {
 *         job.cancel()
 *     }
 * }
 * ```
 */
@Composable
fun useEffect(vararg deps: Any?, block: SuspendAsyncFn) {
    LaunchedEffect(keys = unwrap(deps), block = block)
}
