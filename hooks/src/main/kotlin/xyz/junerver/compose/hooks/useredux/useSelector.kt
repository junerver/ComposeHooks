package xyz.junerver.compose.hooks.useredux

import androidx.compose.runtime.Composable
import xyz.junerver.compose.hooks.useContext

/**
 * Use selector
 *
 * @param T
 * @return
 */
@Composable
inline fun <reified T> useSelector(): T {
    val map = useContext(context = ReduxContext)
    return map.first[T::class] as T
}

/**
 * Use selector, by pass [block], you can also select part of state
 * class,to use in your component
 *
 * @param block
 * @param T
 * @param R
 * @return
 * @receiver
 */
@Composable
inline fun <reified T, R> useSelector(block: T.() -> R): R {
    val map = useContext(context = ReduxContext)
    return (map.first[T::class] as T).run(block)
}
