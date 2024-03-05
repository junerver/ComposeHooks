package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlin.properties.Delegates
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import xyz.junerver.kotlin.Tuple2
import xyz.junerver.kotlin.tuple

/**
 * Description: 一个间隔固定时间执行的interval函数。
 *
 * 效果与参数类似 RxJava 的`Observable.interval(0, 3, TimeUnit.SECONDS)`
 * @author Junerver
 * date: 2024/2/1-10:53
 * Email: junerver@gmail.com
 * Version: v1.0
 */
data class IntervalOptions internal constructor(
    // 初始调用延时
    var initialDelay: Duration = 0.seconds,
    // 调用间隔
    var period: Duration = 5.seconds,
    // 是否就绪，默认就绪，如果为 false，需要通过run函数手动启动
    var ready: Boolean = true,
) {
    companion object : Options<IntervalOptions>(::IntervalOptions)
}

internal class Interval(private val options: IntervalOptions) {
    /**
     * [ready]是动态值，可以通过外部副作用修改传递
     */
    var ready = true

    /**
     * 调用[intervalFn]的组件所在协程作用域
     */
    internal var scope: CoroutineScope by Delegates.notNull()

    /**
     * 外部需要重复执行的函数
     */
    lateinit var intervalFn: () -> Unit

    // 保存正在重复执行的job
    private lateinit var intervalJob: Job

    fun isRunning() = this::intervalJob.isInitialized && intervalJob.isActive

    fun run() {
        if (ready) {
            scope.launch {
                launch {
                    delay(options.initialDelay)
                    while (isActive) {
                        intervalFn()
                        delay(options.period)
                    }
                }.also { intervalJob = it }
            }
        }
    }

    fun cancel() {
        if (this::intervalJob.isInitialized && intervalJob.isActive) {
            // 结束任务
            intervalJob.cancel()
        }
    }
}

@Composable
fun useInterval(
    fn: () -> Unit,
    options: IntervalOptions = defaultOption(),
): Tuple2<NoParamsVoidFunction, NoParamsVoidFunction> {
    val (_, _, ready) = options
    val interval = remember {
        Interval(options).apply {
            this.intervalFn = fn
        }
    }.apply {
        this.scope = rememberCoroutineScope()
        this.ready = ready
    }
    LaunchedEffect(key1 = ready) {
        // 只作为ready 使用，一旦ready后再次变更不处理
        if (ready && !interval.isRunning()) {
            interval.run()
        }
        if (!ready && interval.isRunning()) {
            interval.cancel()
        }
    }
    return with(interval) {
        tuple(
            first = ::run,
            second = ::cancel
        )
    }
}
