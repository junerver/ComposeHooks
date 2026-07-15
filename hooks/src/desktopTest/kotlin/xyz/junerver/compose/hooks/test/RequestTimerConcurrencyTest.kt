/*
  Description: Concurrent linearization tests for useRequest timers
  Author: Junerver
  Date: 2026/7/15
  Email: junerver@gmail.com
  Version: v1.0
*/

package xyz.junerver.compose.hooks.test

import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import xyz.junerver.compose.hooks.userequest.plugins.PollingTimer
import xyz.junerver.compose.hooks.userequest.plugins.RetryTimer

@OptIn(ExperimentalCoroutinesApi::class)
class RequestTimerConcurrencyTest {
    private fun assertConcurrentReplacementKeepsOnlyLatest(
        schedule: (CoroutineScope, Duration, suspend () -> Unit) -> Unit,
        cancel: () -> Unit,
    ) {
        val scope = TestScope(StandardTestDispatcher())
        val executor = Executors.newFixedThreadPool(8)
        val start = CountDownLatch(1)
        val calls = AtomicInteger(0)

        try {
            val futures = (0 until 64).map {
                executor.submit {
                    start.await()
                    schedule(scope, 1.milliseconds) { calls.incrementAndGet() }
                }
            }
            start.countDown()
            futures.forEach { it.get(10, TimeUnit.SECONDS) }
            scope.testScheduler.advanceUntilIdle()

            assertEquals(1, calls.get())
        } finally {
            cancel()
            executor.shutdownNow()
        }
    }

    private fun assertCancelWinsAfterConcurrentPublication(
        schedule: (CoroutineScope, Duration, suspend () -> Unit) -> Unit,
        cancel: () -> Unit,
    ) {
        val scope = TestScope(StandardTestDispatcher())
        val executor = Executors.newFixedThreadPool(8)
        val start = CountDownLatch(1)
        val calls = AtomicInteger(0)

        try {
            val futures = (0 until 64).map {
                executor.submit {
                    start.await()
                    schedule(scope, 1.milliseconds) { calls.incrementAndGet() }
                }
            }
            start.countDown()
            futures.forEach { it.get(10, TimeUnit.SECONDS) }
            cancel()
            scope.testScheduler.advanceUntilIdle()

            assertEquals(0, calls.get())
        } finally {
            cancel()
            executor.shutdownNow()
        }
    }

    @Test
    fun pollingTimerConcurrentReplacementKeepsOnlyLatest() {
        val timer = PollingTimer()
        assertConcurrentReplacementKeepsOnlyLatest(timer::schedule, timer::cancel)
    }

    @Test
    fun pollingTimerCancelWinsAfterConcurrentPublication() {
        val timer = PollingTimer()
        assertCancelWinsAfterConcurrentPublication(timer::schedule, timer::cancel)
    }

    @Test
    fun retryTimerConcurrentReplacementKeepsOnlyLatest() {
        val timer = RetryTimer()
        assertConcurrentReplacementKeepsOnlyLatest(timer::schedule, timer::cancel)
    }

    @Test
    fun retryTimerCancelWinsAfterConcurrentPublication() {
        val timer = RetryTimer()
        assertCancelWinsAfterConcurrentPublication(timer::schedule, timer::cancel)
    }
}
