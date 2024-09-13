package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import xyz.junerver.compose.hooks.utils.currentTime

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
data class DebounceOptions internal constructor(
    var wait: Duration = 1.seconds,
    var leading: Boolean = false,
    var trailing: Boolean = true,
    var maxWait: Duration = 0.seconds,
) {
    companion object : Options<DebounceOptions>(::DebounceOptions)
}

internal class Debounce(
    var fn: VoidFunction,
    private val scope: CoroutineScope,
    private val options: DebounceOptions = DebounceOptions(),
) {
    private var calledCount = 0
    private val jobs: MutableList<Pair<Job, Boolean>> = arrayListOf()
    private var latestInvokedTime = Instant.DISTANT_PAST
    private var latestCalledTime = latestInvokedTime

    private fun clear() {
        if (jobs.isNotEmpty()) {
            jobs.removeAll {
                if (!it.second) {
                    it.first.cancel()
                }
                !it.second
            }
        }
    }

    fun invoke(p1: TParams) {
        val (wait, leading, trailing, maxWait) = options

        fun task(guarantee: Boolean, isDelay: Boolean) {
            scope.launch {
                if (isDelay) delay(wait)
                fn(p1)
                latestInvokedTime = currentTime
            }.also { jobs.add(it to guarantee) }
        }

        val waitTime = currentTime - latestInvokedTime
        val interval = currentTime - latestCalledTime
        val isMaxWait = maxWait in 1.milliseconds..waitTime
        if ((isMaxWait && calledCount != 0) || (calledCount == 0 && leading)) {
            task(guarantee = true, isDelay = false)
        } else {
            if (calledCount > 0 && interval < wait) {
                clear()
                if (trailing) {
                    task(guarantee = false, isDelay = true)
                }
            } else {
                task(guarantee = false, isDelay = true)
            }
        }
        calledCount++
        latestCalledTime = currentTime
    }
}

@Deprecated(
    "Please use the performance-optimized version. Do not pass the Options instance directly. You can simply switch by adding `=` after the `optionsOf` function. If you need to use an older version, you need to explicitly declare the parameters as `options`"
)
@Composable
fun <S> useDebounce(value: S, options: DebounceOptions = remember { DebounceOptions() }): S {
    val (debounced, setDebounced) = _useGetState(value)
    val debouncedSet = useDebounceFn(fn = {
        setDebounced(value)
    }, options)
    useEffect(value) {
        debouncedSet()
    }
    return debounced
}

@Composable
fun <S> useDebounce(value: S, optionsOf: DebounceOptions.() -> Unit): S =
    useDebounce(value, remember(optionsOf) { DebounceOptions.optionOf(optionsOf) })

/**
 * 需要注意：[Debounce] 不返回计算结果，在 Compose 中我们无法使用 [Debounce] 透传出结算结果，应该使用状态，而非
 * [Debounce] 的返回值。 例如我们有一个计算函数，我们应该设置一个状态作为结果的保存。函数计算后的结果，通过调用对应的
 * `setState(state:T)` 函数来传递。保证结算结果（状态）与计算解耦。 这样我们的[Debounce] 就可以无缝接入。
 */
@Deprecated(
    "Please use the performance-optimized version. Do not pass the Options instance directly. You can simply switch by adding `=` after the `optionsOf` function. If you need to use an older version, you need to explicitly declare the parameters as `options`"
)
@Composable
fun useDebounceFn(fn: VoidFunction, options: DebounceOptions = remember { DebounceOptions() }): VoidFunction {
    val latestFn by useLatestState(value = fn)
    val scope = rememberCoroutineScope()
    val debounced = remember {
        Debounce(latestFn, scope, options)
    }.apply { this.fn = latestFn }
    return { p1 -> debounced.invoke(p1) }
}

@Composable
fun useDebounceFn(fn: VoidFunction, optionsOf: DebounceOptions.() -> Unit): VoidFunction =
    useDebounceFn(fn, remember(optionsOf) { DebounceOptions.optionOf(optionsOf) })

@Deprecated(
    "Please use the performance-optimized version. Do not pass the Options instance directly. You can simply switch by adding `=` after the `optionsOf` function. If you need to use an older version, you need to explicitly declare the parameters as `options`"
)
@Composable
fun useDebounceEffect(vararg keys: Any?, options: DebounceOptions = remember { DebounceOptions() }, block: SuspendAsyncFn) {
    val debouncedBlock = useDebounceFn(fn = { params ->
        (params[0] as CoroutineScope).launch {
            this.block()
        }
    }, options)
    val scope = rememberCoroutineScope()
    useEffect(*keys) {
        debouncedBlock(scope)
    }
}

@Composable
fun useDebounceEffect(vararg keys: Any?, optionsOf: DebounceOptions.() -> Unit, block: SuspendAsyncFn) = useDebounceEffect(
    keys = keys,
    remember(optionsOf) { DebounceOptions.optionOf(optionsOf) },
    block
)
