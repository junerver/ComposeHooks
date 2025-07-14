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
import xyz.junerver.compose.hooks.utils.useDynamicOptions

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
 * Options for configuring date formatting behavior
 */
@Stable
data class UseDateFormatOptions internal constructor(
    /**
     * The locale to use for formatting
     * Default is system locale
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
    val options = useDynamicOptions(optionsOf, UseDateFormatOptions::optionOf)
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
internal fun normalizeDate(date: DateLike): LocalDateTime {
    return when (date) {
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
}

/**
 * Formats a date according to the specified format string
 */
internal fun formatDate(date: LocalDateTime, formatStr: String, options: UseDateFormatOptions): String {
    val locale = options.locale
    val timeZone = options.timeZone
    val customMeridiem = options.customMeridiem

    // Regex pattern to match format tokens and literal strings in square brackets
    // Order matters: literal match ([^\]]+) should come first to ensure it's prioritized
    val formatRegex = Regex(
        "(\\[[^]]*])|" + // Capture anything inside square brackets as a literal
            "zzzz|zzz|zz|z|" + // Timezone tokens
            "YYYY|YY|Yo|" +    // Year tokens
            "MMMM|MMM|MM|Mo|M|" + // Month tokens
            "dddd|ddd|DD|Do|dd|D|d|" + // Day of week & Day of month tokens (order DD/Do before D)
            "HH|Ho|H|" +       // 24-hour tokens
            "hh|ho|h|" +       // 12-hour tokens
            "mm|mo|m|" +       // Minute tokens
            "ss|so|s|" +       // Second tokens
            "SSS|" +           // Millisecond token
            "AA|A|aa|a",          // Meridiem tokens
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
                "MMMM" -> getMonthName(date.month.number, locale, false)
                "MMM" -> getMonthName(date.month.number, locale, true)
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
                    getDayName(dayOfWeek, locale, false)
                }

                "ddd" -> {
                    val dayOfWeek = getDayOfWeek(date)
                    getDayName(dayOfWeek, locale, true)
                }

                "dd" -> { // **这里是需要修改的地方**
                    val dayOfWeek = getDayOfWeek(date)
                    if (locale?.startsWith("zh") == true) {
                        // 对于中文，dd 可能期望是单字（例如“一”）或两字（例如“周一”）
                        // 根据Day.js的Min name定义，可以考虑返回更短的，或者直接使用ddd的缩写
                        // 如果希望是“周一”这样的，就直接 getDayName(dayOfWeek, locale, true)
                        // 如果希望是“一”，则需要一个更细粒度的映射
                        when (dayOfWeek) {
                            0 -> "周日"
                            1 -> "周一"
                            2 -> "周二"
                            3 -> "周三"
                            4 -> "周四"
                            5 -> "周五"
                            6 -> "周六"
                            else -> ""
                        }
                    } else {
                        // 英文下仍保持取首字母的逻辑，或者改为 Day.js 约定，如 "Su", "Mo"
                        // 根据您现在的测试，是取首字母，所以保持
                        getDayName(dayOfWeek, locale, true).take(1) // 返回 "M"
                    }
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
 * Gets the month name based on locale
 */
private fun getMonthName(month: Int, locale: String?, abbreviated: Boolean): String {
    val months = if (locale?.startsWith("zh") == true) {
        if (abbreviated) {
            arrayOf("一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月")
        } else {
            arrayOf("一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月")
        }
    } else {
        if (abbreviated) {
            arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        } else {
            arrayOf(
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
        }
    }

    return months[month - 1]
}

/**
 * Gets the day name based on locale
 */
private fun getDayName(day: Int, locale: String?, abbreviated: Boolean): String {
    val days = if (locale?.startsWith("zh") == true) {
        if (abbreviated) {
            arrayOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")
        } else {
            arrayOf("星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六")
        }
    } else {
        if (abbreviated) {
            arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        } else {
            arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        }
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
    // 获取当前时间点，因为时区偏移量可能会因夏令时而变化
    val now = Clock.System.now()
    // 获取指定时区在当前时间点的 UTC 偏移量
    val offset: UtcOffset = timeZone.offsetAt(now)

    // 根据偏移量的总秒数计算小时和分钟，并处理正负号
    val sign = if (offset.totalSeconds >= 0) "+" else "-"
    val absHours = abs(offset.totalSeconds / 3600)
    val absMinutes = abs((offset.totalSeconds % 3600) / 60)

    // 根据 longFormat 参数返回不同的格式
    return if (longFormat) {
        // 长格式 (GMT+HH:MM) - 小时和分钟都需要两位数填充
        val hourStringPadded = absHours.toString().padStart(2, '0')
        val minuteStringPadded = absMinutes.toString().padStart(2, '0')
        "GMT$sign$hourStringPadded:$minuteStringPadded" // 例如 "GMT+08:00"
    } else {
        // 非长格式 (GMT+H 或 GMT+HH) - 只使用小时，不进行零填充
        "GMT$sign$absHours" // 例如 "GMT+8" 或 "GMT+9"
    }
}
