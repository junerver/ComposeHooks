package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import arrow.core.left
import arrow.core.right
import kotlin.test.Test
import xyz.junerver.compose.hooks.useCounter
import xyz.junerver.compose.hooks.useState

/*
  Description: useCounter comprehensive TDD tests
  Author: AI Assistant
  Date: 2026/1/24
  Email: junerver@gmail.com
  Version: v1.0
*/

class UseCounterTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun default_value_is_initial() = runComposeUiTest {
        setContent {
            val counter = useCounter(initialValue = 5) {
                min = 0
                max = 10
            }
            Text("value=${counter.state.value}")
        }
        waitForIdle()
        onNodeWithText("value=5").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun inc_increases_value() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val counter = useCounter(initialValue = 5) {
                min = 0
                max = 10
            }

            SideEffect {
                if (!fired) {
                    fired = true
                    counter.inc(1)
                }
            }

            Text("value=${counter.state.value}")
        }
        waitForIdle()
        onNodeWithText("value=6").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun inc_with_delta() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val counter = useCounter(initialValue = 5) {
                min = 0
                max = 10
            }

            SideEffect {
                if (!fired) {
                    fired = true
                    counter.inc(3)
                }
            }

            Text("value=${counter.state.value}")
        }
        waitForIdle()
        onNodeWithText("value=8").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun dec_decreases_value() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val counter = useCounter(initialValue = 5) {
                min = 0
                max = 10
            }

            SideEffect {
                if (!fired) {
                    fired = true
                    counter.dec(1)
                }
            }

            Text("value=${counter.state.value}")
        }
        waitForIdle()
        onNodeWithText("value=4").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun dec_with_delta() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val counter = useCounter(initialValue = 5) {
                min = 0
                max = 10
            }

            SideEffect {
                if (!fired) {
                    fired = true
                    counter.dec(3)
                }
            }

            Text("value=${counter.state.value}")
        }
        waitForIdle()
        onNodeWithText("value=2").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun inc_respects_max_bound() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val counter = useCounter(initialValue = 8) {
                min = 0
                max = 10
            }

            SideEffect {
                if (!fired) {
                    fired = true
                    counter.inc(5) // Would be 13, but max is 10
                }
            }

            Text("value=${counter.state.value}")
        }
        waitForIdle()
        onNodeWithText("value=10").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun dec_respects_min_bound() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val counter = useCounter(initialValue = 2) {
                min = 0
                max = 10
            }

            SideEffect {
                if (!fired) {
                    fired = true
                    counter.dec(5) // Would be -3, but min is 0
                }
            }

            Text("value=${counter.state.value}")
        }
        waitForIdle()
        onNodeWithText("value=0").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun setValue_with_direct_value() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val counter = useCounter(initialValue = 5) {
                min = 0
                max = 10
            }

            SideEffect {
                if (!fired) {
                    fired = true
                    counter.setValue(7.left())
                }
            }

            Text("value=${counter.state.value}")
        }
        waitForIdle()
        onNodeWithText("value=7").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun setValue_with_function() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val counter = useCounter(initialValue = 5) {
                min = 0
                max = 10
            }

            SideEffect {
                if (!fired) {
                    fired = true
                    counter.setValue({ value: Int -> value * 2 }.right()) // 5 * 2 = 10
                }
            }

            Text("value=${counter.state.value}")
        }
        waitForIdle()
        onNodeWithText("value=10").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun setValue_respects_bounds() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val counter = useCounter(initialValue = 5) {
                min = 0
                max = 10
            }

            SideEffect {
                if (!fired) {
                    fired = true
                    counter.setValue(15.left()) // Exceeds max
                }
            }

            Text("value=${counter.state.value}")
        }
        waitForIdle()
        onNodeWithText("value=10").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun reset_returns_to_initial() = runComposeUiTest {
        setContent {
            var phase by useState(0)
            val counter = useCounter(initialValue = 5) {
                min = 0
                max = 10
            }

            SideEffect {
                when (phase) {
                    0 -> {
                        counter.inc(3) // Now 8
                        phase = 1
                    }
                    1 -> {
                        counter.reset()
                        phase = 2
                    }
                }
            }

            Text("value=${counter.state.value} phase=$phase")
        }
        waitForIdle()
        onNodeWithText("value=5 phase=2").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun initial_value_clamped_to_bounds() = runComposeUiTest {
        setContent {
            val counter = useCounter(initialValue = 15) {
                min = 0
                max = 10
            }
            Text("value=${counter.state.value}")
        }
        waitForIdle()
        onNodeWithText("value=10").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun initial_value_below_min_clamped() = runComposeUiTest {
        setContent {
            val counter = useCounter(initialValue = -5) {
                min = 0
                max = 10
            }
            Text("value=${counter.state.value}")
        }
        waitForIdle()
        onNodeWithText("value=0").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun negative_bounds_work() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val counter = useCounter(initialValue = 0) {
                min = -10
                max = -5
            }

            SideEffect {
                if (!fired) {
                    fired = true
                    counter.dec(3)
                }
            }

            Text("value=${counter.state.value}")
        }
        waitForIdle()
        // Initial 0 clamped to -5 (max), then dec(3) = -8
        onNodeWithText("value=-8").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun multiple_operations_sequence() = runComposeUiTest {
        setContent {
            var phase by useState(0)
            val counter = useCounter(initialValue = 5) {
                min = 0
                max = 10
            }

            SideEffect {
                when (phase) {
                    0 -> {
                        counter.inc(2) // 7
                        phase = 1
                    }
                    1 -> {
                        counter.dec(1) // 6
                        phase = 2
                    }
                    2 -> {
                        counter.inc(10) // clamped to 10
                        phase = 3
                    }
                }
            }

            Text("value=${counter.state.value} phase=$phase")
        }
        waitForIdle()
        onNodeWithText("value=10 phase=3").assertExists()
    }
}
