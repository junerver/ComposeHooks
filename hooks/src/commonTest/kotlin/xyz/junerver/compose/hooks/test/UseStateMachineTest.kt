package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import xyz.junerver.compose.hooks.createMachine
import xyz.junerver.compose.hooks.useRef
import xyz.junerver.compose.hooks.useStateMachine

/*
  Description: Tests for useStateMachine
  Author: Junerver
  Date: 2026/1/8
  Email: junerver@gmail.com
  Version: v1.0
*/

class UseStateMachineTest {
    private enum class MachineState {
        Idle,
        Loading,
    }

    private enum class MachineEvent {
        Start,
        Log,
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun actionOnlyEventIsAvailableAndCanTransition() = runComposeUiTest {
        setContent {
            val graph = createMachine<MachineState, MachineEvent, Int> {
                context(0)
                initial(MachineState.Idle)

                state(MachineState.Idle) {
                    on(MachineEvent.Start) {
                        target(MachineState.Loading)
                        action { ctx, _ -> (ctx ?: 0) + 10 }
                    }
                    on(MachineEvent.Log) {
                        action { ctx, _ -> (ctx ?: 0) + 1 }
                    }
                }
            }

            val holder = useStateMachine(graph)
            val firedRef = useRef(false)

            SideEffect {
                if (firedRef.current) return@SideEffect
                firedRef.current = true

                assertEquals(MachineState.Idle, holder.currentState.value)
                assertTrue(MachineEvent.Log in holder.getAvailableEvents())
                assertTrue(holder.canTransition(MachineEvent.Log))
                holder.transition(MachineEvent.Log)
            }

            Text(text = "state=${holder.currentState.value} ctx=${holder.context.value}")
        }

        waitForIdle()
        onNodeWithText("state=Idle ctx=1").assertExists()
    }
}
