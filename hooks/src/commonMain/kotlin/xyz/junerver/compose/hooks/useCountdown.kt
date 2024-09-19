package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.datetime.Instant
import xyz.junerver.compose.hooks.utils.currentTime
import xyz.junerver.kotlin.asBoolean

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
data class CountdownOptions internal constructor(
    var leftTime: Duration? = null,
    var targetDate: Instant? = null,
    var interval: Duration = 1.seconds,
    var onEnd: OnEndCallback? = null,
) {
    companion object : Options<CountdownOptions>(::CountdownOptions)
}

@Deprecated(
    "Please use the performance-optimized version. Do not pass the Options instance directly. You can simply switch by adding `=` after the `optionsOf` function. If you need to use an older version, you need to explicitly declare the parameters as `options`"
)
@Composable
fun useCountdown(options: CountdownOptions): CountdownHolder {
    val (leftTime, targetDate, interval, onEnd) = options
    require(leftTime.asBoolean()||targetDate.asBoolean()){
        "'leftTime' or 'targetDate' must be set"
    }
    val target = useCreation {
        if (leftTime.asBoolean()) {
            currentTime + leftTime
        } else {
            targetDate
        }
    }.current

    val (timeLeft, setTimeLeft) = useGetState(calcLeft(target))
    val onEndRef = useLatestRef(value = onEnd)
    val pauseRef = useRef(default = {})
    val (resume, pause) = useInterval(
        optionsOf = {
            period = interval
        }
    ) {
        val targetLeft = calcLeft(target)
        setTimeLeft(targetLeft)
        if (targetLeft == 0.seconds) {
            pauseRef.current()
            onEndRef.current?.invoke()
        }
    }
    pauseRef.current = pause
    useEffect(interval) {
        if (!target.asBoolean()) {
            setTimeLeft(0.seconds)
            return@useEffect
        }
        setTimeLeft(calcLeft(target))
        resume()
    }
    val formatRes = useState { parseDuration(timeLeft.value) }
    return remember { CountdownHolder(timeLeft, formatRes) }
}

@Composable
fun useCountdown(optionsOf: CountdownOptions.() -> Unit): CountdownHolder = useCountdown(remember { CountdownOptions.optionOf(optionsOf) })

@Stable
private fun calcLeft(target: Instant?): Duration {
    if (target == null) return 0.seconds
    val left = target - currentTime
    return if (left < 0.seconds) 0.seconds else left
}

@Stable
data class FormattedRes(
    val days: Int,
    val hours: Int,
    val minutes: Int,
    val seconds: Int,
    val milliseconds: Int,
)

@Stable
private fun parseDuration(leftTime: Duration): FormattedRes = FormattedRes(
    days = (leftTime.inWholeDays).toInt(),
    hours = ((leftTime.inWholeHours) % 24).toInt(),
    minutes = ((leftTime.inWholeMinutes) % 60).toInt(),
    seconds = ((leftTime.inWholeSeconds) % 60).toInt(),
    milliseconds = (leftTime.inWholeMilliseconds % 1000).toInt()
)

@Stable
data class CountdownHolder(
    val timeLeft: State<Duration>,
    val formatRes: State<FormattedRes>,
)
