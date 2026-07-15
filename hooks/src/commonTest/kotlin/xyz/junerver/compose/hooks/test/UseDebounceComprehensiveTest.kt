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
import xyz.junerver.compose.hooks.usedebounce.Debounce
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

        advanceTimeBy(10) // t=250 (maxWait since t=0)
        runCurrent()
        assertEquals(listOf(4), calls)

        advanceTimeBy(10) // t=260
        debounce.invoke(5)
        advanceTimeBy(99)
        runCurrent()
        assertEquals(listOf(4), calls)

        advanceTimeBy(1)
        runCurrent()
        assertEquals(listOf(4, 5), calls)
    }

    @Test
    fun debounceLeadingSupportsNullableArguments() = runTest {
        val calls = mutableListOf<String?>()
        val debounce = Debounce<String?>(
            fn = { calls += it },
            scope = this,
            options = UseDebounceOptions.optionOf {
                wait = 100.milliseconds
                leading = true
                trailing = false
            },
            now = { nowInstant() },
        )

        debounce.invoke(null)
        runCurrent()

        assertEquals(listOf<String?>(null), calls)
    }

    @Test
    fun debounceCancelClearsTrailingAndRestoresLeadingWindow() = runTest {
        val calls = mutableListOf<Int>()
        val debounce = Debounce<Int>(
            fn = { calls += it },
            scope = this,
            options = UseDebounceOptions.optionOf {
                wait = 100.milliseconds
                leading = true
                trailing = true
            },
            now = { nowInstant() },
        )

        debounce.invoke(1)
        advanceTimeBy(20)
        runCurrent()
        debounce.invoke(2)
        debounce.cancel()

        advanceTimeBy(200)
        runCurrent()
        assertEquals(listOf(1), calls)

        debounce.invoke(3)
        runCurrent()
        assertEquals(listOf(1, 3), calls)
    }

    @Test
    fun debounceLeadingReentrantCallStaysWithinCurrentWindow() = runTest {
        val calls = mutableListOf<Int>()
        lateinit var debounce: Debounce<Int>
        debounce = Debounce(
            fn = { value ->
                calls += value
                if (value == 1) debounce.invoke(2)
            },
            scope = this,
            options = UseDebounceOptions.optionOf {
                wait = 100.milliseconds
                leading = true
                trailing = true
            },
            now = { nowInstant() },
        )

        debounce.invoke(1)
        assertEquals(listOf(1), calls)

        advanceTimeBy(99)
        runCurrent()
        assertEquals(listOf(1), calls)

        advanceTimeBy(1)
        runCurrent()
        assertEquals(listOf(1, 2), calls)
    }

    @Test
    fun debounceTrailingReentrantCallStartsANewWindow() = runTest {
        val calls = mutableListOf<Int>()
        lateinit var debounce: Debounce<Int>
        debounce = Debounce(
            fn = { value ->
                calls += value
                if (value == 1) debounce.invoke(2)
            },
            scope = this,
            options = UseDebounceOptions.optionOf {
                wait = 100.milliseconds
                leading = false
                trailing = true
            },
            now = { nowInstant() },
        )

        debounce.invoke(1)
        advanceTimeBy(100)
        runCurrent()
        assertEquals(listOf(1), calls)

        advanceTimeBy(99)
        runCurrent()
        assertEquals(listOf(1), calls)

        advanceTimeBy(1)
        runCurrent()
        assertEquals(listOf(1, 2), calls)
    }

    @Test
    fun debounceMaxWaitReentrantCallStartsANewWindow() = runTest {
        val calls = mutableListOf<Int>()
        lateinit var debounce: Debounce<Int>
        debounce = Debounce(
            fn = { value ->
                calls += value
                if (value == 4) debounce.invoke(5)
            },
            scope = this,
            options = UseDebounceOptions.optionOf {
                wait = 100.milliseconds
                leading = false
                trailing = true
                maxWait = 250.milliseconds
            },
            now = { nowInstant() },
        )

        debounce.invoke(1)
        advanceTimeBy(80)
        runCurrent()
        debounce.invoke(2)
        advanceTimeBy(80)
        runCurrent()
        debounce.invoke(3)
        advanceTimeBy(80)
        runCurrent()
        debounce.invoke(4)

        advanceTimeBy(10)
        runCurrent()
        assertEquals(listOf(4), calls)

        advanceTimeBy(99)
        runCurrent()
        assertEquals(listOf(4), calls)

        advanceTimeBy(1)
        runCurrent()
        assertEquals(listOf(4, 5), calls)
    }

    @Test
    fun debounceOptionUpdateAppliesLeadingAndTrailingFlags() = runTest {
        val calls = mutableListOf<Int>()
        val debounce = Debounce(
            fn = { value: Int -> calls += value },
            scope = this,
            options = UseDebounceOptions.optionOf {
                wait = 100.milliseconds
                leading = false
                trailing = true
            },
            now = { nowInstant() },
        )

        debounce.invoke(1)
        advanceTimeBy(50)
        runCurrent()
        debounce.update(
            fn = { calls += it },
            options = UseDebounceOptions.optionOf {
                wait = 100.milliseconds
                leading = true
                trailing = false
            },
        )
        debounce.invoke(2)
        runCurrent()

        assertEquals(listOf(2), calls)
        advanceTimeBy(200)
        runCurrent()
        assertEquals(listOf(2), calls)
    }

    @Test
    fun debounceOptionUpdateAppliesNewMaxWaitAndDropsOldWindow() = runTest {
        val calls = mutableListOf<Int>()
        val debounce = Debounce(
            fn = { value: Int -> calls += value },
            scope = this,
            options = UseDebounceOptions.optionOf {
                wait = 200.milliseconds
                maxWait = 500.milliseconds
            },
            now = { nowInstant() },
        )

        debounce.invoke(1)
        advanceTimeBy(50)
        runCurrent()
        debounce.update(
            fn = { calls += it },
            options = UseDebounceOptions.optionOf {
                wait = 200.milliseconds
                maxWait = 100.milliseconds
            },
        )
        debounce.invoke(2)

        advanceTimeBy(99)
        runCurrent()
        assertEquals(emptyList(), calls)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(listOf(2), calls)

        advanceTimeBy(500)
        runCurrent()
        assertEquals(listOf(2), calls)
    }
}
