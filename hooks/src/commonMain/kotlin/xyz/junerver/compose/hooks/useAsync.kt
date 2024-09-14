package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

/**
 * This hook function accepts a suspend function as a parameter and returns
 * a function(use to execute suspend function). When you call the execution function,
 * the suspend function will be executed within the coroutine scope of the current component.
 *
 * ```kotlin
 * // Declare an suspend lambda like a normal lambda
 * val async = useAsync {
 *     delay(1.seconds)
 *     setState(state + 1)
 * }
 * // easy to use
 * async()
 * ```
 *
 * @param fn
 * @return
 * @receiver
 */
@Composable
fun useAsync(fn: SuspendAsyncFn): () -> Unit {
    val latestFn by useLatestState(value = fn)
    val scope = rememberCoroutineScope()
    return fun () {
        scope.launch {
            latestFn()
        }
    }
}

/**
 * This is a hook function that simplifies the use of coroutine scope.
 * It's usage similar to the [run] function. Equivalent to `scope.launch { }`
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
    return fun(fn) {
        scope.launch { fn() }
    }
}
