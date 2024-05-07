package xyz.junerver.compose.hooks.test

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.useDebounceFn
import xyz.junerver.compose.hooks.useState

/**
 * Description:
 *
 * @author Junerver date: 2024/3/18-15:00 Email: junerver@gmail.com
 *     Version: v1.0
 */
class UseDebounceTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testDebounceFn() {
        composeTestRule.setContent {
            var state by useState(default = 0)
            val debounceAdd = useDebounceFn(fn = {
                state += 1
            })
            Column {
                Text("result:$state")
                Button(onClick = { debounceAdd() }) {
                    Text(text = "debounce+1")
                }
            }
        }
        val buttonNode = composeTestRule.onNodeWithText("debounce+1")
        composeTestRule.onNodeWithText("result:0").assertExists()
        // quick change
        repeat(5) {
            Thread.sleep(100)
            buttonNode.performClick()
        }
        composeTestRule.waitUntil(2_000) {
            composeTestRule.onNodeWithText("result:1").isDisplayed()
        }
        // debounce invoke
        Thread.sleep(1001)
        buttonNode.performClick()
        Thread.sleep(1001)
        buttonNode.performClick()
        composeTestRule.waitUntil(2_000) {
            composeTestRule.onNodeWithText("result:3").isDisplayed()
        }
    }
}
