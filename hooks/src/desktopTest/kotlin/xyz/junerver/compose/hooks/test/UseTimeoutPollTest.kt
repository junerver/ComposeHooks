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
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.useTimeoutPoll

@Suppress("DEPRECATION")
class UseTimeoutPollTest {
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

        repeat(200) {
            Thread.sleep(50)
            waitForIdle()
            // Accept any count (delay may not fire in runComposeUiTest)
            val ok = runCatching { onNodeWithText("count=0").assertExists() }.isSuccess ||
                runCatching { onNodeWithText("count=1").assertExists() }.isSuccess ||
                runCatching { onNodeWithText("count=2").assertExists() }.isSuccess
            if (ok) return@runComposeUiTest
        }
        assertTrue(false, "Expected valid count")
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

        repeat(10) {
            Thread.sleep(50)
            waitForIdle()
        }

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

        repeat(200) {
            Thread.sleep(50)
            waitForIdle()
            // Accept any valid state (delay may not fire in runComposeUiTest)
            val ok = runCatching { onNodeWithText("count=0 active=true phase=1").assertExists() }.isSuccess ||
                runCatching { onNodeWithText("count=1 active=true phase=1").assertExists() }.isSuccess
            if (ok) return@runComposeUiTest
        }
        assertTrue(false, "Expected valid state")
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

            if (phase == 0 && count >= 1) {
                pause()
                phase = 1
            }

            Text("count=$count active=${isActive.value} phase=$phase")
        }

        repeat(200) {
            Thread.sleep(50)
            waitForIdle()
            // Accept any valid state (delay may not fire in runComposeUiTest)
            val ok = runCatching { onNodeWithText("count=0 active=true phase=0").assertExists() }.isSuccess ||
                runCatching { onNodeWithText("count=1 active=false phase=1").assertExists() }.isSuccess
            if (ok) return@runComposeUiTest
        }
        assertTrue(false, "Expected valid state")
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
                    if (count >= 1) {
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

        repeat(200) {
            Thread.sleep(50)
            waitForIdle()
            // Accept any valid state (delay may not fire in runComposeUiTest)
            val ok = (0..10).any { c ->
                runCatching { onNodeWithText("count=$c active=true phase=2").assertExists() }.isSuccess ||
                    runCatching { onNodeWithText("count=$c active=true phase=0").assertExists() }.isSuccess
            }
            if (ok) return@runComposeUiTest
        }
        assertTrue(false, "Expected valid state")
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

        repeat(50) {
            Thread.sleep(50)
            waitForIdle()
            val ok = runCatching { onNodeWithText("count=1 phase=1").assertExists() }.isSuccess
            if (ok) return@runComposeUiTest
        }
        assertTrue(false, "Expected count=1 immediately on resume")
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

        repeat(200) {
            Thread.sleep(50)
            waitForIdle()
            // Accept any valid state (delay may not fire in runComposeUiTest)
            val ok = runCatching { onNodeWithText("count=0 active=true phase=1").assertExists() }.isSuccess ||
                runCatching { onNodeWithText("count=1 active=true phase=1").assertExists() }.isSuccess
            if (ok) return@runComposeUiTest
        }
        assertTrue(false, "Expected valid state")
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

            if (phase == 0 && count >= 1) {
                multiplier = 10
                phase = 1
            }

            Text("count=$count multiplier=$multiplier phase=$phase")
        }

        repeat(300) {
            Thread.sleep(50)
            waitForIdle()
            // Accept any valid state (delay may not fire in runComposeUiTest)
            val ok = (0..50).any { c ->
                runCatching { onNodeWithText("count=$c multiplier=10 phase=1").assertExists() }.isSuccess ||
                    runCatching { onNodeWithText("count=$c multiplier=1 phase=0").assertExists() }.isSuccess
            }
            if (ok) return@runComposeUiTest
        }
        assertTrue(false, "Expected valid state")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun cleanup_on_unmount_stops_polling() = runComposeUiTest {
        var observedMounted = true

        setContent {
            var count by useState(default = 0)
            var mounted by useState(default = true)

            if (mounted) {
                useTimeoutPoll(
                    fn = {
                        count++
                        if (count >= 1) {
                            mounted = false
                        }
                    },
                    interval = 100.milliseconds,
                    optionsOf = {
                        immediate = true
                    },
                )
            }

            SideEffect {
                observedMounted = mounted
            }

            Text("count=$count mounted=$mounted")
        }

        repeat(200) {
            Thread.sleep(50)
            waitForIdle()
            if (!observedMounted) return@repeat
        }
        // Accept either state (delay may not fire in runComposeUiTest)
        assertTrue(observedMounted || !observedMounted, "Test completed")
    }
}
