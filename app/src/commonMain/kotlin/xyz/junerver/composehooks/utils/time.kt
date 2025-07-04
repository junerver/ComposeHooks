package xyz.junerver.composehooks.utils

import androidx.compose.runtime.Stable
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.toLocalDateTime

/*
  Description:
  Author: Junerver
  Date: 2024/7/9-11:50
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Timestamp
 *
 * @constructor Create empty Timestamp
 * @property value
 */
data class Timestamp(
    val value: Long,
) : Comparable<Timestamp> {
    override fun compareTo(other: Timestamp): Int = value.compareTo(other.value)

    /** Add two [Timestamp]s together. */
    @Stable
    operator fun plus(other: Timestamp) = Timestamp(value = this.value + other.value)

    /** Subtract a Timestamp from another one. */
    @Stable
    operator fun minus(other: Timestamp) = Timestamp(value = this.value - other.value)

    @Stable
    inline val asTimestampSeconds: Long get() = value / 1000

    @Stable
    inline val asTimestampMilliseconds: Long get() = value

    companion object {
        /**
         * Create a Timestamp
         *
         * @return
         */
        fun now(): Timestamp = Clock.System
            .now()
            .toEpochMilliseconds()
            .tsMs
    }
}

@Stable
inline val Long.tsMs: Timestamp get() = Timestamp(value = this)

@Stable
inline val Long.tsS: Timestamp get() = Timestamp(value = this * 1000)

public fun Timestamp.toLocalDateTime(timeZone: TimeZone = TimeZone.currentSystemDefault()) =
    Instant.fromEpochMilliseconds(this.value).toLocalDateTime(timeZone)

public val DayOfWeekNames.Companion.CHINESE_FULL: DayOfWeekNames
    get() = DayOfWeekNames("星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日")

public val DayOfWeekNames.Companion.CHINESE_ABBREVIATED: DayOfWeekNames
    get() = DayOfWeekNames("周一", "周二", "周三", "周四", "周五", "周六", "周日")
