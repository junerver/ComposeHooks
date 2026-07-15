package xyz.junerver.compose.hooks.usetimeoutpoll

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.Options
import xyz.junerver.compose.hooks.SuspendAsyncFn
import xyz.junerver.compose.hooks.useDynamicOptions
import xyz.junerver.compose.hooks.usemount.useMountImpl
import xyz.junerver.compose.hooks.useunmount.useUnmountImpl
import xyz.junerver.compose.hooks.usestate.useStateImpl

/*
  Description: Use timeout to poll for content. Triggers the callback after the last task is completed.
  Author: Junerver
  Date: 2025/6/25-19:26
  Email: junerver@gmail.com
  Version: v1.0

  Update: 2025/6/26-9:23 by Junerver
  Version: v1.1
  Description: base on useTimeoutFn
*/

@Stable
data class UseTimeoutPollOptions internal constructor(
    var immediate: Boolean = true,
    var immediateCallback: Boolean = false,
) {
    companion object : Options<UseTimeoutPollOptions>(::UseTimeoutPollOptions)
}

@Stable
data class TimeoutPollHolder(
    val isActive: MutableState<Boolean>,
    val pause: () -> Unit,
    val resume: () -> Unit,
)

@Stable
private class TimeoutPoll(
    private val scope: CoroutineScope,
    private val isActiveState: MutableState<Boolean>,
    options: UseTimeoutPollOptions,
    interval: Duration,
    fn: SuspendAsyncFn,
) {
    private val lock = SynchronizedObject()
    private var immediateCallback = options.immediateCallback
    private var interval = interval
    private var pollFn = fn
    private var active = false
    private var generation = 0L
    private var pollJob: Job? = null

    fun update(options: UseTimeoutPollOptions, interval: Duration, fn: SuspendAsyncFn) {
        synchronized(lock) {
            immediateCallback = options.immediateCallback
            this.interval = interval
            pollFn = fn
        }
    }

    fun resume() {
        val nextSchedule = synchronized(lock) {
            if (active) {
                null
            } else {
                active = true
                generation += 1L
                isActiveState.value = true
                Schedule(
                    generation = generation,
                    delay = if (immediateCallback) Duration.ZERO else interval,
                )
            }
        } ?: return

        schedule(nextSchedule)
    }

    fun pause() {
        val jobToCancel = synchronized(lock) {
            active = false
            generation += 1L
            isActiveState.value = false
            pollJob.also { pollJob = null }
        }
        jobToCancel?.cancel()
    }

    private fun schedule(schedule: Schedule) {
        lateinit var candidate: Job
        candidate = scope.launch(start = CoroutineStart.LAZY) {
            delay(schedule.delay)
            val callback = synchronized(lock) {
                pollFn.takeIf {
                    active && generation == schedule.generation && pollJob === candidate
                }
            } ?: return@launch

            callback(this)

            val nextDelay = synchronized(lock) {
                if (active && generation == schedule.generation && pollJob === candidate) {
                    pollJob = null
                    interval
                } else {
                    null
                }
            }
            nextDelay?.let {
                schedule(Schedule(generation = schedule.generation, delay = it))
            }
        }

        val shouldStart = synchronized(lock) {
            if (active && generation == schedule.generation && pollJob == null) {
                pollJob = candidate
                true
            } else {
                false
            }
        }
        if (!shouldStart) {
            candidate.cancel()
            return
        }

        candidate.invokeOnCompletion {
            synchronized(lock) {
                if (pollJob === candidate) {
                    pollJob = null
                    active = false
                    isActiveState.value = false
                }
            }
        }
        candidate.start()
    }

    private data class Schedule(
        val generation: Long,
        val delay: Duration,
    )
}

@Composable
fun useTimeoutPollImpl(
    fn: SuspendAsyncFn,
    interval: Duration = 1.seconds,
    optionsOf: UseTimeoutPollOptions.() -> Unit,
): TimeoutPollHolder {
    require(interval > Duration.ZERO) { "useTimeoutPoll interval must be positive" }
    val options = useDynamicOptions(optionsOf)
    val isActiveState = useStateImpl(default = false)
    val scope = rememberCoroutineScope()
    val timeoutPoll = remember {
        TimeoutPoll(
            scope = scope,
            isActiveState = isActiveState,
            options = options,
            interval = interval,
            fn = fn,
        )
    }.also { it.update(options = options, interval = interval, fn = fn) }

    useMountImpl {
        if (options.immediate) {
            timeoutPoll.resume()
        }
    }
    useUnmountImpl {
        timeoutPoll.pause()
    }

    return remember {
        TimeoutPollHolder(
            isActive = isActiveState,
            pause = timeoutPoll::pause,
            resume = timeoutPoll::resume,
        )
    }
}

@Composable
fun useTimeoutPollImpl(fn: SuspendAsyncFn, interval: Duration = 1.seconds, immediate: Boolean = true) = useTimeoutPollImpl(
    fn = fn,
    interval = interval,
    optionsOf = { this.immediate = immediate },
)
