package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState

/*
  Description: Hook that returns the latest value can avoid closure problems when using destructuring.
  Author: Junerver
  Date: 2024/2/21-8:45
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * A hook that returns a reference to the latest value.
 *
 * This hook helps avoid closure problems when using destructuring declarations
 * by always providing access to the most recent value. It's particularly useful
 * in scenarios where you need to access the latest value in callbacks or effects.
 *
 * @param value The value to track
 * @return A [Ref] containing the latest value
 *
 * @example
 * ```kotlin
 * val latestValue = useLatestRef(initialValue)
 *
 * // The current value will always be up to date
 * useEffect {
 *     someCallback(latestValue.current)
 * }
 * ```
 */
@Composable
fun <T> useLatestRef(value: T): Ref<T> = useRef(default = value).apply { current = value }

/**
 * A hook that returns a state containing the latest value.
 *
 * This is an alias for [rememberUpdatedState] and provides a way to track
 * the latest value in a state object. It's useful when you need to maintain
 * a state that always reflects the most recent value.
 *
 * @param value The value to track
 * @return A [State] containing the latest value
 *
 * @example
 * ```kotlin
 * val latestState = useLatestState(initialValue)
 *
 * // The state will always contain the most recent value
 * Text(text = "Current value: ${latestState.value}")
 * ```
 */
@Composable
fun <T> useLatestState(value: T): State<T> = rememberUpdatedState(value)
