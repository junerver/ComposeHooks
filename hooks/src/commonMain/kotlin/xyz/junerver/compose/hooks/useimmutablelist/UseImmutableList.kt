package xyz.junerver.compose.hooks.useimmutablelist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentListOf
import xyz.junerver.compose.hooks.usestate._useStateImpl
import xyz.junerver.compose.hooks.usestate.useStateImpl

/**
 * @Author Junerver
 * @Date 2024/9/27-19:24
 * @Email junerver@gmail.com
 * @Version v1.0
 * @Description
 */

@Composable
fun <T> useImmutableListImpl(vararg elements: T): ImmutableListHolder<T> {
    val state = _useStateImpl(persistentListOf(*elements))

    fun mutate(mutator: (MutableList<T>) -> Unit) {
        state.value = state.value.mutate(mutator)
    }
    return remember { ImmutableListHolder(state, ::mutate) }
}

@Stable
data class ImmutableListHolder<T>(
    val list: State<PersistentList<T>>,
    val mutate: (mutator: (MutableList<T>) -> Unit) -> Unit,
)

/**
 * Reactive List.reduce.
 */
@Composable
fun <S, T : S> useImmutableListReduceImpl(list: PersistentList<T>, operation: (acc: S, T) -> S): State<S> = useStateImpl(list) {
    list.reduce(operation)
}

/**
 * Reactive List.reduce that returns null for empty lists.
 */
@Composable
fun <S, T : S> useImmutableListReduceOrNullImpl(list: PersistentList<T>, operation: (acc: S, T) -> S): State<S?> = useStateImpl(list) {
    list.takeIf { it.isNotEmpty() }?.reduce(operation)
}

/**
 * Reactive List.fold with an explicit initial value.
 */
@Composable
fun <S, T> useImmutableListFoldImpl(list: PersistentList<T>, initial: S, operation: (acc: S, T) -> S): State<S> = useStateImpl(list, initial) {
    list.fold(initial, operation)
}
