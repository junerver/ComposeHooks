/*
  Description: Deterministic tests for the useRequest polling timer
  Author: Junerver
  Date: 2026/07/15
  Email: junerver@gmail.com
  Version: v1.0
*/

package xyz.junerver.compose.hooks.test

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import xyz.junerver.compose.hooks.userequest.plugins.PollingTimer

@OptIn(ExperimentalCoroutinesApi::class)
class PollingTimerTest {
    @Test
    fun scheduleReplacesThePendingTask() = runTest {
        val calls = mutableListOf<String>()
        val timer = PollingTimer()

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
    fun cancelPreventsThePendingTask() = runTest {
        val calls = mutableListOf<String>()
        val timer = PollingTimer()

        timer.schedule(this, 100.milliseconds) { calls += "stale" }
        timer.cancel()
        advanceTimeBy(100)
        runCurrent()

        assertEquals(emptyList(), calls)
    }

    @Test
    fun callbackCanScheduleTheNextTask() = runTest {
        val calls = mutableListOf<Int>()
        val timer = PollingTimer()
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
}
