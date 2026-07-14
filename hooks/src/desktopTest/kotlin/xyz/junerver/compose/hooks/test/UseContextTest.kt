/*
  Description: Context hook behavior tests
  Author: Junerver
  Date: 2026/07/14
  Email: junerver@gmail.com
  Version: v1.0
*/

package xyz.junerver.compose.hooks.test

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import xyz.junerver.compose.hooks.createContext
import xyz.junerver.compose.hooks.rememberContext
import xyz.junerver.compose.hooks.useContext

@Suppress("DEPRECATION")
class UseContextTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun context_supports_defaults_nested_providers_updates_and_isolation() = runComposeUiTest {
        val primaryContext = createContext("primary-default")
        val secondaryContext = createContext("secondary-default")
        val outerValue = mutableStateOf("outer")

        setContent {
            Column {
                Text("default=${useContext(primaryContext)}")
                primaryContext.Provider(outerValue.value) {
                    Text("outer=${rememberContext(primaryContext)}")
                    Text("isolated=${useContext(secondaryContext)}")
                    primaryContext.Provider("inner") {
                        Text("inner=${useContext(primaryContext)}")
                    }
                }
            }
        }

        waitForIdle()
        onNodeWithText("default=primary-default").assertExists()
        onNodeWithText("outer=outer").assertExists()
        onNodeWithText("inner=inner").assertExists()
        onNodeWithText("isolated=secondary-default").assertExists()

        runOnIdle { outerValue.value = "updated" }
        waitForIdle()

        onNodeWithText("outer=updated").assertExists()
        onNodeWithText("inner=inner").assertExists()
        onNodeWithText("default=primary-default").assertExists()
        onNodeWithText("isolated=secondary-default").assertExists()
    }
}
