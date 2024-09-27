package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
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

@Stable
class ImmutableListHolder<T>(val list: MutableState<PersistentList<T>>) {
    fun mutate(mutator: (MutableList<T>) -> Unit) {
        list.value = list.value.mutate(mutator)
    }
}

/**
 * 这个 hook 不同于 [useList]。
 *
 * 使用 [useList]，你将会得到一个[SnapshotStateList]，它能带来类似
 * 操作 [MutableList] 一样的体验。但是你无法使用直接使用 [useEffect]
 * 去监听他的变化，需要通过[SnapshotStateList.toList]，触发副作用。
 *
 * 但是使用[useImmutableList]，你无需考虑那么多，直接当作一般的状态即可，
 * 调用[ImmutableListHolder.mutate]函数，操作不可变列表即可。
 */
@Composable
fun <T> useImmutableList(vararg elements: T): ImmutableListHolder<T> {
    val state = _useState(persistentListOf(*elements))
    return remember { ImmutableListHolder(state) }
}
