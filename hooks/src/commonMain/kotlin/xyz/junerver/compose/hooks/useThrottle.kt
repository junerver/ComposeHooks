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
 * Throttle options
 *
 * @constructor Create empty Throttle options
 * @property wait time to delay
 * @property leading Specify invoking on the leading edge of the timeout.
 * @property trailing Specify invoking on the trailing edge of the timeout.
 */
@Stable
data class ThrottleOptions internal constructor(
    var wait: Duration = 1.seconds,
    var leading: Boolean = true,
    var trailing: Boolean = true,
) {
    companion object : Options<ThrottleOptions>(::ThrottleOptions)
}

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

@Composable
fun <S> useThrottle(value: S, optionsOf: ThrottleOptions.() -> Unit = {}): State<S> =
    useThrottle(value, remember { ThrottleOptions.optionOf(optionsOf) })

@Composable
fun useThrottleFn(fn: VoidFunction, optionsOf: ThrottleOptions.() -> Unit = {}): VoidFunction =
    useThrottleFn(fn, remember { ThrottleOptions.optionOf(optionsOf) })

@Composable
fun useThrottleEffect(vararg keys: Any?, optionsOf: ThrottleOptions.() -> Unit = {}, block: SuspendAsyncFn) = useThrottleEffect(
    keys = keys,
    remember { ThrottleOptions.optionOf(optionsOf) },
    block = block
)

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

@Composable
private fun useThrottleFn(fn: VoidFunction, options: ThrottleOptions = remember { ThrottleOptions() }): VoidFunction {
    val latestFn by useLatestState(value = fn)
    val scope = rememberCoroutineScope()
    val throttled = remember {
        Throttle(latestFn, scope, options)
    }.apply { this.fn = latestFn }
    return remember { { p1 -> throttled.invoke(p1) } }
}

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
