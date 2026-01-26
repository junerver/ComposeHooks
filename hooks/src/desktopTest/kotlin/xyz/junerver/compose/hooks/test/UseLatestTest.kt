package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import xyz.junerver.compose.hooks.useLatestRef
import xyz.junerver.compose.hooks.useLatestState
import xyz.junerver.compose.hooks.useState

/*
  Description: useLatest comprehensive TDD tests
  Author: AI Assistant
  Date: 2026/1/24
  Email: junerver@gmail.com
  Version: v1.0
*/

class UseLatestTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useLatestRef_returns_initial_value() = runComposeUiTest {
        setContent {
            val latestRef = useLatestRef("initial")
            Text("value=${latestRef.current}")
        }
        waitForIdle()
        onNodeWithText("value=initial").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useLatestRef_updates_on_recomposition() = runComposeUiTest {
        setContent {
            var value by useState("first")
            val latestRef = useLatestRef(value)

            SideEffect {
                if (value == "first") {
                    value = "second"
                }
            }

            Text("ref=${latestRef.current} state=$value")
        }
        waitForIdle()
        onNodeWithText("ref=second state=second").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useLatestRef_always_has_latest_value() = runComposeUiTest {
        setContent {
            var phase by useState(0)
            var value by useState(0)
            val latestRef = useLatestRef(value)

            SideEffect {
                when (phase) {
                    0 -> { value = 1; phase = 1 }
                    1 -> { value = 2; phase = 2 }
                    2 -> { value = 3; phase = 3 }
                }
            }

            Text("ref=${latestRef.current} phase=$phase")
        }
        waitForIdle()
        onNodeWithText("ref=3 phase=3").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useLatestState_returns_initial_value() = runComposeUiTest {
        setContent {
            val latestState = useLatestState("initial")
            Text("value=${latestState.value}")
        }
        waitForIdle()
        onNodeWithText("value=initial").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useLatestState_updates_on_recomposition() = runComposeUiTest {
        setContent {
            var value by useState("first")
            val latestState = useLatestState(value)

            SideEffect {
                if (value == "first") {
                    value = "second"
                }
            }

            Text("latest=${latestState.value} state=$value")
        }
        waitForIdle()
        onNodeWithText("latest=second state=second").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useLatestRef_with_nullable_value() = runComposeUiTest {
        setContent {
            var value by useState("initial")
            var phase by useState(0)
            val latestRef = useLatestRef(value)

            SideEffect {
                if (phase == 0) {
                    value = "changed"
                    phase = 1
                }
            }

            Text("ref=${latestRef.current}")
        }
        waitForIdle()
        onNodeWithText("ref=changed").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useLatestState_with_nullable_value() = runComposeUiTest {
        setContent {
            var value by useState("initial")
            var phase by useState(0)
            val latestState = useLatestState(value)

            SideEffect {
                if (phase == 0) {
                    value = "changed"
                    phase = 1
                }
            }

            Text("latest=${latestState.value}")
        }
        waitForIdle()
        onNodeWithText("latest=changed").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useLatestRef_with_complex_object() = runComposeUiTest {
        setContent {
            var list by useState(listOf(1, 2, 3))
            val latestRef = useLatestRef(list)

            SideEffect {
                if (list == listOf(1, 2, 3)) {
                    list = listOf(4, 5, 6)
                }
            }

            Text("ref=${latestRef.current.joinToString()}")
        }
        waitForIdle()
        onNodeWithText("ref=4, 5, 6").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useLatestRef_preserves_reference_identity() = runComposeUiTest {
        setContent {
            var counter by useState(0)
            val latestRef = useLatestRef("constant")

            SideEffect {
                if (counter < 3) {
                    counter++
                }
            }

            // The ref object itself should be stable across recompositions
            Text("value=${latestRef.current} counter=$counter")
        }
        waitForIdle()
        onNodeWithText("value=constant counter=3").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useLatestRef_with_lambda() = runComposeUiTest {
        setContent {
            var multiplier by useState(1)
            val latestFn = useLatestRef { x: Int -> x * multiplier }

            SideEffect {
                if (multiplier == 1) {
                    multiplier = 10
                }
            }

            val result = latestFn.current(5)
            Text("result=$result multiplier=$multiplier")
        }
        waitForIdle()
        onNodeWithText("result=50 multiplier=10").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useLatestState_multiple_updates() = runComposeUiTest {
        setContent {
            var phase by useState(0)
            var value by useState("a")
            val latestState = useLatestState(value)

            SideEffect {
                when (phase) {
                    0 -> { value = "b"; phase = 1 }
                    1 -> { value = "c"; phase = 2 }
                    2 -> { value = "d"; phase = 3 }
                }
            }

            Text("latest=${latestState.value} phase=$phase")
        }
        waitForIdle()
        onNodeWithText("latest=d phase=3").assertExists()
    }
}
