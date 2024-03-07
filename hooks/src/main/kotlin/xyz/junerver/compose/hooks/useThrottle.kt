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
 * @author Junerver
 * date: 2024/1/30-11:02
 * Email: junerver@gmail.com
 * Version: v1.0
 */
data class ThrottleOptions internal constructor(
    var wait: Duration = 1.seconds, // 节流时长
    var leading: Boolean = true, // true：第一个任务是不延时
    var trailing: Boolean = true, // true：将最后一次点击添加到延时任务中
) {
    companion object : Options<ThrottleOptions>(::ThrottleOptions)
}

class Throttle(
    private val fn: NormalFunction<Any?>,
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
    // value的最新值
    val latestValueRef by useLatestState(value = value)
    val throttledSet = useThrottleFn(fn = {
        setThrottled(latestValueRef)
    }, options)
    LaunchedEffect(key1 = value, block = {
        throttledSet()
    })
    return throttled
}

@Composable
fun useThrottleFn(
    fn: NormalFunction<Any?>,
    options: ThrottleOptions = defaultOption(),
): VoidFunction {
    val scope = rememberCoroutineScope()
    val throttled = remember {
        Throttle(fn, scope, options)
    }
    return throttled
}

@SuppressLint("ComposableNaming")
@Composable
fun useThrottleEffect(
    vararg keys: Any?,
    options: ThrottleOptions = defaultOption(),
    block: () -> Unit,
) {
    val throttledBlock = useThrottleFn(fn = { block() }, options)
    LaunchedEffect(*keys) {
        throttledBlock()
    }
}
