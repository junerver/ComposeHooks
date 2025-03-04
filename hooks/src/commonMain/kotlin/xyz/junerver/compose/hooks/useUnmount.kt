package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

/*
  Description: 组件卸载时执行
  Author: Junerver
  Date: 2024/1/26-13:29
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * A hook for executing code when a composable is unmounted.
 *
 * This hook is a specialized version of [DisposableEffect] that executes the provided
 * block only when the composable is removed from the composition. It's useful for
 * cleanup tasks, resource disposal, or any operations that should happen when the
 * component is being removed.
 *
 * @param block The function to be executed on unmount
 *
 * @example
 * ```kotlin
 * useUnmount {
 *     // This block will be executed when the component is unmounted
 *     cleanupResources()
 *     cancelSubscriptions()
 *     removeEventListeners()
 * }
 * 
 * // Example with resource cleanup
 * useUnmount {
 *     // Clean up database connections
 *     database.close()
 * 
 *     // Cancel ongoing operations
 *     job.cancel()
 * 
 *     // Remove event listeners
 *     eventBus.unsubscribe()
 * }
 * ```
 */
@Composable
fun useUnmount(block: () -> Unit) = DisposableEffect(Unit) {
    onDispose(block)
}
