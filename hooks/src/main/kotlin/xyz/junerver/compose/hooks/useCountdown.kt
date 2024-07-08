package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import xyz.junerver.kotlin.Tuple2
import xyz.junerver.kotlin.asBoolean

/*
  Description: A hook for manage countdown.
  Author: Junerver
  Date: 2024/7/8-14:22
  Email: junerver@gmail.com
  Version: v1.0
*/

internal typealias OnEndCallback = () -> Unit

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
data class CountdownOptions internal constructor(
    var leftTime: Duration? = null,
    var targetDate: Instant? = null,
    var interval: Duration = 1.seconds,
    var onEnd: OnEndCallback? = null,
) {
    companion object : Options<CountdownOptions>(::CountdownOptions)
}

@Composable
fun useCountdown(options: CountdownOptions): Tuple2<Duration, FormattedRes> {
    val (leftTime, targetDate, interval, onEnd) = options
    val target = useCreation {
        if (leftTime.asBoolean()) {
            Clock.System.now() + leftTime
        } else {
            targetDate
        }
    }.current

    val (timeLeft, setTimeLeft) = useState(calcLeft(target))
    val onEndRef = useLatestRef(value = onEnd)
    val pauseRef = useRef(default = {})
    val (resume, pause) = useInterval(
        options = optionsOf {
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

    return Tuple2(
        timeLeft,
        parseDuration(timeLeft)
    )
}

private fun calcLeft(target: Instant?): Duration {
    if (target == null) return 0.seconds
    val left = target - Clock.System.now()
    return if (left < 0.seconds) 0.seconds else left
}

data class FormattedRes(
    val days: Int,
    val hours: Int,
    val minutes: Int,
    val seconds: Int,
    val milliseconds: Int,
)

private fun parseDuration(leftTime: Duration): FormattedRes {
    return FormattedRes(
        days = (leftTime.inWholeDays).toInt(),
        hours = ((leftTime.inWholeHours) % 24).toInt(),
        minutes = ((leftTime.inWholeMinutes) % 60).toInt(),
        seconds = ((leftTime.inWholeSeconds) % 60).toInt(),
        milliseconds = (leftTime.inWholeMilliseconds % 1000).toInt()
    )
}
