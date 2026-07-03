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
import xyz.junerver.compose.hooks.useDebounceEffect
import xyz.junerver.compose.hooks.useDebounceFn
import xyz.junerver.compose.hooks.utils.instantProvider

@Suppress("DEPRECATION")
class UseDebounceTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun debounce_fn_leading_only_fires_once_per_window() = runComposeUiTest {
        val originalProvider = instantProvider
        var fakeNow = 0L
        instantProvider = { Instant.fromEpochMilliseconds(fakeNow) }
        try {
            val calls = mutableListOf<Int>()
            lateinit var debounced: (Int) -> Unit

            setContent {
                debounced = useDebounceFn<Int>(
                    fn = { calls += it },
                    optionsOf = {
                        wait = 100.milliseconds
                        leading = true
                        trailing = false
                    },
                )
            }

            waitForIdle()
            debounced(1)
            waitForIdle()
            assertEquals(listOf(1), calls)

            fakeNow += 50
            mainClock.advanceTimeBy(50)
            waitForIdle()
            debounced(2)
            waitForIdle()
            assertEquals(listOf(1), calls)

            fakeNow += 100
            mainClock.advanceTimeBy(100)
            waitForIdle()
            debounced(3)
            waitForIdle()
            assertEquals(listOf(1, 3), calls)
        } finally {
            instantProvider = originalProvider
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun debounce_fn_uses_latest_callback_after_recomposition() = runComposeUiTest {
        val originalProvider = instantProvider
        var fakeNow = 0L
        instantProvider = { Instant.fromEpochMilliseconds(fakeNow) }
        try {
            var prefix by mutableStateOf("old")
            val calls = mutableListOf<String>()
            lateinit var debounced: (Int) -> Unit

            setContent {
                debounced = useDebounceFn<Int>(
                    fn = { value -> calls += "$prefix-$value" },
                    optionsOf = {
                        wait = 100.milliseconds
                        leading = false
                        trailing = true
                    },
                )
            }

            waitForIdle()
            debounced(1)
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
    fun debounce_effect_waits_for_quiet_period_and_runs_latest_block() = runComposeUiTest {
        val originalProvider = instantProvider
        var fakeNow = 0L
        instantProvider = { Instant.fromEpochMilliseconds(fakeNow) }
        try {
            var key by mutableStateOf(0)
            val runs = mutableListOf<Int>()

            setContent {
                useDebounceEffect(
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
            fakeNow += 50
            mainClock.advanceTimeBy(50)
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
