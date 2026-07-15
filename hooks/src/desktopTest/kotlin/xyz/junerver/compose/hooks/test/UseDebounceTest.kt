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
import xyz.junerver.compose.hooks.useDebounceEffect
import xyz.junerver.compose.hooks.useDebounceFn
import xyz.junerver.compose.hooks.rememberDebounce
import xyz.junerver.compose.hooks.rememberDebounceEffect
import xyz.junerver.compose.hooks.rememberDebounceFn
import xyz.junerver.compose.hooks.useDebounce
import xyz.junerver.compose.hooks.utils.instantProvider

@Suppress("DEPRECATION")
class UseDebounceTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun debounce_value_and_alias_delay_latest_value_with_stable_states() = runComposeUiTest {
        val originalProvider = instantProvider
        var fakeNow = 0L
        instantProvider = { Instant.fromEpochMilliseconds(fakeNow) }
        try {
            val value = mutableStateOf(0)
            lateinit var debounced: State<Int>
            lateinit var remembered: State<Int>
            setContent {
                debounced = useDebounce(value.value) { wait = 100.milliseconds }
                remembered = rememberDebounce(value.value) { wait = 100.milliseconds }
                Text("debounced=${debounced.value} remembered=${remembered.value}")
            }

            waitForIdle()
            val initialDebounced = debounced
            val initialRemembered = remembered
            runOnIdle { value.value = 1 }
            waitForIdle()

            assertEquals(0, debounced.value)
            assertEquals(0, remembered.value)
            assertSame(initialDebounced, debounced)
            assertSame(initialRemembered, remembered)

            fakeNow += 100
            mainClock.advanceTimeBy(100)
            waitForIdle()

            assertEquals(1, debounced.value)
            assertEquals(1, remembered.value)
            assertSame(initialDebounced, debounced)
            assertSame(initialRemembered, remembered)
        } finally {
            instantProvider = originalProvider
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun debounce_fn_alias_is_stable_and_ignores_calls_after_unmount() = runComposeUiTest {
        val mounted = mutableStateOf(true)
        val phase = mutableStateOf(0)
        val calls = mutableListOf<Int>()
        lateinit var debounced: (Int) -> Unit
        var initialDebounced: Any? = null
        setContent {
            if (mounted.value) {
                debounced = rememberDebounceFn(
                    fn = { calls += it },
                    optionsOf = {
                        wait = 100.milliseconds
                        leading = true
                        trailing = false
                    },
                )
                if (initialDebounced == null) initialDebounced = debounced
                Text("phase=${phase.value}")
            }
        }

        waitForIdle()
        runOnIdle { phase.value = 1 }
        waitForIdle()
        assertSame(initialDebounced, debounced)

        runOnIdle { mounted.value = false }
        waitForIdle()
        runOnIdle { debounced(1) }

        assertEquals(emptyList(), calls)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun debounce_effect_alias_cancels_pending_block_on_unmount() = runComposeUiTest {
        val mounted = mutableStateOf(true)
        val key = mutableStateOf(0)
        val runs = mutableListOf<Int>()
        setContent {
            if (mounted.value) {
                val currentKey = key.value
                rememberDebounceEffect(
                    currentKey,
                    optionsOf = { wait = 100.milliseconds },
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
