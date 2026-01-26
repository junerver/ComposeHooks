package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import arrow.core.left
import arrow.core.right
import kotlin.test.Test
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
}
