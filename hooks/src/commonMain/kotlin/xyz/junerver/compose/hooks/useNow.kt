package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import xyz.junerver.compose.hooks.useDynamicOptions

/*
  Description:
  Author: Junerver
  Date: 2024/3/14-11:41
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Options for configuring the current time display.
 *
 * @constructor Create empty UseNow options
 * @property interval The interval at which the time should be updated
 * @property format Optional custom formatter function for the timestamp
 */
@Stable
data class UseNowOptions internal constructor(
    var interval: Duration = 1.seconds,
    var format: ((Long) -> String)? = null,
) {
    companion object : Options<UseNowOptions>(::UseNowOptions)
}

/**
 * A hook for displaying and updating the current time.
 *
 * This hook provides a way to display the current time that updates at regular intervals.
 * It supports both default formatting (YYYY-MM-DD HH:mm:ss) and custom formatting through
 * a format function.
 *
 * @param optionsOf A lambda to configure the time display options
 * @return A [State] containing the formatted time string
 *
 * @example
 * ```kotlin
 * // Basic usage with default format
 * val time = useNow {
 *     interval = 1.seconds
 * }
 * Text(text = time.value)
 *
 * // Custom format
 * val customTime = useNow {
 *     interval = 1.seconds
 *     format = { timestamp ->
 *         // Custom formatting logic
 *         timestamp.toLocalDateTime().format(
 *             LocalDateTime.Format {
 *                 hour()
 *                 char(':')
 *                 minute()
 *                 char(':')
 *                 second()
 *             }
 *         )
 *     }
 * }
 * Text(text = customTime.value)
 * ```
 */
@Composable
fun useNow(optionsOf: UseNowOptions.() -> Unit = {}) = useNow(useDynamicOptions(optionsOf))

/**
 * Converts a timestamp to a LocalDateTime in the specified timezone.
 *
 * @param timeZone The timezone to use for conversion (defaults to system timezone)
 * @return A LocalDateTime representation of the timestamp
 */
internal fun Long.toLocalDateTime(timeZone: TimeZone = TimeZone.currentSystemDefault()) =
    Instant.fromEpochMilliseconds(this).toLocalDateTime(timeZone)

/**
 * Internal implementation of the useNow hook.
 *
 * @param options The time display options
 * @return A [State] containing the formatted time string
 */
@Composable
private fun useNow(options: UseNowOptions): State<String> {
    val (interval, format) = with(options) { Pair(interval, format) }
    val sdfRef = remember {
        LocalDateTime.Format {
            year()
            char('-')
            monthNumber()
            char('-')
            day(padding = Padding.ZERO)
            char(' ')
            hour()
            char(':')
            minute()
            char(':')
            second()
        }
    }
    val (time) = useTimestamp(
        optionsOf = {
            this.interval = interval
        },
    )
    val date = useState {
        format?.invoke(time.value) ?: time.value.toLocalDateTime().format(sdfRef)
    }
    return date
}
