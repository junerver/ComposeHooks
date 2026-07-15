package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue
import xyz.junerver.compose.hooks.rememberUpdate
import xyz.junerver.compose.hooks.useRef
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.useUpdate

/*
  Description: useUpdate comprehensive TDD tests
  Author: AI Assistant
  Date: 2026/1/24
  Email: junerver@gmail.com
  Version: v1.0
*/

@Suppress("DEPRECATION")
class UseUpdateTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useUpdate_forces_recomposition() = runComposeUiTest {
        setContent {
            val countRef = useRef(0)
            val update = useUpdate()

            // Only increment on first composition
            if (countRef.current == 0) {
                countRef.current = 1
                SideEffect {
                    update()
                }
            }

            Text("count=${countRef.current}")
        }
        waitForIdle()
        // After update(), the component recomposes and countRef.current is still 1
        onNodeWithText("count=1").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useUpdate_with_ref_shows_updated_value() = runComposeUiTest {
        setContent {
            val countRef = useRef(0)
            var phase by useState(0)
            val update = useUpdate()

            SideEffect {
                when (phase) {
                    0 -> {
                        countRef.current = 10
                        phase = 1
                    }
                    1 -> {
                        update() // Force recomposition to show ref value
                        phase = 2
                    }
                }
            }

            Text("ref=${countRef.current} phase=$phase")
        }
        waitForIdle()
        onNodeWithText("ref=10 phase=2").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useUpdate_can_be_invoked_after_recomposition() = runComposeUiTest {
        setContent {
            var phase by useState(0)
            val update = useUpdate()

            SideEffect {
                if (phase == 0) {
                    update()
                    phase = 1
                }
            }

            Text("phase=$phase")
        }
        waitForIdle()
        onNodeWithText("phase=1").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useUpdate_recomposes_once_per_call_and_keeps_function_identity() = runComposeUiTest {
        lateinit var update: () -> Unit
        var committedCompositions = 0

        setContent {
            update = useUpdate()
            SideEffect { committedCompositions += 1 }
        }

        waitForIdle()
        val initialUpdate = update
        val initialCompositions = committedCompositions

        repeat(3) { index ->
            update()
            waitForIdle()
            assertEquals(initialCompositions + index + 1, committedCompositions)
            assertSame(initialUpdate, update)
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useUpdate_rapid_calls_render_latest_ref_value() = runComposeUiTest {
        lateinit var update: () -> Unit
        lateinit var setLatestValue: (String) -> Unit
        var renderedValue = ""
        var committedCompositions = 0

        setContent {
            val latestValue = useRef("initial")
            update = useUpdate()
            setLatestValue = { latestValue.current = it }
            renderedValue = latestValue.current
            SideEffect { committedCompositions += 1 }
        }

        waitForIdle()
        val initialCompositions = committedCompositions
        setLatestValue("latest")
        repeat(5) { update() }
        waitForIdle()

        assertEquals("latest", renderedValue)
        assertTrue(committedCompositions > initialCompositions)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rememberUpdate_matches_stable_force_update_contract() = runComposeUiTest {
        lateinit var update: () -> Unit
        var committedCompositions = 0

        setContent {
            update = rememberUpdate()
            SideEffect { committedCompositions += 1 }
        }

        waitForIdle()
        val initialUpdate = update
        val initialCompositions = committedCompositions
        update()
        waitForIdle()

        assertEquals(initialCompositions + 1, committedCompositions)
        assertSame(initialUpdate, update)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun stale_update_does_not_affect_remount() = runComposeUiTest {
        val mounted = mutableStateOf(true)
        lateinit var update: () -> Unit
        var childCompositions = 0

        setContent {
            if (mounted.value) {
                update = useUpdate()
                SideEffect { childCompositions += 1 }
            }
        }

        waitForIdle()
        val staleUpdate = update
        mounted.value = false
        waitForIdle()
        val compositionsAfterUnmount = childCompositions

        staleUpdate()
        waitForIdle()
        assertEquals(compositionsAfterUnmount, childCompositions)

        mounted.value = true
        waitForIdle()
        assertNotSame(staleUpdate, update)
    }
}
