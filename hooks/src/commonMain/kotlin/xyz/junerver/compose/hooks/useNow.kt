package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.datetime.*
import kotlinx.datetime.format.char

/*
  Description:
  Author: Junerver
  Date: 2024/3/14-11:41
  Email: junerver@gmail.com
  Version: v1.0
*/

data class UseNowOptions(
    var interval: Duration = 1.seconds,
    var format: ((Long) -> String)? = null,
) {
    companion object : Options<UseNowOptions>(::UseNowOptions)
}

@Deprecated(
    "Please use the performance-optimized version. Do not pass the Options instance directly. You can simply switch by adding `=` after the `optionsOf` function. If you need to use an older version, you need to explicitly declare the parameters as `options`"
)
@Composable
fun useNow(options: UseNowOptions = remember { UseNowOptions() }): String {
    val (interval, format) = with(options) { Pair(interval, format) }
    val sdfRef = remember {
        LocalDateTime.Format {
            year()
            char('-')
            monthNumber()
            char('-')
            dayOfMonth()
            char(' ')
            hour()
            char(':')
            minute()
            char(':')
            second()
        }
    }
    val (time) = useTimestamp(
        TimestampOptions.optionOf {
            this.interval = interval
        }
    )
    val date by useState(time) {
        format?.invoke(time) ?: time.toLocalDateTime().format(sdfRef)
    }
    return date
}

@Composable
fun useNow(optionsOf: UseNowOptions.() -> Unit): String = useNow(remember(optionsOf) { UseNowOptions.optionOf(optionsOf) })

internal fun Long.toLocalDateTime(timeZone: TimeZone = TimeZone.currentSystemDefault()) =
    Instant.fromEpochMilliseconds(this).toLocalDateTime(timeZone)
