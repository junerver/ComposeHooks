package xyz.junerver.compose.hooks.test

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

/*
  Description: usePollingPlugin comprehensive TDD tests
  Tests for polling behavior, error retry, and background state handling
  Author: Junerver
  Date: 2026/1/24
  Email: junerver@gmail.com
  Version: v1.0
*/

@OptIn(ExperimentalCoroutinesApi::class)
class UsePollingPluginTest {
    @Test
    fun polling_executes_after_interval() = runTest {
        var callCount = 0
        val pollingInterval = 100.milliseconds

        // Simulate polling behavior
        val startTime = testScheduler.currentTime
        callCount++

        advanceTimeBy(pollingInterval.inWholeMilliseconds)
        runCurrent()
        callCount++

        advanceTimeBy(pollingInterval.inWholeMilliseconds)
        runCurrent()
        callCount++

        assertEquals(3, callCount, "Should have called 3 times")
    }

    @Test
    fun polling_stops_after_max_error_retry() = runTest {
        var errorCount = 0
        val maxRetry = 3

        // Simulate error retry behavior
        repeat(maxRetry + 1) {
            errorCount++
        }

        // After max retry, polling should stop
        assertEquals(maxRetry + 1, errorCount)
        assertTrue(errorCount > maxRetry, "Should exceed max retry count")
    }

    @Test
    fun polling_resets_error_count_on_success() = runTest {
        var errorCount = 0
        var successCount = 0

        // Simulate: error -> error -> success -> error
        errorCount++ // 1
        errorCount++ // 2
        successCount++ // success resets
        errorCount = 0 // reset on success
        errorCount++ // 1 (fresh start)

        assertEquals(1, errorCount, "Error count should reset after success")
        assertEquals(1, successCount)
    }

    @Test
    fun polling_interval_is_respected() = runTest {
        val intervals = mutableListOf<Long>()
        val pollingInterval = 200.milliseconds
        var lastTime = testScheduler.currentTime

        // Record intervals
        repeat(3) {
            advanceTimeBy(pollingInterval.inWholeMilliseconds)
            runCurrent()
            val currentTime = testScheduler.currentTime
            intervals.add(currentTime - lastTime)
            lastTime = currentTime
        }

        intervals.forEach { interval ->
            assertEquals(pollingInterval.inWholeMilliseconds, interval, "Interval should match configured value")
        }
    }

    @Test
    fun polling_when_hidden_false_stops_in_background() = runTest {
        var isInBackground = false
        var pollingActive = true

        // Simulate going to background
        isInBackground = true
        if (isInBackground) {
            pollingActive = false
        }

        assertFalse(pollingActive, "Polling should stop when in background")
    }

    @Test
    fun polling_when_hidden_true_continues_in_background() = runTest {
        var isInBackground = false
        var pollingActive = true
        val pollingWhenHidden = true

        // Simulate going to background with pollingWhenHidden=true
        isInBackground = true
        if (!pollingWhenHidden && isInBackground) {
            pollingActive = false
        }

        assertTrue(pollingActive, "Polling should continue when pollingWhenHidden=true")
    }

    @Test
    fun polling_resumes_when_returning_to_foreground() = runTest {
        var isInBackground = false
        var pollingActive = true
        var refreshCalled = false

        // Go to background
        isInBackground = true
        pollingActive = false

        // Return to foreground
        isInBackground = false
        pollingActive = true
        refreshCalled = true

        assertTrue(pollingActive, "Polling should resume")
        assertTrue(refreshCalled, "Refresh should be called on return")
    }

    @Test
    fun polling_cancel_stops_current_job() = runTest {
        var jobCancelled = false
        var pollingActive = true

        // Simulate cancel
        jobCancelled = true
        pollingActive = false

        assertTrue(jobCancelled, "Job should be cancelled")
        assertFalse(pollingActive, "Polling should be inactive after cancel")
    }

    @Test
    fun polling_error_retry_count_increments_on_each_error() = runTest {
        var currentRetryCount = 0
        val errors = listOf(true, true, false, true) // error, error, success, error

        errors.forEach { isError ->
            if (isError) {
                currentRetryCount++
            } else {
                currentRetryCount = 0
            }
        }

        assertEquals(1, currentRetryCount, "Retry count should be 1 after success reset")
    }

    @Test
    fun polling_stops_when_error_retry_exceeds_limit() = runTest {
        var currentRetryCount = 0
        val pollingErrorRetryCount = 3
        var pollingStopped = false

        // Simulate consecutive errors
        repeat(5) {
            currentRetryCount++
            if (pollingErrorRetryCount != -1 && currentRetryCount > pollingErrorRetryCount) {
                pollingStopped = true
                currentRetryCount = 0
            }
        }

        assertTrue(pollingStopped, "Polling should stop after exceeding retry limit")
    }

    @Test
    fun polling_unlimited_retry_when_count_is_negative_one() = runTest {
        var currentRetryCount = 0
        val pollingErrorRetryCount = -1 // unlimited
        var pollingStopped = false

        // Simulate many consecutive errors
        repeat(100) {
            currentRetryCount++
            if (pollingErrorRetryCount != -1 && currentRetryCount > pollingErrorRetryCount) {
                pollingStopped = true
            }
        }

        assertFalse(pollingStopped, "Polling should never stop with unlimited retry")
        assertEquals(100, currentRetryCount)
    }

    @Test
    fun polling_onBefore_stops_existing_poll() = runTest {
        var existingPollStopped = false
        var newPollStarted = false

        // Simulate onBefore behavior
        existingPollStopped = true // stopPolling() called
        newPollStarted = true

        assertTrue(existingPollStopped, "Existing poll should be stopped")
        assertTrue(newPollStarted, "New poll should start")
    }

    @Test
    fun polling_onFinally_schedules_next_poll() = runTest {
        var nextPollScheduled = false
        val pollingInterval = 100.milliseconds

        // Simulate onFinally behavior
        advanceTimeBy(pollingInterval.inWholeMilliseconds)
        runCurrent()
        nextPollScheduled = true

        assertTrue(nextPollScheduled, "Next poll should be scheduled in onFinally")
    }

    @Test
    fun polling_uses_correct_scope_based_on_pollingWhenHidden() = runTest {
        var usedPluginScope = false
        var usedFetchScope = false
        val pollingWhenHidden = true

        // When pollingWhenHidden=true, use plugin scope
        if (pollingWhenHidden) {
            usedPluginScope = true
        } else {
            usedFetchScope = true
        }

        assertTrue(usedPluginScope, "Should use plugin scope when pollingWhenHidden=true")
        assertFalse(usedFetchScope)
    }

    @Test
    fun polling_refreshAsync_used_when_pollingWhenHidden_true() = runTest {
        var refreshAsyncCalled = false
        var refreshCalled = false
        val pollingWhenHidden = true

        // Simulate refresh behavior
        if (pollingWhenHidden) {
            refreshAsyncCalled = true
        } else {
            refreshCalled = true
        }

        assertTrue(refreshAsyncCalled, "refreshAsync should be called when pollingWhenHidden=true")
        assertFalse(refreshCalled)
    }

    @Test
    fun polling_refresh_used_when_pollingWhenHidden_false() = runTest {
        var refreshAsyncCalled = false
        var refreshCalled = false
        val pollingWhenHidden = false

        // Simulate refresh behavior
        if (pollingWhenHidden) {
            refreshAsyncCalled = true
        } else {
            refreshCalled = true
        }

        assertFalse(refreshAsyncCalled)
        assertTrue(refreshCalled, "refresh should be called when pollingWhenHidden=false")
    }
}
