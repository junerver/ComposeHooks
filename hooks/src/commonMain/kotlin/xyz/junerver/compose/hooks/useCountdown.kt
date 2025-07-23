package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import xyz.junerver.compose.hooks.utils.asBoolean
import xyz.junerver.compose.hooks.utils.currentInstant

/*
  Description: A hook for manage countdown.
  Author: Junerver
  Date: 2024/7/8-14:22
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Countdown options
 *
 * @constructor Create empty Countdown options
 * @property leftTime directly set the remaining time of the countdown
 * @property targetDate Lower priority than [leftTime], set a target time
 *     for the end of the countdown
 * @property interval countdown interval
 * @property onEnd callback function for end of countdown
 */
@Stable
data class UseCountdownOptions internal constructor(
    @Stable
    var leftTime: Duration? = null,
    @Stable
    var targetDate: Instant? = null,
    @Stable
    var interval: Duration = 1.seconds,
    @Stable
    var onEnd: OnEndCallback? = null,
) {
    companion object : Options<UseCountdownOptions>(::UseCountdownOptions)
}

@Composable
private fun useCountdown(options: UseCountdownOptions): CountdownHolder {
    val (leftTime, targetDate, interval, onEnd) = options
    require(leftTime.asBoolean() || targetDate.asBoolean()) {
        "'leftTime' or 'targetDate' must be set"
    }
    val leftTimeState = useLatestState(leftTime)
    val targetDateState = useLatestState(targetDate)
    val target by useState {
        if (leftTimeState.value.asBoolean()) {
            currentInstant + leftTimeState.value!!
        } else {
            targetDateState.value
        }
    }

    val (timeLeft, setTimeLeft) = useGetState(calcLeft(target))
    val onEndRef by useLatestRef(value = onEnd)
    var pauseRef by useRef(default = {})
    val (resume, pause) = useInterval(
        optionsOf = {
            period = interval
        },
    ) {
        val targetLeft = calcLeft(target)
        setTimeLeft(targetLeft)
        if (targetLeft == Duration.ZERO) {
            pauseRef()
            onEndRef?.invoke()
        }
    }
    useEffect(targetDate) {
        resume()
    }
    pauseRef = pause
    useEffect(interval) {
        if (!target.asBoolean()) {
            setTimeLeft(Duration.ZERO)
            return@useEffect
        }
        setTimeLeft(calcLeft(target))
        resume()
    }
    val formatRes = useState { parseDuration(timeLeft.value) }
    return remember { CountdownHolder(timeLeft, formatRes) }
}

/**
 * A hook for managing countdown functionality.
 *
 * This hook provides a way to create and manage countdown timers with various options
 * such as setting a target date or remaining time, custom intervals, and end callbacks.
 *
 * @param optionsOf A lambda to configure the countdown options
 * @return A [CountdownHolder] containing the current time left and formatted result
 *
 * @example
 * ```kotlin
 * val countdown = useCountdown {
 *     leftTime = 60.seconds  // Set initial countdown time
 *     interval = 1.seconds   // Update every second
 *     onEnd = {             // Callback when countdown ends
 *         println("Countdown finished!")
 *     }
 * }
 *
 * // Access the countdown values
 * val timeLeft = countdown.timeLeft.value
 * val formatted = countdown.formatRes.value
 * ```
 */
@Composable
fun useCountdown(optionsOf: UseCountdownOptions.() -> Unit): CountdownHolder = useCountdown(useDynamicOptions(optionsOf))

/**
 * Calculates the remaining time until the target date.
 *
 * @param target The target date to calculate remaining time from
 * @return The remaining duration, or 0 seconds if target is null or in the past
 */
@Stable
private fun calcLeft(target: Instant?): Duration {
    if (target == null) return Duration.ZERO
    val left = target - currentInstant
    return if (left < Duration.ZERO) Duration.ZERO else left
}

/**
 * Represents formatted countdown time components.
 *
 * @property days Number of days remaining
 * @property hours Number of hours remaining (0-23)
 * @property minutes Number of minutes remaining (0-59)
 * @property seconds Number of seconds remaining (0-59)
 * @property milliseconds Number of milliseconds remaining (0-999)
 */
@Stable
data class FormattedRes(
    val days: Int,
    val hours: Int,
    val minutes: Int,
    val seconds: Int,
    val milliseconds: Int,
)

/**
 * Parses a duration into formatted time components.
 *
 * @param leftTime The duration to parse
 * @return A [FormattedRes] containing the parsed time components
 */
@Stable
private fun parseDuration(leftTime: Duration): FormattedRes = FormattedRes(
    days = (leftTime.inWholeDays).toInt(),
    hours = ((leftTime.inWholeHours) % 24).toInt(),
    minutes = ((leftTime.inWholeMinutes) % 60).toInt(),
    seconds = ((leftTime.inWholeSeconds) % 60).toInt(),
    milliseconds = (leftTime.inWholeMilliseconds % 1000).toInt(),
)

/**
 * Holder class for countdown state and formatted results.
 *
 * @property timeLeft The current remaining time as a [State]
 * @property formatRes The formatted time components as a [State]
 */
@Stable
data class CountdownHolder(
    val timeLeft: State<Duration>,
    val formatRes: State<FormattedRes>,
)
