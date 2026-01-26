package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.useUndo

/*
  Description: useUndo comprehensive TDD tests
  Author: AI Assistant
  Date: 2026/1/24
  Email: junerver@gmail.com
  Version: v1.0
*/

class UseUndoTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun initial_present_is_set() = runComposeUiTest {
        setContent {
            val (state) = useUndo("initial")
            Text("present=${state.value.present}")
        }
        waitForIdle()
        onNodeWithText("present=initial").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun initial_past_is_empty() = runComposeUiTest {
        setContent {
            val (state) = useUndo("initial")
            Text("pastSize=${state.value.past.size}")
        }
        waitForIdle()
        onNodeWithText("pastSize=0").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun initial_future_is_empty() = runComposeUiTest {
        setContent {
            val (state) = useUndo("initial")
            Text("futureSize=${state.value.future.size}")
        }
        waitForIdle()
        onNodeWithText("futureSize=0").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun setValue_updates_present() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val (state, setValue) = useUndo("initial")

            SideEffect {
                if (!fired) {
                    fired = true
                    setValue("updated")
                }
            }

            Text("present=${state.value.present}")
        }
        waitForIdle()
        onNodeWithText("present=updated").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun setValue_adds_to_past() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val (state, setValue) = useUndo("initial")

            SideEffect {
                if (!fired) {
                    fired = true
                    setValue("updated")
                }
            }

            Text("pastSize=${state.value.past.size} pastLast=${state.value.past.lastOrNull()}")
        }
        waitForIdle()
        onNodeWithText("pastSize=1 pastLast=initial").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun setValue_clears_future() = runComposeUiTest {
        setContent {
            var phase by useState(0)
            val (state, setValue, _, undo) = useUndo("a")

            SideEffect {
                when (phase) {
                    0 -> {
                        setValue("b")
                        phase = 1
                    }
                    1 -> {
                        undo() // Now future has "b"
                        phase = 2
                    }
                    2 -> {
                        setValue("c") // Should clear future
                        phase = 3
                    }
                }
            }

            Text("futureSize=${state.value.future.size} phase=$phase")
        }
        waitForIdle()
        onNodeWithText("futureSize=0 phase=3").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun undo_restores_previous() = runComposeUiTest {
        setContent {
            var phase by useState(0)
            val (state, setValue, _, undo) = useUndo("a")

            SideEffect {
                when (phase) {
                    0 -> {
                        setValue("b")
                        phase = 1
                    }
                    1 -> {
                        undo()
                        phase = 2
                    }
                }
            }

            Text("present=${state.value.present} phase=$phase")
        }
        waitForIdle()
        onNodeWithText("present=a phase=2").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun undo_adds_to_future() = runComposeUiTest {
        setContent {
            var phase by useState(0)
            val (state, setValue, _, undo) = useUndo("a")

            SideEffect {
                when (phase) {
                    0 -> {
                        setValue("b")
                        phase = 1
                    }
                    1 -> {
                        undo()
                        phase = 2
                    }
                }
            }

            Text("futureFirst=${state.value.future.firstOrNull()} phase=$phase")
        }
        waitForIdle()
        onNodeWithText("futureFirst=b phase=2").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun redo_restores_undone() = runComposeUiTest {
        setContent {
            var phase by useState(0)
            val (state, setValue, _, undo, redo) = useUndo("a")

            SideEffect {
                when (phase) {
                    0 -> {
                        setValue("b")
                        phase = 1
                    }
                    1 -> {
                        undo()
                        phase = 2
                    }
                    2 -> {
                        redo()
                        phase = 3
                    }
                }
            }

            Text("present=${state.value.present} phase=$phase")
        }
        waitForIdle()
        onNodeWithText("present=b phase=3").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun canUndo_is_false_initially() = runComposeUiTest {
        setContent {
            val holder = useUndo("initial")
            Text("canUndo=${holder.canUndo.value}")
        }
        waitForIdle()
        onNodeWithText("canUndo=false").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun canUndo_is_true_after_setValue() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val holder = useUndo("initial")

            SideEffect {
                if (!fired) {
                    fired = true
                    holder.setValue("updated")
                }
            }

            Text("canUndo=${holder.canUndo.value}")
        }
        waitForIdle()
        onNodeWithText("canUndo=true").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun canRedo_is_false_initially() = runComposeUiTest {
        setContent {
            val holder = useUndo("initial")
            Text("canRedo=${holder.canRedo.value}")
        }
        waitForIdle()
        onNodeWithText("canRedo=false").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun canRedo_is_true_after_undo() = runComposeUiTest {
        setContent {
            var phase by useState(0)
            val holder = useUndo("a")

            SideEffect {
                when (phase) {
                    0 -> {
                        holder.setValue("b")
                        phase = 1
                    }
                    1 -> {
                        holder.undo()
                        phase = 2
                    }
                }
            }

            Text("canRedo=${holder.canRedo.value} phase=$phase")
        }
        waitForIdle()
        onNodeWithText("canRedo=true phase=2").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun resetValue_clears_history() = runComposeUiTest {
        setContent {
            var phase by useState(0)
            val holder = useUndo("a")

            SideEffect {
                when (phase) {
                    0 -> {
                        holder.setValue("b")
                        phase = 1
                    }
                    1 -> {
                        holder.setValue("c")
                        phase = 2
                    }
                    2 -> {
                        holder.resetValue("reset")
                        phase = 3
                    }
                }
            }

            val state = holder.undoState.value
            Text("present=${state.present} pastSize=${state.past.size} futureSize=${state.future.size} phase=$phase")
        }
        waitForIdle()
        onNodeWithText("present=reset pastSize=0 futureSize=0 phase=3").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun undo_on_empty_past_does_nothing() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val holder = useUndo("initial")

            SideEffect {
                if (!fired) {
                    fired = true
                    holder.undo() // Should do nothing
                }
            }

            Text("present=${holder.undoState.value.present}")
        }
        waitForIdle()
        onNodeWithText("present=initial").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun redo_on_empty_future_does_nothing() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val holder = useUndo("initial")

            SideEffect {
                if (!fired) {
                    fired = true
                    holder.redo() // Should do nothing
                }
            }

            Text("present=${holder.undoState.value.present}")
        }
        waitForIdle()
        onNodeWithText("present=initial").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun multiple_undo_redo_sequence() = runComposeUiTest {
        setContent {
            var phase by useState(0)
            val holder = useUndo("a")

            SideEffect {
                when (phase) {
                    0 -> {
                        holder.setValue("b")
                        phase = 1
                    }
                    1 -> {
                        holder.setValue("c")
                        phase = 2
                    }
                    2 -> {
                        holder.setValue("d")
                        phase = 3
                    }
                    3 -> {
                        holder.undo()
                        phase = 4
                    } // d -> c
                    4 -> {
                        holder.undo()
                        phase = 5
                    } // c -> b
                    5 -> {
                        holder.redo()
                        phase = 6
                    } // b -> c
                }
            }

            Text("present=${holder.undoState.value.present} phase=$phase")
        }
        waitForIdle()
        onNodeWithText("present=c phase=6").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun setValue_same_value_does_nothing() = runComposeUiTest {
        setContent {
            var phase by useState(0)
            val holder = useUndo("same")

            SideEffect {
                when (phase) {
                    0 -> {
                        holder.setValue("same") // Same value, should not add to past
                        phase = 1
                    }
                }
            }

            Text("pastSize=${holder.undoState.value.past.size} phase=$phase")
        }
        waitForIdle()
        // Note: The implementation uses reference equality (===), so same string literal won't add to past
        onNodeWithText("pastSize=0 phase=1").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun works_with_integer_values() = runComposeUiTest {
        setContent {
            var phase by useState(0)
            val holder = useUndo(0)

            SideEffect {
                when (phase) {
                    0 -> {
                        holder.setValue(1)
                        phase = 1
                    }
                    1 -> {
                        holder.setValue(2)
                        phase = 2
                    }
                    2 -> {
                        holder.undo()
                        phase = 3
                    }
                }
            }

            Text("present=${holder.undoState.value.present} phase=$phase")
        }
        waitForIdle()
        onNodeWithText("present=1 phase=3").assertExists()
    }
}
