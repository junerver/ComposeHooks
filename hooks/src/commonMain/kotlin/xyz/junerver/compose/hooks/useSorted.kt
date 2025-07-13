package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember

/*
  Description: Reactive sort array
  Author: Junerver
  Date: 2025/7/13-14:46
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Compare function for sorting arrays
 *
 * @param T The type of items to compare
 */
typealias SortedCompareFn<T> = (a: T, b: T) -> Int

/**
 * Sort function that takes an array and a compare function and returns a sorted array
 *
 * @param T The type of items in the array
 */
typealias SortedFn<T> = (arr: List<T>, compareFn: SortedCompareFn<T>) -> List<T>

/**
 * Options for the [useSorted] hook.
 *
 * @param T The type of items in the array
 * @property sortFn Custom sort algorithm function
 * @property compareFn Compare function for sorting
 * @property dirty When true, changes the value of the source array instead of creating a new one
 */
@Stable
data class UseSortedOptions<T> internal constructor(
    /**
     * Custom sort algorithm
     */
    var sortFn: SortedFn<T>? = null,
    /**
     * Compare function
     */
    var compareFn: SortedCompareFn<T>? = null,
    /**
     * Change the value of the source array
     * @default false
     */
    var dirty: Boolean = false,
) {
    companion object {
        fun <T> optionOf(opt: UseSortedOptions<T>.() -> Unit): UseSortedOptions<T> = UseSortedOptions<T>().apply {
            opt()
        }
    }
}

/**
 * Default sort function that uses the standard sort method
 */
private val defaultSortFn: SortedFn<Any> = { arr, compareFn ->
    arr.sortedWith(Comparator { a, b -> compareFn(a, b) })
}

/**
 * Default compare function that handles different types appropriately:
 * - For numeric types (Int, Long, Float, Double), compares by numeric value
 * - For other types, converts to strings and compares lexicographically
 */
private val defaultCompare: SortedCompareFn<Any> = { a, b ->
    when {
        // Handle numeric types with numeric comparison
        a is Int && b is Int -> a.compareTo(b)
        a is Long && b is Long -> a.compareTo(b)
        a is Float && b is Float -> a.compareTo(b)
        a is Double && b is Double -> a.compareTo(b)
        // Handle mixed numeric types
        a is Number && b is Number -> a.toDouble().compareTo(b.toDouble())
        // Default to string comparison for other types
        else -> a.toString().compareTo(b.toString())
    }
}

/**
 * A hook for creating a sorted version of an array.
 *
 * This hook takes a source array and returns a sorted version of it. By default,
 * it creates a new sorted array without modifying the source. If the `dirty` option
 * is set to true, it will modify the source array directly.
 *
 * @param source The source array to sort
 * @param compareFn Optional compare function for sorting
 * @return A [State] containing the sorted array
 *
 * @example
 * ```kotlin
 * // Basic usage with default comparison
 * // For numeric types, the default comparison is by numeric value
 * val source = listOf(10, 3, 5, 7, 2, 1, 8, 6, 9, 4)
 * val sorted = useSorted(source)
 * // sorted.value will be [1, 2, 3, 4, 5, 6, 7, 8, 9, 10] (sorted by numeric value)
 * // source remains unchanged
 *
 * // Custom comparison for objects
 * val people = listOf(
 *     Person("John", 40),
 *     Person("Jane", 20),
 *     Person("Joe", 30),
 *     Person("Jenny", 22)
 * )
 * val sortedByAge = useSorted(people) { a, b -> a.age - b.age }
 * ```
 */
@Composable
fun <T> useSorted(
    source: List<T>,
    compareFn: SortedCompareFn<T>,
): State<List<T>> {
    return useSorted(
        source,
        optionsOf = {
            this.compareFn = compareFn
        },
    )
}

/**
 * A hook for creating a sorted version of an array with custom options.
 *
 * This overload allows you to specify custom sorting options through the [UseSortedOptions] class.
 *
 * @param source The source array to sort
 * @param optionsOf Custom sorting options
 * @return A [State] containing the sorted array
 *
 * @example
 * ```kotlin
 * // Using dirty mode to modify the source array
 * val source = mutableListOf(10, 3, 5, 7, 2, 1, 8, 6, 9, 4)
 * val sorted = useSorted(source, UseSortedOptions<Int>().apply {
 *     compareFn = { a, b -> a - b }
 *     dirty = true
 * })
 * // source will be modified to [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
 * ```
 */
@Composable
fun <T> useSorted(
    source: List<T>,
    optionsOf: UseSortedOptions<T>.() -> Unit = {},
): State<List<T>> {
    val options by useCreation { UseSortedOptions.optionOf(optionsOf) }
    // Create updated state for the source list
    val sourceState = useLatestState(source)

    // Extract options with defaults
    @Suppress("UNCHECKED_CAST") val sortFn = options.sortFn ?: defaultSortFn as SortedFn<T>
    val compareFn = options.compareFn ?: { a, b -> defaultCompare(a as Any, b as Any) }
    val dirty = options.dirty

    // If not in dirty mode, create a computed state that sorts a copy of the source
    if (!dirty) {
        return useState(sourceState.value) {
            sortFn(sourceState.value, compareFn)
        }
    }

    // In dirty mode, modify the source directly
    // This assumes source is mutable
    val result = remember(sourceState.value) {
        val sorted = sortFn(sourceState.value, compareFn)
        if (source is MutableList<T>) {
            source.clear()
            source.addAll(sorted)
        }
        sorted
    }

    return useLatestState(result)
}

