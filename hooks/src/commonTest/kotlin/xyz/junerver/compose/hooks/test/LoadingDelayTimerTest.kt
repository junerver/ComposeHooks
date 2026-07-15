package xyz.junerver.compose.hooks.test

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import xyz.junerver.compose.hooks.userequest.plugins.LoadingDelayTimer

/*
  Description: LoadingDelayTimer lifecycle tests
  Author: Junerver
  Date: 2026/7/15
  Email: junerver@gmail.com
  Version: v1.0
*/
@OptIn(ExperimentalCoroutinesApi::class)
class LoadingDelayTimerTest {
    @Test
    fun newer_schedule_replaces_the_previous_task() = runTest {
        val timer = LoadingDelayTimer()
        val calls = mutableListOf<Int>()

        timer.schedule(this, 100.milliseconds) { calls += 1 }
        advanceTimeBy(50)
        runCurrent()
        timer.schedule(this, 100.milliseconds) { calls += 2 }

        advanceTimeBy(50)
        runCurrent()
        assertEquals(emptyList(), calls)

        advanceTimeBy(50)
        runCurrent()
        assertEquals(listOf(2), calls)
    }

    @Test
    fun cancel_drops_the_pending_task() = runTest {
        val timer = LoadingDelayTimer()
        val calls = mutableListOf<Int>()

        timer.schedule(this, 100.milliseconds) { calls += 1 }
        timer.cancel()
        advanceTimeBy(100)
        runCurrent()

        assertEquals(emptyList(), calls)
    }

    @Test
    fun callback_can_schedule_the_next_task() = runTest {
        val timer = LoadingDelayTimer()
        val calls = mutableListOf<Int>()

        timer.schedule(this, 100.milliseconds) {
            calls += 1
            timer.schedule(this, 50.milliseconds) { calls += 2 }
        }

        advanceTimeBy(100)
        runCurrent()
        assertEquals(listOf(1), calls)

        advanceTimeBy(50)
        runCurrent()
        assertEquals(listOf(1, 2), calls)
    }

    @Test
    fun concurrent_schedules_leave_only_one_pending_task() = runTest {
        val timer = LoadingDelayTimer()
        val timerScope = this
        val calls = mutableListOf<Int>()

        (0 until 100).map { value ->
            async(Dispatchers.Default) {
                timer.schedule(timerScope, 100.milliseconds) { calls += value }
            }
        }.awaitAll()

        advanceTimeBy(100)
        runCurrent()
        assertEquals(1, calls.size)
    }
}
