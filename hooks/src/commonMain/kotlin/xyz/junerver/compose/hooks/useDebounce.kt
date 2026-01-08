package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.utils.currentInstant

/*
  Description: Debounce hooks for Compose
  Author: Junerver
  Date: 2024/1/29-14:46
  Email: junerver@gmail.com
  Version: v1.0

  Update: 2025/7/15-18:54 by Junerver
  Version: v1.1
  Description: fix leading
*/

/**
 * Debounce options
 *
 * @constructor Create empty Debounce options
 * @property wait time to delay
 * @property leading Specify invoking on the leading edge of the timeout.
 * @property trailing Specify invoking on the trailing edge of the timeout.
 * @property maxWait The maximum time func is allowed to be delayed before
 *    it’s invoked.
 */
@Stable
data class UseDebounceOptions internal constructor(
    var wait: Duration = 1.seconds,
    var leading: Boolean = false,
    var trailing: Boolean = true,
    var maxWait: Duration = Duration.ZERO,
) {
    companion object : Options<UseDebounceOptions>(::UseDebounceOptions)
}

/**
 * Internal implementation of debounce functionality.
 *
 * This class handles the core debounce logic, managing timeouts and function execution
 * according to the specified options.
 */
@Stable
internal class Debounce<TParams>(
    var fn: VoidFunction<TParams>,
    private val scope: CoroutineScope,
    private val options: UseDebounceOptions = UseDebounceOptions(),
    private val now: () -> Instant = { currentInstant },
) {
    private var timeoutJob: Job? = null // Job for tracking delayed tasks
    private var latestInvokedTime = Instant.DISTANT_PAST
    private var latestCalledTime = latestInvokedTime
    private var lastArgs: TParams? = null // Store the latest parameters for trailing execution

    // Flag to manage whether leading execution is allowed
    // When a debounce cycle ends, isAwaitingNextDebounce is set to true, indicating the next leading can be triggered
    private var isAwaitingNextDebounce: Boolean = true

    private fun cancelTimeout() {
        timeoutJob?.cancel()
        timeoutJob = null
    }

    // The actual function execution
    private fun executeFn(params: TParams?) {
        params?.let { fn(it) }
        latestInvokedTime = now()
    }

    private fun resetDebounceState() {
        isAwaitingNextDebounce = true
        timeoutJob = null // 清空 job
    }

    fun invoke(p1: TParams) {
        val (wait, leading, trailing, maxWait) = options
        lastArgs = p1 // Save the latest parameters on each call

        val currentTimeStamp = now()
        if (latestInvokedTime == Instant.DISTANT_PAST) {
            latestInvokedTime = currentTimeStamp
        }
        val waitTime = currentTimeStamp - latestInvokedTime

        val isMaxWaitExceeded = maxWait > Duration.ZERO && waitTime >= maxWait

        val shouldInvokeImmediately = leading && isAwaitingNextDebounce

        latestCalledTime = currentTimeStamp // Update the latest call time
        cancelTimeout() // Cancel any previous delayed tasks
        // 1. If leading is set and the next leading trigger is allowed, execute immediately
        if (shouldInvokeImmediately) {
            executeFn(p1)
            isAwaitingNextDebounce = false // After leading triggers, no more triggers in the current debounce cycle
            timeoutJob = scope.launch {
                delay(wait)
                resetDebounceState()
            }
        } else {
            // 2. If not executing immediately, cancel previous delayed tasks and set up new ones
            timeoutJob = scope.launch {
                delay(wait)
                // Check if maximum wait time is exceeded or wait time has passed
                // And ensure that no new calls occurred at the end of the delay
                if ((now() - latestCalledTime >= wait || isMaxWaitExceeded) && trailing) {
                    executeFn(lastArgs)
                }
                resetDebounceState()
            }
            // 3. If maxWait is reached, execute immediately regardless of leading/trailing
            if (isMaxWaitExceeded) {
                // If there's a pending trailing task, cancel it and execute immediately
                cancelTimeout()
                executeFn(lastArgs)
                isAwaitingNextDebounce = false // do not allow next leading after execution
                timeoutJob = scope.launch {
                    delay(wait)
                    resetDebounceState()
                }
            }
        }
    }
}

/**
 * A hook that returns a debounced value of the input value.
 *
 * This hook is useful when you want to limit how often a function is called,
 * especially for expensive operations like API calls or DOM updates.
 *
 * @param value The value to be debounced
 * @param optionsOf Lambda with receiver to configure debounce options
 * @return A State containing the debounced value
 */
@Composable
fun <S> useDebounce(value: S, optionsOf: UseDebounceOptions.() -> Unit = {}): State<S> = useDebounce(value, useDynamicOptions(optionsOf))

/**
 * A hook that returns a debounced version of the provided function.
 *
 * This hook creates a debounced function that delays invoking the provided function
 * until after a specified wait time has elapsed since the last time it was invoked.
 *
 * @param fn The function to debounce
 * @param optionsOf Lambda with receiver to configure debounce options
 * @return A debounced version of the provided function
 */
@Composable
fun <TParams> useDebounceFn(fn: VoidFunction<TParams>, optionsOf: UseDebounceOptions.() -> Unit = {}): VoidFunction<TParams> =
    useDebounceFn(fn, useDynamicOptions(optionsOf))

/**
 * A hook that applies debounce to an effect function.
 *
 * This hook is similar to useEffect but with debounce functionality applied.
 * The effect will only run after the specified wait time has elapsed since the last dependency change.
 *
 * @param keys Array of dependencies that will trigger the debounced effect when changed
 * @param optionsOf Lambda with receiver to configure debounce options
 * @param block The suspend function to be executed as the debounced effect
 */
@Composable
fun useDebounceEffect(vararg keys: Any?, optionsOf: UseDebounceOptions.() -> Unit = {}, block: SuspendAsyncFn) = useDebounceEffect(
    keys = keys,
    useDynamicOptions(optionsOf),
    block,
)

/**
 * Internal implementation of the useDebounce hook.
 *
 * This function creates a debounced version of the provided value, which will only update
 * after the specified wait time has elapsed since the last value change.
 *
 * @param value The value to be debounced
 * @param options Debounce configuration options
 * @return A State containing the debounced value
 */
@Composable
private fun <S> useDebounce(value: S, options: UseDebounceOptions): State<S> {
    // Create a state to hold the debounced value, using _useGetState to avoid closure problems
    val (debounced, setDebounced) = _useGetState(value)
    val debouncedSet = useDebounceFn<None>(
        fn = {
            setDebounced(value)
        },
        options,
    )
    useEffect(value) {
        debouncedSet()
    }
    return debounced
}

/**
 * Internal implementation of the useDebounceFn hook.
 *
 * Note: [Debounce] does not return calculation results. In Compose, we cannot use [Debounce] to pass through
 * calculation results directly. Instead, we should use state rather than the return value of [Debounce].
 * For example, if we have a calculation function, we should set up a state to store the result.
 * The result after function calculation should be passed by calling the corresponding `setState(state:T)` function,
 * ensuring that the calculation result (state) is decoupled from the calculation process.
 * This way, our [Debounce] can be seamlessly integrated.
 */
@Composable
private fun <TParams> useDebounceFn(fn: VoidFunction<TParams>, options: UseDebounceOptions): VoidFunction<TParams> {
    val latestFn by useLatestState(value = fn)
    val scope = rememberCoroutineScope()
    val debounced = remember {
        Debounce(latestFn, scope, options)
    }.apply { this.fn = latestFn }
    return remember { { p1 -> debounced.invoke(p1) } }
}

/**
 * Internal implementation of the useDebounceEffect hook.
 *
 * This function applies debounce functionality to an effect, ensuring the effect only runs
 * after the specified wait time has elapsed since the last dependency change.
 *
 * @param keys Array of dependencies that will trigger the debounced effect when changed
 * @param options Debounce configuration options
 * @param block The suspend function to be executed as the debounced effect
 */
@Composable
private fun useDebounceEffect(vararg keys: Any?, options: UseDebounceOptions, block: SuspendAsyncFn) {
    val debouncedBlock = useDebounceFn<CoroutineScope>(
        fn = { coroutineScope ->
            coroutineScope.launch {
                this.block()
            }
        },
        options,
    )
    val scope = rememberCoroutineScope()
    useEffect(*keys) {
        debouncedBlock(scope)
    }
}
