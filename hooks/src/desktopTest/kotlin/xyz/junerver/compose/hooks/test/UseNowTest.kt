package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import xyz.junerver.compose.hooks.rememberNow
import xyz.junerver.compose.hooks.usenow.useNowImpl
import xyz.junerver.compose.hooks.utils.instantProvider

@Suppress("DEPRECATION")
class UseNowTest {
    @AfterTest
    fun tearDown() {
        instantProvider = { Clock.System.now() }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useNow_uses_custom_formatter_when_provided() = runComposeUiTest {
        setContent {
            val state = useNowImpl {
                interval = 10.seconds
                format = { "custom:$it" }
            }
            Text(state.value)
        }

        waitForIdle()
        onNodeWithText("custom:", substring = true).assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useNow_supports_custom_format_pattern() = runComposeUiTest {
        var rendered by mutableStateOf("")
        setContent {
            val state = useNowImpl {
                interval = 10.seconds
                formatPattern = "HH:mm"
            }
            rendered = state.value
            Text(rendered)
        }

        waitForIdle()
        assertTrue(Regex("^\\d{2}:\\d{2}$").matches(rendered), "Expected HH:mm format, got: $rendered")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun formatter_and_pattern_updates_keep_state_identity() = runComposeUiTest {
        val fixedNow = Instant.parse("2025-01-02T03:04:05Z")
        instantProvider = { fixedNow }
        val revision = mutableStateOf(1)
        val useCustom = mutableStateOf(true)
        lateinit var state: androidx.compose.runtime.State<String>

        setContent {
            val currentRevision = revision.value
            state = rememberNow {
                interval = 10.seconds
                format = if (useCustom.value) {
                    { "revision-$currentRevision" }
                } else {
                    null
                }
                formatPattern = "yyyy"
            }
            Text(state.value)
        }

        waitForIdle()
        val initialState = state
        assertEquals("revision-1", state.value)

        runOnIdle { revision.value = 2 }
        waitForIdle()
        assertSame(initialState, state)
        assertEquals("revision-2", state.value)

        runOnIdle { useCustom.value = false }
        waitForIdle()
        assertSame(initialState, state)
        assertEquals("2025", state.value)
    }
}
