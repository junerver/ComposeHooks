package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import xyz.junerver.compose.hooks._rememberControllable
import xyz.junerver.compose.hooks._useControllable
import xyz.junerver.compose.hooks.ControllableHolder
import xyz.junerver.compose.hooks.rememberControllable
import xyz.junerver.compose.hooks.useControllable

@Suppress("DEPRECATION")
class UseControllableTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useControllable_updates_state_and_getter_for_non_null_values() = runComposeUiTest {
        setContent {
            val holder = useControllable(1)

            SideEffect {
                if (holder.state.value == 1) {
                    holder.setValue(2)
                }
            }

            Text("state=${holder.state.value} getter=${holder.getValue()}")
        }

        waitForIdle()
        onNodeWithText("state=2 getter=2").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun private_useControllable_supports_nullable_values() = runComposeUiTest {
        setContent {
            val holder = _useControllable<String?>("initial")

            SideEffect {
                if (holder.state.value == "initial") {
                    holder.setValue(null)
                }
            }

            Text("state=${holder.state.value} getter=${holder.getValue()}")
        }

        waitForIdle()
        onNodeWithText("state=null getter=null").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useControllable_getter_tracks_latest_value_after_multiple_updates() = runComposeUiTest {
        setContent {
            val holder = useControllable(10)

            SideEffect {
                when (holder.state.value) {
                    10 -> holder.setValue(20)
                    20 -> holder.setValue(30)
                }
            }

            Text("state=${holder.state.value} getter=${holder.getValue()}")
        }

        waitForIdle()
        onNodeWithText("state=30 getter=30").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useControllable_getter_reads_latest_value_after_recomposition() = runComposeUiTest {
        lateinit var holder: ControllableHolder<Int>

        setContent {
            holder = useControllable(5)
            Text("state=${holder.state.value}")
        }

        waitForIdle()
        assertEquals(5, holder.getValue())
        holder.setValue(9)
        waitForIdle()
        onNodeWithText("state=9").assertExists()
        assertEquals(9, holder.getValue())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useControllable_keeps_holder_and_function_identity() = runComposeUiTest {
        val trigger = mutableStateOf(0)
        lateinit var observed: ControllableHolder<Int>
        setContent {
            observed = useControllable(1)
            Text("trigger=${trigger.value} state=${observed.state.value}")
        }

        waitForIdle()
        val first = observed
        runOnIdle { trigger.value++ }
        waitForIdle()
        val second = observed

        assertSame(first.state, second.state)
        assertSame(first.setValue, second.setValue, "Setter identity must survive unrelated recomposition")
        assertSame(first.getValue, second.getValue, "Getter identity must survive unrelated recomposition")
        assertSame(first, second, "Stable holder must not be recreated on every recomposition")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun private_useControllable_keeps_nullable_holder_identity() = runComposeUiTest {
        val trigger = mutableStateOf(0)
        lateinit var observed: ControllableHolder<String?>
        setContent {
            observed = _useControllable<String?>(null)
            Text("trigger=${trigger.value} state=${observed.state.value}")
        }

        waitForIdle()
        val first = observed
        runOnIdle { trigger.value++ }
        waitForIdle()
        val second = observed

        assertSame(first.setValue, second.setValue)
        assertSame(first.getValue, second.getValue)
        assertSame(first, second)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rapid_updates_expose_the_latest_value() = runComposeUiTest {
        lateinit var holder: ControllableHolder<Int>
        setContent {
            holder = useControllable(0)
            Text("state=${holder.state.value}")
        }

        waitForIdle()
        runOnIdle {
            repeat(1_000) { value -> holder.setValue(value + 1) }
        }
        waitForIdle()

        assertEquals(1_000, holder.state.value)
        assertEquals(1_000, holder.getValue())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun nullable_value_can_switch_to_null_and_back() = runComposeUiTest {
        lateinit var holder: ControllableHolder<String?>
        setContent {
            holder = _rememberControllable("initial")
            Text("state=${holder.state.value}")
        }

        waitForIdle()
        runOnIdle {
            holder.setValue(null)
            assertEquals(null, holder.getValue())
            holder.setValue("restored")
        }
        waitForIdle()

        assertEquals("restored", holder.state.value)
        assertEquals("restored", holder.getValue())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun remember_alias_matches_use_alias() = runComposeUiTest {
        lateinit var holder: ControllableHolder<Int>
        setContent {
            holder = rememberControllable(4)
            Text("state=${holder.state.value}")
        }

        waitForIdle()
        runOnIdle { holder.setValue(8) }
        waitForIdle()

        assertEquals(8, holder.state.value)
        assertEquals(8, holder.getValue())
    }
}
