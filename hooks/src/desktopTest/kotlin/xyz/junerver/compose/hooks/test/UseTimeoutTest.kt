package xyz.junerver.compose.hooks.test

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.useTimeout

/*
  Description: useTimeout lifecycle and callback tests
  Author: AI Assistant
  Date: 2026/7/15
  Email: junerver@gmail.com
  Version: v1.0
*/

@Suppress("DEPRECATION")
class UseTimeoutTest {
    private fun waitForCondition(maxAttempts: Int = 120, delayMs: Long = 25, condition: () -> Boolean): Boolean {
        repeat(maxAttempts) {
            if (condition()) return true
            Thread.sleep(delayMs)
        }
        return false
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun timeout_uses_latest_callback_after_recomposition() = runComposeUiTest {
        val result = AtomicInteger(0)
        lateinit var updateMultiplier: () -> Unit

        setContent {
            var multiplier by useState(default = 1)
            updateMultiplier = { multiplier = 10 }
            useTimeout(delay = 250.milliseconds) {
                result.addAndGet(multiplier)
            }
        }

        updateMultiplier()
        waitForIdle()
        mainClock.advanceTimeBy(300)
        assertTrue(
            waitForCondition {
                waitForIdle()
                result.get() != 0
            },
            "Expected timeout callback to execute",
        )
        assertEquals(10, result.get(), "Timeout must invoke the latest recomposed callback")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun unmount_cancels_pending_timeout() = runComposeUiTest {
        val count = AtomicInteger(0)
        lateinit var unmount: () -> Unit

        setContent {
            var mounted by useState(default = true)
            unmount = { mounted = false }
            if (mounted) {
                useTimeout(delay = 150.milliseconds) {
                    count.incrementAndGet()
                }
            }
        }

        unmount()
        waitForIdle()
        mainClock.advanceTimeBy(250)
        waitForIdle()
        assertEquals(0, count.get(), "Unmount must cancel the pending timeout callback")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun zero_delay_executes_timeout_once() = runComposeUiTest {
        val count = AtomicInteger(0)

        setContent {
            useTimeout(delay = Duration.ZERO) {
                count.incrementAndGet()
            }
        }

        waitForIdle()
        mainClock.advanceTimeBy(1)
        waitForIdle()
        assertEquals(1, count.get(), "A zero-delay timeout must execute exactly once")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun negative_delay_is_rejected_before_timeout_starts() {
        val result = runCatching {
            runComposeUiTest {
                setContent {
                    useTimeout(delay = (-1).milliseconds) {}
                }
            }
        }

        assertTrue(
            result.exceptionOrNull() is IllegalArgumentException,
            "Expected a negative delay to fail with IllegalArgumentException, actual=${result.exceptionOrNull()}",
        )
    }
}
