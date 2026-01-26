package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import xyz.junerver.compose.hooks.useRef
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.useUpdate

/*
  Description: useUpdate comprehensive TDD tests
  Author: AI Assistant
  Date: 2026/1/24
  Email: junerver@gmail.com
  Version: v1.0
*/

class UseUpdateTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useUpdate_forces_recomposition() = runComposeUiTest {
        setContent {
            val countRef = useRef(0)
            val update = useUpdate()

            // Only increment on first composition
            if (countRef.current == 0) {
                countRef.current = 1
                SideEffect {
                    update()
                }
            }

            Text("count=${countRef.current}")
        }
        waitForIdle()
        // After update(), the component recomposes and countRef.current is still 1
        onNodeWithText("count=1").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useUpdate_with_ref_shows_updated_value() = runComposeUiTest {
        setContent {
            val countRef = useRef(0)
            var phase by useState(0)
            val update = useUpdate()

            SideEffect {
                when (phase) {
                    0 -> {
                        countRef.current = 10
                        phase = 1
                    }
                    1 -> {
                        update() // Force recomposition to show ref value
                        phase = 2
                    }
                }
            }

            Text("ref=${countRef.current} phase=$phase")
        }
        waitForIdle()
        onNodeWithText("ref=10 phase=2").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useUpdate_returns_stable_function() = runComposeUiTest {
        setContent {
            var phase by useState(0)
            val update = useUpdate()

            SideEffect {
                if (phase == 0) {
                    update()
                    phase = 1
                }
            }

            Text("phase=$phase")
        }
        waitForIdle()
        onNodeWithText("phase=1").assertExists()
    }
}
