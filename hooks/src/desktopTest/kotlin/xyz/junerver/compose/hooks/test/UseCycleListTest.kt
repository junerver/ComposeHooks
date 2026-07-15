package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlinx.collections.immutable.persistentListOf
import xyz.junerver.compose.hooks.CycleListHolder
import xyz.junerver.compose.hooks.rememberCycleList
import xyz.junerver.compose.hooks.useCycleList
import xyz.junerver.compose.hooks.useState

/*
  Description: useCycleList comprehensive TDD tests
  Author: AI Assistant
  Date: 2026/1/24
  Email: junerver@gmail.com
  Version: v1.0
*/

@Suppress("DEPRECATION")
class UseCycleListTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useCycleList_initial_value_is_first_item() = runComposeUiTest {
        setContent {
            val list = persistentListOf("A", "B", "C")
            val (state) = useCycleList(list)
            Text("value=${state.value}")
        }
        waitForIdle()
        onNodeWithText("value=A").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useCycleList_next_moves_forward() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val list = persistentListOf("A", "B", "C")
            val (state, _, next) = useCycleList(list)

            SideEffect {
                if (!fired) {
                    fired = true
                    next()
                }
            }

            Text("value=${state.value}")
        }
        waitForIdle()
        onNodeWithText("value=B").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useCycleList_prev_moves_backward() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val list = persistentListOf("A", "B", "C")
            val (state, _, _, prev) = useCycleList(list)

            SideEffect {
                if (!fired) {
                    fired = true
                    prev()
                }
            }

            Text("value=${state.value}")
        }
        waitForIdle()
        // From A, prev should wrap to C
        onNodeWithText("value=C").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useCycleList_next_wraps_around() = runComposeUiTest {
        setContent {
            var phase by useState(0)
            val list = persistentListOf("A", "B", "C")
            val (state, _, next) = useCycleList(list)

            SideEffect {
                when (phase) {
                    0 -> {
                        next()
                        phase = 1
                    } // A -> B
                    1 -> {
                        next()
                        phase = 2
                    } // B -> C
                    2 -> {
                        next()
                        phase = 3
                    } // C -> A (wrap)
                }
            }

            Text("value=${state.value} phase=$phase")
        }
        waitForIdle()
        onNodeWithText("value=A phase=3").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useCycleList_go_jumps_to_index() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val list = persistentListOf("A", "B", "C", "D")
            val (state, _, _, _, go) = useCycleList(list)

            SideEffect {
                if (!fired) {
                    fired = true
                    go(2)
                }
            }

            Text("value=${state.value}")
        }
        waitForIdle()
        onNodeWithText("value=C").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useCycleList_go_handles_negative_index() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val list = persistentListOf("A", "B", "C")
            val (state, _, _, _, go) = useCycleList(list)

            SideEffect {
                if (!fired) {
                    fired = true
                    go(-1)
                }
            }

            Text("value=${state.value}")
        }
        waitForIdle()
        // -1 should wrap to last item (C)
        onNodeWithText("value=C").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useCycleList_go_handles_overflow_index() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val list = persistentListOf("A", "B", "C")
            val (state, _, _, _, go) = useCycleList(list)

            SideEffect {
                if (!fired) {
                    fired = true
                    go(5) // 5 % 3 = 2 -> C
                }
            }

            Text("value=${state.value}")
        }
        waitForIdle()
        onNodeWithText("value=C").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useCycleList_index_tracks_current_position() = runComposeUiTest {
        setContent {
            var phase by useState(0)
            val list = persistentListOf("A", "B", "C")
            val (state, index, next) = useCycleList(list)

            SideEffect {
                when (phase) {
                    0 -> {
                        next()
                        phase = 1
                    }
                    1 -> {
                        next()
                        phase = 2
                    }
                }
            }

            Text("value=${state.value} index=${index.value} phase=$phase")
        }
        waitForIdle()
        onNodeWithText("value=C index=2 phase=2").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useCycleList_with_initial_value() = runComposeUiTest {
        setContent {
            val list = persistentListOf("A", "B", "C")
            val (state) = useCycleList(list) {
                initialValue = "B"
            }
            Text("value=${state.value}")
        }
        waitForIdle()
        onNodeWithText("value=B").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useCycleList_shift_moves_by_delta() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val list = persistentListOf("A", "B", "C", "D", "E")
            val (state, _, _, _, _, shift) = useCycleList(list)

            SideEffect {
                if (!fired) {
                    fired = true
                    shift(3) // A -> D
                }
            }

            Text("value=${state.value}")
        }
        waitForIdle()
        onNodeWithText("value=D").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useCycleList_shift_negative_moves_backward() = runComposeUiTest {
        setContent {
            var phase by useState(0)
            val list = persistentListOf("A", "B", "C", "D", "E")
            val (state, _, _, _, go, shift) = useCycleList(list)

            SideEffect {
                when (phase) {
                    0 -> {
                        go(2)
                        phase = 1
                    } // Go to C
                    1 -> {
                        shift(-2)
                        phase = 2
                    } // C -> A
                }
            }

            Text("value=${state.value} phase=$phase")
        }
        waitForIdle()
        onNodeWithText("value=A phase=2").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useCycleList_with_integers() = runComposeUiTest {
        setContent {
            var phase by useState(0)
            val list = persistentListOf(10, 20, 30, 40)
            val (state, _, next) = useCycleList(list)

            SideEffect {
                when (phase) {
                    0 -> {
                        next()
                        phase = 1
                    }
                    1 -> {
                        next()
                        phase = 2
                    }
                }
            }

            Text("value=${state.value} phase=$phase")
        }
        waitForIdle()
        onNodeWithText("value=30 phase=2").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun navigation_uses_latest_list_and_preserves_holder_identity() = runComposeUiTest {
        var items by mutableStateOf(persistentListOf("A", "B"))
        lateinit var holder: CycleListHolder<String>

        setContent {
            holder = useCycleList(items)
        }

        waitForIdle()
        val initialHolder = holder
        val initialState = holder.state
        val initialIndex = holder.index
        val initialNext = holder.next
        val initialGo = holder.go

        items = persistentListOf("A", "C", "D")
        waitForIdle()
        holder.next()
        waitForIdle()

        assertSame(initialHolder, holder)
        assertSame(initialState, holder.state)
        assertSame(initialIndex, holder.index)
        assertSame(initialNext, holder.next)
        assertSame(initialGo, holder.go)
        assertEquals("C", holder.state.value)
        assertEquals(1, holder.index.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun later_empty_list_is_safe_and_navigation_recovers() = runComposeUiTest {
        var items by mutableStateOf(persistentListOf("A", "B"))
        lateinit var holder: CycleListHolder<String>

        setContent {
            holder = useCycleList(items)
        }

        waitForIdle()
        holder.next()
        waitForIdle()
        assertEquals("B", holder.state.value)

        items = persistentListOf()
        waitForIdle()
        assertEquals("B", holder.state.value)
        assertEquals(0, holder.index.value)
        holder.next()
        assertEquals("B", holder.state.value)

        items = persistentListOf("X", "Y")
        waitForIdle()
        holder.next()
        waitForIdle()
        assertEquals("Y", holder.state.value)
        assertEquals(1, holder.index.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun equal_duplicates_cycle_by_position_under_rapid_updates() = runComposeUiTest {
        lateinit var holder: CycleListHolder<String>

        setContent {
            holder = useCycleList(persistentListOf("A", "A", "B"))
        }

        waitForIdle()
        holder.go(1)
        waitForIdle()
        assertEquals("A", holder.state.value)
        assertEquals(1, holder.index.value)

        holder.next()
        holder.next()
        holder.next()
        waitForIdle()
        assertEquals("A", holder.state.value)
        assertEquals(1, holder.index.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun dynamic_fallback_and_extreme_shifts_are_observed() = runComposeUiTest {
        var fallbackIndex by mutableStateOf(0)
        lateinit var holder: CycleListHolder<String>

        setContent {
            holder = useCycleList(persistentListOf("A", "B", "C")) {
                initialValue = "missing"
                this.fallbackIndex = fallbackIndex
            }
        }

        waitForIdle()
        assertEquals(0, holder.index.value)

        fallbackIndex = 1
        waitForIdle()
        assertEquals(1, holder.index.value)

        holder.shift(Int.MAX_VALUE)
        waitForIdle()
        assertEquals("C", holder.state.value)
        assertEquals(2, holder.index.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun latest_custom_index_lookup_drives_navigation() = runComposeUiTest {
        var useLastMatch by mutableStateOf(false)
        lateinit var holder: CycleListHolder<String>

        setContent {
            val shouldUseLastMatch = useLastMatch
            holder = useCycleList(persistentListOf("B", "C", "B", "D")) {
                getIndexOf = { value, items ->
                    if (shouldUseLastMatch) items.lastIndexOf(value) else items.indexOf(value)
                }
            }
        }

        waitForIdle()
        assertEquals(0, holder.index.value)

        useLastMatch = true
        waitForIdle()
        assertEquals(2, holder.index.value)

        holder.next()
        waitForIdle()
        assertEquals("D", holder.state.value)
        assertEquals(3, holder.index.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun explicit_initial_value_supports_an_empty_list() = runComposeUiTest {
        lateinit var holder: CycleListHolder<String>

        setContent {
            holder = useCycleList(persistentListOf()) {
                initialValue = "fallback"
            }
        }

        waitForIdle()
        assertEquals("fallback", holder.state.value)
        assertEquals(0, holder.index.value)
        holder.go(Int.MIN_VALUE)
        holder.shift(Int.MAX_VALUE)
        assertEquals("fallback", holder.state.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun remember_alias_exposes_the_same_contract() = runComposeUiTest {
        lateinit var holder: CycleListHolder<String>

        setContent {
            holder = rememberCycleList(persistentListOf("A", "B")) {
                initialValue = "B"
            }
        }

        waitForIdle()
        assertEquals("B", holder.state.value)
        assertEquals(1, holder.index.value)
        holder.next()
        waitForIdle()
        assertEquals("A", holder.state.value)
    }
}
