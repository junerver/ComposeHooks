package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
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

@Stable
class ImmutableListHolder<T>(val list: MutableState<PersistentList<T>>) {
    fun mutate(mutator: (MutableList<T>) -> Unit) {
        list.value = list.value.mutate(mutator)
    }
}

@Composable
fun <T> useImmutableList(vararg elements: T): ImmutableListHolder<T> {
    val state = _useState(persistentListOf(*elements))
    return remember { ImmutableListHolder(state) }
}
