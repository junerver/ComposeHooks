package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import arrow.core.left
import arrow.core.right
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import xyz.junerver.compose.hooks.GetStateHolder
import xyz.junerver.compose.hooks.ResetStateHolder
import xyz.junerver.compose.hooks._rememberGetState
import xyz.junerver.compose.hooks._useGetState
import xyz.junerver.compose.hooks.rememberGetState
import xyz.junerver.compose.hooks.rememberResetState
import xyz.junerver.compose.hooks.useGetState
import xyz.junerver.compose.hooks.useResetState
import xyz.junerver.compose.hooks.useState

/*
  Description: useGetState/useResetState comprehensive TDD tests
  Author: AI Assistant
  Date: 2026/1/24
  Email: junerver@gmail.com
  Version: v1.0
*/

@Suppress("DEPRECATION")
class UseGetStateResetStateTest {
    // useGetState tests
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useGetState_initial_value() = runComposeUiTest {
        setContent {
            val (state) = useGetState(42)
            Text("value=${state.value}")
        }
        waitForIdle()
        onNodeWithText("value=42").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useGetState_setValue_with_direct_value() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val (state, setValue) = useGetState(0)

            SideEffect {
                if (!fired) {
                    fired = true
                    setValue(100.left())
                }
            }

            Text("value=${state.value}")
        }
        waitForIdle()
        onNodeWithText("value=100").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useGetState_setValue_with_function() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val (state, setValue) = useGetState(10)

            SideEffect {
                if (!fired) {
                    fired = true
                    setValue({ value: Int -> value * 2 }.right())
                }
            }

            Text("value=${state.value}")
        }
        waitForIdle()
        onNodeWithText("value=20").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useGetState_getValue_returns_latest() = runComposeUiTest {
        setContent {
            var phase by useState(0)
            var capturedValue by useState(0)
            val (state, setValue, getValue) = useGetState(0)

            SideEffect {
                when (phase) {
                    0 -> {
                        setValue(50.left())
                        phase = 1
                    }
                    1 -> {
                        capturedValue = getValue()
                        phase = 2
                    }
                }
            }

            Text("state=${state.value} captured=$capturedValue phase=$phase")
        }
        waitForIdle()
        onNodeWithText("state=50 captured=50 phase=2").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useGetState_multiple_updates() = runComposeUiTest {
        setContent {
            var phase by useState(0)
            val (state, setValue) = useGetState(0)

            SideEffect {
                when (phase) {
                    0 -> {
                        setValue(1.left())
                        phase = 1
                    }
                    1 -> {
                        setValue({ value: Int -> value + 10 }.right())
                        phase = 2
                    }
                    2 -> {
                        setValue({ value: Int -> value * 2 }.right())
                        phase = 3
                    }
                }
            }

            Text("value=${state.value} phase=$phase")
        }
        waitForIdle()
        onNodeWithText("value=22 phase=3").assertExists()
    }

    // useResetState tests
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useResetState_initial_value() = runComposeUiTest {
        setContent {
            val (state) = useResetState(42)
            Text("value=${state.value}")
        }
        waitForIdle()
        onNodeWithText("value=42").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useResetState_reset_returns_to_initial() = runComposeUiTest {
        setContent {
            var phase by useState(0)
            val (state, setValue, _, reset) = useResetState(10)

            SideEffect {
                when (phase) {
                    0 -> {
                        setValue(100.left())
                        phase = 1
                    }
                    1 -> {
                        reset()
                        phase = 2
                    }
                }
            }

            Text("value=${state.value} phase=$phase")
        }
        waitForIdle()
        onNodeWithText("value=10 phase=2").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useResetState_setValue_works() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val (state, setValue) = useResetState(0)

            SideEffect {
                if (!fired) {
                    fired = true
                    setValue(999.left())
                }
            }

            Text("value=${state.value}")
        }
        waitForIdle()
        onNodeWithText("value=999").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useResetState_getValue_works() = runComposeUiTest {
        setContent {
            var phase by useState(0)
            var captured by useState(0)
            val (state, setValue, getValue) = useResetState(5)

            SideEffect {
                when (phase) {
                    0 -> {
                        setValue(25.left())
                        phase = 1
                    }
                    1 -> {
                        captured = getValue()
                        phase = 2
                    }
                }
            }

            Text("state=${state.value} captured=$captured phase=$phase")
        }
        waitForIdle()
        onNodeWithText("state=25 captured=25 phase=2").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useResetState_multiple_reset_cycles() = runComposeUiTest {
        setContent {
            var phase by useState(0)
            val (state, setValue, _, reset) = useResetState(0)

            SideEffect {
                when (phase) {
                    0 -> {
                        setValue(10.left())
                        phase = 1
                    }
                    1 -> {
                        reset()
                        phase = 2
                    }
                    2 -> {
                        setValue(20.left())
                        phase = 3
                    }
                    3 -> {
                        reset()
                        phase = 4
                    }
                }
            }

            Text("value=${state.value} phase=$phase")
        }
        waitForIdle()
        onNodeWithText("value=0 phase=4").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useResetState_with_string() = runComposeUiTest {
        setContent {
            var phase by useState(0)
            val (state, setValue, _, reset) = useResetState("initial")

            SideEffect {
                when (phase) {
                    0 -> {
                        setValue("modified".left())
                        phase = 1
                    }
                    1 -> {
                        reset()
                        phase = 2
                    }
                }
            }

            Text("value=${state.value} phase=$phase")
        }
        waitForIdle()
        onNodeWithText("value=initial phase=2").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun get_state_keeps_holder_functions_and_initial_default_stable() = runComposeUiTest {
        val firstDefault = EqualValue("same")
        val replacementDefault = EqualValue("same")
        val default = mutableStateOf(firstDefault, referentialEqualityPolicy())
        lateinit var holder: GetStateHolder<EqualValue>

        setContent {
            holder = useGetState(default.value)
        }

        waitForIdle()
        val initialHolder = holder
        val initialState = holder.state
        val initialSetter = holder.setValue
        val initialGetter = holder.getValue
        val local = EqualValue("local")
        holder.setValue(local.left())
        default.value = replacementDefault
        waitForIdle()

        assertSame(initialHolder, holder)
        assertSame(initialState, holder.state)
        assertSame(initialSetter, holder.setValue)
        assertSame(initialGetter, holder.getValue)
        assertSame(local, holder.state.value)
        assertSame(local, holder.getValue())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun get_state_applies_rapid_functional_updates_to_latest_value() = runComposeUiTest {
        lateinit var holder: GetStateHolder<Int>

        setContent {
            holder = useGetState(0)
        }

        waitForIdle()
        repeat(3) {
            holder.setValue({ value: Int -> value + 1 }.right())
        }

        assertEquals(3, holder.state.value)
        assertEquals(3, holder.getValue())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun nullable_get_state_and_aliases_preserve_null_contract() = runComposeUiTest {
        lateinit var nullable: GetStateHolder<String?>
        lateinit var nullableAlias: GetStateHolder<String?>
        lateinit var alias: GetStateHolder<Int>

        setContent {
            nullable = _useGetState(null)
            nullableAlias = _rememberGetState(null)
            alias = rememberGetState(1)
        }

        waitForIdle()
        nullable.setValue("value".left())
        nullable.setValue({ value: String? -> value?.uppercase() }.right())
        assertEquals("VALUE", nullable.getValue())

        nullable.setValue((null as String?).left())
        assertEquals(null, nullable.state.value)
        assertEquals(null, nullableAlias.getValue())
        assertEquals(1, alias.getValue())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun reset_uses_latest_dynamic_default_and_keeps_holder_identity() = runComposeUiTest {
        val firstDefault = EqualValue("same")
        val replacementDefault = EqualValue("same")
        val default = mutableStateOf(firstDefault, referentialEqualityPolicy())
        lateinit var holder: ResetStateHolder<EqualValue>

        setContent {
            holder = useResetState(default.value)
        }

        waitForIdle()
        val initialHolder = holder
        val initialReset = holder.reset
        holder.setValue(EqualValue("local").left())
        default.value = replacementDefault
        waitForIdle()
        holder.reset()

        assertSame(initialHolder, holder)
        assertSame(initialReset, holder.reset)
        assertSame(replacementDefault, holder.state.value)
        assertSame(replacementDefault, holder.getValue())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun remember_reset_state_alias_supports_functional_update_and_reset() = runComposeUiTest {
        lateinit var holder: ResetStateHolder<Int>

        setContent {
            holder = rememberResetState(10)
        }

        waitForIdle()
        holder.setValue({ value: Int -> value + 5 }.right())
        assertEquals(15, holder.getValue())
        holder.reset()
        assertEquals(10, holder.getValue())
    }

    private data class EqualValue(val value: String)
}
