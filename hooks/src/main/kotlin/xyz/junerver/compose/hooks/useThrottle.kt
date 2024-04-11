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
 * Description:
 *
 * @author Junerver date: 2024/1/30-11:02 Email: junerver@gmail.com
 *     Version: v1.0
 */
data class ThrottleOptions internal constructor(
    var wait: Duration = 1.seconds, // 节流时长
    var leading: Boolean = true, // 是否在延迟开始前调用函数
    var trailing: Boolean = true, // 是否在延迟开始后调用函数
) {
    companion object : Options<ThrottleOptions>(::ThrottleOptions)
}

internal class Throttle(
    var fn: VoidFunction,
    private val scope: CoroutineScope,
    private val options: ThrottleOptions = defaultOption(),
) : VoidFunction {
    // 调用计数
    private var calledCount = 0

    // 任务 -- 是否结束边缘
    private val trailingJobs: MutableList<Job> = arrayListOf()

    // 执行成功时间
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
            // 尾随任务不自动运行
            scope.launch(start = if (isTrailing) CoroutineStart.LAZY else CoroutineStart.DEFAULT) {
                if (isDelay) delay(wait)
                fn(p1)
                if (!isTrailing && trailingJobs.isNotEmpty()) {
                    // 非尾随任务执行完毕后清空尾随任务
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
            // 有常规任务
            if (trailing) {
                // 移除全部尾任务
                clearTrailing()
                // 追加一个结束边缘,结束边缘不clear
                task(isDelay = true, isTrailing = true)
            }
        }
        calledCount++
    }
}

@Composable
fun <S> useThrottle(value: S, options: ThrottleOptions = defaultOption()): S {
    val (throttled, setThrottled) = _useState(value)
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
