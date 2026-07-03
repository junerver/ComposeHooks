package xyz.junerver.compose.hooks.test

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import xyz.junerver.compose.hooks.TimestampHolder
import xyz.junerver.compose.hooks.TimestampRefHolder
import xyz.junerver.compose.hooks.useTimestamp
import xyz.junerver.compose.hooks.useTimestampRef
import xyz.junerver.compose.hooks.usetimestamp.UseTimestampOptions
import xyz.junerver.compose.hooks.utils.instantProvider

/*
  Description: Tests for usetimestamp hooks
  Author: MiMoCode
  Date: 2026/6/26
  Email: junerver@gmail.com
  Version: v1.1
*/

@OptIn(ExperimentalTestApi::class)
class UseTimestampTest {
    private var currentInstant: Instant = Clock.System.now()

    private fun resetInstantProvider() {
        currentInstant = Clock.System.now()
        instantProvider = { currentInstant }
    }

    private fun advanceTimeBy(duration: Duration) {
        currentInstant += duration
    }

    @AfterTest
    fun tearDown() {
        instantProvider = { Clock.System.now() }
    }

    // region useTimestamp

    @Test
    fun defaultTimestampIsReturned() = runComposeUiTest {
        resetInstantProvider()
        var holder: TimestampHolder? = null
        setContent {
            holder = useTimestamp(autoResume = false)
        }

        waitForIdle()
        assertEquals(currentInstant.toEpochMilliseconds(), holder!!.state.value)
        assertFalse(holder!!.isActive.value)
    }

    @Test
    fun timestampUpdatesAfterConfiguredInterval() = runComposeUiTest {
        resetInstantProvider()
        var holder: TimestampHolder? = null
        setContent {
            holder = useTimestamp(
                optionsOf = { interval = 100.milliseconds },
                autoResume = true,
            )
        }

        waitForIdle()
        val initial = holder!!.state.value

        advanceTimeBy(100.milliseconds)
        mainClock.advanceTimeBy(100)
        waitForIdle()

        assertEquals(currentInstant.toEpochMilliseconds(), holder!!.state.value)
        assertTrue(holder!!.state.value > initial)
    }

    @Test
    fun pause_stops_timestamp_updates_until_resumed() = runComposeUiTest {
        resetInstantProvider()
        var holder: TimestampHolder? = null
        setContent {
            holder = useTimestamp(
                optionsOf = { interval = 100.milliseconds },
                autoResume = true,
            )
        }

        waitForIdle()
        holder!!.pause()
        waitForIdle()
        assertFalse(holder!!.isActive.value)

        val pausedValue = holder!!.state.value
        advanceTimeBy(300.milliseconds)
        mainClock.advanceTimeBy(300)
        waitForIdle()
        assertEquals(pausedValue, holder!!.state.value)

        holder!!.resume()
        waitForIdle()
        assertTrue(holder!!.isActive.value)

        advanceTimeBy(100.milliseconds)
        mainClock.advanceTimeBy(100)
        waitForIdle()
        assertEquals(currentInstant.toEpochMilliseconds(), holder!!.state.value)
    }

    @Test
    fun customOffset_is_applied_to_exposed_timestamp() = runComposeUiTest {
        resetInstantProvider()
        var holder: TimestampHolder? = null
        setContent {
            holder = useTimestamp(
                optionsOf = {
                    interval = 100.milliseconds
                    offset = 1.seconds
                },
                autoResume = true,
            )
        }

        waitForIdle()
        advanceTimeBy(100.milliseconds)
        mainClock.advanceTimeBy(100)
        waitForIdle()

        assertEquals((currentInstant + 1.seconds).toEpochMilliseconds(), holder!!.state.value)
    }

    @Test
    fun callback_receives_updated_timestamp_value() = runComposeUiTest {
        resetInstantProvider()
        val callbackValues = mutableListOf<Long>()
        var holder: TimestampHolder? = null
        setContent {
            holder = useTimestamp(
                optionsOf = {
                    interval = 100.milliseconds
                    callback = { callbackValues += it }
                },
                autoResume = true,
            )
        }

        waitForIdle()
        advanceTimeBy(100.milliseconds)
        mainClock.advanceTimeBy(100)
        waitForIdle()

        assertTrue(callbackValues.isNotEmpty(), "Expected callback to run after interval elapses")
        assertEquals(holder!!.state.value, callbackValues.last())
        assertEquals(currentInstant.toEpochMilliseconds(), callbackValues.last())
    }

    @Test
    fun autoResumeFalseDoesNotStartAutomatically() = runComposeUiTest {
        resetInstantProvider()
        var holder: TimestampHolder? = null
        setContent {
            holder = useTimestamp(
                optionsOf = { interval = 100.milliseconds },
                autoResume = false,
            )
        }

        waitForIdle()
        assertFalse(holder!!.isActive.value)

        val initial = holder!!.state.value
        advanceTimeBy(200.milliseconds)
        mainClock.advanceTimeBy(200)
        waitForIdle()
        assertEquals(initial, holder!!.state.value)
    }

    @Test
    fun resumeAfterAutoResumeFalseActivatesAndUpdatesTimestamp() = runComposeUiTest {
        resetInstantProvider()
        var holder: TimestampHolder? = null
        setContent {
            holder = useTimestamp(
                optionsOf = { interval = 100.milliseconds },
                autoResume = false,
            )
        }

        waitForIdle()
        holder!!.resume()
        waitForIdle()
        assertTrue(holder!!.isActive.value)

        advanceTimeBy(100.milliseconds)
        mainClock.advanceTimeBy(100)
        waitForIdle()
        assertEquals(currentInstant.toEpochMilliseconds(), holder!!.state.value)
    }

    @Test
    fun optionsClassHoldsCorrectDefaults() {
        val options = UseTimestampOptions.optionOf {}
        assertEquals(1.0.milliseconds, options.interval)
        assertEquals(Duration.ZERO, options.offset)
        assertEquals(null, options.callback)
    }

    @Test
    fun optionsClassCustomValues() {
        var callbackInvoked = false
        val options = UseTimestampOptions.optionOf {
            interval = 500.milliseconds
            offset = 2.seconds
            callback = { callbackInvoked = true }
        }
        assertEquals(500.milliseconds, options.interval)
        assertEquals(2.seconds, options.offset)
        assertTrue(options.callback != null)
        options.callback!!.invoke(12345L)
        assertTrue(callbackInvoked)
    }

    @Test
    fun timestampHolderDataClassProperties() = runComposeUiTest {
        resetInstantProvider()
        var holder: TimestampHolder? = null
        setContent {
            holder = useTimestamp(autoResume = false)
        }

        waitForIdle()
        val timestamp = holder!!
        assertEquals(currentInstant.toEpochMilliseconds(), timestamp.state.value)
        assertFalse(timestamp.isActive.value)
        timestamp.resume()
        waitForIdle()
        assertTrue(timestamp.isActive.value)
        timestamp.pause()
        waitForIdle()
        assertFalse(timestamp.isActive.value)
    }

    // endregion

    // region useTimestampRef

    @Test
    fun refDefaultTimestampIsReturned() = runComposeUiTest {
        resetInstantProvider()
        var holder: TimestampRefHolder? = null
        setContent {
            holder = useTimestampRef(autoResume = false)
        }

        waitForIdle()
        assertEquals(currentInstant.toEpochMilliseconds(), holder!!.ref.current)
        assertFalse(holder!!.isActive.value)
    }

    @Test
    fun refTimestampUpdatesAfterConfiguredInterval() = runComposeUiTest {
        resetInstantProvider()
        var holder: TimestampRefHolder? = null
        setContent {
            holder = useTimestampRef(
                optionsOf = { interval = 100.milliseconds },
                autoResume = true,
            )
        }

        waitForIdle()
        val initial = holder!!.ref.current

        advanceTimeBy(100.milliseconds)
        mainClock.advanceTimeBy(100)
        waitForIdle()

        assertEquals(currentInstant.toEpochMilliseconds(), holder!!.ref.current)
        assertTrue(holder!!.ref.current > initial)
    }

    @Test
    fun refPause_stops_updates_until_resumed() = runComposeUiTest {
        resetInstantProvider()
        var holder: TimestampRefHolder? = null
        setContent {
            holder = useTimestampRef(
                optionsOf = { interval = 100.milliseconds },
                autoResume = true,
            )
        }

        waitForIdle()
        holder!!.pause()
        waitForIdle()
        assertFalse(holder!!.isActive.value)

        val pausedValue = holder!!.ref.current
        advanceTimeBy(300.milliseconds)
        mainClock.advanceTimeBy(300)
        waitForIdle()
        assertEquals(pausedValue, holder!!.ref.current)

        holder!!.resume()
        waitForIdle()
        assertTrue(holder!!.isActive.value)

        advanceTimeBy(100.milliseconds)
        mainClock.advanceTimeBy(100)
        waitForIdle()
        assertEquals(currentInstant.toEpochMilliseconds(), holder!!.ref.current)
    }

    @Test
    fun refAutoResumeFalseDoesNotStartAutomatically() = runComposeUiTest {
        resetInstantProvider()
        var holder: TimestampRefHolder? = null
        setContent {
            holder = useTimestampRef(
                optionsOf = { interval = 100.milliseconds },
                autoResume = false,
            )
        }

        waitForIdle()
        assertFalse(holder!!.isActive.value)

        val initial = holder!!.ref.current
        advanceTimeBy(200.milliseconds)
        mainClock.advanceTimeBy(200)
        waitForIdle()
        assertEquals(initial, holder!!.ref.current)
    }

    @Test
    fun refResumeAfterAutoResumeFalseActivatesAndUpdatesTimestamp() = runComposeUiTest {
        resetInstantProvider()
        var holder: TimestampRefHolder? = null
        setContent {
            holder = useTimestampRef(
                optionsOf = { interval = 100.milliseconds },
                autoResume = false,
            )
        }

        waitForIdle()
        holder!!.resume()
        waitForIdle()
        assertTrue(holder!!.isActive.value)

        advanceTimeBy(100.milliseconds)
        mainClock.advanceTimeBy(100)
        waitForIdle()
        assertEquals(currentInstant.toEpochMilliseconds(), holder!!.ref.current)
    }

    @Test
    fun refCallback_receives_updated_timestamp_value() = runComposeUiTest {
        resetInstantProvider()
        val callbackValues = mutableListOf<Long>()
        var holder: TimestampRefHolder? = null
        setContent {
            holder = useTimestampRef(
                optionsOf = {
                    interval = 100.milliseconds
                    callback = { callbackValues += it }
                },
                autoResume = true,
            )
        }

        waitForIdle()
        advanceTimeBy(100.milliseconds)
        mainClock.advanceTimeBy(100)
        waitForIdle()

        assertTrue(callbackValues.isNotEmpty(), "Expected callback to run after interval elapses")
        assertEquals(holder!!.ref.current, callbackValues.last())
        assertEquals(currentInstant.toEpochMilliseconds(), callbackValues.last())
    }

    @Test
    fun refCustomOffset_is_applied_to_exposed_timestamp() = runComposeUiTest {
        resetInstantProvider()
        var holder: TimestampRefHolder? = null
        setContent {
            holder = useTimestampRef(
                optionsOf = {
                    interval = 100.milliseconds
                    offset = 1.seconds
                },
                autoResume = true,
            )
        }

        waitForIdle()
        advanceTimeBy(100.milliseconds)
        mainClock.advanceTimeBy(100)
        waitForIdle()

        assertEquals((currentInstant + 1.seconds).toEpochMilliseconds(), holder!!.ref.current)
    }

    @Test
    fun refTimestampHolderDataClassProperties() = runComposeUiTest {
        resetInstantProvider()
        var holder: TimestampRefHolder? = null
        setContent {
            holder = useTimestampRef(autoResume = false)
        }

        waitForIdle()
        val timestamp = holder!!
        assertEquals(currentInstant.toEpochMilliseconds(), timestamp.ref.current)
        assertFalse(timestamp.isActive.value)
        timestamp.resume()
        waitForIdle()
        assertTrue(timestamp.isActive.value)
        timestamp.pause()
        waitForIdle()
        assertFalse(timestamp.isActive.value)
    }

    // endregion
}
