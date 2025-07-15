package xyz.junerver.compose.hooks.test

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import xyz.junerver.compose.hooks.UseDateFormatOptions
import xyz.junerver.compose.hooks.formatDate
import xyz.junerver.compose.hooks.normalizeDate

class UseDateFormatTest {
    // Test date: Monday, July 14, 2025, 13:45:30.123
    private val testDate = LocalDateTime(2025, 7, 14, 13, 45, 30, 123_000_000)
    private val options = UseDateFormatOptions.Companion.optionOf {}

    @Test
    fun testYearFormats() {
        assertEquals("2025", formatDate(testDate, "YYYY", options))
        assertEquals("25", formatDate(testDate, "YY", options))
        assertEquals("2025th", formatDate(testDate, "Yo", options))
    }

    @Test
    fun testMonthFormats() {
        assertEquals("7", formatDate(testDate, "M", options))
        assertEquals("7th", formatDate(testDate, "Mo", options))
        assertEquals("07", formatDate(testDate, "MM", options))
        assertEquals("Jul", formatDate(testDate, "MMM", options))
        assertEquals("July", formatDate(testDate, "MMMM", options))
    }

    @Test
    fun testdayFormats() {
        assertEquals("14", formatDate(testDate, "D", options))
        assertEquals("14th", formatDate(testDate, "Do", options))
        assertEquals("14", formatDate(testDate, "DD", options))
    }

    @Test
    fun testDayOfWeekFormats() {
        assertEquals("1", formatDate(testDate, "d", options)) // Monday is 1 (Sunday is 0)
        assertEquals("M", formatDate(testDate, "dd", options)) // "Monday" -> "M"
        assertEquals("Mon", formatDate(testDate, "ddd", options))
        assertEquals("Monday", formatDate(testDate, "dddd", options))
    }

    @Test
    fun testHour24Formats() {
        assertEquals("13", formatDate(testDate, "H", options))
        assertEquals("13th", formatDate(testDate, "Ho", options))
        assertEquals("13", formatDate(testDate, "HH", options))
    }

    @Test
    fun testHour12Formats() {
        assertEquals("1", formatDate(testDate, "h", options)) // 13:00 -> 1 PM -> 1
        assertEquals("1st", formatDate(testDate, "ho", options))
        assertEquals("01", formatDate(testDate, "hh", options)) // 13:00 -> 01 PM -> 01
    }

    @Test
    fun testMinuteFormats() {
        assertEquals("45", formatDate(testDate, "m", options))
        assertEquals("45th", formatDate(testDate, "mo", options))
        assertEquals("45", formatDate(testDate, "mm", options))
    }

    @Test
    fun testSecondFormats() {
        assertEquals("30", formatDate(testDate, "s", options))
        assertEquals("30th", formatDate(testDate, "so", options))
        assertEquals("30", formatDate(testDate, "ss", options))
    }

    @Test
    fun testMillisecondFormats() {
        assertEquals("123", formatDate(testDate, "SSS", options))
    }

    @Test
    fun testMeridiemFormats() {
        assertEquals("PM", formatDate(testDate, "A", options))
        assertEquals("P.M.", formatDate(testDate, "AA", options))
        assertEquals("pm", formatDate(testDate, "a", options))
        assertEquals("p.m.", formatDate(testDate, "aa", options))

        // Test AM case with morning time
        val morningDate = LocalDateTime(2025, 7, 14, 9, 30, 15)
        assertEquals("AM", formatDate(morningDate, "A", options))
        assertEquals("A.M.", formatDate(morningDate, "AA", options))
        assertEquals("am", formatDate(morningDate, "a", options))
        assertEquals("a.m.", formatDate(morningDate, "aa", options))
    }

    @Test
    fun testTimezoneFormats() {
        // Using SystemDefault for now, as specific timezone IDs might not be available consistently across platforms
        val systemTimeZone = TimeZone.Companion.currentSystemDefault()
        val systemTimezoneOptions = UseDateFormatOptions.Companion.optionOf {
            timeZone = systemTimeZone
        }

        // The exact output of GMT offset depends on the system's current default timezone and Kotlinx-datetime's implementation.
        // For Singapore, it should be GMT+08:00
        val expectedShortOffset = "+8"
        val expectedLongOffset = "+08:00"

        val zResult = formatDate(testDate, "z", systemTimezoneOptions)
        val zzResult = formatDate(testDate, "zz", systemTimezoneOptions)
        val zzzResult = formatDate(testDate, "zzz", systemTimezoneOptions)
        val zzzzResult = formatDate(testDate, "zzzz", systemTimezoneOptions)

        // All should contain "GMT" and the correct offset for Singapore
        assertTrue(zResult.contains("GMT$expectedShortOffset"))
        assertTrue(zzResult.contains("GMT$expectedShortOffset"))
        assertTrue(zzzResult.contains("GMT$expectedShortOffset"))
        assertTrue(zzzzResult.contains("GMT$expectedLongOffset"))

        // Test with a known specific timezone (if your environment supports it consistently)
        // val newYorkTimeZone = TimeZone.of("America/New_York")
        // val newYorkOptions = UseDateFormatOptions.optionOf { timeZone = newYorkTimeZone }
        // val newYorkZzz = formatDate(testDate, "zzz", newYorkOptions)
        // // Expected for America/New_York on July 14, 2025 (EDT, UTC-04:00)
        // assertTrue(newYorkZzz.contains("GMT-04"))
    }

    @Test
    fun testOrdinalNumbers() {
        // Test various ordinal cases
        val date1st = LocalDateTime(2025, 1, 1, 1, 1, 1)
        val date2nd = LocalDateTime(2025, 2, 2, 2, 2, 2)
        val date3rd = LocalDateTime(2025, 3, 3, 3, 3, 3)
        val date11th = LocalDateTime(2025, 11, 11, 11, 11, 11)
        val date21st = LocalDateTime(2025, 12, 21, 21, 21, 21)

        assertEquals("1st", formatDate(date1st, "Do", options))
        assertEquals("2nd", formatDate(date2nd, "Do", options))
        assertEquals("3rd", formatDate(date3rd, "Do", options))
        assertEquals("11th", formatDate(date11th, "Do", options))
        assertEquals("21st", formatDate(date21st, "Do", options))

        // Also test ordinal for year, month, hour, minute, second
        assertEquals("2025th", formatDate(testDate, "Yo", options))
        assertEquals("7th", formatDate(testDate, "Mo", options))
        assertEquals("13th", formatDate(testDate, "Ho", options))
        assertEquals("1st", formatDate(testDate, "ho", options)) // 1 PM is 1st hour in 12-hour
        assertEquals("45th", formatDate(testDate, "mo", options))
        assertEquals("30th", formatDate(testDate, "so", options))
    }

    @Test
    fun testComplexFormats() {
        // Test complex format strings
        assertEquals("Monday, July 14th, 2025", formatDate(testDate, "dddd, MMMM Do, YYYY", options))
        assertEquals("2025-07-14 13:45:30", formatDate(testDate, "YYYY-MM-DD HH:mm:ss", options))
        // This test now relies on the fix for literal handling
        assertEquals("Jul 14, 2025 at 1:45 PM", formatDate(testDate, "MMM D, YYYY [at] h:mm A", options))
        assertEquals("14/07/25 01:45:30.123 PM", formatDate(testDate, "DD/MM/YY hh:mm:ss.SSS A", options))
        assertEquals(
            "Mon, Jul 14, 2025",
            formatDate(testDate, "ddd, MMM DD, YYYY", options),
        ) // Added from original query context
        assertEquals(
            "1, July 14, 2025",
            formatDate(testDate, "d, MMMM DD, YYYY", options),
        ) // Added from original query context
        assertEquals("July 1, 2025", formatDate(testDate, "MMMM d, YYYY", options)) // Added from original query context
        assertEquals("Monday 1, Mon", formatDate(testDate, "dddd d, ddd", options)) // Added from original query context
    }

    @Test
    fun testCustomMeridiem() {
        val customOptions = UseDateFormatOptions.Companion.optionOf {
            customMeridiem = { hours, _, isLowercase, hasPeriod ->
                val base = if (hours >= 12) "afternoon" else "morning"
                if (isLowercase) base else base.uppercase()
            }
        }

        assertEquals("AFTERNOON", formatDate(testDate, "A", customOptions))
        assertEquals("afternoon", formatDate(testDate, "a", customOptions))

        val morningDate = LocalDateTime(2025, 7, 14, 9, 30, 15)
        assertEquals("MORNING", formatDate(morningDate, "A", customOptions))
        assertEquals("morning", formatDate(morningDate, "a", customOptions))
    }

    @Test
    fun testEdgeCases() {
        // Test midnight
        val midnight = LocalDateTime(2025, 1, 1, 0, 0, 0)
        assertEquals("12", formatDate(midnight, "h", options))
        assertEquals("AM", formatDate(midnight, "A", options))

        // Test noon
        val noon = LocalDateTime(2025, 1, 1, 12, 0, 0)
        assertEquals("12", formatDate(noon, "h", options))
        assertEquals("PM", formatDate(noon, "A", options))

        // Test Sunday (should be 0)
        val sunday = LocalDateTime(2025, 7, 13, 12, 0, 0) // July 13, 2025 is Sunday
        assertEquals("0", formatDate(sunday, "d", options))
        assertEquals("Sunday", formatDate(sunday, "dddd", options))

        // Test with a leap year (February 29th)
        val leapYearDate = LocalDateTime(2024, 2, 29, 10, 0, 0) // 2024 is a leap year
        assertEquals("29", formatDate(leapYearDate, "D", options))
        assertEquals("February", formatDate(leapYearDate, "MMMM", options))
    }

    @Test
    fun testAllFormatsInOneString() {
        // Test a format string that uses many different tokens
        val complexFormat = "Yo YY YYYY Mo M MM MMM MMMM Do D DD d dd ddd dddd Ho H HH ho h hh mo m mm so s ss SSS A AA a aa"
        val result = formatDate(testDate, complexFormat, options)

        // Should contain all the expected parts
        assertTrue(result.contains("2025th"))
        assertTrue(result.contains("25"))
        assertTrue(result.contains("2025"))
        assertTrue(result.contains("7th"))
        assertTrue(result.contains("Jul"))
        assertTrue(result.contains("July"))
        assertTrue(result.contains("14th"))
        assertTrue(result.contains("Monday"))
        assertTrue(result.contains("PM"))
        assertTrue(result.contains("P.M."))
        assertTrue(result.contains("pm"))
        assertTrue(result.contains("p.m."))
        // Check day of week parts specific to testDate (Monday, 1, M, Mon)
        assertTrue(result.contains("1")) // for 'd'
        assertTrue(result.contains("M")) // for 'dd'
        assertTrue(result.contains("Mon")) // for 'ddd'
        assertTrue(result.contains("Monday")) // for 'dddd'
    }

    // --- Added Tests for normalizeDate and Locale ---

    @Test
    fun testNormalizeDate() {
        // Test LocalDateTime input
        val normalizedLdt = normalizeDate(testDate)
        assertEquals(testDate, normalizedLdt)

        // Test Instant input
        val instant = testDate.toInstant(TimeZone.Companion.currentSystemDefault())
        val normalizedInstant = normalizeDate(instant)
        assertEquals(testDate.year, normalizedInstant.year)
        assertEquals(testDate.month, normalizedInstant.month)
        assertEquals(testDate.day, normalizedInstant.day)

        // Test Long (milliseconds) input
        val millis = instant.toEpochMilliseconds()
        val normalizedMillis = normalizeDate(millis)
        assertEquals(testDate.year, normalizedMillis.year)
        assertEquals(testDate.month, normalizedMillis.month)
        assertEquals(testDate.day, normalizedMillis.day)
        // Milliseconds might lose nanosecond precision, so check up to seconds
        assertEquals(testDate.hour, normalizedMillis.hour)
        assertEquals(testDate.minute, normalizedMillis.minute)
        assertEquals(testDate.second, normalizedMillis.second)

        // Test String input (ISO-like)
        val isoString = "2025-07-14T13:45:30.123"
        val normalizedIsoString = normalizeDate(isoString)
        assertEquals(2025, normalizedIsoString.year)
        assertEquals(Month(7), normalizedIsoString.month)
        assertEquals(14, normalizedIsoString.day)
        assertEquals(13, normalizedIsoString.hour)
        assertEquals(45, normalizedIsoString.minute)
        assertEquals(30, normalizedIsoString.second)
        // Milliseconds might be tricky with simple string parse, check up to seconds
        assertTrue(normalizedIsoString.nanosecond / 1_000_000 >= 123 || normalizedIsoString.nanosecond / 1_000_000 == 0) // It multiplies by 1_000_000, so check if at least 123
        // Also check if nanosecond is correctly handled for 123_000_000. It depends on `getOrElse(6) { 0 } * 1_000_000`
        assertEquals(123_000_000, normalizedIsoString.nanosecond)

        // Test malformed String input (should fallback to current time)
        val malformedString = "not a valid date"
        val normalizedMalformed = normalizeDate(malformedString)
        // Since it falls back to Clock.System.now(), we can't assert a fixed date
        // Instead, check if it's a LocalDateTime object and reasonably close to now
        assertIs<LocalDateTime>(normalizedMalformed)
        val now = Clock.System.now().toLocalDateTime(TimeZone.Companion.currentSystemDefault())
        assertTrue(normalizedMalformed.year == now.year && normalizedMalformed.month == now.month) // Should be current month/year
        assertTrue(normalizedMalformed.day == now.day || normalizedMalformed.day == now.day - 1 || normalizedMalformed.day == now.day + 1) // Allow slight day difference due to test execution time
    }

    @Test
    fun testLocaleSpecificFormats() {
        val chineseOptions = UseDateFormatOptions.Companion.optionOf { locale = "zh-CN" }
        val englishOptions = UseDateFormatOptions.Companion.optionOf { locale = "en-US" } // Explicitly set for clarity

        // Test Month names
        assertEquals("七月", formatDate(testDate, "MMMM", chineseOptions))
        assertEquals("七月", formatDate(testDate, "MMM", chineseOptions)) // Abbreviated is same for Chinese
        assertEquals("July", formatDate(testDate, "MMMM", englishOptions))
        assertEquals("Jul", formatDate(testDate, "MMM", englishOptions))

        // Test Day of week names
        assertEquals("星期一", formatDate(testDate, "dddd", chineseOptions))
        assertEquals("周一", formatDate(testDate, "ddd", chineseOptions))
        assertEquals("周一", formatDate(testDate, "dd", chineseOptions)) // "周一" -> "周" (first char)
        assertEquals("Monday", formatDate(testDate, "dddd", englishOptions))
        assertEquals("Mon", formatDate(testDate, "ddd", englishOptions))
        assertEquals("M", formatDate(testDate, "dd", englishOptions)) // "Monday" -> "M" (first char)
    }
}
