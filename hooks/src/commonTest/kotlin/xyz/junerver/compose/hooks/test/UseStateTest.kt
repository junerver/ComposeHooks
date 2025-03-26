package xyz.junerver.compose.hooks.test

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.MutableDoubleState
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableLongState
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import xyz.junerver.compose.hooks.useState

class UseStateTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testStateIsNumber() = runComposeUiTest {
        setContent {
            val intState = useState(default = 1)
            val longState = useState(default = 1L)
            val floatState = useState(default = 1F)
            val doubleState = useState(default = 1.0)
            // by use `useState` create the corresponding number type state
            assertEquals(true, intState is MutableIntState)
            assertEquals(true, longState is MutableLongState)
            assertEquals(true, floatState is MutableFloatState)
            assertEquals(true, doubleState is MutableDoubleState)
        }

    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testCreateControlledComponents() = runComposeUiTest {
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
}
