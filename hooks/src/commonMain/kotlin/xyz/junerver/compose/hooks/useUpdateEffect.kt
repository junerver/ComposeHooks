package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable

/*
  Description: [useUpdateEffect] 用法等同于 [useEffect]，但是会忽略首次执行，只在依赖更新时执行。
  Author: Junerver
  Date: 2024/3/11-11:19
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * A hook similar to [useEffect] but skips the first execution.
 *
 * This hook works like [useEffect], but it only executes the effect when its
 * dependencies change, ignoring the initial mount. This is useful when you want
 * to perform side effects only in response to changes, not during the initial render.
 *
 * @param deps The dependencies that trigger the effect when changed
 * @param block The suspend function to execute when dependencies change
 *
 * @example
 * ```kotlin
 * // Basic usage - effect runs only when count changes, not on initial render
 * var count by useState(0)
 * useUpdateEffect(count) {
 *     println("Count changed to: $count")
 * }
 *
 * // With multiple dependencies
 * var name by useState("")
 * var age by useState(0)
 * useUpdateEffect(name, age) {
 *     println("Profile updated: $name, $age")
 * }
 *
 * // In form handling
 * var formData by useState(FormData())
 * useUpdateEffect(formData) {
 *     // Validate or save form only when data changes
 *     validateForm(formData)
 * }
 *
 * // With API calls
 * var searchQuery by useState("")
 * useUpdateEffect(searchQuery) {
 *     // Search only when query changes, not on initial load
 *     if (searchQuery.isNotEmpty()) {
 *         searchApi(searchQuery)
 *     }
 * }
 *
 * // With animations
 * var isVisible by useState(false)
 * useUpdateEffect(isVisible) {
 *     // Trigger animation only on visibility changes, not initial state
 *     if (isVisible) {
 *         animateIn()
 *     } else {
 *         animateOut()
 *     }
 * }
 * ```
 */
@Composable
fun useUpdateEffect(vararg deps: Any?, block: SuspendAsyncFn) {
    var isMounted by useRef(false)
    useEffect(*deps) {
        if (!isMounted) {
            isMounted = true
        } else {
            block()
        }
    }
}
