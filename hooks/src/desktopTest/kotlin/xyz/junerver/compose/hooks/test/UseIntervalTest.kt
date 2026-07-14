package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import xyz.junerver.compose.hooks.useInterval
import xyz.junerver.compose.hooks.useState

/*
  Description: useInterval contract tests
  Author: AI Assistant
  Date: 2026/1/24
  Email: junerver@gmail.com
  Version: v1.0
*/

@Suppress("DEPRECATION")
class UseIntervalTest {
    private fun waitAndCheck(maxAttempts: Int = 200, delayMs: Long = 50, check: () -> Boolean): Boolean {
        repeat(maxAttempts) {
            Thread.sleep(delayMs)
            if (check()) return true
        }
        return false
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun basic_resume_executes_block_periodically() = runComposeUiTest {
        val count = AtomicInteger(0)
        setContent {
            val (resume, _, isActive) = useInterval(
                optionsOf = {
                    period = 100.milliseconds
                },
            ) {
                count.incrementAndGet()
            }

            SideEffect { resume() }

            Text("active=${isActive.value}")
        }

        waitForIdle()
        val fired = waitAndCheck {
            waitForIdle()
            count.get() >= 1
        }
        assertTrue(fired, "Expected interval block to fire at least once after resume")
        assertTrue(count.get() >= 1, "Expected count >= 1 after resume")
        onNodeWithText("active=true").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pause_stops_execution() = runComposeUiTest {
        val count = AtomicInteger(0)
        val phaseRef = AtomicInteger(0)
        lateinit var isActive: androidx.compose.runtime.State<Boolean>
        lateinit var pause: () -> Unit
        setContent {
            var phase by useState(default = 0)
            phaseRef.set(phase)
            val (resume, p, active) = useInterval(
                optionsOf = {
                    period = 100.milliseconds
                },
            ) {
                count.incrementAndGet()
            }
            pause = p
            isActive = active

            SideEffect {
                when (phase) {
                    0 -> {
                        resume()
                        phase = 1
                    }
                    1 -> {
                        if (count.get() >= 1) {
                            pause()
                            phase = 2
                        }
                    }
                }
            }
        }

        val paused = waitAndCheck {
            waitForIdle()
            count.get() >= 1 && !isActive.value
        }
        assertTrue(paused, "Expected count >= 1 with active=false after pause")
        val countAtPause = count.get()

        Thread.sleep(300)
        waitForIdle()
        assertEquals(countAtPause, count.get(), "Count should not increase after pause")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun double_resume_does_not_create_multiple_jobs() = runComposeUiTest {
        val count = AtomicInteger(0)
        setContent {
            val (resume, _, _) = useInterval(
                optionsOf = {
                    period = 100.milliseconds
                },
            ) {
                count.incrementAndGet()
            }

            SideEffect {
                resume()
                resume()
                resume()
            }
        }

        val fired = waitAndCheck {
            waitForIdle()
            count.get() >= 1
        }
        assertTrue(fired, "Expected interval to run after repeated resume calls")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pause_before_resume_does_nothing() = runComposeUiTest {
        val count = AtomicInteger(0)
        setContent {
            val (resume, pause, _) = useInterval(
                optionsOf = {
                    period = 100.milliseconds
                },
            ) {
                count.incrementAndGet()
            }

            SideEffect {
                pause()
                resume()
            }
        }

        val fired = waitAndCheck {
            waitForIdle()
            count.get() >= 1
        }
        assertTrue(fired, "Expected interval to run after pause-then-resume")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rapid_pause_resume_toggle_keeps_interval_runnable() = runComposeUiTest {
        val count = AtomicInteger(0)
        val phaseRef = AtomicInteger(0)
        lateinit var isActive: androidx.compose.runtime.State<Boolean>
        setContent {
            var phase by useState(default = 0)
            phaseRef.set(phase)
            val (resume, pause, active) = useInterval(
                optionsOf = {
                    period = 100.milliseconds
                },
            ) {
                count.incrementAndGet()
            }
            isActive = active

            SideEffect {
                when (phase) {
                    0 -> {
                        resume()
                        phase = 1
                    }
                    1 -> {
                        if (count.get() >= 1) {
                            pause()
                            resume()
                            pause()
                            resume()
                            phase = 2
                        }
                    }
                }
            }
        }

        val settled = waitAndCheck(maxAttempts = 500) {
            waitForIdle()
            phaseRef.get() == 2 && count.get() >= 1 && isActive.value
        }
        assertTrue(settled, "Expected interval to remain runnable with active=true after rapid toggle")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun initialDelay_delays_first_execution() = runComposeUiTest {
        val count = AtomicInteger(0)
        lateinit var isActive: androidx.compose.runtime.State<Boolean>
        setContent {
            val (resume, _, active) = useInterval(
                optionsOf = {
                    initialDelay = 300.milliseconds
                    period = 100.milliseconds
                },
            ) {
                count.incrementAndGet()
            }
            isActive = active

            SideEffect { resume() }
        }

        // During the initial delay window, the interval is active but the block
        // has not fired yet. Keep this window well under the initialDelay so the
        // block has not had a chance to run.
        repeat(2) {
            Thread.sleep(50)
            waitForIdle()
        }
        assertTrue(isActive.value, "Interval should be active during initialDelay")
        assertEquals(0, count.get(), "Block should not fire before initialDelay elapses")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun ready_state_controls_execution() = runComposeUiTest {
        val count = AtomicInteger(0)
        setContent {
            var ready by useState(default = false)
            useInterval(
                optionsOf = {
                    period = 100.milliseconds
                },
                ready = ready,
            ) {
                count.incrementAndGet()
            }

            SideEffect {
                if (!ready) ready = true
            }
        }

        val fired = waitAndCheck {
            waitForIdle()
            count.get() >= 1
        }
        assertTrue(fired, "Expected count >= 1 when ready becomes true")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun ready_false_stops_execution() = runComposeUiTest {
        val count = AtomicInteger(0)
        setContent {
            var ready by useState(default = true)
            useInterval(
                optionsOf = {
                    period = 100.milliseconds
                },
                ready = ready,
            ) {
                count.incrementAndGet()
                if (count.get() >= 1) {
                    ready = false
                }
            }
        }

        val stopped = waitAndCheck {
            waitForIdle()
            count.get() >= 1
        }
        assertTrue(stopped, "Expected at least one fire before ready flips to false")

        Thread.sleep(300)
        waitForIdle()
        val countAtStop = count.get()
        // Give it another window; it must not advance.
        Thread.sleep(200)
        waitForIdle()
        assertEquals(countAtStop, count.get(), "Count should not increase after ready=false")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun interval_uses_latest_callback_after_recomposition() = runComposeUiTest {
        val count = AtomicInteger(0)
        val phaseRef = AtomicInteger(0)
        setContent {
            var phase by useState(default = 0)
            phaseRef.set(phase)
            var multiplier by useState(default = 1)
            val (resume, pause, _) = useInterval(
                optionsOf = {
                    period = 100.milliseconds
                },
            ) {
                count.addAndGet(multiplier)
            }

            SideEffect {
                when (phase) {
                    0 -> {
                        resume()
                        phase = 1
                    }
                    1 -> {
                        if (count.get() >= 1) {
                            pause()
                            count.set(0)
                            multiplier = 10
                            phase = 2
                        }
                    }
                    2 -> {
                        resume()
                        phase = 3
                    }
                }
            }
        }

        val fired = waitAndCheck(maxAttempts = 300) {
            waitForIdle()
            count.get() >= 10 && phaseRef.get() == 3
        }
        assertTrue(fired, "Expected recomposed interval callback to increment by latest multiplier")
    }
}
