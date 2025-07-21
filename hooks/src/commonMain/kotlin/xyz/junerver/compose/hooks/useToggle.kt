package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import arrow.core.Either
import arrow.core.left
import arrow.core.right

/**
 * A hook for toggling between two values.
 *
 * This hook provides a way to switch between two values of the same type.
 * It's useful for implementing toggle functionality in UI components.
 *
 * @param defaultValue The first value to toggle between
 * @param reverseValue The second value to toggle between
 * @return A pair containing the current value and a toggle function
 *
 * @example
 * ```kotlin
 * // Toggle between true and false
 * val (value, toggle) = useToggle(true, false)
 *
 * // Toggle between strings
 * val (text, toggleText) = useToggle("Show", "Hide")
 *
 * // Use in UI
 * Button(onClick = toggle) {
 *     Text(text = value ?: "Toggle")
 * }
 * ```
 */
@Composable
fun <T> useToggle(defaultValue: T? = null, reverseValue: T? = null): Pair<T?, ToggleFn> {
    val (isLeft, toggle) = useBoolean(true)
    return (if (isLeft.value) defaultValue else reverseValue) to toggle
}

/**
 * A hook for toggling between two values of different types using [Either].
 *
 * This hook provides a type-safe way to switch between two values of different types
 * using the [Either] type from Arrow. It's useful when you need to maintain type
 * information while toggling between different states.
 *
 * @param defaultValue The first value of type L
 * @param reverseValue The second value of type R
 * @return A pair containing the current [Either] value and a toggle function
 *
 * @example
 * ```kotlin
 * // Toggle between String and Int
 * val (value, toggle) = useToggleEither("text", 42)
 *
 * // Handle the Either value
 * value.fold(
 *     { text -> Text(text ?: "No text") },
 *     { number -> Text(number?.toString() ?: "No number") }
 * )
 * ```
 */
@Composable
fun <L, R> useToggleEither(defaultValue: L? = null, reverseValue: R? = null): Pair<Either<L?, R?>, ToggleFn> {
    val (isLeft, toggle) = useBoolean(true)
    val leftEither by useLatestRef(defaultValue.left())
    val rightEither by useLatestRef(reverseValue.right())
    return (if (isLeft.value) leftEither else rightEither) to toggle
}

/**
 * A hook for toggling component visibility.
 *
 * This hook provides a convenient way to switch between showing and hiding
 * a component. It's useful for implementing show/hide functionality in UI.
 *
 * @param isVisible The initial visibility state
 * @param content The component to toggle visibility for
 * @return A pair containing the current component and a toggle function
 *
 * @example
 * ```kotlin
 * val (component, toggle) = useToggleVisible(true) {
 *     Text("Toggle me")
 * }
 *
 * Column {
 *     Button(onClick = toggle) {
 *         Text("Toggle Visibility")
 *     }
 *     component()
 * }
 * ```
 */
@Composable
fun useToggleVisible(isVisible: Boolean = false, content: ComposeComponent): Pair<ComposeComponent, ToggleFn> {
    val empty: ComposeComponent = {}
    return useToggleVisible(isVisible, content, empty)
}

/**
 * A hook for toggling between two different components.
 *
 * This hook provides a way to switch between two different components.
 * It's useful for implementing component switching functionality.
 *
 * @param isFirst Whether to show the first component initially
 * @param content1 The first component to show
 * @param content2 The second component to show
 * @return A pair containing the current component and a toggle function
 *
 * @example
 * ```kotlin
 * val (component, toggle) = useToggleVisible(
 *     isFirst = true,
 *     content1 = { Text("First Component") },
 *     content2 = { Text("Second Component") }
 * )
 *
 * Column {
 *     Button(onClick = toggle) {
 *         Text("Switch Component")
 *     }
 *     component()
 * }
 * ```
 */
@Composable
fun useToggleVisible(isFirst: Boolean = true, content1: ComposeComponent, content2: ComposeComponent): Pair<ComposeComponent, ToggleFn> {
    val (visible, toggle) = useBoolean(isFirst)
    return (if (visible.value) content1 else content2) to toggle
}
