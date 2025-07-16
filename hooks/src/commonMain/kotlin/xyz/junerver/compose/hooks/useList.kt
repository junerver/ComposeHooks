package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList

/*
  Description: More convenient to use dynamic List state
  Author: Junerver
  Date: 2024/3/7-15:02
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * A hook for creating and managing dynamic list state in Compose.
 *
 * This hook provides a convenient way to create a [SnapshotStateList] from a collection
 * of elements. It's particularly useful when you need to maintain a mutable list that
 * can trigger recompositions when modified.
 *
 * @param elements The initial collection of elements
 * @return A [SnapshotStateList] containing the elements
 *
 * @example
 * ```kotlin
 * val list = useList(listOf(1, 2, 3))
 *
 * // Add elements
 * list.add(4)
 *
 * // Remove elements
 * list.remove(1)
 *
 * // Update elements
 * list[0] = 5
 *
 * // Clear the list
 * list.clear()
 * ```
 */
@Composable
fun <T> useList(elements: Collection<T>): SnapshotStateList<T> = remember {
    elements.toMutableStateList()
}

/**
 * A hook for creating and managing dynamic list state in Compose.
 *
 * This overload allows you to create a [SnapshotStateList] from a vararg list of elements.
 * It's a convenient way to initialize a list with a fixed set of elements.
 *
 * @param elements The initial elements as varargs
 * @return A [SnapshotStateList] containing the elements
 *
 * @example
 * ```kotlin
 * val list = useList(1, 2, 3)
 *
 * // Add elements
 * list.add(4)
 *
 * // Remove elements
 * list.remove(1)
 *
 * // Update elements
 * list[0] = 5
 *
 * // Clear the list
 * list.clear()
 * ```
 */
@Composable
fun <T> useList(vararg elements: T): SnapshotStateList<T> = remember {
    mutableStateListOf(*elements)
}

/**
 * Reactive List.reduce.
 */
@Composable
fun <S, T : S> useListReduce(list: List<T>, operation: (acc: S, T) -> S): State<S?> {
    val latestList = useLatestState(list)
    return useState {
        latestList.value.takeIf { it.isNotEmpty() }?.reduce(operation)
    }
}
