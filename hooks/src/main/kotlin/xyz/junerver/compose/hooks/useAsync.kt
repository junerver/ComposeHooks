package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * This hook function accepts a suspend function as a parameter and returns
 * an function(use to execute suspend function). When you call the execution function,
 * the suspend function will be executed within the coroutine scope of the current component.
 *
 * @param fn
 * @return
 * @receiver
 */
@Composable
fun useAsync(fn: SuspendAsyncFn): () -> Unit {
    val latestFn by useLatestState(value = fn)
    val scope = rememberCoroutineScope()
    val async = useCreation {
        Async(latestFn, scope)
    }.apply { this.current.fn = latestFn }.current
    return async
}

internal typealias AsyncRunFn = (SuspendAsyncFn) -> Unit

/**
 * This is a hook function that simplifies the use of coroutine scope.
 * It holds the coroutine scope through an object to achieve a usage similar
 * to the [run] function. Equivalent to `scope.launch { }`
 * ```
 * val asyncRun = useAsync()
 * asyncRun {
 *   // do something
 * }
 *
 * ```
 *
 * @return
 */
@Composable
fun useAsync(): AsyncRunFn {
    val scope = rememberCoroutineScope()
    val async = useCreation {
        AsyncRun(scope)
    }.current
    return async
}

private class Async(
    var fn: SuspendAsyncFn,
    private val scope: CoroutineScope,
) : () -> Unit {
    override fun invoke() {
        scope.launch { fn() }
    }
}

private class AsyncRun(
    private val scope: CoroutineScope,
) : AsyncRunFn {
    override fun invoke(p1: SuspendAsyncFn) {
        scope.launch { p1() }
    }
}
