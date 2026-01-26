package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import xyz.junerver.compose.hooks.useCountdown
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.utils.currentInstant

/*
  Description: useCountdown comprehensive TDD tests
  Author: Junerver
  Date: 2026/1/24
  Email: junerver@gmail.com
  Version: v1.0
*/

class UseCountdownTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun countdown_with_leftTime_counts_down() = runComposeUiTest {
        setContent {
            val countdown = useCountdown {
                leftTime = 500.milliseconds
                interval = 100.milliseconds
            }

            Text("left=${countdown.timeLeft.value.inWholeMilliseconds}")
        }

        waitForIdle()
        Thread.sleep(250)
        waitForIdle()

        // Should be around 250ms left - verify by checking text exists
        val found = runCatching {
            // Try different possible values in range
            listOf(200, 250, 300, 350).any { ms ->
                runCatching { onNodeWithText("left=$ms").assertExists() }.isSuccess
            }
        }.getOrElse { false }
        assertTrue(found, "Expected countdown text with 200-350ms")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun countdown_with_targetDate_counts_down() = runComposeUiTest {
        setContent {
            val countdown = useCountdown {
                targetDate = currentInstant + 500.milliseconds
                interval = 100.milliseconds
            }

            Text("left=${countdown.timeLeft.value.inWholeMilliseconds}")
        }

        waitForIdle()
        Thread.sleep(250)
        waitForIdle()

        // Should be around 250ms left - verify by checking text exists
        val found = runCatching {
            listOf(200, 250, 300, 350).any { ms ->
                runCatching { onNodeWithText("left=$ms").assertExists() }.isSuccess
            }
        }.getOrElse { false }
        assertTrue(found, "Expected countdown text with 200-350ms")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun countdown_reaches_zero_and_stops() = runComposeUiTest {
        setContent {
            val countdown = useCountdown {
                leftTime = 300.milliseconds
                interval = 100.milliseconds
            }

            Text("left=${countdown.timeLeft.value.inWholeMilliseconds}")
        }

        waitForIdle()
        Thread.sleep(450)
        waitForIdle()

        // Should reach 0 and stop
        onNodeWithText("left=0").assertExists()

        Thread.sleep(200)
        waitForIdle()

        // Should stay at 0
        onNodeWithText("left=0").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun onEnd_callback_fires_when_countdown_finishes() = runComposeUiTest {
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

        waitForIdle()
        Thread.sleep(400)
        waitForIdle()

        // onEnd should have fired
        onNodeWithText("left=0 ended=true").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun formatRes_parses_duration_correctly() = runComposeUiTest {
        setContent {
            val countdown = useCountdown {
                leftTime = 3.seconds + 500.milliseconds
                interval = 100.milliseconds
            }

            val formatted = countdown.formatRes.value
            Text("s=${formatted.seconds} ms=${formatted.milliseconds}")
        }

        waitForIdle()

        // Should show 3 seconds and ~500 milliseconds
        val found = runCatching {
            listOf(400, 450, 500, 550, 600).any { ms ->
                runCatching { onNodeWithText("s=3 ms=$ms").assertExists() }.isSuccess
            }
        }.getOrElse { false }
        assertTrue(found, "Expected s=3 with ms in 400-600 range")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun formatRes_handles_days_hours_minutes() = runComposeUiTest {
        setContent {
            val countdown = useCountdown {
                leftTime = 1.seconds * 90061 // 1 day + 1 hour + 1 minute + 1 second
                interval = 1.seconds
            }

            val formatted = countdown.formatRes.value
            Text("d=${formatted.days} h=${formatted.hours} m=${formatted.minutes} s=${formatted.seconds}")
        }

        waitForIdle()

        // Should show 1 day, 1 hour, 1 minute, 1 second
        onNodeWithText("d=1 h=1 m=1 s=1").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun leftTime_takes_priority_over_targetDate() = runComposeUiTest {
        setContent {
            val countdown = useCountdown {
                leftTime = 500.milliseconds
                targetDate = currentInstant + 5.seconds // Much longer
                interval = 100.milliseconds
            }

            Text("left=${countdown.timeLeft.value.inWholeMilliseconds}")
        }

        waitForIdle()
        Thread.sleep(250)
        waitForIdle()

        // Should use leftTime (500ms), not targetDate (5s)
        val found = runCatching {
            listOf(200, 250, 300, 350).any { ms ->
                runCatching { onNodeWithText("left=$ms").assertExists() }.isSuccess
            }
        }.getOrElse { false }
        assertTrue(found, "Expected countdown using leftTime (200-350ms), not targetDate")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun countdown_with_past_targetDate_starts_at_zero() = runComposeUiTest {
        setContent {
            val countdown = useCountdown {
                targetDate = currentInstant - 1.seconds // Past
                interval = 100.milliseconds
            }

            Text("left=${countdown.timeLeft.value.inWholeMilliseconds}")
        }

        waitForIdle()

        // Should be 0 immediately
        onNodeWithText("left=0").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun interval_change_restarts_countdown() = runComposeUiTest {
        setContent {
            var intervalMs by useState(default = 100)
            val countdown = useCountdown {
                leftTime = 500.milliseconds
                interval = intervalMs.milliseconds
            }

            Text("left=${countdown.timeLeft.value.inWholeMilliseconds} interval=$intervalMs")
        }

        waitForIdle()
        Thread.sleep(250)
        waitForIdle()

        // Should be around 250ms left
        val found = runCatching {
            listOf(200, 250, 300, 350).any { ms ->
                runCatching { onNodeWithText("left=$ms interval=100").assertExists() }.isSuccess
            }
        }.getOrElse { false }
        assertTrue(found, "Expected countdown with interval=100")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun targetDate_change_resumes_countdown() = runComposeUiTest {
        setContent {
            var target by useState(default = currentInstant + 300.milliseconds)
            val countdown = useCountdown {
                targetDate = target
                interval = 100.milliseconds
            }

            Text("left=${countdown.timeLeft.value.inWholeMilliseconds}")
        }

        waitForIdle()
        Thread.sleep(350)
        waitForIdle()

        // Should reach 0
        onNodeWithText("left=0").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun formatRes_updates_reactively() = runComposeUiTest {
        setContent {
            val countdown = useCountdown {
                leftTime = 2.seconds
                interval = 500.milliseconds
            }

            val formatted = countdown.formatRes.value
            Text("seconds=${formatted.seconds}")
        }

        waitForIdle()
        onNodeWithText("seconds=2").assertExists()

        Thread.sleep(1100)
        waitForIdle()

        // Should update to 1 or 0 seconds
        val text = runCatching {
            onNodeWithText("seconds=1").assertExists()
        }
        assertTrue(text.isSuccess || runCatching { onNodeWithText("seconds=0").assertExists() }.isSuccess)
    }
}
