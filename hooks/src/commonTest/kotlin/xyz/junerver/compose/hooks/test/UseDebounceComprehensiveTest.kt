package xyz.junerver.compose.hooks.test

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import xyz.junerver.compose.hooks.Debounce
import xyz.junerver.compose.hooks.UseDebounceOptions

/*
  Description: Comprehensive tests for useDebounce core behavior
  Author: Junerver
  Date: 2026/1/8
  Email: junerver@gmail.com
  Version: v1.0
*/

@OptIn(ExperimentalCoroutinesApi::class)
class UseDebounceComprehensiveTest {
    private fun TestScope.nowInstant(): Instant = Instant.fromEpochMilliseconds(testScheduler.currentTime)

    @Test
    fun debounceTrailingEmitsLastAfterWait() = runTest {
        val calls = mutableListOf<Int>()
        val options = UseDebounceOptions.optionOf {
            wait = 100.milliseconds
            leading = false
            trailing = true
        }
        val debounce = Debounce<Int>(
            fn = { calls += it },
            scope = this,
            options = options,
            now = { nowInstant() },
        )

        debounce.invoke(1)
        advanceTimeBy(50)
        runCurrent()
        debounce.invoke(2)

        advanceTimeBy(99)
        runCurrent()
        assertEquals(emptyList(), calls)

        advanceTimeBy(1)
        runCurrent()
        assertEquals(listOf(2), calls)
    }

    @Test
    fun debounceLeadingOnlyEmitsImmediatelyOncePerWindow() = runTest {
        val calls = mutableListOf<Int>()
        val options = UseDebounceOptions.optionOf {
            wait = 100.milliseconds
            leading = true
            trailing = false
        }
        val debounce = Debounce<Int>(
            fn = { calls += it },
            scope = this,
            options = options,
            now = { nowInstant() },
        )

        debounce.invoke(1)
        runCurrent()
        assertEquals(listOf(1), calls)

        advanceTimeBy(50)
        runCurrent()
        debounce.invoke(2)
        runCurrent()
        assertEquals(listOf(1), calls)

        advanceTimeBy(100)
        runCurrent()
        debounce.invoke(3)
        runCurrent()
        assertEquals(listOf(1, 3), calls)

        advanceTimeBy(100)
        runCurrent()
    }

    @Test
    fun debounceLeadingAndTrailingOnlyTrailsWhenCalledAgain() = runTest {
        val calls = mutableListOf<Int>()
        val options = UseDebounceOptions.optionOf {
            wait = 100.milliseconds
            leading = true
            trailing = true
        }
        val debounce = Debounce<Int>(
            fn = { calls += it },
            scope = this,
            options = options,
            now = { nowInstant() },
        )

        debounce.invoke(1)
        runCurrent()
        assertEquals(listOf(1), calls)
        advanceTimeBy(100)
        runCurrent()
        assertEquals(listOf(1), calls)

        calls.clear()
        debounce.invoke(1)
        runCurrent()
        advanceTimeBy(10)
        runCurrent()
        debounce.invoke(2)
        advanceTimeBy(10)
        runCurrent()
        debounce.invoke(3)

        assertEquals(listOf(1), calls)
        advanceTimeBy(99)
        runCurrent()
        assertEquals(listOf(1), calls)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(listOf(1, 3), calls)
    }

    @Test
    fun debounceMaxWaitForcesInvocation() = runTest {
        val calls = mutableListOf<Int>()
        val options = UseDebounceOptions.optionOf {
            wait = 100.milliseconds
            leading = false
            trailing = true
            maxWait = 250.milliseconds
        }
        val debounce = Debounce<Int>(
            fn = { calls += it },
            scope = this,
            options = options,
            now = { nowInstant() },
        )

        debounce.invoke(1) // t=0
        advanceTimeBy(80) // t=80
        runCurrent()
        debounce.invoke(2)
        advanceTimeBy(80) // t=160
        runCurrent()
        debounce.invoke(3)
        advanceTimeBy(80) // t=240
        runCurrent()
        debounce.invoke(4)
        assertEquals(emptyList(), calls)

        advanceTimeBy(20) // t=260 (>= maxWait since t=0)
        runCurrent()
        debounce.invoke(5)
        runCurrent()
        assertEquals(listOf(5), calls)

        advanceTimeBy(500)
        runCurrent()
        assertEquals(listOf(5), calls)
    }
}
