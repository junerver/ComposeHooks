package xyz.junerver.compose.hooks.usecyclelist
import xyz.junerver.compose.hooks.usestate.useStateImpl
import xyz.junerver.compose.hooks.useDynamicOptions
import xyz.junerver.compose.hooks.usegetstate._useGetStateImpl
import xyz.junerver.compose.hooks.useref.setValue
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.useref.getValue
import xyz.junerver.compose.hooks.Options

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.remember
import kotlinx.collections.immutable.PersistentList
import xyz.junerver.compose.hooks.usecreation.useCreationImpl
import xyz.junerver.compose.hooks.uselatest.useLatestStateImpl

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
fun <T> useCycleListImpl(list: PersistentList<T>, optionsOf: UseCycleListOptions<T>.() -> Unit = {}): CycleListHolder<T> {
    val options = useDynamicOptions(optionsOf)
    val currentList = useIdentityState(list)
    val currentFallbackIndex = useLatestStateImpl(options.fallbackIndex)
    val currentGetIndexOf = useIdentityState(options.getIndexOf)
    val initialValue = useCreationImpl { getInitialValue(list, options) }.current
    val initialIndex = useCreationImpl {
        getCurrentIndex(initialValue, list, options.fallbackIndex, options.getIndexOf)
    }.current
    val (state, setState) = _useGetStateImpl(initialValue)
    val currentPosition = useStateImpl(initialIndex)

    fun resolveCurrentIndex(): Int {
        val latestList = currentList.value
        if (latestList.isEmpty()) return 0

        currentGetIndexOf.value?.let { getIndexOf ->
            return getIndexOf(state.value, latestList)
        }

        val trackedIndex = currentPosition.value
        if (trackedIndex in latestList.indices && latestList[trackedIndex] == state.value) {
            return trackedIndex
        }

        return latestList.indexOf(state.value).takeIf { it >= 0 } ?: currentFallbackIndex.value
    }

    fun set(i: Long) {
        val latestList = currentList.value
        if (latestList.isEmpty()) return

        val index = normalizeIndex(i, latestList.size)
        currentPosition.value = index
        setState(latestList[index])
    }

    fun shift(delta: Int = 1) {
        set(resolveCurrentIndex().toLong() + delta.toLong())
    }

    fun next() = shift(1)

    fun prev() = shift(-1)

    fun go(i: Int) = set(i.toLong())

    val index = useStateImpl {
        resolveCurrentIndex()
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
 * @param fallbackIndex Index used when [value] is not found
 * @param getIndexOf Optional custom lookup function
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
private fun <T> getCurrentIndex(
    value: T,
    list: PersistentList<T>,
    fallbackIndex: Int,
    getIndexOf: ((value: T, list: PersistentList<T>) -> Int)?,
): Int {
    if (list.isEmpty()) return 0

    return getIndexOf?.invoke(value, list) ?: list.indexOf(value).takeIf { it >= 0 } ?: fallbackIndex
}

private fun normalizeIndex(index: Long, size: Int): Int {
    val longSize = size.toLong()
    return (((index % longSize) + longSize) % longSize).toInt()
}

@Composable
private fun <T> useIdentityState(value: T): State<T> {
    val state = remember { mutableStateOf(value, referentialEqualityPolicy()) }
    state.value = value
    return state
}
