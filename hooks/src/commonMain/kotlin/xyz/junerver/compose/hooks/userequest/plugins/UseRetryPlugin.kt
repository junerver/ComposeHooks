package xyz.junerver.compose.hooks.userequest.plugins

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.math.pow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.tuple
import xyz.junerver.compose.hooks.userequest.Fetch
import xyz.junerver.compose.hooks.userequest.GenPluginLifecycleFn
import xyz.junerver.compose.hooks.userequest.Plugin
import xyz.junerver.compose.hooks.userequest.PluginLifecycle
import xyz.junerver.compose.hooks.userequest.PluginOnBefore
import xyz.junerver.compose.hooks.userequest.PluginOnCancel
import xyz.junerver.compose.hooks.userequest.PluginOnError
import xyz.junerver.compose.hooks.userequest.PluginOnSuccess
import xyz.junerver.compose.hooks.userequest.UseRequestOptions
import xyz.junerver.compose.hooks.userequest.useEmptyPlugin
import xyz.junerver.compose.hooks.utils.asBoolean

/*
  Description:
  Author: Junerver
  Date: 2024/2/20-10:26
  Email: junerver@gmail.com
  Version: v1.0
*/
internal class RetryTimer {
    private class ScheduledTask {
        var job: Job? = null
    }

    private var scheduledTask: ScheduledTask? = null

    fun schedule(scope: CoroutineScope, interval: Duration, block: suspend () -> Unit) {
        cancel()
        val task = ScheduledTask()
        scheduledTask = task
        task.job = scope.launch {
            delay(interval)
            if (scheduledTask !== task) return@launch
            scheduledTask = null
            block()
        }
    }

    fun cancel() {
        val task = scheduledTask
        scheduledTask = null
        task?.job?.cancel()
    }
}

private class RetryPlugin<TParams, TData : Any> : Plugin<TParams, TData>() {
    var count = 0
    var triggerByRetry = false // 触发retry标志
    private val retryTimer = RetryTimer()

    override val invoke: GenPluginLifecycleFn<TParams, TData>
        get() = { fetch: Fetch<TParams, TData>, useRequestOptions: UseRequestOptions<TParams, TData> ->
            val (retryInterval, retryCount) = with(useRequestOptions) {
                tuple(retryInterval, retryCount)
            }

            object : PluginLifecycle<TParams, TData>() {
                override val onBefore: PluginOnBefore<TParams, TData>
                    get() = {
                        // 未触发retry时，
                        if (!triggerByRetry) {
                            retryTimer.cancel()
                            count = 0
                        }
                        triggerByRetry = false
                        null
                    }

                override val onSuccess: PluginOnSuccess<TParams, TData>
                    get() = { _, _ ->
                        retryTimer.cancel()
                        count = 0
                    }

                override val onError: PluginOnError<TParams>
                    get() = { _, _ ->
                        count++
                        if (retryCount == -1 || count <= retryCount) {
                            val delayDuration = if (retryInterval.asBoolean()) {
                                retryInterval
                            } else {
                                (1.seconds * 2f.pow(count).toInt()).coerceAtMost(30.seconds)
                            }
                            retryTimer.schedule(this@RetryPlugin, delayDuration) {
                                triggerByRetry = true
                                fetch.refreshAsync()
                            }
                        } else {
                            retryTimer.cancel()
                            count = 0
                        }
                    }
                override val onCancel: PluginOnCancel
                    get() = {
                        cancel()
                    }
            }
        }

    override fun cancel() {
        retryTimer.cancel()
        count = 0
        triggerByRetry = false
        super.cancel()
    }
}

@Composable
internal fun <TParams, TData : Any> useRetryPlugin(options: UseRequestOptions<TParams, TData>): Plugin<TParams, TData> {
    val retryCount = options.retryCount
    val retryInterval = options.retryInterval
    if (retryCount == 0) {
        return useEmptyPlugin()
    }
    val retryPlugin = remember(retryCount, retryInterval) {
        RetryPlugin<TParams, TData>()
    }
    return retryPlugin
}
