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
  Description: Simple debug test for useInterval
  Author: Junerver
  Date: 2026/1/24
  Email: junerver@gmail.com
  Version: v1.0
*/

class UseIntervalDebugTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun simple_interval_test() = runComposeUiTest {
        setContent {
            var count by useState(default = 0)
            var started by useState(default = false)
            val (resume, _, isActive) = useInterval(
                optionsOf = {
                    period = 50.milliseconds
                },
            ) {
                count++
            }

            SideEffect {
                if (!started) {
                    started = true
                    resume()
                }
            }

            Text("count=$count active=${isActive.value} started=$started")
        }

        // Initial state
        waitForIdle()
        println("Initial state checked")

        // Wait and check multiple times
        for (i in 1..100) {
            Thread.sleep(50)
            waitForIdle()
            val success = runCatching {
                onNodeWithText("count=1 active=true started=true").assertExists()
            }.isSuccess || runCatching {
                onNodeWithText("count=2 active=true started=true").assertExists()
            }.isSuccess || runCatching {
                onNodeWithText("count=3 active=true started=true").assertExists()
            }.isSuccess

            if (success) {
                println("Found count >= 1 at iteration $i")
                return@runComposeUiTest
            }
        }

        // If we get here, print what we actually see
        println("Test failed - checking final state")
        onNodeWithText("count=1 active=true started=true").assertExists()
    }
}
