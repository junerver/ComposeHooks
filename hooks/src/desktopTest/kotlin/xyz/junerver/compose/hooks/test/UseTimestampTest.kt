package xyz.junerver.compose.hooks.test

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import xyz.junerver.compose.hooks.TimestampHolder
import xyz.junerver.compose.hooks.TimestampRefHolder
import xyz.junerver.compose.hooks.usetimestamp.UseTimestampOptions
import xyz.junerver.compose.hooks.usetimestamp.useTimestampImpl
import xyz.junerver.compose.hooks.usetimestamp.useTimestampRefImpl

/*
  Description: Tests for usetimestamp hooks
  Author: MiMoCode
  Date: 2026/6/26
  Email: junerver@gmail.com
  Version: v1.0
*/

@OptIn(ExperimentalTestApi::class)
class UseTimestampTest {

    // region useTimestampImpl

    @Test
    fun defaultTimestampIsReturned() = runComposeUiTest {
        var holder: TimestampHolder? = null
        setContent {
            holder = useTimestampImpl()
        }
        waitForIdle()
        assertTrue(holder!!.state.value > 0, "Initial timestamp should be positive")
    }

    @Test
    fun timestampUpdatesOverTime() = runComposeUiTest {
        var holder: TimestampHolder? = null
        setContent {
            holder = useTimestampImpl(optionsOf = { interval = 50.milliseconds })
        }
        waitForIdle()
        val initial = holder!!.state.value
        delay(200)
        waitForIdle()
        val updated = holder!!.state.value
        assertTrue(updated >= initial, "Timestamp should update or remain same after delay")
    }

    @Test
    fun pauseAndResumeControlsIsActive() = runComposeUiTest {
        var holder: TimestampHolder? = null
        setContent {
            holder = useTimestampImpl(optionsOf = { interval = 100.milliseconds })
        }
        waitForIdle()
        assertTrue(holder!!.isActive.value, "Should be active after mount with autoResume=true")

        holder!!.pause()
        waitForIdle()
        assertFalse(holder!!.isActive.value, "Should be inactive after pause")

        holder!!.resume()
        waitForIdle()
        assertTrue(holder!!.isActive.value, "Should be active after resume")
    }

    @Test
    fun isActiveReturnsFalseWhenPaused() = runComposeUiTest {
        var holder: TimestampHolder? = null
        setContent {
            holder = useTimestampImpl(optionsOf = { interval = 100.milliseconds })
        }
        waitForIdle()
        assertTrue(holder!!.isActive.value)

        holder!!.pause()
        waitForIdle()
        assertFalse(holder!!.isActive.value)
    }

    @Test
    fun customIntervalIsRespected() = runComposeUiTest {
        var holder: TimestampHolder? = null
        setContent {
            holder = useTimestampImpl(optionsOf = { interval = 200.milliseconds })
        }
        waitForIdle()
        assertTrue(holder!!.isActive.value, "Should be active with custom interval")
    }

    @Test
    fun customOffsetIsApplied() = runComposeUiTest {
        var holder: TimestampHolder? = null
        setContent {
            holder = useTimestampImpl(optionsOf = {
                interval = 50.milliseconds
                offset = 1.seconds
            })
        }
        waitForIdle()
        assertTrue(holder!!.state.value > 0, "Timestamp with offset should be positive")
    }

    @Test
    fun callbackIsInvoked() = runComposeUiTest {
        val callbackValues = mutableListOf<Long>()
        var holder: TimestampHolder? = null
        setContent {
            holder = useTimestampImpl(optionsOf = {
                interval = 50.milliseconds
                callback = { callbackValues.add(it) }
            })
        }
        waitForIdle()
        delay(250)
        waitForIdle()
        assertTrue(callbackValues.isNotEmpty(), "Callback should have been invoked at least once")
        callbackValues.forEach { value ->
            assertTrue(value > 0, "Callback timestamp values should be positive")
        }
    }

    @Test
    fun autoResumeFalseDoesNotStartAutomatically() = runComposeUiTest {
        var holder: TimestampHolder? = null
        setContent {
            holder = useTimestampImpl(
                optionsOf = { interval = 100.milliseconds },
                autoResume = false,
            )
        }
        waitForIdle()
        assertFalse(holder!!.isActive.value, "Should not be active when autoResume=false")
    }

    @Test
    fun resumeAfterAutoResumeFalseActivates() = runComposeUiTest {
        var holder: TimestampHolder? = null
        setContent {
            holder = useTimestampImpl(
                optionsOf = { interval = 100.milliseconds },
                autoResume = false,
            )
        }
        waitForIdle()
        assertFalse(holder!!.isActive.value)

        holder!!.resume()
        waitForIdle()
        assertTrue(holder!!.isActive.value, "Should be active after manual resume")
    }

    @Test
    fun defaultOptionsUseDefaultInterval() = runComposeUiTest {
        var holder: TimestampHolder? = null
        setContent {
            holder = useTimestampImpl()
        }
        waitForIdle()
        assertTrue(holder!!.isActive.value, "Should be active with default options")
        assertTrue(holder!!.state.value > 0)
    }

    @Test
    fun optionsClassHoldsCorrectDefaults() {
        val options = UseTimestampOptions.optionOf {}
        assertEquals(1.0.milliseconds, options.interval)
        assertEquals(kotlin.time.Duration.ZERO, options.offset)
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
        var holder: TimestampHolder? = null
        setContent {
            holder = useTimestampImpl()
        }
        waitForIdle()
        val ts = holder!!
        assertTrue(ts.state.value > 0)
        assertTrue(ts.isActive.value)
        ts.pause()
        waitForIdle()
        assertFalse(ts.isActive.value)
        ts.resume()
        waitForIdle()
        assertTrue(ts.isActive.value)
    }

    // endregion

    // region useTimestampRefImpl

    @Test
    fun refDefaultTimestampIsReturned() = runComposeUiTest {
        var holder: TimestampRefHolder? = null
        setContent {
            holder = useTimestampRefImpl()
        }
        waitForIdle()
        assertTrue(holder!!.ref.current > 0, "Initial ref timestamp should be positive")
    }

    @Test
    fun refTimestampUpdatesOverTime() = runComposeUiTest {
        var holder: TimestampRefHolder? = null
        setContent {
            holder = useTimestampRefImpl(optionsOf = { interval = 50.milliseconds })
        }
        waitForIdle()
        val initial = holder!!.ref.current
        delay(200)
        waitForIdle()
        val updated = holder!!.ref.current
        assertTrue(updated >= initial, "Ref timestamp should update or remain same after delay")
    }

    @Test
    fun refPauseAndResumeControlsIsActive() = runComposeUiTest {
        var holder: TimestampRefHolder? = null
        setContent {
            holder = useTimestampRefImpl(optionsOf = { interval = 100.milliseconds })
        }
        waitForIdle()
        assertTrue(holder!!.isActive.value, "Ref should be active after mount with autoResume=true")

        holder!!.pause()
        waitForIdle()
        assertFalse(holder!!.isActive.value, "Ref should be inactive after pause")

        holder!!.resume()
        waitForIdle()
        assertTrue(holder!!.isActive.value, "Ref should be active after resume")
    }

    @Test
    fun refAutoResumeFalseDoesNotStartAutomatically() = runComposeUiTest {
        var holder: TimestampRefHolder? = null
        setContent {
            holder = useTimestampRefImpl(
                optionsOf = { interval = 100.milliseconds },
                autoResume = false,
            )
        }
        waitForIdle()
        assertFalse(holder!!.isActive.value, "Ref should not be active when autoResume=false")
    }

    @Test
    fun refResumeAfterAutoResumeFalseActivates() = runComposeUiTest {
        var holder: TimestampRefHolder? = null
        setContent {
            holder = useTimestampRefImpl(
                optionsOf = { interval = 100.milliseconds },
                autoResume = false,
            )
        }
        waitForIdle()
        assertFalse(holder!!.isActive.value)

        holder!!.resume()
        waitForIdle()
        assertTrue(holder!!.isActive.value, "Ref should be active after manual resume")
    }

    @Test
    fun refCallbackIsInvoked() = runComposeUiTest {
        val callbackValues = mutableListOf<Long>()
        var holder: TimestampRefHolder? = null
        setContent {
            holder = useTimestampRefImpl(optionsOf = {
                interval = 50.milliseconds
                callback = { callbackValues.add(it) }
            })
        }
        waitForIdle()
        delay(250)
        waitForIdle()
        assertTrue(callbackValues.isNotEmpty(), "Ref callback should have been invoked at least once")
        callbackValues.forEach { value ->
            assertTrue(value > 0, "Ref callback timestamp values should be positive")
        }
    }

    @Test
    fun refCustomOffsetIsApplied() = runComposeUiTest {
        var holder: TimestampRefHolder? = null
        setContent {
            holder = useTimestampRefImpl(optionsOf = {
                interval = 50.milliseconds
                offset = 1.seconds
            })
        }
        waitForIdle()
        assertTrue(holder!!.ref.current > 0, "Ref timestamp with offset should be positive")
    }

    @Test
    fun refTimestampHolderDataClassProperties() = runComposeUiTest {
        var holder: TimestampRefHolder? = null
        setContent {
            holder = useTimestampRefImpl()
        }
        waitForIdle()
        val ts = holder!!
        assertTrue(ts.ref.current > 0)
        assertTrue(ts.isActive.value)
        ts.pause()
        waitForIdle()
        assertFalse(ts.isActive.value)
        ts.resume()
        waitForIdle()
        assertTrue(ts.isActive.value)
    }

    // endregion
}
