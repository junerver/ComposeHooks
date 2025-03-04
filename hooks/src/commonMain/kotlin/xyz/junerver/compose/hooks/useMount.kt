package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable

/*
  Description:Execute `fn` function when component is mounted
  Author: Junerver
  Date: 2024/1/25-8:26
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * A hook for executing code when a composable is mounted.
 *
 * This hook is a specialized version of [useEffect] that executes the provided
 * block only once when the composable is first mounted. It's useful for
 * initialization tasks, data fetching, or any one-time setup that should
 * happen when the component enters the composition.
 *
 * @param block The suspend function to be executed on mount
 *
 * @example
 * ```kotlin
 * useMount {
 *     // This block will be executed only once when the component is mounted
 *     loadInitialData()
 *     setupSubscriptions()
 *     initializeResources()
 * }
 * ```
 */
@Composable
fun useMount(block: SuspendAsyncFn) = useEffect(Unit) { block() }
