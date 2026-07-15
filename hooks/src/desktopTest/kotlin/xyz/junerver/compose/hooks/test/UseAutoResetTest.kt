package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import xyz.junerver.compose.hooks.rememberAutoReset
import xyz.junerver.compose.hooks.useAutoReset
import xyz.junerver.compose.hooks.useState

/*
  Description: useAutoReset comprehensive TDD tests
  Author: AI Assistant
  Date: 2026/1/24
  Email: junerver@gmail.com
  Version: v1.0
*/

@Suppress("DEPRECATION")
class UseAutoResetTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useAutoReset_initial_value() = runComposeUiTest {
        setContent {
            val state by useAutoReset(0, 1000.milliseconds)
            Text("value=$state")
        }
        waitForIdle()
        onNodeWithText("value=0").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useAutoReset_can_be_modified() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            var state by useAutoReset(0, 5000.milliseconds) // Long interval to avoid reset during test

            SideEffect {
                if (!fired) {
                    fired = true
                    state = 100
                }
            }

            Text("value=$state")
        }
        waitForIdle()
        onNodeWithText("value=100").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useAutoReset_with_string_initial() = runComposeUiTest {
        setContent {
            val state by useAutoReset("default", 5000.milliseconds)
            Text("value=$state")
        }
        waitForIdle()
        onNodeWithText("value=default").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useAutoReset_string_can_be_modified() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            var state by useAutoReset("default", 5000.milliseconds)

            SideEffect {
                if (!fired) {
                    fired = true
                    state = "modified"
                }
            }

            Text("value=$state")
        }
        waitForIdle()
        onNodeWithText("value=modified").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun changing_value_restarts_the_reset_delay() = runComposeUiTest {
        mainClock.autoAdvance = false
        lateinit var state: MutableState<Int>
        setContent {
            state = useAutoReset(0, 200.milliseconds)
            Text("value=${state.value}")
        }
        mainClock.advanceTimeByFrame()
        waitForIdle()

        runOnIdle { state.value = 1 }
        mainClock.advanceTimeByFrame()
        waitForIdle()
        mainClock.advanceTimeBy(120)
        runOnIdle { state.value = 2 }
        mainClock.advanceTimeByFrame()
        waitForIdle()

        mainClock.advanceTimeBy(120)
        waitForIdle()
        assertEquals(2, state.value, "A stale reset must not win after a newer value restarts the delay")

        mainClock.advanceTimeBy(100)
        waitForIdle()
        assertEquals(0, state.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun changing_interval_restarts_with_the_latest_duration() = runComposeUiTest {
        mainClock.autoAdvance = false
        val interval = mutableStateOf(100.milliseconds)
        lateinit var state: MutableState<String>
        setContent {
            state = useAutoReset("idle", interval.value)
            Text("value=${state.value}")
        }
        mainClock.advanceTimeByFrame()
        waitForIdle()

        runOnIdle { state.value = "busy" }
        mainClock.advanceTimeByFrame()
        waitForIdle()
        mainClock.advanceTimeBy(40)
        runOnIdle { interval.value = 250.milliseconds }
        mainClock.advanceTimeByFrame()
        waitForIdle()

        mainClock.advanceTimeBy(100)
        waitForIdle()
        assertEquals("busy", state.value, "Changing interval must cancel the old shorter delay")

        mainClock.advanceTimeBy(170)
        waitForIdle()
        assertEquals("idle", state.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun reset_uses_latest_default_value() = runComposeUiTest {
        mainClock.autoAdvance = false
        val default = mutableStateOf("first")
        lateinit var state: MutableState<String>
        setContent {
            state = useAutoReset(default.value, 100.milliseconds)
            Text("value=${state.value}")
        }
        mainClock.advanceTimeByFrame()
        waitForIdle()

        runOnIdle { state.value = "dirty" }
        mainClock.advanceTimeByFrame()
        waitForIdle()
        mainClock.advanceTimeBy(40)
        runOnIdle { default.value = "latest" }
        mainClock.advanceTimeByFrame()
        waitForIdle()

        mainClock.advanceTimeBy(110)
        waitForIdle()
        assertEquals("latest", state.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun leaving_composition_cancels_pending_reset() = runComposeUiTest {
        mainClock.autoAdvance = false
        val visible = mutableStateOf(true)
        lateinit var state: MutableState<Int>
        setContent {
            if (visible.value) {
                state = useAutoReset(0, 100.milliseconds)
                Text("value=${state.value}")
            }
        }
        mainClock.advanceTimeByFrame()
        waitForIdle()

        runOnIdle { state.value = 9 }
        mainClock.advanceTimeByFrame()
        waitForIdle()
        runOnIdle { visible.value = false }
        mainClock.advanceTimeByFrame()
        waitForIdle()

        mainClock.advanceTimeBy(200)
        waitForIdle()
        assertEquals(9, state.value, "Disposal must cancel the pending reset coroutine")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun remember_alias_has_the_same_reset_behavior() = runComposeUiTest {
        mainClock.autoAdvance = false
        lateinit var state: MutableState<Int>
        setContent {
            state = rememberAutoReset(3, 50.milliseconds)
            Text("value=${state.value}")
        }
        mainClock.advanceTimeByFrame()
        waitForIdle()

        runOnIdle { state.value = 7 }
        mainClock.advanceTimeByFrame()
        waitForIdle()
        mainClock.advanceTimeBy(60)
        waitForIdle()

        assertEquals(3, state.value)
    }
}
