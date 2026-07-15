package xyz.junerver.compose.hooks.test

import androidx.compose.runtime.MutableDoubleState
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import xyz.junerver.compose.hooks.rememberDouble
import xyz.junerver.compose.hooks.rememberFloat
import xyz.junerver.compose.hooks.rememberInt
import xyz.junerver.compose.hooks.rememberLong
import xyz.junerver.compose.hooks.useDouble
import xyz.junerver.compose.hooks.useFloat
import xyz.junerver.compose.hooks.useInt
import xyz.junerver.compose.hooks.useLong

/*
  Description: Primitive number state hook tests
  Author: Junerver
  Date: 2026/7/15-00:00
  Email: junerver@gmail.com
  Version: v1.0
*/

@Suppress("DEPRECATION")
class UseNumberTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun primitive_states_keep_identity_and_values_when_defaults_change() = runComposeUiTest {
        var intDefault by mutableStateOf(1)
        var longDefault by mutableStateOf(2L)
        var floatDefault by mutableStateOf(3f)
        var doubleDefault by mutableStateOf(4.0)
        lateinit var intState: MutableIntState
        lateinit var longState: MutableLongState
        lateinit var floatState: MutableFloatState
        lateinit var doubleState: MutableDoubleState

        setContent {
            intState = useInt(intDefault)
            longState = useLong(longDefault)
            floatState = useFloat(floatDefault)
            doubleState = useDouble(doubleDefault)
        }

        waitForIdle()
        val initialIntState = intState
        val initialLongState = longState
        val initialFloatState = floatState
        val initialDoubleState = doubleState

        runOnIdle {
            intState.intValue = 11
            longState.longValue = 12L
            floatState.floatValue = 13f
            doubleState.doubleValue = 14.0
            intDefault = 101
            longDefault = 102L
            floatDefault = 103f
            doubleDefault = 104.0
        }
        waitForIdle()

        assertSame(initialIntState, intState)
        assertSame(initialLongState, longState)
        assertSame(initialFloatState, floatState)
        assertSame(initialDoubleState, doubleState)
        assertEquals(11, intState.intValue)
        assertEquals(12L, longState.longValue)
        assertEquals(13f, floatState.floatValue)
        assertEquals(14.0, doubleState.doubleValue)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun remember_aliases_expose_primitive_state_defaults() = runComposeUiTest {
        lateinit var intState: MutableIntState
        lateinit var longState: MutableLongState
        lateinit var floatState: MutableFloatState
        lateinit var doubleState: MutableDoubleState

        setContent {
            intState = rememberInt()
            longState = rememberLong()
            floatState = rememberFloat()
            doubleState = rememberDouble()
        }

        waitForIdle()
        assertEquals(0, intState.intValue)
        assertEquals(0L, longState.longValue)
        assertEquals(0f, floatState.floatValue)
        assertEquals(0.0, doubleState.doubleValue)
    }
}
