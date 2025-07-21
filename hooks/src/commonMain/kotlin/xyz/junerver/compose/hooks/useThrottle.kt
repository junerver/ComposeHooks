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
import xyz.junerver.compose.hooks.utils.currentTime

/*
  Description:
  Author: Junerver
  date: 2024/1/30-11:02
  Email: junerver@gmail.com
  Version: v1.0

  Update: 2025/7/16-10:00 by Junerver
  Version: v1.1
  Description: fix leading
 */

/**
 * Options for configuring throttle behavior.
 *
 * This class provides configuration options for the throttle functionality,
 * allowing you to customize the delay time and execution behavior.
 *
 * @property wait The time to delay between executions
 * @property leading Whether to execute on the leading edge of the timeout
 * @property trailing Whether to execute on the trailing edge of the timeout
 *
 * @example
 * ```kotlin
 * val options = ThrottleOptions {
 *     wait = 2.seconds
 *     leading = true
 *     trailing = false
 * }
 * ```
 */
@Stable
data class ThrottleOptions internal constructor(
    var wait: Duration = 1.seconds,
    var leading: Boolean = true,
    var trailing: Boolean = true,
) {
    companion object : Options<ThrottleOptions>(::ThrottleOptions)
}

/**
 * Internal implementation of the throttle functionality.
 *
 * This class handles the throttling logic, ensuring that a function is not
 * called more frequently than the specified wait time.
 *
 * @param fn The function to throttle
 * @param scope The coroutine scope for managing async operations
 * @param options The throttle configuration options
 */
@Stable
internal class Throttle<TParams>(
    var fn: VoidFunction<TParams>,
    private val scope: CoroutineScope,
    private val options: ThrottleOptions = ThrottleOptions(),
) {
    private var timeoutJob: Job? = null
    private var lastArgs: TParams? = null
    private var latestInvokedTime = Instant.DISTANT_PAST

    private fun trailingEdge() {
        if (options.trailing) {
            lastArgs?.let { fn(it) }
            latestInvokedTime = currentTime
        }
        timeoutJob = null
    }

    fun cancel() {
        timeoutJob?.cancel()
        timeoutJob = null
        latestInvokedTime = Instant.DISTANT_PAST
    }

    fun invoke(p1: TParams) {
        val (wait, leading, trailing) = options
        val now = currentTime
        lastArgs = p1

        if (latestInvokedTime == Instant.DISTANT_PAST && !leading) {
            latestInvokedTime = now
        }

        val remaining = wait - (now - latestInvokedTime)

        if (remaining <= Duration.ZERO || remaining > wait) {
            timeoutJob?.cancel()
            timeoutJob = null
            latestInvokedTime = now
            if (leading) {
                fn(p1)
            } else if (trailing) {
                // If leading is false, we still need to schedule a trailing call
                // to ensure the function is called after the cooldown period.
                timeoutJob = scope.launch {
                    delay(wait)
                    trailingEdge()
                }
            }
        } else if (timeoutJob == null && trailing) {
            timeoutJob = scope.launch {
                delay(remaining)
                trailingEdge()
            }
        }
    }
}

/**
 * A hook for throttling state updates.
 *
 * This hook ensures that state updates are not processed more frequently than
 * the specified wait time. It's useful for handling rapid state changes, such
 * as scroll events or search input.
 *
 * @param value The value to throttle
 * @param optionsOf A lambda to configure throttle options
 * @return A [State] containing the throttled value
 *
 * @example
 * ```kotlin
 * val searchQuery = useThrottle("") {
 *     wait = 500.milliseconds
 *     leading = true
 *     trailing = true
 * }
 *
 * // Update value (will be throttled)
 * searchQuery.value = "new query"
 * ```
 */
@Composable
fun <S> useThrottle(value: S, optionsOf: ThrottleOptions.() -> Unit = {}): State<S> = useThrottle(value, useDynamicOptions(optionsOf))

/**
 * A hook for creating a throttled function.
 *
 * This hook returns a function that will not be called more frequently than
 * the specified wait time. It's useful for handling rapid user interactions.
 *
 * @param fn The function to throttle
 * @param optionsOf A lambda to configure throttle options
 * @return A throttled version of the input function
 *
 * @example
 * ```kotlin
 * val throttledScroll = useThrottleFn({
 *     // Handle scroll event
 *     updateScrollPosition()
 * }) {
 *     wait = 100.milliseconds
 * }
 *
 * // Use in scroll listener
 * Box(
 *     modifier = Modifier
 *         .scrollable(
 *             orientation = Orientation.Vertical,
 *             state = rememberScrollableState { delta ->
 *                 throttledScroll()
 *             }
 *         )
 * )
 * ```
 */
@Composable
fun <TParams> useThrottleFn(fn: VoidFunction<TParams>, optionsOf: ThrottleOptions.() -> Unit = {}): VoidFunction<TParams> =
    useThrottleFn(fn, useDynamicOptions(optionsOf))

/**
 * A hook for throttling effect execution.
 *
 * This hook ensures that an effect is not executed more frequently than
 * the specified wait time. It's useful for handling rapid dependency changes.
 *
 * @param keys The dependencies that trigger the effect
 * @param optionsOf A lambda to configure throttle options
 * @param block The effect to throttle
 *
 * @example
 * ```kotlin
 * useThrottleEffect(
 *     keys = arrayOf(searchQuery),
 *     optionsOf = {
 *         wait = 1.seconds
 *     }
 * ) {
 *     // Perform search
 *     searchItems(searchQuery)
 * }
 * ```
 */
@Composable
fun useThrottleEffect(vararg keys: Any?, optionsOf: ThrottleOptions.() -> Unit = {}, block: SuspendAsyncFn) = useThrottleEffect(
    keys = keys,
    useDynamicOptions(optionsOf),
    block = block,
)

/**
 * Internal implementation of the throttled state hook.
 *
 * @param value The value to throttle
 * @param options The throttle configuration options
 * @return A [State] containing the throttled value
 */
@Composable
private fun <S> useThrottle(value: S, options: ThrottleOptions): State<S> {
    val (throttled, setThrottled) = _useGetState(value)
    val throttledSet = useThrottleFn<None>(
        fn = {
            setThrottled(value)
        },
        options,
    )
    useEffect(value) {
        throttledSet()
    }
    return throttled
}

/**
 * Internal implementation of the throttled function hook.
 *
 * @param fn The function to throttle
 * @param options The throttle configuration options
 * @return A throttled version of the input function
 */
@Composable
private fun <TParams> useThrottleFn(fn: VoidFunction<TParams>, options: ThrottleOptions): VoidFunction<TParams> {
    val latestFn by useLatestState(value = fn)
    val scope = rememberCoroutineScope()
    val throttled = remember {
        Throttle(latestFn, scope, options)
    }.apply { this.fn = latestFn }
    return remember { { p1: TParams -> throttled.invoke(p1) } }
}

/**
 * Internal implementation of the throttled effect hook.
 *
 * @param keys The dependencies that trigger the effect
 * @param options The throttle configuration options
 * @param block The effect to throttle
 */
@Composable
private fun useThrottleEffect(vararg keys: Any?, options: ThrottleOptions, block: SuspendAsyncFn) {
    val throttledBlock = useThrottleFn<CoroutineScope>(
        fn = { coroutineScope ->
            coroutineScope.launch {
                this.block()
            }
        },
        options,
    )
    val scope = rememberCoroutineScope()
    useEffect(*keys) {
        throttledBlock(scope)
    }
}
