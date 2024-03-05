package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.toMutableStateList
import androidx.compose.runtime.toMutableStateMap
import xyz.junerver.kotlin.Tuple2

/**
 * Description: 在compose中使用state需要写一点模板代码，虽然谈不上有多麻烦，但是还是能简化一下的；
 * 返回值是 tuple，React风格，需要解构后使用。如果是可空对象需要显示声明：
 * ```
 * val (state,setState) = useState<Boolean?>(null)
 * ```
 * 这样做还有一个好处就是减少闭包层级，我们可以轻松的构造一些简单的受控组件：
 * ```
 * val (state, setState) = useState("")
 * OutlinedTextField(
 *     value = name,
 *     onValueChange = setState,
 *     label = { Text("Name") }
 * )
 * ```
 * @author Junerver
 * date: 2024/2/29-9:00
 * Email: junerver@gmail.com
 * Version: v1.0
 */
@Composable
fun <T> useState(default: T): Tuple2<T, (T) -> Unit> {
    val (state, setState) = remember {
        mutableStateOf(default)
    }
    return state to setState
}

@Composable
fun useIntState(default: Int = 0): Tuple2<Int, (Int) -> Unit> {
    val (state, setState) = remember {
        mutableIntStateOf(default)
    }
    return state to setState
}

@Composable
fun <T> useListState(elements: Collection<T>): SnapshotStateList<T> {
    val list = remember {
        elements.toMutableStateList()
    }
    return list
}

@Composable
fun <T> useListState(vararg elements: T): SnapshotStateList<T> {
    val list = remember {
        mutableStateListOf(*elements)
    }
    return list
}

@Composable
fun <K, V> useMapState(vararg pairs: Pair<K, V>): SnapshotStateMap<K, V> {
    val map = remember {
        mutableStateMapOf(*pairs)
    }
    return map
}

@Composable
fun <K, V> useMapState(pairs: Iterable<Pair<K, V>>): SnapshotStateMap<K, V> {
    val map = remember {
        pairs.toMutableStateMap()
    }
    return map
}
