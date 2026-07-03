package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import xyz.junerver.compose.hooks.useTimeAgo
import xyz.junerver.compose.hooks.usetimeago.DefaultChineseTimeAgoMessages

@Suppress("DEPRECATION")
class UseTimeAgoTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useTimeAgo_renders_initial_relative_time() = runComposeUiTest {
        val fiveMinutesAgo = Clock.System.now() - 5.minutes

        setContent {
            val state = useTimeAgo(
                fiveMinutesAgo,
                optionsOf = {
                    updateInterval = Duration.ZERO
                },
            )
            Text(state.value)
        }

        waitForIdle()
        onNodeWithText("5 minutes ago").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useTimeAgo_supports_custom_messages() = runComposeUiTest {
        val fiveMinutesAgo = Clock.System.now() - 5.minutes

        setContent {
            val state = useTimeAgo(
                fiveMinutesAgo,
                optionsOf = {
                    updateInterval = Duration.ZERO
                    messages = DefaultChineseTimeAgoMessages
                },
            )
            Text(state.value)
        }

        waitForIdle()
        onNodeWithText("5分钟前").assertExists()
    }
}
