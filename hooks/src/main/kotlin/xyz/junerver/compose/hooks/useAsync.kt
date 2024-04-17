package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Use async
 *
 * @param fn
 * @return
 * @receiver
 */
@Composable
fun useAsync(fn: SuspendAsyncFn): () -> Unit {
    val latestFn by useLatestState(value = fn)
    val scope = rememberCoroutineScope()
    val async = remember {
        Async(latestFn, scope)
    }.apply { this.fn = latestFn }
    return async
}

internal typealias AsyncRunFn = (SuspendAsyncFn) -> Unit

/**
 * Use async
 *
 * @return
 */
@Composable
fun useAsync(): AsyncRunFn {
    val scope = rememberCoroutineScope()
    val async = remember {
        AsyncRun(scope)
    }
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
