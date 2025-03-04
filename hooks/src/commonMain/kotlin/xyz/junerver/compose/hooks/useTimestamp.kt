package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import xyz.junerver.compose.hooks.utils.currentTime

/*
  Description:
  Author: Junerver
  Date: 2024/3/14-10:18
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Options for configuring timestamp behavior.
 *
 * This class provides configuration options for timestamp functionality,
 * allowing you to customize the update interval, offset, and callback behavior.
 *
 * @property interval The time between timestamp updates
 * @property offset The offset to add to the current timestamp
 * @property callback Optional callback function to be called with each timestamp update
 *
 * @example
 * ```kotlin
 * val options = TimestampOptions {
 *     interval = 100.milliseconds
 *     offset = 1.seconds
 *     callback = { timestamp ->
 *         println("Current timestamp: $timestamp")
 *     }
 * }
 * ```
 */
@Stable
data class TimestampOptions internal constructor(
    var interval: Duration = 1.0.milliseconds,
    var offset: Duration = 0.milliseconds,
    var callback: ((Long) -> Unit)? = null,
) {
    companion object : Options<TimestampOptions>(::TimestampOptions)
}

/**
 * A hook for tracking the current timestamp with state updates.
 *
 * This hook provides a way to track the current timestamp that updates at regular
 * intervals. It returns a [TimestampHolder] with the current timestamp and control
 * functions.
 *
 * @param optionsOf A lambda to configure timestamp options
 * @param autoResume Whether to automatically start tracking when the component is mounted
 * @return A [TimestampHolder] containing the timestamp and control functions
 *
 * @example
 * ```kotlin
 * val (state, pause, resume, isActive) = useTimestamp {
 *     interval = 1.second
 *     offset = 2.seconds
 * }
 * 
 * // Display timestamp
 * Text("Current time: ${state.value}")
 * 
 * // Control tracking
 * if (isActive()) {
 *     Button(onClick = pause) { Text("Pause") }
 * } else {
 *     Button(onClick = resume) { Text("Resume") }
 * }
 * ```
 */
@Composable
fun useTimestamp(optionsOf: TimestampOptions.() -> Unit = {}, autoResume: Boolean = true): TimestampHolder =
    useTimestamp(remember { TimestampOptions.optionOf(optionsOf) }, autoResume)

/**
 * A hook for tracking the current timestamp with a reference.
 *
 * This hook provides a way to track the current timestamp that updates at regular
 * intervals, using a reference instead of state. It's useful when you don't need
 * UI updates on timestamp changes.
 *
 * @param optionsOf A lambda to configure timestamp options
 * @param autoResume Whether to automatically start tracking when the component is mounted
 * @return A [TimestampRefHolder] containing the timestamp reference and control functions
 *
 * @example
 * ```kotlin
 * val (ref, pause, resume, isActive) = useTimestampRef {
 *     interval = 100.milliseconds
 * }
 * 
 * // Access timestamp without triggering recomposition
 * val currentTime = ref.current
 * 
 * // Control tracking
 * if (isActive()) {
 *     Button(onClick = pause) { Text("Pause") }
 * } else {
 *     Button(onClick = resume) { Text("Resume") }
 * }
 * ```
 */
@Composable
fun useTimestampRef(optionsOf: TimestampOptions.() -> Unit = {}, autoResume: Boolean = true): TimestampRefHolder = useTimestampRef(
    remember { TimestampOptions.optionOf(optionsOf) },
    autoResume
)

/**
 * A holder class for timestamp state and control functions.
 *
 * This class provides access to the current timestamp and functions for controlling
 * the timestamp tracking.
 *
 * @param state The current timestamp value
 * @param pause Function to pause timestamp updates
 * @param resume Function to resume timestamp updates
 * @param isActive Function to check if timestamp tracking is active
 */
@Stable
data class TimestampHolder(
    val state: State<Long>,
    val pause: PauseFn,
    val resume: ResumeFn,
    val isActive: IsActive,
)

/**
 * A holder class for timestamp reference and control functions.
 *
 * This class provides access to the current timestamp through a reference and
 * functions for controlling the timestamp tracking.
 *
 * @param ref The current timestamp reference
 * @param pause Function to pause timestamp updates
 * @param resume Function to resume timestamp updates
 * @param isActive Function to check if timestamp tracking is active
 */
@Stable
data class TimestampRefHolder(
    val ref: Ref<Long>,
    val pause: PauseFn,
    val resume: ResumeFn,
    val isActive: IsActive,
)

/**
 * Internal implementation of the timestamp state hook.
 *
 * @param options The timestamp configuration options
 * @param autoResume Whether to automatically start tracking when mounted
 * @return A [TimestampHolder] containing the timestamp and control functions
 */
@Composable
private fun useTimestamp(options: TimestampOptions = remember { TimestampOptions() }, autoResume: Boolean = true): TimestampHolder {
    val (interval, offset, callback) = with(options) { Triple(interval, offset, callback) }
    val timestamp = useState(default = currentTime)
    val (resume, pause, isActive) = useInterval(
        optionsOf = {
            period = interval
        }
    ) {
        timestamp.value = currentTime + offset
        callback?.invoke(timestamp.value.toEpochMilliseconds())
    }
    useMount {
        if (autoResume) resume()
    }
    val timestampState = useState { timestamp.value.toEpochMilliseconds() }
    return remember { TimestampHolder(timestampState, pause, resume, isActive) }
}

/**
 * Internal implementation of the timestamp reference hook.
 *
 * @param options The timestamp configuration options
 * @param autoResume Whether to automatically start tracking when mounted
 * @return A [TimestampRefHolder] containing the timestamp reference and control functions
 */
@Composable
private fun useTimestampRef(options: TimestampOptions = remember { TimestampOptions() }, autoResume: Boolean = true): TimestampRefHolder {
    val (interval, offset, callback) = with(options) { Triple(interval, offset, callback) }
    val timestampRef = useRef(default = currentTime.toEpochMilliseconds())
    val (resume, pause, isActive) = useInterval(
        optionsOf = {
            period = interval
        }
    ) {
        timestampRef.current = (currentTime + offset).toEpochMilliseconds()
        callback?.invoke(timestampRef.current)
    }
    useMount {
        if (autoResume) resume()
    }
    return remember { TimestampRefHolder(timestampRef, pause, resume, isActive) }
}
