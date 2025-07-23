package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import kotlin.math.abs
import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number
import kotlinx.datetime.offsetAt
import xyz.junerver.compose.hooks.utils.currentInstant
import xyz.junerver.compose.hooks.utils.currentLocalDateTime
import xyz.junerver.compose.hooks.utils.toLocalDateTime

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
internal typealias DateLike = Any?

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
        "一月",
        "二月",
        "三月",
        "四月",
        "五月",
        "六月",
        "七月",
        "八月",
        "九月",
        "十月",
        "十一月",
        "十二月",
    )
    override val monthsShort = arrayOf(
        "一月",
        "二月",
        "三月",
        "四月",
        "五月",
        "六月",
        "七月",
        "八月",
        "九月",
        "十月",
        "十一月",
        "十二月",
    )
    override val weekdays = arrayOf(
        "星期日",
        "星期一",
        "星期二",
        "星期三",
        "星期四",
        "星期五",
        "星期六",
    )
    override val weekdaysShort = arrayOf(
        "周日",
        "周一",
        "周二",
        "周三",
        "周四",
        "周五",
        "周六",
    )
    override val weekdaysMin = arrayOf(
        "周日",
        "周一",
        "周二",
        "周三",
        "周四",
        "周五",
        "周六",
    )
}

/**
 * Default English date format messages
 */
object DefaultEnglishDateFormatMessages : DateFormatMessages {
    override val months = arrayOf(
        "January",
        "February",
        "March",
        "April",
        "May",
        "June",
        "July",
        "August",
        "September",
        "October",
        "November",
        "December",
    )
    override val monthsShort = arrayOf(
        "Jan",
        "Feb",
        "Mar",
        "Apr",
        "May",
        "Jun",
        "Jul",
        "Aug",
        "Sep",
        "Oct",
        "Nov",
        "Dec",
    )
    override val weekdays = arrayOf(
        "Sunday",
        "Monday",
        "Tuesday",
        "Wednesday",
        "Thursday",
        "Friday",
        "Saturday",
    )
    override val weekdaysShort = arrayOf(
        "Sun",
        "Mon",
        "Tue",
        "Wed",
        "Thu",
        "Fri",
        "Sat",
    )
    override val weekdaysMin = arrayOf(
        "Su",
        "Mo",
        "Tu",
        "We",
        "Th",
        "Fr",
        "Sa",
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
 * Internal implementation function for date formatting that accepts any DateLike type.
 *
 * This is the core implementation that handles the actual date formatting logic.
 * It accepts a DateLike parameter (which can be Instant, LocalDateTime, String, Long, or null)
 * and converts it to a standardized format before applying the formatting rules.
 *
 * @param date The date to format, can be:
 *             - [kotlin.time.Instant]: UTC moment in time
 *             - [kotlinx.datetime.LocalDateTime]: Local date and time without timezone
 *             - [String]: ISO 8601 formatted date string (e.g., "2023-12-25T14:30:00")
 *             - [Long]: Timestamp in milliseconds since Unix epoch
 *             - null: Uses current system time as fallback
 * @param formatStr The format string with tokens (default: "HH:mm:ss")
 * @param optionsOf Configuration options for formatting behavior
 * @return A [State] containing the formatted date string that updates reactively
 *
 * ## Supported Format Tokens:
 *
 * ### Year Tokens:
 * - **YYYY**: 4-digit year (e.g., "2023")
 * - **YY**: 2-digit year (e.g., "23")
 * - **Yo**: Year with ordinal suffix (e.g., "2023rd")
 *
 * ### Month Tokens:
 * - **MMMM**: Full month name (e.g., "January", "一月")
 * - **MMM**: Abbreviated month name (e.g., "Jan", "一月")
 * - **MM**: 2-digit month with leading zero (e.g., "01", "12")
 * - **Mo**: Month with ordinal suffix (e.g., "1st", "12th")
 * - **M**: Month without leading zero (e.g., "1", "12")
 *
 * ### Day of Month Tokens:
 * - **DD**: 2-digit day with leading zero (e.g., "01", "31")
 * - **Do**: Day with ordinal suffix (e.g., "1st", "31st")
 * - **D**: Day without leading zero (e.g., "1", "31")
 *
 * ### Day of Week Tokens:
 * - **dddd**: Full day name (e.g., "Sunday", "星期日")
 * - **ddd**: Abbreviated day name (e.g., "Sun", "周日")
 * - **dd**: Minimal day name (e.g., "Su", "周日")
 * - **d**: Day of week as number (0-6, Sunday is 0)
 *
 * ### Hour Tokens (24-hour format):
 * - **HH**: 2-digit hour with leading zero (e.g., "00", "23")
 * - **Ho**: Hour with ordinal suffix (e.g., "0th", "23rd")
 * - **H**: Hour without leading zero (e.g., "0", "23")
 *
 * ### Hour Tokens (12-hour format):
 * - **hh**: 2-digit hour with leading zero (e.g., "01", "12")
 * - **ho**: Hour with ordinal suffix (e.g., "1st", "12th")
 * - **h**: Hour without leading zero (e.g., "1", "12")
 *
 * ### Minute Tokens:
 * - **mm**: 2-digit minute with leading zero (e.g., "00", "59")
 * - **mo**: Minute with ordinal suffix (e.g., "0th", "59th")
 * - **m**: Minute without leading zero (e.g., "0", "59")
 *
 * ### Second Tokens:
 * - **ss**: 2-digit second with leading zero (e.g., "00", "59")
 * - **so**: Second with ordinal suffix (e.g., "0th", "59th")
 * - **s**: Second without leading zero (e.g., "0", "59")
 *
 * ### Millisecond Tokens:
 * - **SSS**: 3-digit millisecond with leading zeros (e.g., "000", "999")
 *
 * ### Meridiem Tokens (AM/PM):
 * - **AA**: Uppercase meridiem with periods (e.g., "A.M.", "P.M.")
 * - **A**: Uppercase meridiem (e.g., "AM", "PM")
 * - **aa**: Lowercase meridiem with periods (e.g., "a.m.", "p.m.")
 * - **a**: Lowercase meridiem (e.g., "am", "pm")
 *
 * ### Timezone Tokens:
 * - **zzzz**: Long timezone with offset (e.g., "GMT+08:00")
 * - **zzz**: Timezone with offset (e.g., "GMT+8")
 * - **zz**: Timezone with offset (e.g., "GMT+8")
 * - **z**: Timezone with offset (e.g., "GMT+8")
 *
 * ### Literal Text:
 * - **[text]**: Literal text that won't be formatted (e.g., "[at] HH:mm" → "at 14:30")
 *
 * ## Format Examples:
 * - "YYYY-MM-DD HH:mm:ss" → "2023-12-25 14:30:00"
 * - "dddd, MMMM Do, YYYY" → "Monday, December 25th, 2023"
 * - "h:mm A" → "2:30 PM"
 * - "[Today is] dddd" → "Today is Monday"
 * - "YYYY年MM月DD日 HH:mm:ss" → "2023年12月25日 14:30:00"
 */
@Composable
private fun useDateFormatImpl(
    date: DateLike,
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
    is Instant -> date.toLocalDateTime()
    is Long -> date.toLocalDateTime()
    is String -> {
        try {
            LocalDateTime.parse(date)
        } catch (_: Exception) {
            currentLocalDateTime
        }
    }

    null -> currentLocalDateTime
    else -> currentLocalDateTime
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
        else -> DefaultEnglishDateFormatMessages
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
private fun getDayName(
    day: Int,
    messages: DateFormatMessages,
    abbreviated: Boolean,
    minimal: Boolean = false,
): String {
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
    val now = currentInstant
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

/**
 * A hook for formatting dates using Instant objects.
 *
 * This overload accepts kotlinx.datetime.Instant objects, which represent a specific moment in time
 * in UTC. The Instant will be converted to LocalDateTime using the system's default timezone
 * before formatting.
 *
 * @param date The Instant object to format. Defaults to current system time.
 *             Instant represents a moment in time in UTC timezone.
 * @param formatStr The format string with tokens (default: "HH:mm:ss")
 *                  Supports all standard format tokens like YYYY, MM, DD, HH, mm, ss, etc.
 * @param optionsOf Configuration options for formatting behavior
 * @return A [State] containing the formatted date string that updates reactively
 *
 * @see useDateFormatImpl for detailed format token documentation
 */
@Composable
fun useDateFormat(
    date: Instant = currentInstant,
    formatStr: String = "HH:mm:ss",
    optionsOf: UseDateFormatOptions.() -> Unit = {},
): State<String> = useDateFormatImpl(date, formatStr, optionsOf)

/**
 * A hook for formatting dates using LocalDateTime objects.
 *
 * This overload accepts kotlinx.datetime.LocalDateTime objects, which represent a date and time
 * without timezone information. The LocalDateTime is used directly for formatting without
 * any timezone conversion.
 *
 * @param date The LocalDateTime object to format.
 *             LocalDateTime represents a date and time in local timezone without UTC offset info.
 *             Format: YYYY-MM-DDTHH:mm:ss (e.g., "2023-12-25T14:30:00")
 * @param formatStr The format string with tokens (default: "HH:mm:ss")
 *                  Supports all standard format tokens like YYYY, MM, DD, HH, mm, ss, etc.
 * @param optionsOf Configuration options for formatting behavior
 * @return A [State] containing the formatted date string that updates reactively
 *
 * @see useDateFormatImpl for detailed format token documentation
 */
@Composable
fun useDateFormat(date: LocalDateTime, formatStr: String = "HH:mm:ss", optionsOf: UseDateFormatOptions.() -> Unit = {}): State<String> =
    useDateFormatImpl(date, formatStr, optionsOf)

/**
 * A hook for formatting dates using String representations.
 *
 * This overload accepts date strings that can be parsed into LocalDateTime objects.
 * If the string cannot be parsed, the current system time will be used as fallback.
 *
 * @param date The date string to format.
 *             Supported formats:
 *             - ISO 8601 format: "2023-12-25T14:30:00" or "2023-12-25T14:30:00.123"
 *             - Date only: "2023-12-25" (time defaults to 00:00:00)
 *             - If parsing fails, current system time is used as fallback
 * @param formatStr The format string with tokens (default: "HH:mm:ss")
 *                  Supports all standard format tokens like YYYY, MM, DD, HH, mm, ss, etc.
 * @param optionsOf Configuration options for formatting behavior
 * @return A [State] containing the formatted date string that updates reactively
 *
 * @see useDateFormatImpl for detailed format token documentation
 */
@Composable
fun useDateFormat(date: String, formatStr: String = "HH:mm:ss", optionsOf: UseDateFormatOptions.() -> Unit = {}): State<String> =
    useDateFormatImpl(date, formatStr, optionsOf)

/**
 * A hook for formatting dates using Long timestamp values.
 *
 * This overload accepts Long values representing timestamps in milliseconds since
 * the Unix epoch (January 1, 1970, 00:00:00 UTC). The timestamp will be converted
 * to LocalDateTime using the system's default timezone before formatting.
 *
 * @param date The timestamp in milliseconds since Unix epoch to format.
 *             Examples:
 *             - 1703512200000L represents "2023-12-25T14:30:00" in UTC
 *             - System.currentTimeMillis() for current time
 *             - Date.getTime() from Java Date objects
 * @param formatStr The format string with tokens (default: "HH:mm:ss")
 *                  Supports all standard format tokens like YYYY, MM, DD, HH, mm, ss, etc.
 * @param optionsOf Configuration options for formatting behavior
 * @return A [State] containing the formatted date string that updates reactively
 *
 * @see useDateFormatImpl for detailed format token documentation
 */
@Composable
fun useDateFormat(date: Long, formatStr: String = "HH:mm:ss", optionsOf: UseDateFormatOptions.() -> Unit = {}): State<String> =
    useDateFormatImpl(date, formatStr, optionsOf)
