package xyz.junerver.compose.hooks.useredux

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.State
import xyz.junerver.compose.hooks.useContext
import xyz.junerver.compose.hooks.useState

/**
 * Use selector
 *
 * @param T
 * @return
 */
@Composable
inline fun <reified T> useSelector(alias: String? = null): State<T> = alias?.let {
    useContext(context = ReduxContext).aliasMap[it]?.first as? State<T> ?: registerErr("alias:<$alias>")
}
    ?: useContext(context = ReduxContext).stateMap[T::class] as? State<T> ?: registerErr("type:<${T::class.qualifiedName}>")

/**
 * Use selector, by pass [block], you can also select part of state
 * class,to use in your component
 *
 * ```kotlin
 * data class SimpleData(
 *     val name: String,
 *     val age: Int,
 * )
 *
 * //...
 * val age by useSelector<SimpleData, String> { "age : $age" }
 * ```
 *
 *
 * @param block
 * @param T
 * @param R
 * @return
 * @receiver
 */
@Composable
inline fun <reified T, R> useSelector(alias: String? = null, crossinline block: @DisallowComposableCalls T.() -> R): State<R> {
    val state = useSelector<T>(alias)
    return useState { state.value.block() }
}
