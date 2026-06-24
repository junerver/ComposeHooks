package xyz.junerver.compose.hooks.usedebounce

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
import xyz.junerver.compose.hooks.uselatest.useLatestStateImpl
import xyz.junerver.compose.hooks.usegetstate._useGetStateImpl
import xyz.junerver.compose.hooks.invoke
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

@Stable
data class UseDebounceOptions internal constructor(
    var wait: Duration = 1.seconds,
    var leading: Boolean = false,
    var trailing: Boolean = true,
    var maxWait: Duration = Duration.ZERO,
) {
    companion object : Options<UseDebounceOptions>(::UseDebounceOptions)
}

@Stable
internal class Debounce<TParams>(
    var fn: VoidFunction<TParams>,
    private val scope: CoroutineScope,
    var options: UseDebounceOptions = UseDebounceOptions(),
    private val now: () -> Instant = { currentInstant },
) {
    private var timeoutJob: Job? = null
    private var latestInvokedTime = Instant.DISTANT_PAST
    private var latestCalledTime = latestInvokedTime
    private var lastArgs: TParams? = null

    private var isAwaitingNextDebounce: Boolean = true

    private fun cancelTimeout() {
        timeoutJob?.cancel()
        timeoutJob = null
    }

    private fun executeFn(params: TParams?) {
        params?.let { fn(it) }
        latestInvokedTime = now()
    }

    private fun resetDebounceState() {
        isAwaitingNextDebounce = true
        timeoutJob = null
    }

    fun invoke(p1: TParams) {
        val (wait, leading, trailing, maxWait) = options
        lastArgs = p1

        val currentTimeStamp = now()
        if (latestInvokedTime == Instant.DISTANT_PAST) {
            latestInvokedTime = currentTimeStamp
        }
        val waitTime = currentTimeStamp - latestInvokedTime

        val isMaxWaitExceeded = maxWait > Duration.ZERO && waitTime >= maxWait

        val shouldInvokeImmediately = leading && isAwaitingNextDebounce

        latestCalledTime = currentTimeStamp
        cancelTimeout()
        if (shouldInvokeImmediately) {
            executeFn(p1)
            isAwaitingNextDebounce = false
            timeoutJob = scope.launch {
                delay(wait)
                resetDebounceState()
            }
        } else {
            timeoutJob = scope.launch {
                delay(wait)
                if ((now() - latestCalledTime >= wait || isMaxWaitExceeded) && trailing) {
                    executeFn(lastArgs)
                }
                resetDebounceState()
            }
            if (isMaxWaitExceeded) {
                cancelTimeout()
                executeFn(lastArgs)
                isAwaitingNextDebounce = false
                timeoutJob = scope.launch {
                    delay(wait)
                    resetDebounceState()
                }
            }
        }
    }
}

@Composable
fun <S> useDebounceImpl(value: S, optionsOf: UseDebounceOptions.() -> Unit = {}): State<S> =
    useDebounceImpl(value, useDynamicOptions(optionsOf))

@Composable
fun <TParams> useDebounceFnImpl(fn: VoidFunction<TParams>, optionsOf: UseDebounceOptions.() -> Unit = {}): VoidFunction<TParams> =
    useDebounceFnImpl(fn, useDynamicOptions(optionsOf))

@Composable
fun useDebounceEffectImpl(vararg keys: Any?, optionsOf: UseDebounceOptions.() -> Unit = {}, block: SuspendAsyncFn) = useDebounceEffectImpl(
    keys = keys,
    useDynamicOptions(optionsOf),
    block,
)

@Composable
private fun <S> useDebounceImpl(value: S, options: UseDebounceOptions): State<S> {
    val (debounced, setDebounced) = _useGetStateImpl(value)
    val debouncedSet = useDebounceFnImpl<None>(
        fn = {
            setDebounced(value)
        },
        options,
    )
    useEffectImpl(value) {
        debouncedSet()
    }
    return debounced
}

@Composable
private fun <TParams> useDebounceFnImpl(fn: VoidFunction<TParams>, options: UseDebounceOptions): VoidFunction<TParams> {
    val latestFn by useLatestStateImpl(value = fn)
    val scope = rememberCoroutineScope()
    val debounced = remember {
        Debounce(latestFn, scope, options)
    }.apply {
        this.fn = latestFn
        this.options = options
    }
    return remember { { p1 -> debounced.invoke(p1) } }
}

@Composable
private fun useDebounceEffectImpl(vararg keys: Any?, options: UseDebounceOptions, block: SuspendAsyncFn) {
    val debouncedBlock = useDebounceFnImpl<CoroutineScope>(
        fn = { coroutineScope ->
            coroutineScope.launch {
                this.block()
            }
        },
        options,
    )
    val scope = rememberCoroutineScope()
    useEffectImpl(*keys) {
        debouncedBlock(scope)
    }
}
