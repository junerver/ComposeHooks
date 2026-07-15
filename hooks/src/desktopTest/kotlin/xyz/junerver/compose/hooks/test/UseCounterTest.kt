package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import arrow.core.left
import arrow.core.right
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import xyz.junerver.compose.hooks.CounterHolder
import xyz.junerver.compose.hooks.rememberCounter
import xyz.junerver.compose.hooks.useCounter
import xyz.junerver.compose.hooks.useState

/*
  Description: useCounter comprehensive TDD tests
  Author: AI Assistant
  Date: 2026/1/24
  Email: junerver@gmail.com
  Version: v1.0
*/

@Suppress("DEPRECATION")
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

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun increment_saturates_before_integer_overflow() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val counter = useCounter(initialValue = Int.MAX_VALUE) {
                min = Int.MIN_VALUE
                max = Int.MAX_VALUE
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
        onNodeWithText("value=${Int.MAX_VALUE}").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun decrement_saturates_before_integer_underflow() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val counter = useCounter(initialValue = Int.MIN_VALUE) {
                min = Int.MIN_VALUE
                max = Int.MAX_VALUE
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
        onNodeWithText("value=${Int.MIN_VALUE}").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun dynamic_bounds_clamp_the_existing_value() = runComposeUiTest {
        val min = mutableStateOf(0)
        val max = mutableStateOf(10)
        lateinit var counter: CounterHolder
        setContent {
            counter = useCounter(initialValue = 8) {
                this.min = min.value
                this.max = max.value
            }
            Text("value=${counter.state.value}")
        }

        waitForIdle()
        runOnIdle { max.value = 5 }
        waitForIdle()
        assertEquals(5, counter.state.value, "Existing value must respect a dynamically lowered max")

        runOnIdle {
            min.value = 7
            max.value = 10
        }
        waitForIdle()
        assertEquals(7, counter.state.value, "Existing value must respect a dynamically raised min")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun holder_and_functions_keep_identity_across_recomposition() = runComposeUiTest {
        val trigger = mutableStateOf(0)
        lateinit var observed: CounterHolder
        setContent {
            observed = useCounter(initialValue = 2) {
                min = 0
                max = 10
            }
            Text("trigger=${trigger.value} value=${observed.state.value}")
        }

        waitForIdle()
        val first = observed
        runOnIdle { trigger.value++ }
        waitForIdle()
        val second = observed

        assertSame(first.state, second.state)
        assertSame(first.inc, second.inc)
        assertSame(first.dec, second.dec)
        assertSame(first.setValue, second.setValue)
        assertSame(first.reset, second.reset)
        assertSame(first, second)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rapid_functional_updates_use_the_latest_value() = runComposeUiTest {
        lateinit var counter: CounterHolder
        setContent {
            counter = useCounter(initialValue = 0) {
                min = -2_000
                max = 2_000
            }
            Text("value=${counter.state.value}")
        }

        waitForIdle()
        runOnIdle {
            repeat(1_000) { counter.inc(1) }
            repeat(250) { counter.dec(1) }
        }
        waitForIdle()

        assertEquals(750, counter.state.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun remember_alias_preserves_counter_behavior() = runComposeUiTest {
        lateinit var counter: CounterHolder
        setContent {
            counter = rememberCounter(initialValue = 4) {
                min = 0
                max = 10
            }
            Text("value=${counter.state.value}")
        }

        waitForIdle()
        runOnIdle { counter.inc(3) }
        waitForIdle()
        assertEquals(7, counter.state.value)
    }
}
