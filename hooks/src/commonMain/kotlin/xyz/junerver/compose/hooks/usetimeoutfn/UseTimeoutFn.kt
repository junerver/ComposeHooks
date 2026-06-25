package xyz.junerver.compose.hooks.usetimeoutfn

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlin.properties.Delegates
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.IsActive
import xyz.junerver.compose.hooks.NoParamsVoidFunction
import xyz.junerver.compose.hooks.Options
import xyz.junerver.compose.hooks.useref.Ref
import xyz.junerver.compose.hooks.SuspendAsyncFn
import xyz.junerver.compose.hooks.useDynamicOptions
import xyz.junerver.compose.hooks.uselatest.useLatestRefImpl
import xyz.junerver.compose.hooks.usemount.useMountImpl
import xyz.junerver.compose.hooks.useunmount.useUnmountImpl
import xyz.junerver.compose.hooks.usestate.useStateImpl

/*
  Description: A wrapper for delayed function execution with controls.
  Author: Junerver
  Date: 2025/6/25-9:06
  Email: junerver@gmail.com
  Version: v1.0
*/

@Stable
data class UseTimeoutFnOptions internal constructor(
    var immediate: Boolean = true,
    var immediateCallback: Boolean = false,
) {
    companion object : Options<UseTimeoutFnOptions>(::UseTimeoutFnOptions)
}

@Stable
data class TimeoutFnHolder(
    val isPending: IsActive,
    val start: StartFn,
    val stop: StopFn,
)

typealias StartFn = NoParamsVoidFunction

typealias StopFn = () -> Unit

@Stable
private class TimeoutFn(var options: UseTimeoutFnOptions) {
    var scope: CoroutineScope by Delegates.notNull()
    var isPendingState: MutableState<Boolean>? = null
    lateinit var timeoutFn: Ref<SuspendAsyncFn>
    var interval: Duration by Delegates.notNull()
    private var timeoutJob: Job? = null

    fun isRunning() = timeoutJob?.isActive == true

    fun start() {
        if (isRunning()) {
            stop()
        }

        scope.launch {
            isPendingState?.value = true
            try {
                if (options.immediateCallback) {
                    timeoutFn.current(this)
                } else {
                    delay(interval)
                    timeoutFn.current(this)
                }
            } finally {
                isPendingState?.value = false
            }
        }.also { timeoutJob = it }
    }

    fun stop() {
        timeoutJob?.cancel()
        timeoutJob = null
        isPendingState?.value = false
    }
}

@Composable
fun useTimeoutFnImpl(
    fn: SuspendAsyncFn,
    interval: Duration = 1.seconds,
    optionsOf: UseTimeoutFnOptions.() -> Unit = {},
): TimeoutFnHolder {
    val options = useDynamicOptions(optionsOf)
    val latestFn = useLatestRefImpl(value = fn)
    val isPendingState = useStateImpl(default = false)
    val scope = rememberCoroutineScope()

    val timeoutFn = remember {
        TimeoutFn(options).apply {
            this.isPendingState = isPendingState
            this.timeoutFn = latestFn
            this.scope = scope
            this.interval = interval
        }
    }.apply {
        this.options = options
        this.timeoutFn = latestFn
        this.scope = scope
        this.interval = interval
    }

    useMountImpl {
        if (options.immediate) {
            timeoutFn.start()
        }
    }

    useUnmountImpl {
        timeoutFn.stop()
    }

    return remember {
        TimeoutFnHolder(
            isPending = isPendingState,
            start = timeoutFn::start,
            stop = timeoutFn::stop,
        )
    }
}
