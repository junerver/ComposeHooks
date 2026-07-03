package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import xyz.junerver.compose.hooks._useControllable
import xyz.junerver.compose.hooks.ControllableHolder
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
}
