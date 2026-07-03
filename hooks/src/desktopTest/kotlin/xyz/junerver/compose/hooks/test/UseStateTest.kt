package xyz.junerver.compose.hooks.test

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks._useState

@Suppress("DEPRECATION")
class UseStateTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun state_updates_when_setter_is_called() = runComposeUiTest {
        setContent {
            val state = useState(default = 1)

            SideEffect {
                if (state.value == 1) {
                    state.value = 2
                }
            }

            Text("value=${state.value}")
        }

        waitForIdle()
        onNodeWithText("value=2").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun create_controlled_components() = runComposeUiTest {
        setContent {
            val (state, setState) = useState("")
            OutlinedTextField(value = state, onValueChange = setState, label = { Text("Label") })
        }
        val textField = onNode(hasText("Label"))
        textField.performTextInput("Hello, Compose!")
        onNode(hasText("Hello, Compose!")).assertExists()
        textField.performTextClearance()
        onNode(hasText("")).assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun nullable_state_supports_null_and_later_updates() = runComposeUiTest {
        lateinit var state: MutableState<String?>
        setContent {
            state = _useState<String?>(null)
            Text("value=${state.value ?: "null"}")
        }

        waitForIdle()
        onNodeWithText("value=null").assertExists()

        state.value = "updated"
        waitForIdle()
        onNodeWithText("value=updated").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun derived_state_recomputes_when_dependency_keys_change() = runComposeUiTest {
        var source by mutableStateOf(1)
        var latestValue = 0
        var recomputeCount = 0

        setContent {
            val derived = useState(source) {
                recomputeCount += 1
                source * 10
            }
            latestValue = derived.value
            Text("derived=$latestValue count=$recomputeCount")
        }

        waitForIdle()
        assertEquals(10, latestValue)
        assertEquals(1, recomputeCount)

        source = 2
        waitForIdle()
        assertEquals(20, latestValue)
        assertEquals(2, recomputeCount)
    }
}
