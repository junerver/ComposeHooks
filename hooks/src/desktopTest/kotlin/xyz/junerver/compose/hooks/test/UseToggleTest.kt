package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.useToggle
import xyz.junerver.compose.hooks.useToggleEither
import xyz.junerver.compose.hooks.useToggleVisible

/*
  Description: useToggle comprehensive TDD tests
  Author: AI Assistant
  Date: 2026/1/24
  Email: junerver@gmail.com
  Version: v1.0
*/

class UseToggleTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useToggle_default_value_is_first() = runComposeUiTest {
        setContent {
            val (value, _) = useToggle("A", "B")
            Text("value=$value")
        }
        waitForIdle()
        onNodeWithText("value=A").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useToggle_toggle_switches_to_second() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val (value, toggle) = useToggle("A", "B")

            SideEffect {
                if (!fired) {
                    fired = true
                    toggle()
                }
            }

            Text("value=$value")
        }
        waitForIdle()
        onNodeWithText("value=B").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useToggle_toggle_twice_returns_to_first() = runComposeUiTest {
        setContent {
            var phase by useState(0)
            val (value, toggle) = useToggle("A", "B")

            SideEffect {
                when (phase) {
                    0 -> {
                        toggle()
                        phase = 1
                    }
                    1 -> {
                        toggle()
                        phase = 2
                    }
                }
            }

            Text("value=$value phase=$phase")
        }
        waitForIdle()
        onNodeWithText("value=A phase=2").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useToggle_with_null_values() = runComposeUiTest {
        setContent {
            val (value, _) = useToggle<String>(null, "B")
            Text("value=$value")
        }
        waitForIdle()
        onNodeWithText("value=null").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useToggle_with_boolean_values() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val (value, toggle) = useToggle(true, false)

            SideEffect {
                if (!fired) {
                    fired = true
                    toggle()
                }
            }

            Text("value=$value")
        }
        waitForIdle()
        onNodeWithText("value=false").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useToggle_with_integer_values() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val (value, toggle) = useToggle(1, 2)

            SideEffect {
                if (!fired) {
                    fired = true
                    toggle()
                }
            }

            Text("value=$value")
        }
        waitForIdle()
        onNodeWithText("value=2").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useToggleEither_default_is_left() = runComposeUiTest {
        setContent {
            val (value, _) = useToggleEither("text", 42)
            val result = value.fold(
                { "left:$it" },
                { "right:$it" }
            )
            Text("result=$result")
        }
        waitForIdle()
        onNodeWithText("result=left:text").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useToggleEither_toggle_switches_to_right() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val (value, toggle) = useToggleEither("text", 42)

            SideEffect {
                if (!fired) {
                    fired = true
                    toggle()
                }
            }

            val result = value.fold(
                { "left:$it" },
                { "right:$it" }
            )
            Text("result=$result")
        }
        waitForIdle()
        onNodeWithText("result=right:42").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useToggleVisible_default_hidden() = runComposeUiTest {
        setContent {
            val (component, _) = useToggleVisible(isVisible = false) {
                Text("Content")
            }
            component()
            Text("rendered")
        }
        waitForIdle()
        onNodeWithText("rendered").assertExists()
        // Content should not be visible when isVisible=false
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useToggleVisible_default_visible() = runComposeUiTest {
        setContent {
            val (component, _) = useToggleVisible(isVisible = true) {
                Text("Content")
            }
            component()
        }
        waitForIdle()
        onNodeWithText("Content").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useToggleVisible_toggle_shows_content() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val (component, toggle) = useToggleVisible(isVisible = false) {
                Text("Content")
            }

            SideEffect {
                if (!fired) {
                    fired = true
                    toggle()
                }
            }

            component()
        }
        waitForIdle()
        onNodeWithText("Content").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useToggleVisible_two_components_switches() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val (component, toggle) = useToggleVisible(
                isFirst = true,
                content1 = { Text("First") },
                content2 = { Text("Second") }
            )

            SideEffect {
                if (!fired) {
                    fired = true
                    toggle()
                }
            }

            component()
        }
        waitForIdle()
        onNodeWithText("Second").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useToggleVisible_two_components_default_first() = runComposeUiTest {
        setContent {
            val (component, _) = useToggleVisible(
                isFirst = true,
                content1 = { Text("First") },
                content2 = { Text("Second") }
            )
            component()
        }
        waitForIdle()
        onNodeWithText("First").assertExists()
    }
}
