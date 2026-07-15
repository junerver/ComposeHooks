package xyz.junerver.compose.hooks.uselist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import xyz.junerver.compose.hooks.uselatest.useLatestRefImpl
import xyz.junerver.compose.hooks.usestate.useStateImpl

/*
  Description: More convenient to use dynamic List state
  Author: Junerver
  Date: 2024/3/7-15:02
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun <T> useListImpl(elements: Collection<T>): SnapshotStateList<T> = remember {
    elements.toMutableStateList()
}

@Composable
fun <T> useListImpl(vararg elements: T): SnapshotStateList<T> = remember {
    elements.asList().toMutableStateList()
}

/**
 * Reactive List.reduce.
 */
@Composable
fun <S, T : S> useListReduceImpl(list: List<T>, operation: (acc: S, T) -> S): State<S?> {
    val currentList = useLatestRefImpl(list)
    val currentListIdentity = useListIdentityState(list)
    val currentOperation = useIdentityState(operation)
    return useStateImpl {
        currentListIdentity.value
        currentList.current.reduceOrNull(currentOperation.value)
    }
}

@Composable
private fun <T> useListIdentityState(list: List<T>): State<ListIdentitySnapshot<T>> {
    val nextSnapshot = ListIdentitySnapshot(list)
    val state = remember { mutableStateOf(nextSnapshot) }
    state.value = nextSnapshot
    return state
}

@Composable
private fun <T> useIdentityState(value: T): State<T> {
    val state = remember { mutableStateOf(value, referentialEqualityPolicy()) }
    state.value = value
    return state
}

private class ListIdentitySnapshot<T>(list: List<T>) {
    private val elements = list.toList()

    override fun equals(other: Any?): Boolean =
        other is ListIdentitySnapshot<*> &&
            elements.size == other.elements.size &&
            elements.indices.all { index -> (elements[index] as Any?) === other.elements[index] }

    override fun hashCode(): Int = elements.size
}
