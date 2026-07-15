package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant
import xyz.junerver.compose.hooks.useThrottleEffect
import xyz.junerver.compose.hooks.useThrottleFn
import xyz.junerver.compose.hooks.rememberThrottle
import xyz.junerver.compose.hooks.rememberThrottleEffect
import xyz.junerver.compose.hooks.rememberThrottleFn
import xyz.junerver.compose.hooks.useThrottle
import xyz.junerver.compose.hooks.utils.instantProvider

@Suppress("DEPRECATION")
class UseThrottleTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun throttle_value_and_alias_delay_latest_value_with_stable_states() = runComposeUiTest {
        val originalProvider = instantProvider
        var fakeNow = 0L
        instantProvider = { Instant.fromEpochMilliseconds(fakeNow) }
        try {
            val value = mutableStateOf(0)
            lateinit var throttled: State<Int>
            lateinit var remembered: State<Int>
            setContent {
                throttled = useThrottle(value.value) {
                    wait = 100.milliseconds
                    leading = false
                    trailing = true
                }
                remembered = rememberThrottle(value.value) {
                    wait = 100.milliseconds
                    leading = false
                    trailing = true
                }
                Text("throttled=${throttled.value} remembered=${remembered.value}")
            }

            waitForIdle()
            val initialThrottled = throttled
            val initialRemembered = remembered
            runOnIdle { value.value = 1 }
            waitForIdle()

            assertEquals(0, throttled.value)
            assertEquals(0, remembered.value)
            assertSame(initialThrottled, throttled)
            assertSame(initialRemembered, remembered)

            fakeNow += 100
            mainClock.advanceTimeBy(100)
            waitForIdle()

            assertEquals(1, throttled.value)
            assertEquals(1, remembered.value)
            assertSame(initialThrottled, throttled)
            assertSame(initialRemembered, remembered)
        } finally {
            instantProvider = originalProvider
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun throttle_fn_alias_is_stable_and_ignores_calls_after_unmount() = runComposeUiTest {
        val mounted = mutableStateOf(true)
        val phase = mutableStateOf(0)
        val calls = mutableListOf<Int>()
        lateinit var throttled: (Int) -> Unit
        var initialThrottled: Any? = null
        setContent {
            if (mounted.value) {
                throttled = rememberThrottleFn(
                    fn = { calls += it },
                    optionsOf = {
                        wait = 100.milliseconds
                        leading = true
                        trailing = false
                    },
                )
                if (initialThrottled == null) initialThrottled = throttled
                Text("phase=${phase.value}")
            }
        }

        waitForIdle()
        runOnIdle { phase.value = 1 }
        waitForIdle()
        assertSame(initialThrottled, throttled)

        runOnIdle { mounted.value = false }
        waitForIdle()
        runOnIdle { throttled(1) }

        assertEquals(emptyList(), calls)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun throttle_effect_alias_cancels_pending_block_on_unmount() = runComposeUiTest {
        val mounted = mutableStateOf(true)
        val key = mutableStateOf(0)
        val runs = mutableListOf<Int>()
        setContent {
            if (mounted.value) {
                val currentKey = key.value
                rememberThrottleEffect(
                    currentKey,
                    optionsOf = {
                        wait = 100.milliseconds
                        leading = false
                        trailing = true
                    },
                ) {
                    runs += currentKey
                }
            }
        }

        waitForIdle()
        runOnIdle { key.value = 1 }
        waitForIdle()
        runOnIdle { mounted.value = false }
        waitForIdle()

        mainClock.advanceTimeBy(150)
        waitForIdle()

        assertEquals(emptyList(), runs)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun throttle_fn_drops_calls_within_window_when_trailing_is_disabled() = runComposeUiTest {
        val originalProvider = instantProvider
        var fakeNow = 0L
        instantProvider = { Instant.fromEpochMilliseconds(fakeNow) }
        try {
            val calls = mutableListOf<Int>()
            lateinit var throttled: (Int) -> Unit

            setContent {
                throttled = useThrottleFn<Int>(
                    fn = { calls += it },
                    optionsOf = {
                        wait = 100.milliseconds
                        leading = true
                        trailing = false
                    },
                )
            }

            waitForIdle()
            throttled(1)
            waitForIdle()
            assertEquals(listOf(1), calls)

            throttled(2)
            throttled(3)
            waitForIdle()
            assertEquals(listOf(1), calls)

            fakeNow += 100
            mainClock.advanceTimeBy(100)
            waitForIdle()
            throttled(4)
            waitForIdle()
            assertEquals(listOf(1, 4), calls)
        } finally {
            instantProvider = originalProvider
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun throttle_fn_uses_latest_callback_after_recomposition() = runComposeUiTest {
        val originalProvider = instantProvider
        var fakeNow = 0L
        instantProvider = { Instant.fromEpochMilliseconds(fakeNow) }
        try {
            var prefix by mutableStateOf("old")
            val calls = mutableListOf<String>()
            lateinit var throttled: (Int) -> Unit

            setContent {
                throttled = useThrottleFn<Int>(
                    fn = { value -> calls += "$prefix-$value" },
                    optionsOf = {
                        wait = 100.milliseconds
                        leading = false
                        trailing = true
                    },
                )
            }

            waitForIdle()
            throttled(1)
            waitForIdle()
            prefix = "new"
            waitForIdle()

            fakeNow += 100
            mainClock.advanceTimeBy(100)
            waitForIdle()
            assertEquals(listOf("new-1"), calls)
        } finally {
            instantProvider = originalProvider
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun throttle_effect_coalesces_rapid_key_changes_and_runs_latest_block() = runComposeUiTest {
        val originalProvider = instantProvider
        var fakeNow = 0L
        instantProvider = { Instant.fromEpochMilliseconds(fakeNow) }
        try {
            var key by mutableStateOf(0)
            val runs = mutableListOf<Int>()

            setContent {
                useThrottleEffect(
                    key,
                    optionsOf = {
                        wait = 100.milliseconds
                        leading = false
                        trailing = true
                    },
                ) {
                    runs += key
                }
            }

            waitForIdle()
            assertEquals(emptyList(), runs)

            key = 1
            waitForIdle()
            key = 2
            waitForIdle()
            assertEquals(emptyList(), runs)

            fakeNow += 100
            mainClock.advanceTimeBy(100)
            waitForIdle()
            assertEquals(listOf(2), runs)
        } finally {
            instantProvider = originalProvider
        }
    }
}
