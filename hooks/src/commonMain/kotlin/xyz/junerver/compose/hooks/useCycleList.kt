package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import kotlinx.collections.immutable.PersistentList

/*
  Description: Cycle through a list of items
  Author: Junerver
  Date: 2025/7/3-17:20
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Options for the [useCycleList] hook.
 *
 * @param T The type of items in the list
 * @property initialValue The initial value of the state. Default is the first item in the list.
 * @property fallbackIndex The default index to use when the current value is not found in the list.
 * @property getIndexOf Custom function to get the index of the current value in the list. Useful when custom index lookup logic is needed.
 */
@Stable
data class UseCycleListOptions<T> internal constructor(
    var initialValue: T? = null,
    var fallbackIndex: Int = 0,
    var getIndexOf: ((value: T, list: PersistentList<T>) -> Int)? = null,
) {
    companion object {
        fun <T> optionOf(opt: UseCycleListOptions<T>.() -> Unit): UseCycleListOptions<T> = UseCycleListOptions<T>().apply {
            opt()
        }
    }
}

/**
 * A hook for cycling through a list of items.
 *
 * This hook provides a convenient way to cycle through items in a list, with functions
 * to move to the next or previous item, or jump to a specific index.
 * Suitable for scenarios requiring cycling through a limited set of options, such as carousels, tabs, theme switching, etc.
 *
 * @param list The list of items to cycle through
 * @param optionsOf Configuration options for the cycle behavior, configured using a lambda expression
 * @return A [CycleListHolder] object containing the current state and navigation functions
 *
 * @example
 * ```kotlin
 * val animals = persistentListOf("Dog", "Cat", "Lizard", "Shark")
 * val (state, index, next, prev, go) = useCycleList(animals)
 *
 * Text("Current animal: ${state.value}")
 * Button(onClick = { next() }) { Text("Next") }
 * Button(onClick = { prev() }) { Text("Previous") }
 * Button(onClick = { go(2) }) { Text("Go to Lizard") }
 * ```
 */
@Composable
fun <T> useCycleList(list: PersistentList<T>, optionsOf: UseCycleListOptions<T>.() -> Unit = {}): CycleListHolder<T> {
    // Initialize options with remember and apply user-provided configuration
    val options = remember { UseCycleListOptions.optionOf(optionsOf) }
    // Create a mutable reference with useRef, initialized with the configured initial value or the first item in the list
    val (state, setState) = _useGetState(getInitialValue(list, options))

    // Create a function to set the state to a specific index
    fun set(i: Int) {
        val length = list.size
        if (length == 0) return // If the list is empty, do nothing

        // Ensure the index wraps around properly, handling negative indices and out-of-range indices
        // The formula (i % length + length) % length ensures the result is always in the range [0, length-1]
        val index = (i % length + length) % length
        val value = list[index]
        setState(value) // Update the current value of the reference
    }

    // Create a function to shift the current index by a delta
    fun shift(delta: Int = 1) {
        // Get the index of the current value in the list
        val currentIndex = getCurrentIndex(state.value, list, options)
        // Move the index by delta positions and set the new value
        set(currentIndex + delta)
    }

    // Create navigation functions: next item, previous item, and jump to specific index
    fun next() = shift(1) // Move forward n positions

    fun prev() = shift(-1) // Move backward n positions

    fun go(i: Int) = set(i) // Jump directly to index i

    // Create a derived state to track the current index
    val index = useState {
        getCurrentIndex(state.value, list, options)
    }

    return remember { CycleListHolder(state, index, ::next, ::prev, ::go, ::shift) }
}

/**
 * Return type for the [useCycleList] hook.
 *
 * This class provides the current state and navigation functions, supporting destructuring syntax for ease of use.
 *
 * @param T The type of items in the list
 * @property state The current item state, an observable State object
 * @property index The current index in the list, an observable State object
 * @property next Function to move to the next item(s), can specify steps n
 * @property prev Function to move to the previous item(s), can specify steps n
 * @property go Function to go to a specific index
 * @property shift Function to shift the current index by a delta, can specify steps n
 */
@Stable
data class CycleListHolder<T>(
    val state: State<T>,
    val index: State<Int>,
    val next: () -> Unit,
    val prev: () -> Unit,
    val go: (i: Int) -> Unit,
    val shift: (delta: Int) -> Unit,
)

/**
 * Helper function to get the initial value.
 *
 * @param list The item list
 * @param options Configuration options
 * @return The initial value
 * @throws IllegalArgumentException when the list is empty and no initial value is provided
 */
private fun <T> getInitialValue(list: PersistentList<T>, options: UseCycleListOptions<T>): T = options.initialValue ?: list.firstOrNull()
    ?: throw IllegalArgumentException("List cannot be empty when no initialValue is provided")

/**
 * Helper function to get the current index of a value in the list.
 *
 * Lookup order:
 * 1. If the list is empty, return 0
 * 2. If a custom getIndexOf function is provided, use it
 * 3. Try using the standard indexOf method
 * 4. If not found, use fallbackIndex
 *
 * @param value The current value
 * @param list The item list
 * @param options Configuration options
 * @return The index of the current value in the list
 */
private fun <T> getCurrentIndex(value: T, list: PersistentList<T>, options: UseCycleListOptions<T>): Int {
    if (list.isEmpty()) return 0

    return options.getIndexOf?.invoke(value, list) ?: list.indexOf(value).takeIf { it >= 0 } ?: options.fallbackIndex
}
