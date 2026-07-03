package xyz.junerver.compose.hooks.test

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant
import xyz.junerver.compose.hooks.useLastChanged
import xyz.junerver.compose.hooks.utils.instantProvider

@Suppress("DEPRECATION")
class UseLastChangedTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun last_changed_tracks_latest_source_update_time() = runComposeUiTest {
        val originalProvider = instantProvider
        var fakeNow = 1_000L
        instantProvider = { Instant.fromEpochMilliseconds(fakeNow) }
        try {
            var source by mutableStateOf("first")
            var lastChanged: State<Instant>? = null

            setContent {
                lastChanged = useLastChanged(source)
            }

            waitForIdle()
            val initial = lastChanged!!.value

            fakeNow = 2_000L
            source = "second"
            waitForIdle()
            val updated = lastChanged!!.value

            assertTrue(updated > initial)
            assertEquals(Instant.fromEpochMilliseconds(2_000L), updated)
        } finally {
            instantProvider = originalProvider
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun last_changed_does_not_change_without_source_update() = runComposeUiTest {
        val originalProvider = instantProvider
        var fakeNow = 1_000L
        instantProvider = { Instant.fromEpochMilliseconds(fakeNow) }
        try {
            var lastChanged: State<Instant>? = null

            setContent {
                lastChanged = useLastChanged("stable")
            }

            waitForIdle()
            val initial = lastChanged!!.value

            fakeNow = 5_000L
            mainClock.advanceTimeBy(500)
            waitForIdle()

            assertEquals(initial, lastChanged!!.value)
        } finally {
            instantProvider = originalProvider
        }
    }
}
