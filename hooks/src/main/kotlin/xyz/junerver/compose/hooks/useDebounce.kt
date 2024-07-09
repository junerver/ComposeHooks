package xyz.junerver.compose.hooks

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.datetime.Clock
import xyz.junerver.kotlin.Tuple2

/**
 * Debounce options
 *
 * @property wait time to delay
 * @property leading Specify invoking on the leading edge of the timeout.
 * @property trailing Specify invoking on the trailing edge of the timeout.
 * @property maxWait The maximum time func is allowed to be delayed before it’s invoked.
 * @constructor Create empty Debounce options
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
    private val options: DebounceOptions = defaultOption(),
) : VoidFunction {

    private var calledCount = 0
    private val jobs: MutableList<Tuple2<Job, Boolean>> = arrayListOf()
    private var latestInvokedTime = Clock.System.now()
    private var latestCalledTime = Clock.System.now()

    private fun clear() {
        if (jobs.isNotEmpty()) {
            jobs.removeIf {
                if (!it.second) {
                    it.first.cancel()
                }
                !it.second
            }
        }
    }

    override fun invoke(p1: TParams) {
        val (wait, leading, trailing, maxWait) = options
        fun task(guarantee: Boolean, isDelay: Boolean) {
            if (guarantee) {
                latestInvokedTime = Clock.System.now()
            }
            scope.launch {
                if (isDelay) delay(wait)
                fn(p1)
                latestInvokedTime = Clock.System.now()
            }.also { jobs.add(it to guarantee) }
        }

        val currentTime = Clock.System.now()
        val waitTime = currentTime - latestInvokedTime
        val interval = currentTime - latestCalledTime
        val isMaxWait = maxWait in 1.milliseconds..waitTime
        if (isMaxWait || (calledCount == 0 && leading)) {
            task(guarantee = isMaxWait, isDelay = false)
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
        latestCalledTime = Clock.System.now()
    }
}

@Composable
fun <S> useDebounce(
    value: S,
    options: DebounceOptions = defaultOption(),
): S {
    val (debounced, setDebounced) = _useGetState(value)
    val debouncedSet = useDebounceFn(fn = {
        setDebounced(value)
    }, options)
    LaunchedEffect(key1 = value, block = {
        debouncedSet()
    })
    return debounced
}

/**
 * 需要注意：[Debounce] 不返回计算结果，在 Compose 中我们无法使用 [Debounce] 透传出结算结果，应该使用状态，而非
 * [Debounce] 的返回值。 例如我们有一个计算函数，我们应该设置一个状态作为结果的保存。函数计算后的结果，通过调用对应的
 * `setState(state:T)` 函数来传递。保证结算结果（状态）与计算解耦。 这样我们的[Debounce] 就可以无缝接入。
 */
@Composable
fun useDebounceFn(
    fn: VoidFunction,
    options: DebounceOptions = defaultOption(),
): VoidFunction {
    val latestFn by useLatestState(value = fn)
    val scope = rememberCoroutineScope()
    val debounced = remember {
        Debounce(latestFn, scope, options)
    }.apply { this.fn = latestFn }
    return debounced
}

@SuppressLint("ComposableNaming")
@Composable
fun useDebounceEffect(
    vararg keys: Any?,
    options: DebounceOptions = defaultOption(),
    block: SuspendAsyncFn,
) {
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
