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
inline fun <reified T> useSelector(alias: String? = null): T =
    alias?.let {
        useContext(context = ReduxContext).third[alias]!!.first as? T ?: registerErr()
    } ?: useContext(
        context = ReduxContext
    ).first[T::class] as? T ?: registerErr()

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
inline fun <reified T, R> useSelector(alias: String? = null, block: T.() -> R) =
    useSelector<T>(alias).run(block)
