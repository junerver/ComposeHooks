package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import xyz.junerver.compose.hooks.usenow.useNowImpl

@Suppress("DEPRECATION")
class UseNowTest {
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
}
