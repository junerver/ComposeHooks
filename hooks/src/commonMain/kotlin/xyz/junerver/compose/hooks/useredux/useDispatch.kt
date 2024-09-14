package xyz.junerver.compose.hooks.useredux

import androidx.compose.runtime.Composable
import xyz.junerver.compose.hooks.Dispatch
import xyz.junerver.compose.hooks.DispatchAsync
import xyz.junerver.compose.hooks.DispatchCallback
import xyz.junerver.compose.hooks.useAsync
import xyz.junerver.compose.hooks.useContext

/**
 * Use dispatch, Through this hook, you can easily obtain the global
 * dispatch function
 *
 * @param A
 * @return
 */
@Suppress("UNCHECKED_CAST")
@Composable
inline fun <reified A> useDispatch(alias: String? = null): Dispatch<A> = alias?.let {
    useContext(context = ReduxContext).third[it]?.second as? Dispatch<A> ?: registerErr("alias:<$alias>")
}
    ?: useContext(context = ReduxContext).second[A::class] as? Dispatch<A> ?: registerErr("type:<${A::class.qualifiedName}>")


/**
 * Get a dispatch function that supports asynchronous execution. This
 * function receives a suspend function whose return value is Action as a
 * parameter.
 *
 * @param alias
 * @param onBefore
 * @param onFinally
 */
@Composable
inline fun <reified A> useDispatchAsync(
    alias: String? = null,
    noinline onBefore: DispatchCallback<A>? = null,
    noinline onFinally: DispatchCallback<A>? = null,
): DispatchAsync<A> {
    val dispatch: Dispatch<A> = useDispatch(alias)
    val asyncRun = useAsync()
    return { block ->
        onBefore?.invoke(dispatch)
        asyncRun {
            dispatch(block(dispatch))
        }
        onFinally?.invoke(dispatch)
    }
}
