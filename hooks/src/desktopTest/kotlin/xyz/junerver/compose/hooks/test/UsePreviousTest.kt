package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import xyz.junerver.compose.hooks.usePrevious
import xyz.junerver.compose.hooks.useState

/*
  Description: usePrevious comprehensive TDD tests
  Author: AI Assistant
  Date: 2026/1/24
  Email: junerver@gmail.com
  Version: v1.0
*/

class UsePreviousTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun initial_previous_is_null() = runComposeUiTest {
        setContent {
            val previous = usePrevious("current")
            Text("previous=${previous.value}")
        }
        waitForIdle()
        onNodeWithText("previous=null").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun previous_tracks_value_changes() = runComposeUiTest {
        setContent {
            var current by useState("a")
            val previous = usePrevious(current)

            SideEffect {
                if (current == "a") {
                    current = "b"
                }
            }

            Text("current=$current previous=${previous.value}")
        }
        waitForIdle()
        onNodeWithText("current=b previous=a").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun previous_updates_on_multiple_changes() = runComposeUiTest {
        setContent {
            var phase by useState(0)
            var current by useState("a")
            val previous = usePrevious(current)

            SideEffect {
                when (phase) {
                    0 -> {
                        current = "b"
                        phase = 1
                    }
                    1 -> {
                        current = "c"
                        phase = 2
                    }
                }
            }

            Text("current=$current previous=${previous.value} phase=$phase")
        }
        waitForIdle()
        onNodeWithText("current=c previous=b phase=2").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun previous_with_integer_values() = runComposeUiTest {
        setContent {
            var current by useState(1)
            val previous = usePrevious(current)

            SideEffect {
                if (current == 1) {
                    current = 2
                }
            }

            Text("current=$current previous=${previous.value}")
        }
        waitForIdle()
        onNodeWithText("current=2 previous=1").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun previous_with_nullable_values() = runComposeUiTest {
        setContent {
            var current by useState("initial")
            var phase by useState(0)
            val previous = usePrevious(current)

            SideEffect {
                if (phase == 0) {
                    current = "changed"
                    phase = 1
                }
            }

            Text("current=$current previous=${previous.value}")
        }
        waitForIdle()
        onNodeWithText("current=changed previous=initial").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun previous_same_value_no_change() = runComposeUiTest {
        setContent {
            var phase by useState(0)
            var current by useState("same")
            val previous = usePrevious(current)

            SideEffect {
                when (phase) {
                    0 -> {
                        current = "same"
                        phase = 1
                    } // Same value
                }
            }

            Text("previous=${previous.value} phase=$phase")
        }
        waitForIdle()
        // Previous should still be null since value didn't actually change
        onNodeWithText("previous=null phase=1").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun previous_with_object_values() = runComposeUiTest {
        setContent {
            var current by useState(listOf(1, 2, 3))
            val previous = usePrevious(current)

            SideEffect {
                if (current == listOf(1, 2, 3)) {
                    current = listOf(4, 5, 6)
                }
            }

            Text("current=${current.joinToString()} previous=${previous.value?.joinToString()}")
        }
        waitForIdle()
        onNodeWithText("current=4, 5, 6 previous=1, 2, 3").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun previous_three_value_sequence() = runComposeUiTest {
        setContent {
            var phase by useState(0)
            var current by useState("first")
            val previous = usePrevious(current)

            SideEffect {
                when (phase) {
                    0 -> {
                        current = "second"
                        phase = 1
                    }
                    1 -> {
                        current = "third"
                        phase = 2
                    }
                    2 -> {
                        current = "fourth"
                        phase = 3
                    }
                }
            }

            Text("current=$current previous=${previous.value} phase=$phase")
        }
        waitForIdle()
        onNodeWithText("current=fourth previous=third phase=3").assertExists()
    }
}
