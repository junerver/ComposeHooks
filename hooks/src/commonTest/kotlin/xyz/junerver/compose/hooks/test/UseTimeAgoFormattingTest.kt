package xyz.junerver.compose.hooks.test

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import xyz.junerver.compose.hooks.usetimeago.DefaultChineseTimeAgoMessages
import xyz.junerver.compose.hooks.usetimeago.FormatTimeAgoOptions
import xyz.junerver.compose.hooks.usetimeago.TimeAgoUnit
import xyz.junerver.compose.hooks.usetimeago.TimeAgoUnitNames
import xyz.junerver.compose.hooks.usetimeago.formatTimeAgo

class UseTimeAgoFormattingTest {
    private val now = Instant.fromEpochMilliseconds(1_000_000)

    @Test
    fun format_time_ago_returns_just_now_for_small_diff_without_seconds() {
        val from = now - 20.seconds

        val result = formatTimeAgo(from, now = now)

        assertEquals("just now", result)
    }

    @Test
    fun format_time_ago_returns_seconds_when_enabled() {
        val from = now - 20.seconds
        val options = FormatTimeAgoOptions.optionOf {
            showSecond = true
        }

        val result = formatTimeAgo(from, options = options, now = now)

        assertEquals("20 seconds ago", result)
    }

    @Test
    fun format_time_ago_supports_future_times() {
        val from = now + 2.hours

        val result = formatTimeAgo(from, now = now)

        assertEquals("in 2 hours", result)
    }

    @Test
    fun format_time_ago_respects_floor_rounding() {
        val from = now - 89.minutes
        val options = FormatTimeAgoOptions.optionOf {
            rounding = "floor"
        }

        val result = formatTimeAgo(from, options = options, now = now)

        assertEquals("1 hour ago", result)
    }

    @Test
    fun format_time_ago_uses_full_date_formatter_after_max_threshold() {
        val from = now - 10.days
        val options = FormatTimeAgoOptions.optionOf {
            max = 7.days.inWholeMilliseconds
            fullDateFormatter = { "FULL:${it.toEpochMilliseconds()}" }
        }

        val result = formatTimeAgo(from, options = options, now = now)

        assertEquals("FULL:${from.toEpochMilliseconds()}", result)
    }

    @Test
    fun format_time_ago_supports_custom_messages() {
        val from = now - 3.minutes
        val options = FormatTimeAgoOptions.optionOf {
            messages = DefaultChineseTimeAgoMessages
        }

        val result = formatTimeAgo(from, options = options, now = now)

        assertEquals("3分钟前", result)
    }

    @Test
    fun format_time_ago_supports_custom_units() {
        val from = now - 36.hours
        val options = FormatTimeAgoOptions.optionOf {
            units = listOf(
                TimeAgoUnit(max = 48.hours.inWholeMilliseconds, value = 12.hours.inWholeMilliseconds, name = TimeAgoUnitNames.HOUR),
                TimeAgoUnit(max = Long.MAX_VALUE, value = 24.hours.inWholeMilliseconds, name = TimeAgoUnitNames.DAY),
            )
        }

        val result = formatTimeAgo(from, options = options, now = now)

        assertEquals("3 hours ago", result)
    }

    @Test
    fun exact_now_is_just_now_even_when_seconds_are_enabled() {
        val options = FormatTimeAgoOptions.optionOf { showSecond = true }

        assertEquals("just now", formatTimeAgo(now, options = options, now = now))
    }

    @Test
    fun distant_instants_do_not_overflow_into_future_seconds() {
        val result = formatTimeAgo(
            from = Instant.DISTANT_PAST,
            options = FormatTimeAgoOptions.optionOf { showSecond = true },
            now = Instant.DISTANT_FUTURE,
        )

        assertTrue(result.endsWith("years ago"), "Expected a distant past result, got: $result")
    }

    @Test
    fun invalid_custom_units_fail_with_clear_argument_error() {
        listOf(
            emptyList(),
            listOf(TimeAgoUnit(max = 1_000, value = 0, name = TimeAgoUnitNames.SECOND)),
        ).forEach { invalidUnits ->
            val options = FormatTimeAgoOptions.optionOf {
                showSecond = true
                units = invalidUnits
            }

            assertFailsWith<IllegalArgumentException> {
                formatTimeAgo(now - 1.seconds, options = options, now = now)
            }
        }
    }
}
