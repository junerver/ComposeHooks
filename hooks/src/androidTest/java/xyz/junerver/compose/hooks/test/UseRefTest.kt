package xyz.junerver.compose.hooks.test

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import xyz.junerver.compose.hooks.useRef
import xyz.junerver.compose.hooks.useUpdate

/**
 * Description:
 *
 * @author Junerver date: 2024/3/18-15:00 Email: junerver@gmail.com
 *     Version: v1.0
 */
class UseRefTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testRefWhenSetCurrent() {
        var refresh = 0
        composeTestRule.setContent {
            val countRef = useRef(default = 0)
            val update = useUpdate()
            SideEffect {
                refresh++
            }
            Column {
                Text(text = "Result:${countRef.current}")
                Button(onClick = { countRef.current += 1 }) {
                    Text(text = "+1")
                }
                Button(onClick = { update() }) {
                    Text(text = "update")
                }
            }
        }
        val buttonNode = composeTestRule.onNodeWithText("+1")
        val updateNode = composeTestRule.onNodeWithText("update")
        composeTestRule.onNodeWithText("Result:0").assertExists()
        // when click button will not recompose
        buttonNode.performClick() // ref -> 1
        composeTestRule.onNodeWithText("Result:0").assertExists()
        assertEquals(1, refresh)
        buttonNode.performClick()  // ref -> 2
        assertEquals(1, refresh) // will not execute sideEffect
        updateNode.performClick() // recompose ui
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Result:2").assertExists()
        assertEquals(2, refresh) //component recompose sideEffect executed
    }
}
