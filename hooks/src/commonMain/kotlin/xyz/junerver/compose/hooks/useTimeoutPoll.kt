package xyz.junerver.compose.hooks

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

/*
  Description: Use timeout to poll for content. Triggers the callback after the last task is completed.
  Author: Junerver
  Date: 2025/6/25-19:26
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * UseTimeoutPoll options interface
 */
@Stable
data class UseTimeoutPollOptions internal constructor(
    /**
     * Start the timer immediately
     *
     * @default true
     */
    var immediate: Boolean = true,

    /**
     * Execute callback immediately after calling `resume`
     *
     * @default false
     */
    var immediateCallback: Boolean = false,
) {
    companion object : Options<UseTimeoutPollOptions>(::UseTimeoutPollOptions)
}

/**
 * TimeoutPoll holder, provides control functions
 */
@Stable
data class TimeoutPollHolder(
    val isActive: MutableState<Boolean>,
    val pause: () -> Unit,
    val resume: () -> Unit,
)

/**
 * TimeoutPoll internal implementation class
 */
@Stable
private class TimeoutPoll(private val options: UseTimeoutPollOptions) {
    var scope: CoroutineScope by Delegates.notNull()
    var isActiveState: MutableState<Boolean>? = null
    lateinit var timeoutFn: Ref<suspend () -> Unit>
    var interval: Duration by Delegates.notNull()
    private var timeoutJob: Job? = null

    /**
     * Asynchronous loop function
     */
    suspend fun loop() {
        if (isActiveState?.value != true) return

        timeoutFn.current()
        start()
    }

    /**
     * Start the timer
     */
    fun start() {
        if (timeoutJob?.isActive == true) {
            timeoutJob?.cancel()
        }

        timeoutJob = scope.launch {
            delay(interval)
            loop()
        }
    }

    /**
     * Resume polling
     */
    fun resume() {
        if (isActiveState?.value != true) {
            isActiveState?.value = true
            if (options.immediateCallback) {
                scope.launch {
                    timeoutFn.current()
                }
            }
            start()
        }
    }

    /**
     * Pause polling
     */
    fun pause() {
        isActiveState?.value = false
        timeoutJob?.cancel()
        timeoutJob = null
    }
}

/**
 * Use timeout to poll for content. Triggers the callback after the last task is completed.
 *
 * @param fn Function to execute
 * @param interval Time interval
 * @param optionsOf Options configuration
 * @return TimeoutPollHolder containing isActive, pause, resume
 */
@Composable
fun useTimeoutPoll(
    fn: suspend () -> Unit,
    interval: Duration = 1.seconds,
    optionsOf: UseTimeoutPollOptions.() -> Unit = {},
): TimeoutPollHolder {
    val options = remember { UseTimeoutPollOptions.optionOf(optionsOf) }
    val latestFn = useLatestRef(value = fn)
    val isActiveState = useState(default = false)
    val scope = rememberCoroutineScope()

    val timeoutPoll = remember {
        TimeoutPoll(options).apply {
            this.isActiveState = isActiveState
            this.timeoutFn = latestFn
            this.scope = scope
            this.interval = interval
        }
    }

    // If configured to start immediately, start when component is mounted
    useMount {
        if (options.immediate) {
            timeoutPoll.resume()
        }
    }
    useUnmount {
        // Clean up when component is unmounted
        timeoutPoll.pause()
    }

    return remember {
        TimeoutPollHolder(
            isActive = isActiveState,
            pause = timeoutPoll::pause,
            resume = timeoutPoll::resume
        )
    }
}

/**
 * Use timeout to poll for content, simplified version, does not return control functions.
 *
 * @param fn Function to execute
 * @param interval Time interval
 * @param immediate Whether to start immediately
 */
@Composable
fun useTimeoutPoll(
    fn: suspend () -> Unit,
    interval: Duration = 1.seconds,
    immediate: Boolean = true,
) {
    useTimeoutPoll(
        fn = fn,
        interval = interval,
        optionsOf = { this.immediate = immediate }
    )
}

