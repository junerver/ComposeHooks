package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import xyz.junerver.compose.hooks.utils.toLocalDateTime

/*
  Description:
  Author: Junerver
  Date: 2024/3/14-11:41
  Email: junerver@gmail.com
  Version: v1.0

  Update: 2025/7/23-9:25 by Junerver
  Version: v1.1
  Description: [UseNowOptions] add [formatPattern] to support custom formatting
*/

/**
 * Options for configuring the current time display.
 *
 * @constructor Create empty UseNow options
 * @property interval The interval at which the time should be updated
 * @property format Optional custom formatter function for the timestamp. When provided, this function
 *                  takes precedence over formatPattern and will be used for formatting.
 * @property formatPattern The pattern for formatting the timestamp. This is used only when
 *                        format function is null. It supports a limited set of Unicode date format patterns
 *                        as defined in the kotlinx-datetime library. For supported patterns, see
 *                        [byUnicodePattern](https://kotlinlang.org/api/kotlinx-datetime/kotlinx-datetime/kotlinx.datetime.format/by-unicode-pattern.html)
 * @note Priority: format function > formatPattern. If format function is provided, formatPattern will be ignored.
 */
@Stable
data class UseNowOptions internal constructor(
    var interval: Duration = 1.seconds,
    var format: ((Long) -> String)? = null,
    var formatPattern: String = "yyyy-MM-dd HH:mm:ss",
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
 *                 byUnicodePattern("HH:mm:ss")
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
 * Internal implementation of the useNow hook.
 *
 * @param options The time display options
 * @return A [State] containing the formatted time string
 */
@OptIn(FormatStringsInDatetimeFormats::class)
@Composable
private fun useNow(options: UseNowOptions): State<String> {
    val (interval, format, formatPattern) = with(options) { tuple(interval, format, formatPattern) }
    val sdfRef by useCreation {
        LocalDateTime.Format {
            byUnicodePattern(formatPattern)
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
