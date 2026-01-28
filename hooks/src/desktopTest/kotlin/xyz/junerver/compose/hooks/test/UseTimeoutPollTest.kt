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
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.useTimeoutPoll

/*
  Description: useTimeoutPoll comprehensive TDD tests
  Author: Junerver
  Date: 2026/1/24
  Email: junerver@gmail.com
  Version: v1.0
*/

class UseTimeoutPollTest {
    private fun waitForCondition(maxAttempts: Int = 80, delayMs: Long = 50, condition: () -> Boolean): Boolean {
        for (i in 0 until maxAttempts) {
            if (condition()) return true
            Thread.sleep(delayMs)
        }
        return false
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun poll_executes_repeatedly_with_interval() = runComposeUiTest {
        setContent {
            var count by useState(default = 0)
            useTimeoutPoll(
                fn = {
                    count++
                },
                interval = 100.milliseconds,
                optionsOf = {
                    immediate = true
                },
            )

            Text("count=$count")
        }

        waitForIdle()
        Thread.sleep(350)
        waitForIdle()

        // Should execute at least 3 times
        val text = runCatching {
            onNodeWithText("count=3").assertExists()
        }
        assertTrue(text.isSuccess || runCatching { onNodeWithText("count=4").assertExists() }.isSuccess)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun immediate_false_does_not_start_automatically() = runComposeUiTest {
        setContent {
            var count by useState(default = 0)
            val (isActive, _, resume) = useTimeoutPoll(
                fn = {
                    count++
                },
                interval = 100.milliseconds,
                optionsOf = {
                    immediate = false
                },
            )

            Text("count=$count active=${isActive.value}")
        }

        waitForIdle()
        Thread.sleep(250)
        waitForIdle()

        // Should not execute
        onNodeWithText("count=0 active=false").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun resume_starts_polling() = runComposeUiTest {
        setContent {
            var count by useState(default = 0)
            var phase by useState(default = 0)
            val (isActive, _, resume) = useTimeoutPoll(
                fn = {
                    count++
                },
                interval = 100.milliseconds,
                optionsOf = {
                    immediate = false
                },
            )

            if (phase == 0) {
                resume()
                phase = 1
            }

            Text("count=$count active=${isActive.value} phase=$phase")
        }

        waitForIdle()
        Thread.sleep(350)
        waitForIdle()

        // Should execute after resume
        val text = runCatching {
            onNodeWithText("count=3 active=true phase=1").assertExists()
        }
        assertTrue(text.isSuccess || runCatching { onNodeWithText("count=4 active=true phase=1").assertExists() }.isSuccess)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pause_stops_polling() = runComposeUiTest {
        setContent {
            var count by useState(default = 0)
            var phase by useState(default = 0)
            val (isActive, pause, _) = useTimeoutPoll(
                fn = {
                    count++
                },
                interval = 100.milliseconds,
                optionsOf = {
                    immediate = true
                },
            )

            if (phase == 0 && count >= 2) {
                pause()
                phase = 1
            }

            Text("count=$count active=${isActive.value} phase=$phase")
        }

        waitForIdle()
        Thread.sleep(350)
        waitForIdle()

        // Should stop at count=2 or 3
        val finalCount = runCatching {
            onNodeWithText("count=2 active=false phase=1").assertExists()
            2
        }.getOrElse {
            onNodeWithText("count=3 active=false phase=1").assertExists()
            3
        }

        Thread.sleep(200)
        waitForIdle()

        // Count should not increase
        onNodeWithText("count=$finalCount active=false phase=1").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pause_then_resume_restarts_polling() = runComposeUiTest {
        setContent {
            var count by useState(default = 0)
            var phase by useState(default = 0)
            val (isActive, pause, resume) = useTimeoutPoll(
                fn = {
                    count++
                },
                interval = 100.milliseconds,
                optionsOf = {
                    immediate = true
                },
            )

            when (phase) {
                0 -> {
                    if (count >= 2) {
                        pause()
                        phase = 1
                    }
                }
                1 -> {
                    resume()
                    phase = 2
                }
            }

            Text("count=$count active=${isActive.value} phase=$phase")
        }

        waitForIdle()
        Thread.sleep(550)
        waitForIdle()

        // Should resume and continue counting
        val found = runCatching {
            (4..6).any { c ->
                runCatching { onNodeWithText("count=$c active=true phase=2").assertExists() }.isSuccess
            }
        }.getOrElse { false }
        assertTrue(found, "Expected count 4-6 with active=true phase=2")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun immediateCallback_executes_function_on_resume() = runComposeUiTest {
        setContent {
            var count by useState(default = 0)
            var phase by useState(default = 0)
            val (_, _, resume) = useTimeoutPoll(
                fn = {
                    count++
                },
                interval = 200.milliseconds,
                optionsOf = {
                    immediate = false
                    immediateCallback = true
                },
            )

            if (phase == 0) {
                resume()
                phase = 1
            }

            Text("count=$count phase=$phase")
        }

        waitForIdle()
        Thread.sleep(50)
        waitForIdle()

        // Should execute immediately on resume (before first interval)
        onNodeWithText("count=1 phase=1").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun multiple_resume_calls_do_not_create_multiple_polls() = runComposeUiTest {
        setContent {
            var count by useState(default = 0)
            var phase by useState(default = 0)
            val (isActive, _, resume) = useTimeoutPoll(
                fn = {
                    count++
                },
                interval = 100.milliseconds,
                optionsOf = {
                    immediate = false
                },
            )

            if (phase == 0) {
                resume()
                resume()
                resume()
                phase = 1
            }

            Text("count=$count active=${isActive.value} phase=$phase")
        }

        waitForIdle()
        Thread.sleep(350)
        waitForIdle()

        // Should execute ~3 times, not 9 times
        val text = runCatching {
            onNodeWithText("count=3 active=true phase=1").assertExists()
        }
        assertTrue(text.isSuccess || runCatching { onNodeWithText("count=4 active=true phase=1").assertExists() }.isSuccess)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun function_reference_updates_on_recomposition() = runComposeUiTest {
        setContent {
            var multiplier by useState(default = 1)
            var count by useState(default = 0)
            var phase by useState(default = 0)

            useTimeoutPoll(
                fn = {
                    count += multiplier
                },
                interval = 100.milliseconds,
                optionsOf = {
                    immediate = true
                },
            )

            if (phase == 0 && count >= 2) {
                multiplier = 10
                phase = 1
            }

            Text("count=$count multiplier=$multiplier phase=$phase")
        }

        waitForIdle()
        val found = waitForCondition(maxAttempts = 120, delayMs = 50) {
            waitForIdle()
            (20..80).any { c ->
                runCatching { onNodeWithText("count=$c multiplier=10 phase=1").assertExists() }.isSuccess
            }
        }
        assertTrue(found, "Expected count >= 20 with multiplier=10")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun cleanup_on_unmount_stops_polling() = runComposeUiTest {
        setContent {
            var count by useState(default = 0)
            var mounted by useState(default = true)

            if (mounted) {
                useTimeoutPoll(
                    fn = {
                        count++
                        if (count >= 2) {
                            mounted = false
                        }
                    },
                    interval = 100.milliseconds,
                    optionsOf = {
                        immediate = true
                    },
                )
            }

            Text("count=$count mounted=$mounted")
        }

        waitForIdle()
        Thread.sleep(350)
        waitForIdle()

        // Should stop after unmount
        val finalCount = runCatching {
            onNodeWithText("count=2 mounted=false").assertExists()
            2
        }.getOrElse {
            onNodeWithText("count=3 mounted=false").assertExists()
            3
        }

        Thread.sleep(200)
        waitForIdle()

        // Count should not increase after unmount
        onNodeWithText("count=$finalCount mounted=false").assertExists()
    }
}
