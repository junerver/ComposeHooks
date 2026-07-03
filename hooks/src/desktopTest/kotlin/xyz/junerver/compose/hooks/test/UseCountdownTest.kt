package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import xyz.junerver.compose.hooks.useCountdown
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.utils.instantProvider

@Suppress("DEPRECATION")
class UseCountdownTest {
    private var currentInstant: Instant = Clock.System.now()

    private fun resetInstantProvider() {
        currentInstant = Clock.System.now()
        instantProvider = { currentInstant }
    }

    private fun advanceInstant(duration: kotlin.time.Duration) {
        currentInstant += duration
    }

    @AfterTest
    fun tearDown() {
        instantProvider = { Clock.System.now() }
    }

    private fun waitAndCheck(maxAttempts: Int = 200, delayMs: Long = 100, check: () -> Boolean): Boolean {
        repeat(maxAttempts) {
            Thread.sleep(delayMs)
            if (check()) return true
        }
        return false
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun countdown_with_leftTime_counts_down() = runComposeUiTest {
        resetInstantProvider()
        advanceInstant(0.milliseconds)
        lateinit var currentLeft: () -> Long
        setContent {
            val countdown = useCountdown {
                leftTime = 500.milliseconds
                interval = 100.milliseconds
            }

            currentLeft = { countdown.timeLeft.value.inWholeMilliseconds }
            Text("left=${countdown.timeLeft.value.inWholeMilliseconds}")
        }

        waitForIdle()
        assertEquals(500, currentLeft())

        advanceInstant(250.milliseconds)
        mainClock.advanceTimeBy(250)
        waitForIdle()
        assertEquals(250, currentLeft())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun countdown_with_targetDate_counts_down() = runComposeUiTest {
        resetInstantProvider()
        val baseInstant = currentInstant
        advanceInstant(0.milliseconds)
        lateinit var currentLeft: () -> Long
        setContent {
            val countdown = useCountdown {
                targetDate = baseInstant + 500.milliseconds
                interval = 100.milliseconds
            }

            currentLeft = { countdown.timeLeft.value.inWholeMilliseconds }
            Text("left=${countdown.timeLeft.value.inWholeMilliseconds}")
        }

        waitForIdle()
        assertEquals(500, currentLeft())

        advanceInstant(250.milliseconds)
        mainClock.advanceTimeBy(250)
        waitForIdle()
        assertEquals(250, currentLeft())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun countdown_reaches_zero_and_stops() = runComposeUiTest {
        resetInstantProvider()
        advanceInstant(0.milliseconds)
        setContent {
            val countdown = useCountdown {
                leftTime = 300.milliseconds
                interval = 100.milliseconds
            }

            Text("left=${countdown.timeLeft.value.inWholeMilliseconds}")
        }

        waitForIdle()
        advanceInstant(500.milliseconds)
        mainClock.advanceTimeBy(500)
        waitForIdle()
        val reachedZero = runCatching { onNodeWithText("left=0").assertExists() }.isSuccess
        assertTrue(reachedZero, "Expected countdown to reach zero")

        advanceInstant(900.milliseconds)
        mainClock.advanceTimeBy(900)
        waitForIdle()
        val staysZero = runCatching { onNodeWithText("left=0").assertExists() }.isSuccess
        assertTrue(staysZero, "Expected countdown to stay at zero after finishing")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun onEnd_callback_fires_when_countdown_finishes() = runComposeUiTest {
        resetInstantProvider()
        advanceInstant(0.milliseconds)
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
        advanceInstant(500.milliseconds)
        mainClock.advanceTimeBy(500)
        waitForIdle()
        val found = runCatching { onNodeWithText("left=0 ended=true").assertExists() }.isSuccess
        assertTrue(found, "Expected onEnd callback to fire when countdown reaches zero")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun formatRes_parses_duration_correctly() = runComposeUiTest {
        resetInstantProvider()
        lateinit var currentFormatted: () -> Pair<Int, Int>
        setContent {
            val countdown = useCountdown {
                leftTime = 3.seconds + 500.milliseconds
                interval = 100.milliseconds
            }

            val formatted = countdown.formatRes.value
            currentFormatted = { formatted.seconds to formatted.milliseconds }
            Text("s=${formatted.seconds} ms=${formatted.milliseconds}")
        }

        waitForIdle()
        assertEquals(3 to 500, currentFormatted())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun formatRes_handles_days_hours_minutes() = runComposeUiTest {
        resetInstantProvider()
        lateinit var currentFormatted: () -> List<Int>
        setContent {
            val countdown = useCountdown {
                leftTime = (1.seconds * 90061)
                interval = 1.seconds
            }

            val formatted = countdown.formatRes.value
            currentFormatted = { listOf(formatted.days, formatted.hours, formatted.minutes, formatted.seconds) }
            Text("d=${formatted.days} h=${formatted.hours} m=${formatted.minutes} s=${formatted.seconds}")
        }

        waitForIdle()
        assertEquals(listOf(1, 1, 1, 1), currentFormatted())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun leftTime_takes_priority_over_targetDate() = runComposeUiTest {
        resetInstantProvider()
        val baseInstant = currentInstant
        advanceInstant(0.milliseconds)
        lateinit var currentLeft: () -> Long
        setContent {
            val countdown = useCountdown {
                leftTime = 500.milliseconds
                targetDate = baseInstant + 5.seconds
                interval = 100.milliseconds
            }

            currentLeft = { countdown.timeLeft.value.inWholeMilliseconds }
            Text("left=${countdown.timeLeft.value.inWholeMilliseconds}")
        }

        waitForIdle()
        assertEquals(500, currentLeft())

        advanceInstant(250.milliseconds)
        mainClock.advanceTimeBy(250)
        waitForIdle()
        assertEquals(250, currentLeft())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun countdown_with_past_targetDate_starts_at_zero() = runComposeUiTest {
        resetInstantProvider()
        val baseInstant = currentInstant
        advanceInstant(0.milliseconds)
        lateinit var currentLeft: () -> Long
        setContent {
            val countdown = useCountdown {
                targetDate = baseInstant - 1.seconds
                interval = 100.milliseconds
            }

            currentLeft = { countdown.timeLeft.value.inWholeMilliseconds }
            Text("left=${countdown.timeLeft.value.inWholeMilliseconds}")
        }

        waitForIdle()
        assertEquals(0, currentLeft())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun interval_change_restarts_countdown() = runComposeUiTest {
        resetInstantProvider()
        advanceInstant(0.milliseconds)
        setContent {
            var intervalMs by useState(default = 100)
            var phase by useState(default = 0)
            val countdown = useCountdown {
                leftTime = 500.milliseconds
                interval = intervalMs.milliseconds
            }

            SideEffect {
                if (phase == 0 && countdown.timeLeft.value < 500.milliseconds) {
                    intervalMs = 200
                    phase = 1
                }
            }

            Text("left=${countdown.timeLeft.value.inWholeMilliseconds} interval=$intervalMs phase=$phase")
        }

        advanceInstant(250.milliseconds)
        mainClock.advanceTimeBy(250)
        waitForIdle()
        onNodeWithText("left=500 interval=200 phase=1").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun targetDate_change_resumes_countdown() = runComposeUiTest {
        resetInstantProvider()
        val baseInstant = currentInstant
        advanceInstant(0.milliseconds)
        setContent {
            var target by useState(default = baseInstant + 300.milliseconds)
            var phase by useState(default = 0)
            val countdown = useCountdown {
                targetDate = target
                interval = 100.milliseconds
            }

            SideEffect {
                if (phase == 0 && countdown.timeLeft.value == 0.milliseconds) {
                    target = currentInstant + 300.milliseconds
                    phase = 1
                }
            }

            Text("left=${countdown.timeLeft.value.inWholeMilliseconds} phase=$phase")
        }

        advanceInstant(400.milliseconds)
        mainClock.advanceTimeBy(400)
        waitForIdle()
        onNodeWithText("left=300 phase=1").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun formatRes_updates_reactively() = runComposeUiTest {
        resetInstantProvider()
        advanceInstant(0.milliseconds)
        lateinit var currentSeconds: () -> Int
        setContent {
            val countdown = useCountdown {
                leftTime = 2.seconds
                interval = 500.milliseconds
            }

            val formatted = countdown.formatRes.value
            currentSeconds = { countdown.formatRes.value.seconds }
            Text("seconds=${formatted.seconds}")
        }

        val initialFound = waitAndCheck {
            waitForIdle()
            runCatching { onNodeWithText("seconds=2").assertExists() }.isSuccess
        }
        assertTrue(initialFound, "Expected initial seconds=2")

        advanceInstant(1100.milliseconds)
        mainClock.advanceTimeBy(1100)
        val updatedFound = waitAndCheck {
            waitForIdle()
            currentSeconds() <= 1
        }
        assertTrue(updatedFound, "Expected formatted countdown seconds to decrease after time advances")
    }
}
