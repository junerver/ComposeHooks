package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

/**
 * Description: 在compose中使用state需要写一点模板代码，虽然谈不上有多麻烦，但是还是能简化一下的；
 * 返回值是[MutableState]，可以解构后使用。如果是可空对象需要显示声明：
 * ```
 * val (state,setState) = useState<Boolean?>(null)
 * val otherState by useState("")
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
 * 重要提示：这种方式在便捷的同时也有俩个缺陷：
 * 1. 带来闭包问题，当我们在某个闭包函数中使用状态，会出现闭包问题，需要使用[useLatestRef]避免。
 * 2. 当你快速的（毫秒级）调用set函数时，会触发 Compose 的重组优化，导致状态丢失。
 *
 * 这种情况下，可以退回到使用 `by` 委托来获取状态对象
 *
 * ---
 *
 * Important note: While this method is convenient, it also has two flaws:
 * 1. It causes closure problems. When we use state in a closure function,
 *  closure problems will occur, need to use [useLatestRef] to avoid.
 * 2. When you call the set function quickly(millisecond level),
 *  Compose's recompose optimization will be triggered, resulting in state loss.
 *
 * In this case, you can fall back to using the `by` delegate to get the state object
 *
 * @author Junerver
 * date: 2024/2/29-9:00
 * Email: junerver@gmail.com
 * Version: v1.0
 */
@Composable
fun <T> useState(default: T & Any): MutableState<T> {
    return when (default) {
        is Int -> useInt(default)
        is Float -> useFloat(default)
        is Double -> useDouble(default)
        is Long -> useLong(default)
        else -> _useState(default)
    } as MutableState<T>
}

@Composable
fun <T> useState(vararg keys: Any, factory: () -> T) = remember(keys = keys) {
    derivedStateOf(factory)
}

/**
 * 这是一个可空的[useState]，如果对象的状态可能为空，应该使用它
 */
@Composable
fun <T> _useState(default: T) = remember {
    mutableStateOf(default)
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
