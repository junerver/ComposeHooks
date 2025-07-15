package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.round
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import xyz.junerver.compose.hooks.useDynamicOptions

/*
  Description: Reactive time ago. Automatically update the time ago string when the time changes.
  Author: Junerver
  Date: 2025/6/24-16:22
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Default time unit names
 */
enum class TimeAgoUnitNames {
    SECOND,
    MINUTE,
    HOUR,
    DAY,
    WEEK,
    MONTH,
    YEAR,
}

/**
 * Time formatters for customizing time difference display format
 */
typealias TimeUnitMessageFormatter = (n: Int) -> String
typealias TimeAgoMessageFormatter = (unit: String) -> String

/**
 * Built-in time difference messages
 */
interface TimeAgoMessagesBuiltIn {
    val justNow: String
    val past: TimeAgoMessageFormatter
    val future: TimeAgoMessageFormatter
    val invalid: String
}

/**
 * Time difference message configuration
 */
interface TimeAgoMessages : TimeAgoMessagesBuiltIn {
    val second: TimeUnitMessageFormatter
    val minute: TimeUnitMessageFormatter
    val hour: TimeUnitMessageFormatter
    val day: TimeUnitMessageFormatter
    val week: TimeUnitMessageFormatter
    val month: TimeUnitMessageFormatter
    val year: TimeUnitMessageFormatter
}

/**
 * Default Chinese time difference messages
 */
object DefaultChineseTimeAgoMessages : TimeAgoMessages {
    override val justNow: String = "刚刚"
    override val past: TimeAgoMessageFormatter = { unit -> "${unit}前" }
    override val future: TimeAgoMessageFormatter = { unit -> "${unit}后" }
    override val invalid: String = "无效日期"
    override val second: TimeUnitMessageFormatter = { n -> "${n}秒" }
    override val minute: TimeUnitMessageFormatter = { n -> "${n}分钟" }
    override val hour: TimeUnitMessageFormatter = { n -> "${n}小时" }
    override val day: TimeUnitMessageFormatter = { n -> "${n}天" }
    override val week: TimeUnitMessageFormatter = { n -> "${n}周" }
    override val month: TimeUnitMessageFormatter = { n -> "${n}个月" }
    override val year: TimeUnitMessageFormatter = { n -> "${n}年" }
}

/**
 * Default English time difference messages
 */
object DefaultEnglishTimeAgoMessages : TimeAgoMessages {
    override val justNow: String = "just now"
    override val past: TimeAgoMessageFormatter = { unit -> "$unit ago" }
    override val future: TimeAgoMessageFormatter = { unit -> "in $unit" }
    override val invalid: String = "invalid date"
    override val second: TimeUnitMessageFormatter = { n -> "$n ${if (n == 1) "second" else "seconds"}" }
    override val minute: TimeUnitMessageFormatter = { n -> "$n ${if (n == 1) "minute" else "minutes"}" }
    override val hour: TimeUnitMessageFormatter = { n -> "$n ${if (n == 1) "hour" else "hours"}" }
    override val day: TimeUnitMessageFormatter = { n -> "$n ${if (n == 1) "day" else "days"}" }
    override val week: TimeUnitMessageFormatter = { n -> "$n ${if (n == 1) "week" else "weeks"}" }
    override val month: TimeUnitMessageFormatter = { n -> "$n ${if (n == 1) "month" else "months"}" }
    override val year: TimeUnitMessageFormatter = { n -> "$n ${if (n == 1) "year" else "years"}" }
}

/**
 * Time unit definition
 */
data class TimeAgoUnit(
    val max: Long,
    val value: Long,
    val name: TimeAgoUnitNames,
)

/**
 * Format time difference options
 */
@Stable
open class FormatTimeAgoOptions internal constructor(
    /**
     * Maximum unit (millisecond difference), exceeding this value will display full date instead of relative time
     */
    var max: Long? = null,
    /**
     * Full date formatter
     */
    var fullDateFormatter: ((Instant) -> String)? = null,
    /**
     * Time difference message configuration
     */
    var messages: TimeAgoMessages = DefaultChineseTimeAgoMessages,
    /**
     * Whether to show second-level units (default minimum is minute)
     */
    var showSecond: Boolean = false,
    /**
     * Rounding method
     */
    var rounding: String = "round",
    /**
     * Custom units
     */
    var units: List<TimeAgoUnit>? = null,
) {
    companion object : Options<FormatTimeAgoOptions>(::FormatTimeAgoOptions)
}

/**
 * Use time difference options
 */
@Stable
data class UseTimeAgoOptions internal constructor(
    /**
     * Update interval, set to 0 to disable automatic updates
     */
    var updateInterval: Duration = 30.seconds,
) : FormatTimeAgoOptions() {
    companion object : Options<UseTimeAgoOptions>(::UseTimeAgoOptions)
}

/**
 * Default time unit list
 */
private val DEFAULT_UNITS = listOf(
    TimeAgoUnit(60000, 1000, TimeAgoUnitNames.SECOND),
    TimeAgoUnit(2760000, 60000, TimeAgoUnitNames.MINUTE),
    TimeAgoUnit(72000000, 3600000, TimeAgoUnitNames.HOUR),
    TimeAgoUnit(518400000, 86400000, TimeAgoUnitNames.DAY),
    TimeAgoUnit(2419200000, 604800000, TimeAgoUnitNames.WEEK),
    TimeAgoUnit(28512000000, 2592000000, TimeAgoUnitNames.MONTH),
    TimeAgoUnit(Long.MAX_VALUE, 31536000000, TimeAgoUnitNames.YEAR),
)

/**
 * Format time difference
 *
 * @param from Start time
 * @param options Formatting options
 * @param now Current time (defaults to system current time)
 * @return Formatted time difference string
 */
fun formatTimeAgo(from: Instant, options: FormatTimeAgoOptions = FormatTimeAgoOptions(), now: Instant = Clock.System.now()): String {
    val diff = now.toEpochMilliseconds() - from.toEpochMilliseconds()
    val absDiff = abs(diff)
    val isPast = diff > 0

    // Check if exceeds maximum unit
    if (options.max != null && absDiff > options.max!!) {
        return options.fullDateFormatter?.invoke(from) ?: from.toLocalDateTime(TimeZone.currentSystemDefault()).toString()
    }

    // If time difference is very small, show "just now"
    if (absDiff < 30000 && !options.showSecond) {
        return options.messages.justNow
    }

    // Use custom units or default units
    val units = options.units ?: DEFAULT_UNITS
    val filteredUnits = if (options.showSecond) units else units.filter { it.name != TimeAgoUnitNames.SECOND }

    // Find suitable time unit
    val unit = filteredUnits.find { absDiff < it.max } ?: filteredUnits.last()
    val value = absDiff.toDouble() / unit.value

    // Process value according to rounding method
    val roundedValue = when (options.rounding) {
        "floor" -> floor(value).toLong()
        "ceil" -> ceil(value).toLong()
        else -> round(value).toLong()
    }

    // Get unit string
    val unitStr = when (unit.name) {
        TimeAgoUnitNames.SECOND -> options.messages.second(roundedValue.toInt())
        TimeAgoUnitNames.MINUTE -> options.messages.minute(roundedValue.toInt())
        TimeAgoUnitNames.HOUR -> options.messages.hour(roundedValue.toInt())
        TimeAgoUnitNames.DAY -> options.messages.day(roundedValue.toInt())
        TimeAgoUnitNames.WEEK -> options.messages.week(roundedValue.toInt())
        TimeAgoUnitNames.MONTH -> options.messages.month(roundedValue.toInt())
        TimeAgoUnitNames.YEAR -> options.messages.year(roundedValue.toInt())
    }

    // Format final string based on past/future
    return if (isPast) {
        options.messages.past(unitStr)
    } else {
        options.messages.future(unitStr)
    }
}

/**
 * Time difference hook
 *
 * This hook provides a way to display reactive time differences, automatically updating the time difference string based on the configured interval.
 *
 * @param time The time point to calculate the time difference from
 * @param optionsOf Lambda to configure time difference options
 * @return State containing the formatted time difference string
 *
 * @example
 * ```kotlin
 * // Basic usage
 * val timeAgo = useTimeAgo(Clock.System.now() - 5.minutes) {
 *     updateInterval = 1.seconds
 * }
 * Text(text = timeAgo.value) // Displays "5 minutes ago"
 *
 * // Custom format
 * val customTimeAgo = useTimeAgo(Clock.System.now() - 1.days) {
 *     messages = DefaultEnglishTimeAgoMessages
 *     showSecond = true
 * }
 * Text(text = customTimeAgo.value) // Displays "1 day ago"
 * ```
 */
@Composable
fun useTimeAgo(time: Instant, optionsOf: UseTimeAgoOptions.() -> Unit = {}): State<String> =
    useTimeAgo(time, useDynamicOptions(optionsOf))

/**
 * Internal implementation of the time difference hook
 *
 * @param time The time point to calculate the time difference from
 * @param options Time difference options
 * @return State containing the formatted time difference string
 */
@Composable
private fun useTimeAgo(time: Instant, options: UseTimeAgoOptions = remember { UseTimeAgoOptions() }): State<String> {
    val latestTime by useLatestState(time)
    val updateInterval = options.updateInterval
    val (timestamp) = useTimestamp({ interval = updateInterval }, updateInterval > 0.milliseconds)
    return useState {
        formatTimeAgo(latestTime, options, Instant.fromEpochMilliseconds(timestamp.value))
    }
}
