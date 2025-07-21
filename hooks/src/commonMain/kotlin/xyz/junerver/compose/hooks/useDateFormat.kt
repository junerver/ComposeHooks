package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import kotlin.math.abs
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number
import kotlinx.datetime.offsetAt
import kotlinx.datetime.toLocalDateTime

/*
  Description: Format date according to the string of tokens passed in, inspired by dayjs
  Author: Junerver
  Date: 2025/7/14-14:30
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Type alias for date-like values that can be formatted
 */
typealias DateLike = Any?

/**
 * Custom function type for meridiem formatting
 */
typealias CustomMeridiemFn = (hours: Int, minutes: Int, isLowercase: Boolean, hasPeriod: Boolean) -> String

/**
 * Date format messages interface for localization
 */
interface DateFormatMessages {
    val months: Array<String>
    val monthsShort: Array<String>
    val weekdays: Array<String>
    val weekdaysShort: Array<String>
    val weekdaysMin: Array<String>
}

/**
 * Default Chinese date format messages
 */
object DefaultChineseDateFormatMessages : DateFormatMessages {
    override val months = arrayOf(
        "一月", "二月", "三月", "四月", "五月", "六月",
        "七月", "八月", "九月", "十月", "十一月", "十二月"
    )
    override val monthsShort = arrayOf(
        "一月", "二月", "三月", "四月", "五月", "六月",
        "七月", "八月", "九月", "十月", "十一月", "十二月"
    )
    override val weekdays = arrayOf(
        "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"
    )
    override val weekdaysShort = arrayOf(
        "周日", "周一", "周二", "周三", "周四", "周五", "周六"
    )
    override val weekdaysMin = arrayOf(
        "周日", "周一", "周二", "周三", "周四", "周五", "周六"
    )
}

/**
 * Default English date format messages
 */
object DefaultEnglishDateFormatMessages : DateFormatMessages {
    override val months = arrayOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    override val monthsShort = arrayOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )
    override val weekdays = arrayOf(
        "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    )
    override val weekdaysShort = arrayOf(
        "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"
    )
    override val weekdaysMin = arrayOf(
        "Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"
    )
}

/**
 * Options for configuring date formatting behavior
 */
@Stable
data class UseDateFormatOptions internal constructor(
    /**
     * The locale to use for formatting
     * Default is system locale, support "zh-CN", "en-US"
     */
    var locale: String? = null,
    /**
     * A custom function to modify how meridiem is displayed
     */
    var customMeridiem: CustomMeridiemFn? = null,
    /**
     * The timezone to use for formatting
     * Default is system timezone
     */
    var timeZone: TimeZone = TimeZone.currentSystemDefault(),
) {
    companion object : Options<UseDateFormatOptions>(::UseDateFormatOptions)
}

/**
 * A hook for formatting dates according to a specified format string.
 *
 * This hook provides a way to format dates using a pattern similar to dayjs.
 * It supports various format tokens and returns a state containing the formatted date string.
 *
 * @param date The date to format, can be a [kotlin.time.Instant], [kotlinx.datetime.LocalDateTime],
 * [Long] (timestamp in milliseconds), or [String] (parseable date string)
 * @param formatStr The format string with tokens (default: "HH:mm:ss")
 * @param optionsOf A lambda to configure formatting options
 * @return A [State] containing the formatted date string
 *
 * @example
 * ```kotlin
 * // Basic usage with current time
 * val formattedDate = useDateFormat(Clock.System.now(), "YYYY-MM-DD HH:mm:ss")
 * Text(text = formattedDate.value)
 *
 * // With custom options
 * val customFormatted = useDateFormat(
 * date = Clock.System.now(),
 * formatStr = "YYYY-MM-DD hh:mm:ss A",
 * optionsOf = {
 * locale = "en-US"
 * customMeridiem = { hours, _, isLowercase, _ ->
 * if (hours > 11) {
 * if (isLowercase) "pm" else "PM"
 * } else {
 * if (isLowercase) "am" else "AM"
 * }
 * }
 * }
 * )
 * Text(text = customFormatted.value)
 * ```
 *
 * Available format tokens:
 * - Yo: Ordinal formatted year (2018th)
 * - YYYY: Four-digit year (2018)
 * - YY: Two-digit year (18)
 * - Mo: Ordinal formatted month (1st, 2nd, ..., 12th)
 * - MMMM: Full month name (January-December)
 * - MMM: Abbreviated month name (Jan-Dec)
 * - MM: Month, 2-digits (01-12)
 * - M: Month (1-12)
 * - Do: Ordinal formatted day of month (1st, 2nd, ..., 31st)
 * - DD: Day of month, 2-digits (01-31)
 * - D: Day of month (1-31)
 * - Ho: Ordinal formatted hour (0th, 1st, 2nd, ..., 23rd)
 * - HH: Hour, 2-digits (00-23)
 * - H: Hour (0-23)
 * - ho: Ordinal formatted hour, 12-hour clock (1st, 2nd, ..., 12th)
 * - hh: Hour, 12-hour clock, 2-digits (01-12)
 * - h: Hour, 12-hour clock (1-12)
 * - mo: Ordinal formatted minute (0th, 1st, ..., 59th)
 * - mm: Minute, 2-digits (00-59)
 * - m: Minute (0-59)
 * - so: Ordinal formatted second (0th, 1st, ..., 59th)
 * - ss: Second, 2-digits (00-59)
 * - s: Second (0-59)
 * - SSS: Millisecond, 3-digits (000-999)
 * - AA: Meridiem with periods (A.M./P.M.)
 * - A: Meridiem (AM/PM)
 * - aa: Meridiem lowercase with periods (a.m./p.m.)
 * - a: Meridiem lowercase (am/pm)
 * - dddd: Full name of day of week (Sunday-Saturday)
 * - ddd: Short name of day of week (Sun-Sat)
 * - dd: Min name of day of week (S-S)
 * - d: Day of week (0-6, Sunday is 0)
 * - zzzz: Long timezone with offset (GMT+01:00)
 * - zzz: Timezone with offset (GMT+1)
 * - zz: Timezone with offset (GMT+1)
 * - z: Timezone with offset (GMT+1)
 */
@Composable
fun useDateFormat(
    date: DateLike = Clock.System.now(),
    formatStr: String = "HH:mm:ss",
    optionsOf: UseDateFormatOptions.() -> Unit = {},
): State<String> {
    val options = useDynamicOptions(optionsOf)
    val dateState = useLatestState(date)
    val formatStrState = useLatestState(formatStr)

    // Create a derived state that updates when date or format changes
    return useState {
        formatDate(normalizeDate(dateState.value), formatStrState.value, options)
    }
}

/**
 * Normalizes various date types to a LocalDateTime object
 */
internal fun normalizeDate(date: DateLike): LocalDateTime = when (date) {
    is LocalDateTime -> date
    is Instant -> date.toLocalDateTime(TimeZone.currentSystemDefault())
    is Long -> Instant.fromEpochMilliseconds(date).toLocalDateTime(TimeZone.currentSystemDefault())
    is String -> {
        try {
            LocalDateTime.parse(date)
        } catch (_: Exception) {
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        }
    }

    null -> Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    else -> Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
}

/**
 * Formats a date according to the specified format string
 */
internal fun formatDate(date: LocalDateTime, formatStr: String, options: UseDateFormatOptions): String {
    val locale = options.locale
    val timeZone = options.timeZone
    val customMeridiem = options.customMeridiem
    // Select messages based on locale
    val messages = when (locale) {
        "zh-CN" -> DefaultChineseDateFormatMessages
        "en-US" -> DefaultEnglishDateFormatMessages
        else ->  DefaultEnglishDateFormatMessages
    }

    // Regex pattern to match format tokens and literal strings in square brackets
    // Order matters: literal match ([^\]]+) should come first to ensure it's prioritized
    val formatRegex = Regex(
        "(\\[[^]]*])|" + // Capture anything inside square brackets as a literal
            "zzzz|zzz|zz|z|" + // Timezone tokens
            "YYYY|YY|Yo|" + // Year tokens
            "MMMM|MMM|MM|Mo|M|" + // Month tokens
            "dddd|ddd|DD|Do|dd|D|d|" + // Day of week & Day of month tokens (order DD/Do before D)
            "HH|Ho|H|" + // 24-hour tokens
            "hh|ho|h|" + // 12-hour tokens
            "mm|mo|m|" + // Minute tokens
            "ss|so|s|" + // Second tokens
            "SSS|" + // Millisecond token
            "AA|A|aa|a", // Meridiem tokens
    )

    // Using replace (or replaceAll in newer versions) with a lambda to handle each match
    return formatRegex.replace(formatStr) { matchResult ->
        val token = matchResult.value
        val literalContent = matchResult.groups[1] // Check if the first capture group (for literals) has content

        literalContent?.value?.removePrefix("[")?.removeSuffix("]")
            ?: when (token) {
                // Year
                "YYYY" -> date.year.toString()
                "YY" -> (date.year % 100).toString().padStart(2, '0')
                "Yo" -> getOrdinal(date.year)

                // Month
                "MMMM" -> getMonthName(date.month.number, messages, false)
                "MMM" -> getMonthName(date.month.number, messages, true)
                "MM" -> date.month.number.toString().padStart(2, '0')
                "Mo" -> getOrdinal(date.month.number)
                "M" -> date.month.number.toString()

                // Day of month
                "DD" -> date.day.toString().padStart(2, '0')
                "Do" -> getOrdinal(date.day)
                "D" -> date.day.toString()

                // Day of week
                "dddd" -> {
                    val dayOfWeek = getDayOfWeek(date)
                    getDayName(dayOfWeek, messages, false)
                }

                "ddd" -> {
                    val dayOfWeek = getDayOfWeek(date)
                    getDayName(dayOfWeek, messages, true)
                }

                "dd" -> {
                    val dayOfWeek = getDayOfWeek(date)
                    getDayName(dayOfWeek, messages, true, true)
                }

                "d" -> {
                    val dayOfWeek = getDayOfWeek(date)
                    dayOfWeek.toString()
                }

                // Hours (24-hour)
                "HH" -> date.hour.toString().padStart(2, '0')
                "Ho" -> getOrdinal(date.hour)
                "H" -> date.hour.toString()

                // Hours (12-hour)
                "hh" -> {
                    val hours12 = if (date.hour % 12 == 0) 12 else date.hour % 12
                    hours12.toString().padStart(2, '0')
                }

                "ho" -> {
                    val hours12 = if (date.hour % 12 == 0) 12 else date.hour % 12
                    getOrdinal(hours12)
                }

                "h" -> {
                    val hours12 = if (date.hour % 12 == 0) 12 else date.hour % 12
                    hours12.toString()
                }

                // Minutes
                "mm" -> date.minute.toString().padStart(2, '0')
                "mo" -> getOrdinal(date.minute)
                "m" -> date.minute.toString()

                // Seconds
                "ss" -> date.second.toString().padStart(2, '0')
                "so" -> getOrdinal(date.second)
                "s" -> date.second.toString()

                // Milliseconds
                "SSS" -> {
                    val ms = (date.nanosecond / 1_000_000)
                    ms.toString().padStart(3, '0')
                }

                // AM/PM
                "AA" -> {
                    val isAM = date.hour < 12
                    if (customMeridiem != null) {
                        customMeridiem(date.hour, date.minute, false, true)
                    } else {
                        if (isAM) "A.M." else "P.M."
                    }
                }

                "A" -> {
                    val isAM = date.hour < 12
                    if (customMeridiem != null) {
                        customMeridiem(date.hour, date.minute, false, false)
                    } else {
                        if (isAM) "AM" else "PM"
                    }
                }

                "aa" -> {
                    val isAM = date.hour < 12
                    if (customMeridiem != null) {
                        customMeridiem(date.hour, date.minute, true, true)
                    } else {
                        if (isAM) "a.m." else "p.m."
                    }
                }

                "a" -> {
                    val isAM = date.hour < 12
                    if (customMeridiem != null) {
                        customMeridiem(date.hour, date.minute, true, false)
                    } else {
                        if (isAM) "am" else "pm"
                    }
                }

                // Timezone
                "zzzz" -> getTimezoneString(timeZone, true)
                "zzz" -> getTimezoneString(timeZone, false)
                "zz" -> getTimezoneString(timeZone, false)
                "z" -> getTimezoneString(timeZone, false)

                // If no match found, return the original token (this case should ideally not be hit for defined tokens)
                else -> token
            }
    }
}

/**
 * Gets the day of week (0-6, Sunday is 0) for a LocalDateTime
 */
private fun getDayOfWeek(date: LocalDateTime): Int {
    // In kotlinx.datetime, dayOfWeek is 1-7 where Monday is 1 and Sunday is 7
    // Convert to 0-6 where Sunday is 0
    return if (date.dayOfWeek.isoDayNumber == 7) 0 else date.dayOfWeek.isoDayNumber
}

/**
 * Gets the month name based on messages
 */
private fun getMonthName(month: Int, messages: DateFormatMessages, abbreviated: Boolean): String {
    val months = if (abbreviated) messages.monthsShort else messages.months
    return months[month - 1]
}

/**
 * Gets the day name based on messages
 */
private fun getDayName(day: Int, messages: DateFormatMessages, abbreviated: Boolean, minimal: Boolean = false): String {
    val days = if (minimal) {
        messages.weekdaysMin
    } else if (abbreviated) {
        messages.weekdaysShort
    } else {
        messages.weekdays
    }
    return days[day]
}

/**
 * Gets the ordinal suffix for a number (1st, 2nd, 3rd, etc.)
 */
private fun getOrdinal(number: Int): String {
    val suffix = when {
        number % 100 in 11..13 -> "th"
        number % 10 == 1 -> "st"
        number % 10 == 2 -> "nd"
        number % 10 == 3 -> "rd"
        else -> "th"
    }
    return "$number$suffix"
}

/**
 * Gets the timezone string representation
 */
private fun getTimezoneString(timeZone: TimeZone, longFormat: Boolean): String {
    // Get the current time point, as timezone offset may vary due to daylight saving time
    val now = Clock.System.now()
    // Get the UTC offset for the specified timezone at the current time point
    val offset: UtcOffset = timeZone.offsetAt(now)

    // Calculate hours and minutes based on the total seconds of the offset, and handle the sign
    val sign = if (offset.totalSeconds >= 0) "+" else "-"
    val absHours = abs(offset.totalSeconds / 3600)
    val absMinutes = abs((offset.totalSeconds % 3600) / 60)

    // Return different formats based on the longFormat parameter
    return if (longFormat) {
        // Long format (GMT+HH:MM) - both hours and minutes need to be padded to two digits
        val hourStringPadded = absHours.toString().padStart(2, '0')
        val minuteStringPadded = absMinutes.toString().padStart(2, '0')
        "GMT$sign$hourStringPadded:$minuteStringPadded" // e.g., "GMT+08:00"
    } else {
        // Non-long format (GMT+H or GMT+HH) - only use hours, no zero padding
        "GMT$sign$absHours" // e.g., "GMT+8" or "GMT+9"
    }
}
