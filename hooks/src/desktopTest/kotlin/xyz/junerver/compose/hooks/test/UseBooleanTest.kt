package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue
import xyz.junerver.compose.hooks.BooleanHolder
import xyz.junerver.compose.hooks.rememberBoolean
import xyz.junerver.compose.hooks.useBoolean
import xyz.junerver.compose.hooks.useState

/*
  Description: useBoolean comprehensive TDD tests
  Author: AI Assistant
  Date: 2026/1/24
  Email: junerver@gmail.com
  Version: v1.0
*/

@Suppress("DEPRECATION")
class UseBooleanTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun default_value_is_false() = runComposeUiTest {
        setContent {
            val (state) = useBoolean()
            Text("value=${state.value}")
        }
        waitForIdle()
        onNodeWithText("value=false").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun default_value_can_be_set_to_true() = runComposeUiTest {
        setContent {
            val (state) = useBoolean(default = true)
            Text("value=${state.value}")
        }
        waitForIdle()
        onNodeWithText("value=true").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun toggle_switches_value() = runComposeUiTest {
        setContent {
            var phase by useState(0)
            val (state, toggle) = useBoolean(default = false)

            SideEffect {
                when (phase) {
                    0 -> {
                        toggle()
                        phase = 1
                    }
                }
            }

            Text("value=${state.value} phase=$phase")
        }
        waitForIdle()
        onNodeWithText("value=true phase=1").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun toggle_twice_returns_to_original() = runComposeUiTest {
        setContent {
            var phase by useState(0)
            val (state, toggle) = useBoolean(default = false)

            SideEffect {
                when (phase) {
                    0 -> {
                        toggle()
                        phase = 1
                    }
                    1 -> {
                        toggle()
                        phase = 2
                    }
                }
            }

            Text("value=${state.value} phase=$phase")
        }
        waitForIdle()
        onNodeWithText("value=false phase=2").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun setTrue_sets_value_to_true() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val holder = useBoolean(default = false)

            SideEffect {
                if (!fired) {
                    fired = true
                    holder.setTrue()
                }
            }

            Text("value=${holder.state.value}")
        }
        waitForIdle()
        onNodeWithText("value=true").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun setFalse_sets_value_to_false() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val holder = useBoolean(default = true)

            SideEffect {
                if (!fired) {
                    fired = true
                    holder.setFalse()
                }
            }

            Text("value=${holder.state.value}")
        }
        waitForIdle()
        onNodeWithText("value=false").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun setValue_sets_specific_value() = runComposeUiTest {
        setContent {
            var phase by useState(0)
            val holder = useBoolean(default = false)

            SideEffect {
                when (phase) {
                    0 -> {
                        holder.setValue(true)
                        phase = 1
                    }
                    1 -> {
                        holder.setValue(false)
                        phase = 2
                    }
                }
            }

            Text("value=${holder.state.value} phase=$phase")
        }
        waitForIdle()
        onNodeWithText("value=false phase=2").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun setTrue_when_already_true_stays_true() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val holder = useBoolean(default = true)

            SideEffect {
                if (!fired) {
                    fired = true
                    holder.setTrue()
                }
            }

            Text("value=${holder.state.value}")
        }
        waitForIdle()
        onNodeWithText("value=true").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun setFalse_when_already_false_stays_false() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val holder = useBoolean(default = false)

            SideEffect {
                if (!fired) {
                    fired = true
                    holder.setFalse()
                }
            }

            Text("value=${holder.state.value}")
        }
        waitForIdle()
        onNodeWithText("value=false").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun destructuring_works_correctly() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val (state, toggle, setValue, setTrue, setFalse) = useBoolean(default = false)

            SideEffect {
                if (!fired) {
                    fired = true
                    // Verify all functions are accessible
                    setTrue()
                }
            }

            Text("value=${state.value}")
        }
        waitForIdle()
        onNodeWithText("value=true").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun holder_and_functions_keep_identity_across_recomposition() = runComposeUiTest {
        val trigger = mutableStateOf(0)
        lateinit var observed: BooleanHolder
        setContent {
            observed = useBoolean()
            Text("trigger=${trigger.value} value=${observed.state.value}")
        }

        waitForIdle()
        val first = observed
        runOnIdle { trigger.value++ }
        waitForIdle()
        val second = observed

        assertSame(first.state, second.state)
        assertSame(first.toggle, second.toggle)
        assertSame(first.setValue, second.setValue)
        assertSame(first.setTrue, second.setTrue)
        assertSame(first.setFalse, second.setFalse)
        assertSame(first, second)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rapid_toggles_read_the_latest_value() = runComposeUiTest {
        lateinit var holder: BooleanHolder
        setContent {
            holder = useBoolean()
            Text("value=${holder.state.value}")
        }

        waitForIdle()
        runOnIdle { repeat(1_001) { holder.toggle() } }
        waitForIdle()
        assertTrue(holder.state.value)

        runOnIdle { repeat(1_001) { holder.toggle() } }
        waitForIdle()
        assertFalse(holder.state.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun remember_alias_preserves_boolean_behavior() = runComposeUiTest {
        lateinit var holder: BooleanHolder
        setContent {
            holder = rememberBoolean(default = true)
            Text("value=${holder.state.value}")
        }

        waitForIdle()
        runOnIdle { holder.toggle() }
        waitForIdle()
        assertFalse(holder.state.value)
    }
}
