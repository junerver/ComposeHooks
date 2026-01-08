package xyz.junerver.compose.hooks.test

import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import xyz.junerver.compose.hooks.Throttle
import xyz.junerver.compose.hooks.UseThrottleOptions

/*
  Description: Comprehensive tests for useThrottle core behavior
  Author: Junerver
  Date: 2026/1/8
  Email: junerver@gmail.com
  Version: v1.0
*/

@OptIn(ExperimentalCoroutinesApi::class)
class UseThrottleComprehensiveTest {
    private fun TestScope.nowInstant(): Instant = Instant.fromEpochMilliseconds(testScheduler.currentTime)

    @Test
    fun throttleLeadingTrueTrailingTrueCallsImmediatelyAndTrailsOnce() = runTest {
        val calls = mutableListOf<Int>()
        val options = UseThrottleOptions.optionOf {
            wait = 100.milliseconds
            leading = true
            trailing = true
        }
        val throttle = Throttle<Int>(
            fn = { calls += it },
            scope = this,
            options = options,
            now = { nowInstant() },
        )

        throttle.invoke(1)
        runCurrent()
        assertEquals(listOf(1), calls)

        advanceTimeBy(10)
        runCurrent()
        throttle.invoke(2)
        advanceTimeBy(10)
        runCurrent()
        throttle.invoke(3)

        advanceTimeBy(79)
        runCurrent()
        assertEquals(listOf(1), calls)

        advanceTimeBy(1)
        runCurrent()
        assertEquals(listOf(1, 3), calls)
    }

    @Test
    fun throttleLeadingFalseTrailingTrueDelaysFirstAndUsesLastArgs() = runTest {
        val calls = mutableListOf<Int>()
        val options = UseThrottleOptions.optionOf {
            wait = 100.milliseconds
            leading = false
            trailing = true
        }
        val throttle = Throttle<Int>(
            fn = { calls += it },
            scope = this,
            options = options,
            now = { nowInstant() },
        )

        throttle.invoke(1)
        runCurrent()
        assertEquals(emptyList(), calls)

        advanceTimeBy(50)
        runCurrent()
        throttle.invoke(2)

        advanceTimeBy(49)
        runCurrent()
        assertEquals(emptyList(), calls)

        advanceTimeBy(1)
        runCurrent()
        assertEquals(listOf(2), calls)
    }

    @Test
    fun throttleTrailingFalseIgnoresCallsDuringWindow() = runTest {
        val calls = mutableListOf<Int>()
        val options = UseThrottleOptions.optionOf {
            wait = 100.milliseconds
            leading = true
            trailing = false
        }
        val throttle = Throttle<Int>(
            fn = { calls += it },
            scope = this,
            options = options,
            now = { nowInstant() },
        )

        throttle.invoke(1)
        runCurrent()
        assertEquals(listOf(1), calls)

        advanceTimeBy(50)
        runCurrent()
        throttle.invoke(2)
        runCurrent()
        assertEquals(listOf(1), calls)

        advanceTimeBy(50)
        runCurrent()
        throttle.invoke(3)
        runCurrent()
        assertEquals(listOf(1, 3), calls)
    }

    @Test
    fun throttleCancelPreventsScheduledTrailing() = runTest {
        val calls = mutableListOf<Int>()
        val options = UseThrottleOptions.optionOf {
            wait = 100.milliseconds
            leading = false
            trailing = true
        }
        val throttle = Throttle<Int>(
            fn = { calls += it },
            scope = this,
            options = options,
            now = { nowInstant() },
        )

        throttle.invoke(1)
        advanceTimeBy(50)
        runCurrent()
        throttle.invoke(2)

        advanceTimeBy(10)
        runCurrent()
        throttle.cancel()

        advanceTimeBy(200)
        runCurrent()
        assertEquals(emptyList(), calls)

        throttle.invoke(3)
        advanceTimeBy(100)
        runCurrent()
        assertEquals(listOf(3), calls)
    }
}
