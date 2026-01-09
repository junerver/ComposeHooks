package xyz.junerver.compose.hooks.test

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import xyz.junerver.compose.hooks.useRef
import xyz.junerver.compose.hooks.useUpdate

/*
  Description:
  Author: Junerver
  Date: 2024/3/18-15:00
  Email: junerver@gmail.com
  Version: v1.0
*/
class UseRefTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testRefWhenSetCurrent() = runComposeUiTest {
        var refresh = 0
        setContent {
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
        val buttonNode = onNodeWithText("+1")
        val updateNode = onNodeWithText("update")
        onNodeWithText("Result:0").assertExists()
        // when click button will not recompose
        buttonNode.performClick() // ref -> 1
        onNodeWithText("Result:0").assertExists()
        assertEquals(1, refresh)
        buttonNode.performClick() // ref -> 2
        assertEquals(1, refresh) // will not execute sideEffect
        updateNode.performClick() // recompose ui
        waitForIdle()
        onNodeWithText("Result:2").assertExists()
        assertEquals(2, refresh) // component recompose sideEffect executed
    }
}
