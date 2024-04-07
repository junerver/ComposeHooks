package xyz.junerver.compose.hooks.useredux

import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import xyz.junerver.compose.hooks.Dispatch
import xyz.junerver.compose.hooks.useAsync
import xyz.junerver.compose.hooks.useContext

/**
 * Use dispatch, Through this hook, you can easily obtain the global
 * dispatch function
 *
 * @param A
 * @return
 */
@Composable
inline fun <reified A> useDispatch(alias: String? = null): Dispatch<A> =
    alias?.let {
        useContext(context = ReduxContext).third[alias]?.second as? Dispatch<A> ?: registerErr()
    }
        ?: useContext(context = ReduxContext).second[A::class] as? Dispatch<A> ?: registerErr()

typealias DispatchAsync<A> = (block: suspend CoroutineScope.(Dispatch<A>) -> A) -> Unit
private typealias DispatchCallback<A> = (Dispatch<A>) -> Unit

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
