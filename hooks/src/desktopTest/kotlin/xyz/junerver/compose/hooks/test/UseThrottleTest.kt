package xyz.junerver.compose.hooks.test

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant
import xyz.junerver.compose.hooks.useThrottleEffect
import xyz.junerver.compose.hooks.useThrottleFn
import xyz.junerver.compose.hooks.utils.instantProvider

@Suppress("DEPRECATION")
class UseThrottleTest {
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
