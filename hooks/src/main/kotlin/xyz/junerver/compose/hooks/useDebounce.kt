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
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Description:
 *
 * @author Junerver date: 2024/1/29-14:46 Email: junerver@gmail.com
 *     Version: v1.0
 */

data class DebounceOptions internal constructor(
    var wait: Duration = 1.seconds, // 防抖间隔
    var leading: Boolean = true, // true：第一次点击保障执行且不延时
    var trailing: Boolean = true, // false 则只取消事件。true 则添加一次延时任务
    var maxWait: Duration = 0.seconds, // 最大等待时长，防抖超过该时长则不再拦截
) {
    companion object : Options<DebounceOptions>(::DebounceOptions)
}

class Debounce(
    var fn: VoidFunction,
    private val scope: CoroutineScope,
    private val options: DebounceOptions = defaultOption(),
) : VoidFunction {
    // 调用计数
    private var calledCount = 0

    // 任务 - 是否保障执行
    private val jobs: MutableList<Pair<Job, Boolean>> = arrayListOf()

    // 执行成功时间
    private var latestInvokedTime = System.currentTimeMillis()

    // 点击调用时间
    private var latestCalledTime = System.currentTimeMillis()

    /** 移除队列中非保障任务 */
    private fun clear() {
        if (jobs.isNotEmpty()) {
            jobs.removeIf {
                // 不需要保障执行的，则取消任务
                if (!it.second) {
                    it.first.cancel()
                }
                !it.second
            }
        }
    }

    override fun invoke(p1: TParams) {
        val (wait, leading, trailing, maxWait) = options

        /** 可以被取消的计划任务： [guarantee] 保障执行，该任务添加后保障执行，不会被抖动取消， [isDelay] 是否延时 */
        fun task(guarantee: Boolean, isDelay: Boolean) {
            if (guarantee) {
                // 如果是保障性任务立即修改成功调用的时间，避免连续创建保障性任务
                latestInvokedTime = System.currentTimeMillis()
            }
            scope.launch {
                if (isDelay) delay(wait)
                fn(p1)
                latestInvokedTime = System.currentTimeMillis()
            }.also { jobs.add(it to guarantee) }
        }

        // 等待超时 || 首次&&leading直接执行
        val currentTime = System.currentTimeMillis()
        // 总计等待（当前时间-上次成功）
        val waitTime = (currentTime - latestInvokedTime).toDuration(DurationUnit.MILLISECONDS)
        // 调用间隔(毫秒)
        val interval = (currentTime - latestCalledTime).toDuration(DurationUnit.MILLISECONDS)
        // 是否为超时
        val isMaxWait = maxWait in 1.milliseconds..waitTime
        if (isMaxWait || (calledCount == 0 && leading)) {
            // 超时、leading的首次调用则保障任务执行且不在延时
            task(guarantee = isMaxWait, isDelay = false)
        } else {
            if (calledCount > 0 && interval < wait) {
                // 后续调用，且间隔时间小于设定的wait，清除之前的任务
                clear()
                // 是结束边缘则添加
                if (trailing) {
                    task(guarantee = false, isDelay = true)
                }
            } else {
                // 1. ==0，但是非leading
                // 2. >0 && 满足间隔
                task(guarantee = false, isDelay = true)
            }
        }
        // 保存本次调用时间
        calledCount++
        latestCalledTime = System.currentTimeMillis()
    }
}

/** 使用 [useDebounceFn] 实现的 */
@Composable
fun <S> useDebounce(
    value: S,
    options: DebounceOptions = defaultOption(),
): S {
    val (debounced, setDebounced) = _useState(value)
    // 最简单的创建 noop 函数的方法就是使用 匿名函数 lambda。
    val debouncedSet = useDebounceFn(fn = {
        setDebounced(value)
    }, options)
    LaunchedEffect(key1 = value, block = {
        // 外部状态变更时，调用debounced后的setState函数
        debouncedSet()
    })
    // 对外只暴露状态的值，避免外部修改状态。
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
    block: () -> Unit,
) {
    val debouncedBlock = useDebounceFn(fn = { block() }, options)
    LaunchedEffect(*keys) {
        debouncedBlock()
    }
}
