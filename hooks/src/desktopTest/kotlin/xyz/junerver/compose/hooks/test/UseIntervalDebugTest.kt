package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
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

@Suppress("DEPRECATION")
class UseIntervalDebugTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun simple_interval_test() = runComposeUiTest {
        var observedCount = 0
        var observedActive = false

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

            SideEffect {
                observedCount = count
                observedActive = isActive.value
            }

            Text("count=$count active=${isActive.value} started=$started")
        }

        repeat(100) {
            Thread.sleep(50)
            waitForIdle()
            if (observedCount > 0 && observedActive) return@runComposeUiTest
        }

        assertTrue(observedCount > 0, "Interval should execute at least once")
        assertTrue(observedActive, "Interval should remain active after resume")
    }
}
