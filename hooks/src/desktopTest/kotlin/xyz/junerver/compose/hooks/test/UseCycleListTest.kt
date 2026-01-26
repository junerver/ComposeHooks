package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import xyz.junerver.compose.hooks.useCycleList
import xyz.junerver.compose.hooks.useState

/*
  Description: useCycleList comprehensive TDD tests
  Author: AI Assistant
  Date: 2026/1/24
  Email: junerver@gmail.com
  Version: v1.0
*/

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
                    0 -> { next(); phase = 1 } // A -> B
                    1 -> { next(); phase = 2 } // B -> C
                    2 -> { next(); phase = 3 } // C -> A (wrap)
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
                    0 -> { next(); phase = 1 }
                    1 -> { next(); phase = 2 }
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
                    0 -> { go(2); phase = 1 } // Go to C
                    1 -> { shift(-2); phase = 2 } // C -> A
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
                    0 -> { next(); phase = 1 }
                    1 -> { next(); phase = 2 }
                }
            }

            Text("value=${state.value} phase=$phase")
        }
        waitForIdle()
        onNodeWithText("value=30 phase=2").assertExists()
    }
}
