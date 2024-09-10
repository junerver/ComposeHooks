package xyz.junerver.compose.hooks.test

import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.MutableDoubleState
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableLongState
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import kotlin.reflect.full.isSubclassOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import xyz.junerver.compose.hooks.useState

/*
  Description:
  Author: Junerver
  Date: 2024/3/15-15:15
  Email: junerver@gmail.com
  Version: v1.0
*/

class UseStateTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testStateIsNumber() {
        composeTestRule.setContent {
            val intState = useState(default = 1)
            val longState = useState(default = 1L)
            val floatState = useState(default = 1F)
            val doubleState = useState(default = 1.0)
            // by use `useState` create the corresponding number type state
            assertEquals(true, intState::class.isSubclassOf(MutableIntState::class))
            assertEquals(true, longState::class.isSubclassOf(MutableLongState::class))
            assertEquals(true, floatState::class.isSubclassOf(MutableFloatState::class))
            assertEquals(true, doubleState::class.isSubclassOf(MutableDoubleState::class))
        }

        composeTestRule.onRoot().printToLog("currentLabelExists")
    }

    @Test
    fun testCreateControlledComponents() {
        composeTestRule.setContent {
            val (state, setState) = useState("")
            OutlinedTextField(value = state, onValueChange = setState, label = { Text("Label") })
        }
        val textField = composeTestRule.onNode(hasText("Label"))
        textField.performTextInput("Hello, Compose!")
        composeTestRule.onNode(hasText("Hello, Compose!")).assertExists()
        textField.performTextClearance()
        composeTestRule.onNode(hasText("")).assertExists()
    }
}
