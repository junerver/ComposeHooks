package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import arrow.core.Either
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import xyz.junerver.compose.hooks.rememberToggle
import xyz.junerver.compose.hooks.rememberToggleEither
import xyz.junerver.compose.hooks.rememberToggleVisible
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

@Suppress("DEPRECATION")
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
                { "right:$it" },
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
                { "right:$it" },
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
                content2 = { Text("Second") },
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
                content2 = { Text("Second") },
            )
            component()
        }
        waitForIdle()
        onNodeWithText("First").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun toggle_uses_latest_equal_distinct_values_and_stable_rapid_action() = runComposeUiTest {
        val firstLeft = EqualValue("left")
        val replacementLeft = EqualValue("left")
        val firstRight = EqualValue("right")
        val replacementRight = EqualValue("right")
        val left = mutableStateOf(firstLeft, referentialEqualityPolicy())
        val right = mutableStateOf(firstRight, referentialEqualityPolicy())
        var current: EqualValue? = null
        lateinit var toggle: () -> Unit

        setContent {
            val result = useToggle(left.value, right.value)
            current = result.first
            toggle = result.second
        }

        waitForIdle()
        val initialToggle = toggle
        assertSame(firstLeft, current)

        left.value = replacementLeft
        waitForIdle()
        assertSame(initialToggle, toggle)
        assertSame(replacementLeft, current)

        toggle()
        waitForIdle()
        assertSame(firstRight, current)

        right.value = replacementRight
        waitForIdle()
        assertSame(replacementRight, current)

        toggle()
        toggle()
        waitForIdle()
        assertSame(initialToggle, toggle)
        assertSame(replacementRight, current)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun toggle_either_tracks_latest_values_and_keeps_toggle_identity() = runComposeUiTest {
        val left = mutableStateOf("left")
        val right = mutableStateOf(1)
        lateinit var current: Either<String?, Int?>
        lateinit var toggle: () -> Unit

        setContent {
            val result = useToggleEither(left.value, right.value)
            current = result.first
            toggle = result.second
        }

        waitForIdle()
        val initialToggle = toggle
        assertEquals("left", current.fold({ it }, { null }))

        left.value = "latest-left"
        waitForIdle()
        assertEquals("latest-left", current.fold({ it }, { null }))

        toggle()
        waitForIdle()
        assertEquals(1, current.fold({ null }, { it }))

        right.value = 2
        waitForIdle()
        assertSame(initialToggle, toggle)
        assertEquals(2, current.fold({ null }, { it }))
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun toggle_visible_uses_latest_content_and_rapid_parity() = runComposeUiTest {
        val label = mutableStateOf("first")
        lateinit var toggle: () -> Unit

        setContent {
            val currentLabel = label.value
            val result = useToggleVisible(isVisible = false) {
                Text("content:$currentLabel")
            }
            toggle = result.second
            result.first()
        }

        waitForIdle()
        val initialToggle = toggle
        onNodeWithText("content:first").assertDoesNotExist()

        label.value = "latest"
        toggle()
        waitForIdle()
        assertSame(initialToggle, toggle)
        onNodeWithText("content:latest").assertExists()

        toggle()
        toggle()
        waitForIdle()
        onNodeWithText("content:latest").assertExists()

        toggle()
        waitForIdle()
        onNodeWithText("content:latest").assertDoesNotExist()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun two_component_visibility_direction_is_initial_only() = runComposeUiTest {
        val isFirst = mutableStateOf(true)
        lateinit var toggle: () -> Unit

        setContent {
            val result = useToggleVisible(
                isFirst = isFirst.value,
                content1 = { Text("First") },
                content2 = { Text("Second") },
            )
            toggle = result.second
            result.first()
        }

        waitForIdle()
        val initialToggle = toggle
        isFirst.value = false
        waitForIdle()
        assertSame(initialToggle, toggle)
        onNodeWithText("First").assertExists()

        toggle()
        waitForIdle()
        onNodeWithText("Second").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun remember_toggle_aliases_match_all_overloads() = runComposeUiTest {
        var plainValue: String? = null
        lateinit var plainToggle: () -> Unit
        lateinit var eitherValue: Either<String?, Int?>
        lateinit var visibleToggle: () -> Unit
        lateinit var twoContentToggle: () -> Unit

        setContent {
            rememberToggle("a", "b").also { result ->
                plainValue = result.first
                plainToggle = result.second
            }
            rememberToggleEither("left", 1).also { result -> eitherValue = result.first }
            rememberToggleVisible(isVisible = true) { Text("Visible Alias") }.also { result ->
                visibleToggle = result.second
                result.first()
            }
            rememberToggleVisible(
                isFirst = false,
                content1 = { Text("First Alias") },
                content2 = { Text("Second Alias") },
            ).also { result ->
                twoContentToggle = result.second
                result.first()
            }
        }

        waitForIdle()
        assertEquals("a", plainValue)
        assertEquals("left", eitherValue.fold({ it }, { null }))
        onNodeWithText("Visible Alias").assertExists()
        onNodeWithText("Second Alias").assertExists()

        plainToggle()
        visibleToggle()
        twoContentToggle()
        waitForIdle()
        assertEquals("b", plainValue)
        onNodeWithText("Visible Alias").assertDoesNotExist()
        onNodeWithText("First Alias").assertExists()
    }

    private data class EqualValue(val value: String)
}
