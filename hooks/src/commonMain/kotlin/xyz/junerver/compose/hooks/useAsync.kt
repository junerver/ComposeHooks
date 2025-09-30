package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * This hook function accepts a suspend function as a parameter and returns
 * a function to execute that suspend function. When you call the returned function,
 * the suspend function will be executed within the coroutine scope of the current component.
 *
 * ```kotlin
 * // Declare a suspend lambda like a normal lambda
 * val async = useAsync {
 *     delay(1.seconds)
 *     setState(state + 1)
 * }
 * // Easy to use
 * async()
 * ```
 *
 * @param block The suspend function to be executed when the returned function is called
 * @return A function that, when called, executes the provided suspend function in a coroutine scope
 */
@Composable
fun useAsync(block: SuspendAsyncFn): () -> Unit {
    val latestFn by useLatestState(value = block)
    val asyncRunFn = useAsync()
    return fun() {
        asyncRunFn {
            latestFn()
        }
    }
}

/**
 * This is a hook function that simplifies the use of coroutine scope.
 * Its usage is similar to the [run] function and is equivalent to `scope.launch { }`.
 * This overload doesn't take any parameters and returns a function that can be used
 * to execute suspend functions.
 *
 * ```kotlin
 * val asyncRun = useAsync()
 * asyncRun {
 *   // Do something asynchronously
 *   delay(1.seconds)
 *   setState { it + 1 }
 * }
 * ```
 *
 * @return A function that takes a suspend function and executes it in a coroutine scope
 */
@Composable
fun useAsync(): AsyncRunFn {
    val scope = rememberCoroutineScope()
    return fun(fn) {
        scope.launch { fn() }
    }
}

/**
 * This hook function provides a cancelable asynchronous execution mechanism.
 * It returns a holder object containing functions to run and cancel asynchronous operations,
 * as well as a state to track whether an operation is currently running.
 *
 * ```kotlin
 * // Destructure the returned holder to get the run function, cancel function, and running state
 * val (cancelableAsyncRun, cancel, isActive) = useCancelableAsync()
 *
 * // Execute an asynchronous operation
 * cancelableAsyncRun {
 *     delay(1.seconds)
 *     // Do something
 * }
 *
 * // Cancel the operation if needed
 * cancel()
 *
 * // Check if an operation is running
 * if (isActive.value) {
 *     // Operation is in progress
 * }
 * ```
 *
 * @return A holder object containing functions to run and cancel asynchronous operations,
 *         and a state to track the running status
 */
@Composable
fun useCancelableAsync(): CancelableAsyncHolder {
    val scope = rememberCoroutineScope()
    var job by useRef<Job?>(null)
    val (isActive, setIsActive) = useGetState(false)

    val asyncRun = { fn: SuspendAsyncFn ->
        job?.cancel()
        job = scope.launch {
            setIsActive(true)
            try {
                fn()
            } finally {
                setIsActive(false)
                job = null
            }
        }
    }
    val cancel = {
        job?.cancel()
        job = null
        setIsActive(false)
    }
    return remember {
        CancelableAsyncHolder(
            asyncRun,
            cancel,
            isActive,
        )
    }
}

/**
 * A holder class for cancelable asynchronous operations.
 * This class is designed to be used with the [useCancelableAsync] hook.
 *
 * @property asyncRun A function that executes the provided suspend function in a coroutine scope.
 *                    It automatically tracks the running state and stores the job for potential cancellation.
 * @property cancel A function that cancels the currently running asynchronous operation if any.
 *                 It also resets the running state to false.
 * @property isActive A state that indicates whether an asynchronous operation is currently running.
 *                    This can be used to conditionally enable/disable UI elements.
 */
@Stable
data class CancelableAsyncHolder(
    val asyncRun: (SuspendAsyncFn) -> Unit,
    val cancel: () -> Unit,
    val isActive: IsActive,
)
