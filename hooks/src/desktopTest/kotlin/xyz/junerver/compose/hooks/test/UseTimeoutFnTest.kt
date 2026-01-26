package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.useTimeoutFn

/*
  Description: useTimeoutFn comprehensive TDD tests
  Author: AI Assistant
  Date: 2026/1/24
  Email: junerver@gmail.com
  Version: v1.0
*/

class UseTimeoutFnTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useTimeoutFn_isPending_is_true_while_waiting() = runComposeUiTest {
        setContent {
            val (isPending) = useTimeoutFn(
                fn = { },
                interval = 10.seconds, // Long interval
                optionsOf = { immediate = true },
            )

            Text("isPending=${isPending.value}")
        }

        waitForIdle()
        // Should be pending immediately after start
        onNodeWithText("isPending=true").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useTimeoutFn_stop_cancels_execution() = runComposeUiTest {
        setContent {
            var stopped by useState(false)

            val (isPending, _, stop) = useTimeoutFn(
                fn = { },
                interval = 10.seconds,
                optionsOf = { immediate = true },
            )

            SideEffect {
                if (!stopped && isPending.value) {
                    stopped = true
                    stop()
                }
            }

            Text("isPending=${isPending.value} stopped=$stopped")
        }

        waitForIdle()
        // After stop, isPending should be false
        onNodeWithText("isPending=false stopped=true").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useTimeoutFn_immediate_false_does_not_start() = runComposeUiTest {
        setContent {
            val (isPending) = useTimeoutFn(
                fn = { },
                interval = 100.milliseconds,
                optionsOf = { immediate = false },
            )

            Text("isPending=${isPending.value}")
        }

        waitForIdle()
        onNodeWithText("isPending=false").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useTimeoutFn_manual_start_sets_pending() = runComposeUiTest {
        setContent {
            var started by useState(false)

            val (isPending, start) = useTimeoutFn(
                fn = { },
                interval = 10.seconds,
                optionsOf = { immediate = false },
            )

            SideEffect {
                if (!started) {
                    started = true
                    start()
                }
            }

            Text("isPending=${isPending.value} started=$started")
        }

        waitForIdle()
        // After manual start, should be pending
        onNodeWithText("isPending=true started=true").assertExists()
    }
}
