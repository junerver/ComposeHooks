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
import xyz.junerver.compose.hooks.useAutoReset
import xyz.junerver.compose.hooks.useState

/*
  Description: useAutoReset comprehensive TDD tests
  Author: AI Assistant
  Date: 2026/1/24
  Email: junerver@gmail.com
  Version: v1.0
*/

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
}
