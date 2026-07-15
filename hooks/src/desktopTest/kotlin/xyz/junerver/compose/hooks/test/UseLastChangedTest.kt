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
import kotlin.test.assertTrue
import kotlin.time.Instant
import xyz.junerver.compose.hooks.MutableRef
import xyz.junerver.compose.hooks.rememberLastChanged
import xyz.junerver.compose.hooks.useLastChanged
import xyz.junerver.compose.hooks.utils.instantProvider

@Suppress("DEPRECATION")
class UseLastChangedTest {
    private data class Source(val value: Int)

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun last_changed_reads_clock_once_on_mount_and_ignores_unrelated_recomposition() = runComposeUiTest {
        val originalProvider = instantProvider
        var reads = 0
        var marker by mutableStateOf(0)
        var lastChanged: State<Instant>? = null
        instantProvider = {
            reads += 1
            Instant.fromEpochMilliseconds(reads.toLong())
        }
        try {
            setContent {
                lastChanged = useLastChanged("stable")
                Text("marker=$marker")
            }

            waitForIdle()
            assertEquals(1, reads)
            assertEquals(Instant.fromEpochMilliseconds(1), lastChanged!!.value)

            marker = 1
            waitForIdle()
            assertEquals(1, reads)
            assertEquals(Instant.fromEpochMilliseconds(1), lastChanged!!.value)
        } finally {
            instantProvider = originalProvider
        }
    }

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

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun equal_source_keeps_timestamp_and_remember_alias_state_identity() = runComposeUiTest {
        val originalProvider = instantProvider
        var fakeNow = 1_000L
        var source = Source(1)
        var marker by mutableStateOf(0)
        var initialState: State<Instant>? = null
        lateinit var latestState: State<Instant>
        instantProvider = { Instant.fromEpochMilliseconds(fakeNow) }
        try {
            setContent {
                latestState = rememberLastChanged(source)
                if (initialState == null) {
                    initialState = latestState
                }
                Text("marker=$marker")
            }

            waitForIdle()
            assertEquals(Instant.fromEpochMilliseconds(1_000), latestState.value)

            fakeNow = 2_000L
            source = Source(1)
            marker = 1
            waitForIdle()
            assertSame(initialState, latestState)
            assertEquals(Instant.fromEpochMilliseconds(1_000), latestState.value)

            fakeNow = 3_000L
            source = Source(2)
            marker = 2
            waitForIdle()
            assertSame(initialState, latestState)
            assertEquals(Instant.fromEpochMilliseconds(3_000), latestState.value)
        } finally {
            instantProvider = originalProvider
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun ref_source_value_change_updates_timestamp() = runComposeUiTest {
        val originalProvider = instantProvider
        var fakeNow = 1_000L
        val source = MutableRef("first")
        lateinit var lastChanged: State<Instant>
        instantProvider = { Instant.fromEpochMilliseconds(fakeNow) }
        try {
            setContent {
                lastChanged = useLastChanged(source)
            }

            waitForIdle()
            assertEquals(Instant.fromEpochMilliseconds(1_000), lastChanged.value)

            fakeNow = 2_000L
            runOnIdle { source.current = "second" }
            waitForIdle()
            assertEquals(Instant.fromEpochMilliseconds(2_000), lastChanged.value)
        } finally {
            instantProvider = originalProvider
        }
    }
}
