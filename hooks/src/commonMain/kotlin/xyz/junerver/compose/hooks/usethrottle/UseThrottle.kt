package xyz.junerver.compose.hooks.usethrottle

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
import xyz.junerver.compose.hooks.None
import xyz.junerver.compose.hooks.Options
import xyz.junerver.compose.hooks.SuspendAsyncFn
import xyz.junerver.compose.hooks.VoidFunction
import xyz.junerver.compose.hooks.useeffect.useEffectImpl
import xyz.junerver.compose.hooks.useDynamicOptions
import xyz.junerver.compose.hooks.useLatestState
import xyz.junerver.compose.hooks.usegetstate._useGetStateImpl
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.utils.currentInstant

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

@Stable
data class UseThrottleOptions internal constructor(
    var wait: Duration = 1.seconds,
    var leading: Boolean = true,
    var trailing: Boolean = true,
) {
    companion object : Options<UseThrottleOptions>(::UseThrottleOptions)
}

@Stable
internal class Throttle<TParams>(
    var fn: VoidFunction<TParams>,
    private val scope: CoroutineScope,
    var options: UseThrottleOptions = UseThrottleOptions(),
    private val now: () -> Instant = { currentInstant },
) {
    private var timeoutJob: Job? = null
    private var lastArgs: TParams? = null
    private var latestInvokedTime = Instant.DISTANT_PAST

    private fun trailingEdge() {
        if (options.trailing) {
            lastArgs?.let { fn(it) }
            latestInvokedTime = now()
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
        val nowInstant = now()
        lastArgs = p1

        if (latestInvokedTime == Instant.DISTANT_PAST && !leading) {
            latestInvokedTime = nowInstant
        }

        val remaining = wait - (nowInstant - latestInvokedTime)

        if (remaining <= Duration.ZERO || remaining > wait) {
            timeoutJob?.cancel()
            timeoutJob = null
            latestInvokedTime = nowInstant
            if (leading) {
                fn(p1)
            } else if (trailing) {
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

@Composable
fun <S> useThrottleImpl(value: S, optionsOf: UseThrottleOptions.() -> Unit = {}): State<S> =
    useThrottleImpl(value, useDynamicOptions(optionsOf))

@Composable
fun <TParams> useThrottleFnImpl(fn: VoidFunction<TParams>, optionsOf: UseThrottleOptions.() -> Unit = {}): VoidFunction<TParams> =
    useThrottleFnImpl(fn, useDynamicOptions(optionsOf))

@Composable
fun useThrottleEffectImpl(vararg keys: Any?, optionsOf: UseThrottleOptions.() -> Unit = {}, block: SuspendAsyncFn) = useThrottleEffectImpl(
    keys = keys,
    useDynamicOptions(optionsOf),
    block = block,
)

@Composable
private fun <S> useThrottleImpl(value: S, options: UseThrottleOptions): State<S> {
    val (throttled, setThrottled) = _useGetStateImpl(value)
    val throttledSet = useThrottleFnImpl<None>(
        fn = {
            setThrottled(value)
        },
        options,
    )
    useEffectImpl(value) {
        throttledSet()
    }
    return throttled
}

@Composable
private fun <TParams> useThrottleFnImpl(fn: VoidFunction<TParams>, options: UseThrottleOptions): VoidFunction<TParams> {
    val latestFn by useLatestState(value = fn)
    val scope = rememberCoroutineScope()
    val throttled = remember {
        Throttle(latestFn, scope, options)
    }.apply {
        this.fn = latestFn
        this.options = options
    }
    return remember { { p1: TParams -> throttled.invoke(p1) } }
}

@Composable
private fun useThrottleEffectImpl(vararg keys: Any?, options: UseThrottleOptions, block: SuspendAsyncFn) {
    val throttledBlock = useThrottleFnImpl<CoroutineScope>(
        fn = { coroutineScope ->
            coroutineScope.launch {
                this.block()
            }
        },
        options,
    )
    val scope = rememberCoroutineScope()
    useEffectImpl(*keys) {
        throttledBlock(scope)
    }
}
