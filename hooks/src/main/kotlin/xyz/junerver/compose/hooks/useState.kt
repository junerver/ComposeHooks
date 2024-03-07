package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
inline fun <reified T> useState(default: T & Any): Tuple2<T, (T) -> Unit> {
    return when (default) {
        is Int -> useInt(default)
        is Float -> useFloat(default)
        is Double -> useDouble(default)
        is Long -> useLong(default)
        else -> _useState(default)
    } as Tuple2<T, (T) -> Unit>
}

/**
 * 这是一个可空的[useState]，如果对象的状态可能为空，应该使用它
 */
@Composable
fun <T> _useState(default: T): Tuple2<T, (T) -> Unit> {
    val (state, setState) = remember {
        mutableStateOf(default)
    }
    return state to setState
}

@Deprecated("change fun name", ReplaceWith("useInt(default)"))
@Composable
fun useIntState(default: Int = 0) = useInt(default)

@Deprecated("change fun name", ReplaceWith("useList(elements)"))
@Composable
fun <T> useListState(elements: Collection<T>) = useList(elements)

@Deprecated("change fun name", ReplaceWith("useList(elements)"))
@Composable
fun <T> useListState(vararg elements: T) = useList(elements = elements)

@Deprecated("change fun name", ReplaceWith("useMap(pairs)"))
@Composable
fun <K, V> useMapState(vararg pairs: Pair<K, V>) = useMap(pairs = pairs)

@Deprecated("change fun name", ReplaceWith("useMap(pairs)"))
@Composable
fun <K, V> useMapState(pairs: Iterable<Pair<K, V>>) = useMap(pairs)
