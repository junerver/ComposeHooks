package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import xyz.junerver.compose.hooks.utils.currentTime

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
internal class Throttle(
    var fn: VoidFunction,
    private val scope: CoroutineScope,
    private val options: ThrottleOptions = ThrottleOptions(),
) {
    private var calledCount = 0
    private val trailingJobs: MutableList<Job> = arrayListOf()
    private var latestInvokedTime = Instant.DISTANT_PAST

    private fun clearTrailing() {
        if (trailingJobs.isNotEmpty()) {
            trailingJobs.forEach {
                it.cancel()
            }
            trailingJobs.clear()
        }
    }

    fun invoke(p1: TParams) {
        val (wait, leading, trailing) = options
        val waitTime = currentTime - latestInvokedTime

        fun task(isTrailing: Boolean) {
            scope.launch(start = CoroutineStart.DEFAULT) {
                if (isTrailing) delay(wait)
                fn(p1)
                if (isTrailing) latestInvokedTime = currentTime
            }.also {
                if (isTrailing) {
                    trailingJobs.add(it)
                }
            }
        }
        if (waitTime > wait) {
            task(isTrailing = calledCount == 0 && !leading)
            latestInvokedTime = currentTime
        } else {
            if (trailing) {
                clearTrailing()
                task(isTrailing = true)
            }
        }
        calledCount++
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
fun <S> useThrottle(value: S, optionsOf: ThrottleOptions.() -> Unit = {}): State<S> =
    useThrottle(value, remember { ThrottleOptions.optionOf(optionsOf) })

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
fun useThrottleFn(fn: VoidFunction, optionsOf: ThrottleOptions.() -> Unit = {}): VoidFunction =
    useThrottleFn(fn, remember { ThrottleOptions.optionOf(optionsOf) })

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
    remember { ThrottleOptions.optionOf(optionsOf) },
    block = block
)

/**
 * Internal implementation of the throttled state hook.
 *
 * @param value The value to throttle
 * @param options The throttle configuration options
 * @return A [State] containing the throttled value
 */
@Composable
private fun <S> useThrottle(value: S, options: ThrottleOptions = remember { ThrottleOptions() }): State<S> {
    val (throttled, setThrottled) = _useGetState(value)
    val throttledSet = useThrottleFn(fn = {
        setThrottled(value)
    }, options)
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
private fun useThrottleFn(fn: VoidFunction, options: ThrottleOptions = remember { ThrottleOptions() }): VoidFunction {
    val latestFn by useLatestState(value = fn)
    val scope = rememberCoroutineScope()
    val throttled = remember {
        Throttle(latestFn, scope, options)
    }.apply { this.fn = latestFn }
    return remember { { p1 -> throttled.invoke(p1) } }
}

/**
 * Internal implementation of the throttled effect hook.
 *
 * @param keys The dependencies that trigger the effect
 * @param options The throttle configuration options
 * @param block The effect to throttle
 */
@Composable
private fun useThrottleEffect(vararg keys: Any?, options: ThrottleOptions = remember { ThrottleOptions() }, block: SuspendAsyncFn) {
    val throttledBlock = useThrottleFn(fn = { params ->
        (params[0] as CoroutineScope).launch {
            this.block()
        }
    }, options)
    val scope = rememberCoroutineScope()
    useEffect(*keys) {
        throttledBlock(scope)
    }
}
