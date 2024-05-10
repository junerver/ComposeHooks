package xyz.junerver.compose.hooks

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Throttle options
 *
 * @property wait time to delay
 * @property leading Specify invoking on the leading edge of the timeout.
 * @property trailing Specify invoking on the trailing edge of the timeout.
 * @constructor Create empty Throttle options
 */
data class ThrottleOptions internal constructor(
    var wait: Duration = 1.seconds,
    var leading: Boolean = true,
    var trailing: Boolean = true,
) {
    companion object : Options<ThrottleOptions>(::ThrottleOptions)
}

internal class Throttle(
    var fn: VoidFunction,
    private val scope: CoroutineScope,
    private val options: ThrottleOptions = defaultOption(),
) : VoidFunction {

    private var calledCount = 0
    private val trailingJobs: MutableList<Job> = arrayListOf()
    private var latestInvokedTime = 0L

    private fun clearTrailing() {
        if (trailingJobs.isNotEmpty()) {
            trailingJobs.forEach {
                it.cancel()
            }
            trailingJobs.clear()
        }
    }

    override fun invoke(p1: TParams) {
        val (wait, leading, trailing) = options
        val waitTime =
            (System.currentTimeMillis() - latestInvokedTime).toDuration(DurationUnit.MILLISECONDS)

        fun task(isDelay: Boolean, isTrailing: Boolean = false) {
            scope.launch(start = if (isTrailing) CoroutineStart.LAZY else CoroutineStart.DEFAULT) {
                if (isDelay) delay(wait)
                fn(p1)
                if (!isTrailing && trailingJobs.isNotEmpty()) {
                    trailingJobs.last().apply {
                        start()
                        join()
                    }
                }
            }.also {
                if (isTrailing) {
                    trailingJobs.add(it)
                }
            }
        }
        if (waitTime > wait) {
            task(isDelay = !(calledCount == 0 && leading))
            latestInvokedTime = System.currentTimeMillis()
        } else {
            if (trailing) {
                clearTrailing()
                task(isDelay = true, isTrailing = true)
            }
        }
        calledCount++
    }
}

@Composable
fun <S> useThrottle(value: S, options: ThrottleOptions = defaultOption()): S {
    val (throttled, setThrottled) = _useGetState(value)
    val throttledSet = useThrottleFn(fn = {
        setThrottled(value)
    }, options)
    LaunchedEffect(key1 = value, block = {
        throttledSet()
    })
    return throttled
}

@Composable
fun useThrottleFn(
    fn: VoidFunction,
    options: ThrottleOptions = defaultOption(),
): VoidFunction {
    val latestFn by useLatestState(value = fn)
    val scope = rememberCoroutineScope()
    val throttled = remember {
        Throttle(latestFn, scope, options)
    }.apply { this.fn = latestFn }
    return throttled
}

@SuppressLint("ComposableNaming")
@Composable
fun useThrottleEffect(
    vararg keys: Any?,
    options: ThrottleOptions = defaultOption(),
    block: SuspendAsyncFn,
) {
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
