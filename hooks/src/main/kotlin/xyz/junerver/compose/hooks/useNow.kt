package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import xyz.junerver.kotlin.tuple

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

@Composable
fun useNow(options: UseNowOptions = defaultOption()): String {
    val (interval, format) = with(options) { tuple(interval, format) }
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
        optionsOf {
            this.interval = interval
        }
    )
    val date by useState(time) {
        format?.invoke(time) ?: time.toLocalDateTime().format(sdfRef)
    }
    return date
}

internal fun Long.toLocalDateTime(timeZone: TimeZone = TimeZone.currentSystemDefault()) =
    Instant.fromEpochMilliseconds(this).toLocalDateTime(timeZone)
