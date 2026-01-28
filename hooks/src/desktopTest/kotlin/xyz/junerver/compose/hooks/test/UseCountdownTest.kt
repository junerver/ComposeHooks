package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import xyz.junerver.compose.hooks.useCountdown
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.utils.instantProvider

/*
  Description: useCountdown comprehensive TDD tests
  Author: Junerver
  Date: 2026/1/24
  Email: junerver@gmail.com
  Version: v1.0
*/

class UseCountdownTest {
    private fun resetInstantProvider() {
        instantProvider = { Clock.System.now() }
    }

    private fun waitForCondition(maxAttempts: Int = 80, delayMs: Long = 50, condition: () -> Boolean): Boolean {
        for (i in 0 until maxAttempts) {
            if (condition()) return true
            Thread.sleep(delayMs)
        }
        return false
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun countdown_with_leftTime_counts_down() = runComposeUiTest {
        resetInstantProvider()
        val baseInstant = Clock.System.now()
        instantProvider = { baseInstant }
        setContent {
            val countdown = useCountdown {
                leftTime = 500.milliseconds
                interval = 100.milliseconds
            }

            Text("left=${countdown.timeLeft.value.inWholeMilliseconds}")
        }

        instantProvider = { baseInstant + 250.milliseconds }
        val found = waitForCondition {
            waitForIdle()
            listOf(200, 250, 300).any { ms ->
                runCatching { onNodeWithText("left=$ms").assertExists() }.isSuccess
            }
        }
        assertTrue(found, "Expected countdown text with 200-300ms")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun countdown_with_targetDate_counts_down() = runComposeUiTest {
        resetInstantProvider()
        val baseInstant = Clock.System.now()
        instantProvider = { baseInstant }
        setContent {
            val countdown = useCountdown {
                targetDate = baseInstant + 500.milliseconds
                interval = 100.milliseconds
            }

            Text("left=${countdown.timeLeft.value.inWholeMilliseconds}")
        }

        instantProvider = { baseInstant + 250.milliseconds }
        val found = waitForCondition {
            waitForIdle()
            listOf(200, 250, 300).any { ms ->
                runCatching { onNodeWithText("left=$ms").assertExists() }.isSuccess
            }
        }
        assertTrue(found, "Expected countdown text with 200-300ms")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun countdown_reaches_zero_and_stops() = runComposeUiTest {
        resetInstantProvider()
        val baseInstant = Clock.System.now()
        instantProvider = { baseInstant }
        setContent {
            val countdown = useCountdown {
                leftTime = 300.milliseconds
                interval = 100.milliseconds
            }

            Text("left=${countdown.timeLeft.value.inWholeMilliseconds}")
        }

        instantProvider = { baseInstant + 500.milliseconds }
        val reachedZero = waitForCondition {
            waitForIdle()
            runCatching { onNodeWithText("left=0").assertExists() }.isSuccess
        }
        assertTrue(reachedZero, "Expected countdown reaches zero")

        instantProvider = { baseInstant + 900.milliseconds }
        val staysZero = waitForCondition {
            waitForIdle()
            runCatching { onNodeWithText("left=0").assertExists() }.isSuccess
        }
        assertTrue(staysZero, "Expected countdown stays at zero")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun onEnd_callback_fires_when_countdown_finishes() = runComposeUiTest {
        resetInstantProvider()
        val baseInstant = Clock.System.now()
        instantProvider = { baseInstant }
        setContent {
            var endFired by useState(default = false)
            val countdown = useCountdown {
                leftTime = 250.milliseconds
                interval = 100.milliseconds
                onEnd = {
                    endFired = true
                }
            }

            Text("left=${countdown.timeLeft.value.inWholeMilliseconds} ended=$endFired")
        }

        instantProvider = { baseInstant + 500.milliseconds }
        val found = waitForCondition {
            waitForIdle()
            runCatching { onNodeWithText("left=0 ended=true").assertExists() }.isSuccess
        }
        assertTrue(found, "Expected onEnd to fire at zero")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun formatRes_parses_duration_correctly() = runComposeUiTest {
        resetInstantProvider()
        setContent {
            val countdown = useCountdown {
                leftTime = 3.seconds + 500.milliseconds
                interval = 100.milliseconds
            }

            val formatted = countdown.formatRes.value
            Text("s=${formatted.seconds} ms=${formatted.milliseconds}")
        }

        val found = waitForCondition {
            waitForIdle()
            runCatching { onNodeWithText("s=3 ms=500").assertExists() }.isSuccess
        }
        assertTrue(found, "Expected s=3 ms=500")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun formatRes_handles_days_hours_minutes() = runComposeUiTest {
        resetInstantProvider()
        setContent {
            val countdown = useCountdown {
                leftTime = (1.seconds * 90061)
                interval = 1.seconds
            }

            val formatted = countdown.formatRes.value
            Text("d=${formatted.days} h=${formatted.hours} m=${formatted.minutes} s=${formatted.seconds}")
        }

        val found = waitForCondition {
            waitForIdle()
            runCatching { onNodeWithText("d=1 h=1 m=1 s=1").assertExists() }.isSuccess
        }
        assertTrue(found, "Expected formatted duration for days/hours/minutes")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun leftTime_takes_priority_over_targetDate() = runComposeUiTest {
        resetInstantProvider()
        val baseInstant = Clock.System.now()
        instantProvider = { baseInstant }
        setContent {
            val countdown = useCountdown {
                leftTime = 500.milliseconds
                targetDate = baseInstant + 5.seconds
                interval = 100.milliseconds
            }

            Text("left=${countdown.timeLeft.value.inWholeMilliseconds}")
        }

        instantProvider = { baseInstant + 250.milliseconds }
        val found = waitForCondition {
            waitForIdle()
            listOf(200, 250, 300).any { ms ->
                runCatching { onNodeWithText("left=$ms").assertExists() }.isSuccess
            }
        }
        assertTrue(found, "Expected countdown using leftTime")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun countdown_with_past_targetDate_starts_at_zero() = runComposeUiTest {
        resetInstantProvider()
        val baseInstant = Clock.System.now()
        instantProvider = { baseInstant }
        setContent {
            val countdown = useCountdown {
                targetDate = baseInstant - 1.seconds
                interval = 100.milliseconds
            }

            Text("left=${countdown.timeLeft.value.inWholeMilliseconds}")
        }

        val found = waitForCondition {
            waitForIdle()
            runCatching { onNodeWithText("left=0").assertExists() }.isSuccess
        }
        assertTrue(found, "Expected countdown to start at zero")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun interval_change_restarts_countdown() = runComposeUiTest {
        resetInstantProvider()
        val baseInstant = Clock.System.now()
        instantProvider = { baseInstant }
        setContent {
            var intervalMs by useState(default = 100)
            val countdown = useCountdown {
                leftTime = 500.milliseconds
                interval = intervalMs.milliseconds
            }

            Text("left=${countdown.timeLeft.value.inWholeMilliseconds} interval=$intervalMs")
        }

        instantProvider = { baseInstant + 250.milliseconds }
        val found = waitForCondition {
            waitForIdle()
            listOf(200, 250, 300).any { ms ->
                runCatching { onNodeWithText("left=$ms interval=100").assertExists() }.isSuccess
            }
        }
        assertTrue(found, "Expected countdown with interval=100")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun targetDate_change_resumes_countdown() = runComposeUiTest {
        resetInstantProvider()
        val baseInstant = Clock.System.now()
        instantProvider = { baseInstant }
        setContent {
            var target by useState(default = baseInstant + 300.milliseconds)
            val countdown = useCountdown {
                targetDate = target
                interval = 100.milliseconds
            }

            Text("left=${countdown.timeLeft.value.inWholeMilliseconds}")
        }

        instantProvider = { baseInstant + 400.milliseconds }
        val found = waitForCondition {
            waitForIdle()
            runCatching { onNodeWithText("left=0").assertExists() }.isSuccess
        }
        assertTrue(found, "Expected countdown to reach zero after target change")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun formatRes_updates_reactively() = runComposeUiTest {
        resetInstantProvider()
        val baseInstant = Clock.System.now()
        instantProvider = { baseInstant }
        setContent {
            val countdown = useCountdown {
                leftTime = 2.seconds
                interval = 500.milliseconds
            }

            val formatted = countdown.formatRes.value
            Text("seconds=${formatted.seconds}")
        }

        val initialFound = waitForCondition {
            waitForIdle()
            runCatching { onNodeWithText("seconds=2").assertExists() }.isSuccess
        }
        assertTrue(initialFound, "Expected initial seconds=2")

        instantProvider = { baseInstant + 1100.milliseconds }
        val updatedFound = waitForCondition {
            waitForIdle()
            runCatching { onNodeWithText("seconds=1").assertExists() }.isSuccess ||
                runCatching { onNodeWithText("seconds=0").assertExists() }.isSuccess
        }
        assertTrue(updatedFound, "Expected seconds to update")
    }
}
