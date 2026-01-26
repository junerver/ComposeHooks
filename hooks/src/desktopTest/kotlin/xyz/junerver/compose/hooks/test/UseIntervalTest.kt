package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import xyz.junerver.compose.hooks.useInterval
import xyz.junerver.compose.hooks.useState

/*
  Description: useInterval comprehensive TDD tests
  Author: Junerver
  Date: 2026/1/24
  Email: junerver@gmail.com
  Version: v1.0
*/

class UseIntervalTest {
    private fun waitForCondition(maxAttempts: Int = 50, delayMs: Long = 50, condition: () -> Boolean): Boolean {
        for (i in 0 until maxAttempts) {
            if (condition()) return true
            Thread.sleep(delayMs)
        }
        return false
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun basic_resume_executes_block_periodically() = runComposeUiTest {
        setContent {
            var count by useState(default = 0)
            var fired by useState(default = false)
            val (resume, _, isActive) = useInterval(
                optionsOf = {
                    period = 100.milliseconds
                },
            ) {
                count++
            }

            SideEffect {
                if (fired) return@SideEffect
                fired = true
                resume()
            }

            Text("count=$count active=${isActive.value}")
        }

        // Wait for count to reach at least 2
        val found = waitForCondition {
            waitForIdle()
            runCatching { onNodeWithText("count=2 active=true").assertExists() }.isSuccess ||
                runCatching { onNodeWithText("count=3 active=true").assertExists() }.isSuccess ||
                runCatching { onNodeWithText("count=4 active=true").assertExists() }.isSuccess
        }
        assertTrue(found, "Expected count >= 2 with active=true")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pause_stops_execution() = runComposeUiTest {
        setContent {
            var count by useState(default = 0)
            var phase by useState(default = 0)
            val (resume, pause, isActive) = useInterval(
                optionsOf = {
                    period = 100.milliseconds
                },
            ) {
                count++
            }

            SideEffect {
                when (phase) {
                    0 -> {
                        resume()
                        phase = 1
                    }
                    1 -> {
                        if (count >= 2) {
                            pause()
                            phase = 2
                        }
                    }
                }
            }

            Text("count=$count active=${isActive.value} phase=$phase")
        }

        // Wait for pause to happen
        val found = waitForCondition {
            waitForIdle()
            runCatching { onNodeWithText("count=2 active=false phase=2").assertExists() }.isSuccess ||
                runCatching { onNodeWithText("count=3 active=false phase=2").assertExists() }.isSuccess
        }
        assertTrue(found, "Expected count >= 2 with active=false after pause")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun double_resume_does_not_create_multiple_jobs() = runComposeUiTest {
        setContent {
            var count by useState(default = 0)
            var fired by useState(default = false)
            val (resume, _, isActive) = useInterval(
                optionsOf = {
                    period = 100.milliseconds
                },
            ) {
                count++
            }

            SideEffect {
                if (fired) return@SideEffect
                fired = true
                resume()
                resume() // Double resume
                resume() // Triple resume
            }

            Text("count=$count active=${isActive.value}")
        }

        // Wait for count to reach 2-4 (not 6-12 which would indicate multiple jobs)
        val found = waitForCondition {
            waitForIdle()
            runCatching { onNodeWithText("count=2 active=true").assertExists() }.isSuccess ||
                runCatching { onNodeWithText("count=3 active=true").assertExists() }.isSuccess ||
                runCatching { onNodeWithText("count=4 active=true").assertExists() }.isSuccess
        }
        assertTrue(found, "Expected count 2-4 (not multiplied by 3)")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pause_before_resume_does_nothing() = runComposeUiTest {
        setContent {
            var count by useState(default = 0)
            var fired by useState(default = false)
            val (resume, pause, isActive) = useInterval(
                optionsOf = {
                    period = 100.milliseconds
                },
            ) {
                count++
            }

            SideEffect {
                if (fired) return@SideEffect
                fired = true
                pause() // Pause before starting
                resume()
            }

            Text("count=$count active=${isActive.value}")
        }

        // Should work normally
        val found = waitForCondition {
            waitForIdle()
            runCatching { onNodeWithText("count=2 active=true").assertExists() }.isSuccess ||
                runCatching { onNodeWithText("count=3 active=true").assertExists() }.isSuccess
        }
        assertTrue(found, "Expected count >= 2 after pause then resume")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rapid_pause_resume_toggle() = runComposeUiTest {
        setContent {
            var count by useState(default = 0)
            var phase by useState(default = 0)
            val (resume, pause, isActive) = useInterval(
                optionsOf = {
                    period = 100.milliseconds
                },
            ) {
                count++
            }

            SideEffect {
                when (phase) {
                    0 -> {
                        resume()
                        phase = 1
                    }
                    1 -> {
                        if (count >= 1) {
                            pause()
                            resume()
                            pause()
                            resume()
                            phase = 2
                        }
                    }
                }
            }

            Text("count=$count active=${isActive.value} phase=$phase")
        }

        // Should be active after rapid toggle ending with resume
        val found = waitForCondition {
            waitForIdle()
            runCatching { onNodeWithText("count=2 active=true phase=2").assertExists() }.isSuccess ||
                runCatching { onNodeWithText("count=3 active=true phase=2").assertExists() }.isSuccess ||
                runCatching { onNodeWithText("count=4 active=true phase=2").assertExists() }.isSuccess
        }
        assertTrue(found, "Expected active=true after rapid toggle")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun initialDelay_delays_first_execution() = runComposeUiTest {
        setContent {
            var count by useState(default = 0)
            var fired by useState(default = false)
            val (resume, _, isActive) = useInterval(
                optionsOf = {
                    initialDelay = 300.milliseconds
                    period = 100.milliseconds
                },
            ) {
                count++
            }

            SideEffect {
                if (fired) return@SideEffect
                fired = true
                resume()
            }

            Text("count=$count active=${isActive.value}")
        }

        // Initially count should be 0 (waiting for initialDelay)
        waitForIdle()
        Thread.sleep(100)
        waitForIdle()
        onNodeWithText("count=0 active=true").assertExists()

        // After initialDelay, count should increase
        val found = waitForCondition(maxAttempts = 30) {
            waitForIdle()
            runCatching { onNodeWithText("count=1 active=true").assertExists() }.isSuccess ||
                runCatching { onNodeWithText("count=2 active=true").assertExists() }.isSuccess
        }
        assertTrue(found, "Expected count >= 1 after initialDelay")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun ready_state_controls_execution() = runComposeUiTest {
        setContent {
            var count by useState(default = 0)
            var ready by useState(default = false)

            useInterval(
                optionsOf = {
                    period = 100.milliseconds
                },
                ready = ready,
            ) {
                count++
            }

            SideEffect {
                if (count == 0 && !ready) {
                    ready = true
                }
            }

            Text("count=$count ready=$ready")
        }

        // Should execute when ready=true
        val found = waitForCondition {
            waitForIdle()
            runCatching { onNodeWithText("count=2 ready=true").assertExists() }.isSuccess ||
                runCatching { onNodeWithText("count=3 ready=true").assertExists() }.isSuccess
        }
        assertTrue(found, "Expected count >= 2 when ready=true")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun ready_false_stops_execution() = runComposeUiTest {
        setContent {
            var count by useState(default = 0)
            var ready by useState(default = true)

            useInterval(
                optionsOf = {
                    period = 100.milliseconds
                },
                ready = ready,
            ) {
                count++
                if (count >= 2) {
                    ready = false
                }
            }

            Text("count=$count ready=$ready")
        }

        // Should stop at count=2 when ready becomes false
        val found = waitForCondition {
            waitForIdle()
            runCatching { onNodeWithText("count=2 ready=false").assertExists() }.isSuccess ||
                runCatching { onNodeWithText("count=3 ready=false").assertExists() }.isSuccess
        }
        assertTrue(found, "Expected count >= 2 with ready=false")

        // Verify count doesn't increase after ready=false
        Thread.sleep(300)
        waitForIdle()
        val stillStopped = runCatching { onNodeWithText("count=2 ready=false").assertExists() }.isSuccess ||
            runCatching { onNodeWithText("count=3 ready=false").assertExists() }.isSuccess
        assertTrue(stillStopped, "Count should not increase after ready=false")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun function_reference_updates_on_recomposition() = runComposeUiTest {
        setContent {
            var multiplier by useState(default = 1)
            var count by useState(default = 0)
            var phase by useState(default = 0)

            val (resume, _, _) = useInterval(
                optionsOf = {
                    period = 100.milliseconds
                },
            ) {
                count += multiplier
            }

            SideEffect {
                when (phase) {
                    0 -> {
                        resume()
                        phase = 1
                    }
                    1 -> {
                        if (count >= 2) {
                            multiplier = 10
                            phase = 2
                        }
                    }
                }
            }

            Text("count=$count multiplier=$multiplier phase=$phase")
        }

        // Wait for multiplier to change and count to increase with new multiplier
        val found = waitForCondition(maxAttempts = 60) {
            waitForIdle()
            // After phase 2, count should be at least 12 (2 + 10)
            (12..50).any { c ->
                runCatching { onNodeWithText("count=$c multiplier=10 phase=2").assertExists() }.isSuccess
            }
        }
        assertTrue(found, "Expected count >= 12 with multiplier=10")
    }
}
