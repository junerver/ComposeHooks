package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

/*
  Description: 定时一段时间后执行的任务
  Author: Junerver
  Date: 2024/2/1-15:08
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * A hook for executing a task after a specified delay.
 *
 * This hook provides a way to schedule a task to run after a given time period.
 * It's useful for implementing timeouts, delayed actions, or cleanup operations.
 *
 * @param delay The time to wait before executing the task
 * @param block The task to execute after the delay
 *
 * @example
 * ```kotlin
 * // Show a loading indicator for 2 seconds
 * useTimeout(2.seconds) {
 *     // Hide loading indicator
 *     hideLoading()
 * }
 * 
 * // Implement a cleanup timeout
 * useTimeout(5.minutes) {
 *     // Clean up resources
 *     cleanupResources()
 * }
 * 
 * // Show a temporary message
 * useTimeout(3.seconds) {
 *     // Clear the message
 *     clearMessage()
 * }
 * ```
 */
@Composable
fun useTimeout(delay: Duration = 1.seconds, block: () -> Unit) {
    useEffect {
        delay(delay)
        block()
    }
}
