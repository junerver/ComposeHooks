package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.AfterTest
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import xyz.junerver.compose.hooks.useTimeAgo
import xyz.junerver.compose.hooks.usetimeago.DefaultChineseTimeAgoMessages
import xyz.junerver.compose.hooks.utils.instantProvider

@Suppress("DEPRECATION")
class UseTimeAgoTest {
    @AfterTest
    fun tearDown() {
        instantProvider = { Clock.System.now() }
    }

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

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun options_update_immediately_with_stable_state_identity() = runComposeUiTest {
        val now = Instant.parse("2025-01-01T00:10:00Z")
        instantProvider = { now }
        val useChinese = mutableStateOf(false)
        lateinit var state: androidx.compose.runtime.State<String>

        setContent {
            state = useTimeAgo(now - 5.minutes) {
                updateInterval = Duration.ZERO
                if (useChinese.value) {
                    messages = DefaultChineseTimeAgoMessages
                }
            }
            Text(state.value)
        }

        waitForIdle()
        val initialState = state
        assertEquals("5 minutes ago", state.value)

        runOnIdle { useChinese.value = true }
        waitForIdle()

        assertSame(initialState, state)
        assertEquals("5分钟前", state.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun update_interval_can_enable_and_disable_without_stale_timer() = runComposeUiTest {
        var now = Instant.parse("2025-01-01T00:10:00Z")
        instantProvider = { now }
        val source = now - 5.minutes
        val enabled = mutableStateOf(false)
        lateinit var state: androidx.compose.runtime.State<String>

        setContent {
            state = useTimeAgo(source) {
                updateInterval = if (enabled.value) 100.milliseconds else Duration.ZERO
            }
            Text(state.value)
        }

        waitForIdle()
        assertEquals("5 minutes ago", state.value)

        now += 1.minutes
        mainClock.advanceTimeBy(300)
        waitForIdle()
        assertEquals("5 minutes ago", state.value)

        runOnIdle { enabled.value = true }
        waitForIdle()
        assertEquals("6 minutes ago", state.value)

        runOnIdle { enabled.value = false }
        waitForIdle()
        now += 1.minutes
        mainClock.advanceTimeBy(300)
        waitForIdle()
        assertEquals("6 minutes ago", state.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun negative_update_interval_is_rejected() {
        val result = runCatching {
            runComposeUiTest {
                setContent {
                    useTimeAgo(Clock.System.now()) {
                        updateInterval = (-1).milliseconds
                    }
                }
            }
        }

        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }
}
