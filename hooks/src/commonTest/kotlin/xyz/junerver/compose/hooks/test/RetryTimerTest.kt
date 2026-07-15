/*
  Description: Deterministic tests for the useRequest retry timer
  Author: Junerver
  Date: 2026/07/15
  Email: junerver@gmail.com
  Version: v1.0
*/

package xyz.junerver.compose.hooks.test

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import xyz.junerver.compose.hooks.userequest.plugins.RetryTimer
import xyz.junerver.compose.hooks.userequest.plugins.retryDelay

@OptIn(ExperimentalCoroutinesApi::class)
class RetryTimerTest {
    @Test
    fun scheduleReplacesThePendingRetry() = runTest {
        val calls = mutableListOf<String>()
        val timer = RetryTimer()

        timer.schedule(this, 100.milliseconds) { calls += "stale" }
        advanceTimeBy(50)
        runCurrent()
        timer.schedule(this, 100.milliseconds) { calls += "latest" }

        advanceTimeBy(99)
        runCurrent()
        assertEquals(emptyList(), calls)

        advanceTimeBy(1)
        runCurrent()
        assertEquals(listOf("latest"), calls)
    }

    @Test
    fun cancelPreventsThePendingRetry() = runTest {
        val calls = mutableListOf<String>()
        val timer = RetryTimer()

        timer.schedule(this, 100.milliseconds) { calls += "stale" }
        timer.cancel()
        advanceTimeBy(100)
        runCurrent()

        assertEquals(emptyList(), calls)
    }

    @Test
    fun callbackCanScheduleAnotherRetry() = runTest {
        val calls = mutableListOf<Int>()
        val timer = RetryTimer()
        val scope = this

        timer.schedule(scope, 50.milliseconds) {
            calls += 1
            timer.schedule(scope, 50.milliseconds) { calls += 2 }
        }

        advanceTimeBy(50)
        runCurrent()
        assertEquals(listOf(1), calls)

        advanceTimeBy(50)
        runCurrent()
        assertEquals(listOf(1, 2), calls)
    }

    @Test
    fun defaultDelayUsesExponentialBackoffAndCapsAtThirtySeconds() {
        assertEquals(2.seconds, retryDelay(0.seconds, 1))
        assertEquals(4.seconds, retryDelay(0.seconds, 2))
        assertEquals(8.seconds, retryDelay(0.seconds, 3))
        assertEquals(16.seconds, retryDelay(0.seconds, 4))
        assertEquals(30.seconds, retryDelay(0.seconds, 5))
        assertEquals(30.seconds, retryDelay(0.seconds, 100))
    }

    @Test
    fun configuredPositiveDelayWinsAndNegativeDelayUsesDefault() {
        assertEquals(250.milliseconds, retryDelay(250.milliseconds, 4))
        assertEquals(4.seconds, retryDelay((-1).seconds, 2))
    }
}
