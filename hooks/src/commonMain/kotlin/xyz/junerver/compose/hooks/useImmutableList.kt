package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentListOf

/**
 * @Author Junerver
 * @Date 2024/9/27-19:24
 * @Email junerver@gmail.com
 * @Version v1.0
 * @Description
 */

/**
 * A hook for managing immutable lists in Compose.
 *
 * This hook is different from [useList]. While [useList] provides a [SnapshotStateList]
 * that offers a [MutableList]-like experience, it requires using [SnapshotStateList.toList]
 * to trigger side effects with [useEffect].
 *
 * [useImmutableList] simplifies this by treating the list as a regular state,
 * allowing direct mutation through the [ImmutableListHolder.mutate] function.
 *
 * @param elements Initial elements of the list
 * @return An [ImmutableListHolder] containing the immutable list and mutation function
 *
 * @example
 * ```kotlin
 * val (list, mutate) = useImmutableList(1, 2, 3)
 *
 * // Add an element
 * mutate { it.add(4) }
 *
 * // Remove elements
 * mutate { it.removeAll { it > 2 } }
 *
 * // Update elements
 * mutate { it.replaceAll { it * 2 } }
 *
 * // Access the current list
 * val currentList = list.value
 * ```
 */
@Composable
fun <T> useImmutableList(vararg elements: T): ImmutableListHolder<T> {
    val state = _useState(persistentListOf(*elements))

    fun mutate(mutator: (MutableList<T>) -> Unit) {
        state.value = state.value.mutate(mutator)
    }
    return remember { ImmutableListHolder(state, ::mutate) }
}

/**
 * Holder class for immutable list state and mutation operations.
 *
 * @property list The current immutable list as a [State]
 * @property mutate Function to modify the list using a mutator function
 */
@Stable
data class ImmutableListHolder<T>(
    val list: State<PersistentList<T>>,
    val mutate: (mutator: (MutableList<T>) -> Unit) -> Unit,
)
