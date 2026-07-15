package xyz.junerver.compose.hooks.useimmutablelist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.remember
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentListOf
import xyz.junerver.compose.hooks.usecreation.useCreationImpl
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
    val initialList = useCreationImpl { persistentListOf(*elements) }.current
    val state = _useStateImpl(initialList)

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
fun <S, T : S> useImmutableListReduceImpl(list: PersistentList<T>, operation: (acc: S, T) -> S): State<S> {
    val currentList = useIdentityState(list)
    val currentOperation = useIdentityState(operation)
    return useStateImpl {
        currentList.value.reduce(currentOperation.value)
    }
}

/**
 * Reactive List.reduce that returns null for empty lists.
 */
@Composable
fun <S, T : S> useImmutableListReduceOrNullImpl(list: PersistentList<T>, operation: (acc: S, T) -> S): State<S?> {
    val currentList = useIdentityState(list)
    val currentOperation = useIdentityState(operation)
    return useStateImpl {
        currentList.value.reduceOrNull(currentOperation.value)
    }
}

/**
 * Reactive List.fold with an explicit initial value.
 */
@Composable
fun <S, T> useImmutableListFoldImpl(list: PersistentList<T>, initial: S, operation: (acc: S, T) -> S): State<S> {
    val currentList = useIdentityState(list)
    val currentInitial = useIdentityState(initial)
    val currentOperation = useIdentityState(operation)
    return useStateImpl {
        currentList.value.fold(currentInitial.value, currentOperation.value)
    }
}

@Composable
private fun <T> useIdentityState(value: T): State<T> {
    val state = remember { mutableStateOf(value, referentialEqualityPolicy()) }
    state.value = value
    return state
}
