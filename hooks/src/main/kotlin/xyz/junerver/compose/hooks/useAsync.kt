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
fun useAsync(fn: suspend CoroutineScope.() -> Unit): () -> Unit {
    val latestFn by useLatestState(value = fn)
    val scope = rememberCoroutineScope()
    val async = remember {
        Async(latestFn, scope)
    }.apply { this.fn = latestFn }
    return async
}

typealias AsyncRunFn = (suspend CoroutineScope.() -> Unit) -> Unit

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

internal class Async(
    var fn: suspend CoroutineScope.() -> Unit,
    private val scope: CoroutineScope,
) : () -> Unit {
    override fun invoke() {
        scope.launch { fn() }
    }
}

internal class AsyncRun(
    private val scope: CoroutineScope,
) : AsyncRunFn {
    override fun invoke(p1: suspend CoroutineScope.() -> Unit) {
        scope.launch { p1() }
    }
}
