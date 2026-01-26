package xyz.junerver.compose.hooks.test

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import xyz.junerver.compose.hooks.useMount
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.useUnmount

/*
  Description: useMount/useUnmount comprehensive TDD tests
  Author: AI Assistant
  Date: 2026/1/24
  Email: junerver@gmail.com
  Version: v1.0
*/

class UseMountUnmountTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useMount_executes_once_on_mount() = runComposeUiTest {
        setContent {
            var mountCount by useState(0)

            useMount {
                mountCount++
            }

            Text("mountCount=$mountCount")
        }
        waitForIdle()
        onNodeWithText("mountCount=1").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useMount_does_not_execute_on_recomposition() = runComposeUiTest {
        setContent {
            var mountCount by useState(0)
            var recomposeCount by useState(0)

            useMount {
                mountCount++
            }

            SideEffect {
                if (recomposeCount < 3) {
                    recomposeCount++
                }
            }

            Text("mountCount=$mountCount recomposeCount=$recomposeCount")
        }
        waitForIdle()
        // Mount should only happen once despite multiple recompositions
        onNodeWithText("mountCount=1 recomposeCount=3").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useUnmount_executes_on_component_removal() = runComposeUiTest {
        var unmountCalled = false

        setContent {
            var showChild by useState(true)

            if (showChild) {
                key("child") {
                    ChildWithUnmount { unmountCalled = true }
                }
            }

            SideEffect {
                if (showChild) {
                    showChild = false
                }
            }

            Text("showChild=$showChild")
        }

        waitForIdle()
        Thread.sleep(100)
        waitForIdle()
        assertTrue(unmountCalled, "useUnmount should be called when component is removed")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useMount_with_suspend_function() = runComposeUiTest {
        setContent {
            var result by useState("")

            useMount {
                // Simulate async operation
                result = "loaded"
            }

            Text("result=$result")
        }
        waitForIdle()
        onNodeWithText("result=loaded").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun multiple_useMount_all_execute() = runComposeUiTest {
        setContent {
            var count1 by useState(0)
            var count2 by useState(0)

            useMount {
                count1 = 1
            }

            useMount {
                count2 = 2
            }

            Text("count1=$count1 count2=$count2")
        }
        waitForIdle()
        onNodeWithText("count1=1 count2=2").assertExists()
    }
}

@androidx.compose.runtime.Composable
private fun ChildWithUnmount(onUnmount: () -> Unit) {
    useUnmount {
        onUnmount()
    }
    Text("Child")
}
