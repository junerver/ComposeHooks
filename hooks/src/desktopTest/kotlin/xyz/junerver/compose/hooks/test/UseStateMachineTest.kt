package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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
        Success,
        Error,
    }

    private enum class MachineEvent {
        Start,
        Complete,
        Fail,
        Retry,
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

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun basicStateTransition() = runComposeUiTest {
        setContent {
            val graph = createMachine<MachineState, MachineEvent, Unit> {
                initial(MachineState.Idle)

                state(MachineState.Idle) {
                    on(MachineEvent.Start) { target(MachineState.Loading) }
                }
                state(MachineState.Loading) {
                    on(MachineEvent.Complete) { target(MachineState.Success) }
                    on(MachineEvent.Fail) { target(MachineState.Error) }
                }
            }

            val holder = useStateMachine(graph)
            val stepRef = useRef(0)

            SideEffect {
                when (stepRef.current) {
                    0 -> {
                        stepRef.current = 1
                        assertEquals(MachineState.Idle, holder.currentState.value)
                        assertTrue(holder.canTransition(MachineEvent.Start))
                        assertFalse(holder.canTransition(MachineEvent.Complete))
                        holder.transition(MachineEvent.Start)
                    }
                    1 -> {
                        stepRef.current = 2
                        assertEquals(MachineState.Loading, holder.currentState.value)
                        assertTrue(holder.canTransition(MachineEvent.Complete))
                        holder.transition(MachineEvent.Complete)
                    }
                }
            }

            Text(text = "state=${holder.currentState.value}")
        }

        waitForIdle()
        onNodeWithText("state=Success").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun targetWithActionUpdatesStateAndContext() = runComposeUiTest {
        setContent {
            val graph = createMachine<MachineState, MachineEvent, Int> {
                context(0)
                initial(MachineState.Idle)

                state(MachineState.Idle) {
                    on(MachineEvent.Start) {
                        target(MachineState.Loading)
                        action { ctx, _ -> (ctx ?: 0) + 100 }
                    }
                }
                state(MachineState.Loading) {
                    on(MachineEvent.Complete) {
                        target(MachineState.Success)
                        action { ctx, _ -> (ctx ?: 0) + 50 }
                    }
                }
            }

            val holder = useStateMachine(graph)
            val stepRef = useRef(0)

            SideEffect {
                when (stepRef.current) {
                    0 -> {
                        stepRef.current = 1
                        holder.transition(MachineEvent.Start)
                    }
                    1 -> {
                        stepRef.current = 2
                        holder.transition(MachineEvent.Complete)
                    }
                }
            }

            Text(text = "state=${holder.currentState.value} ctx=${holder.context.value}")
        }

        waitForIdle()
        onNodeWithText("state=Success ctx=150").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun multipleActionOnlyEventsAccumulateContext() = runComposeUiTest {
        setContent {
            val graph = createMachine<MachineState, MachineEvent, Int> {
                context(0)
                initial(MachineState.Idle)

                state(MachineState.Idle) {
                    on(MachineEvent.Log) {
                        action { ctx, _ -> (ctx ?: 0) + 1 }
                    }
                }
            }

            val holder = useStateMachine(graph)
            val stepRef = useRef(0)

            SideEffect {
                when (stepRef.current) {
                    0 -> { stepRef.current = 1; holder.transition(MachineEvent.Log) }
                    1 -> { stepRef.current = 2; holder.transition(MachineEvent.Log) }
                    2 -> { stepRef.current = 3; holder.transition(MachineEvent.Log) }
                }
            }

            Text(text = "state=${holder.currentState.value} ctx=${holder.context.value}")
        }

        waitForIdle()
        onNodeWithText("state=Idle ctx=3").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun actionOnlyEventDoesNotChangeState() = runComposeUiTest {
        setContent {
            val graph = createMachine<MachineState, MachineEvent, Int> {
                context(0)
                initial(MachineState.Idle)

                state(MachineState.Idle) {
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

                holder.transition(MachineEvent.Log)
            }

            Text(text = "state=${holder.currentState.value} ctx=${holder.context.value}")
        }

        waitForIdle()
        // State should remain Idle, but context should increment
        onNodeWithText("state=Idle ctx=1").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun resetRestoresInitialStateAndContext() = runComposeUiTest {
        setContent {
            val graph = createMachine<MachineState, MachineEvent, Int> {
                context(100)
                initial(MachineState.Idle)

                state(MachineState.Idle) {
                    on(MachineEvent.Start) {
                        target(MachineState.Loading)
                        action { ctx, _ -> (ctx ?: 0) + 50 }
                    }
                }
            }

            val holder = useStateMachine(graph)
            val stepRef = useRef(0)

            SideEffect {
                when (stepRef.current) {
                    0 -> {
                        stepRef.current = 1
                        holder.transition(MachineEvent.Start)
                    }
                    1 -> {
                        stepRef.current = 2
                        assertEquals(MachineState.Loading, holder.currentState.value)
                        holder.reset()
                    }
                }
            }

            Text(text = "state=${holder.currentState.value} ctx=${holder.context.value}")
        }

        waitForIdle()
        onNodeWithText("state=Idle ctx=100").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun goBackReturnsToPerviousState() = runComposeUiTest {
        setContent {
            val graph = createMachine<MachineState, MachineEvent, Unit> {
                initial(MachineState.Idle)

                state(MachineState.Idle) {
                    on(MachineEvent.Start) { target(MachineState.Loading) }
                }
                state(MachineState.Loading) {
                    on(MachineEvent.Complete) { target(MachineState.Success) }
                }
            }

            val holder = useStateMachine(graph)
            val stepRef = useRef(0)

            SideEffect {
                when (stepRef.current) {
                    0 -> {
                        stepRef.current = 1
                        assertFalse(holder.canGoBack.value)
                        holder.transition(MachineEvent.Start)
                    }
                    1 -> {
                        stepRef.current = 2
                        assertTrue(holder.canGoBack.value)
                        holder.transition(MachineEvent.Complete)
                    }
                    2 -> {
                        stepRef.current = 3
                        assertEquals(MachineState.Success, holder.currentState.value)
                        holder.goBack()
                    }
                    3 -> {
                        stepRef.current = 4
                        holder.goBack()
                    }
                }
            }

            Text(text = "state=${holder.currentState.value} canGoBack=${holder.canGoBack.value}")
        }

        waitForIdle()
        onNodeWithText("state=Idle canGoBack=false").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun getAvailableEventsDeduplicatesTransitionAndActionEvents() = runComposeUiTest {
        setContent {
            val graph = createMachine<MachineState, MachineEvent, Int> {
                context(0)
                initial(MachineState.Idle)

                state(MachineState.Idle) {
                    // Same event has both target and action
                    on(MachineEvent.Start) {
                        target(MachineState.Loading)
                        action { ctx, _ -> (ctx ?: 0) + 1 }
                    }
                    // Action-only event
                    on(MachineEvent.Log) {
                        action { ctx, _ -> (ctx ?: 0) + 1 }
                    }
                }
            }

            val holder = useStateMachine(graph)
            val events = holder.getAvailableEvents()
            val hasStart = MachineEvent.Start in events
            val hasLog = MachineEvent.Log in events

            Text(text = "count=${events.size} hasStart=$hasStart hasLog=$hasLog")
        }

        waitForIdle()
        onNodeWithText("count=2 hasStart=true hasLog=true").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun cannotTransitionWithInvalidEvent() = runComposeUiTest {
        setContent {
            val graph = createMachine<MachineState, MachineEvent, Unit> {
                initial(MachineState.Idle)

                state(MachineState.Idle) {
                    on(MachineEvent.Start) { target(MachineState.Loading) }
                }
            }

            val holder = useStateMachine(graph)
            val checkedRef = useRef(false)

            SideEffect {
                if (checkedRef.current) return@SideEffect
                checkedRef.current = true

                assertFalse(holder.canTransition(MachineEvent.Complete))
                assertFalse(holder.canTransition(MachineEvent.Log))
                // Try invalid transition, state should not change
                holder.transition(MachineEvent.Complete)
            }

            Text(text = "state=${holder.currentState.value}")
        }

        waitForIdle()
        onNodeWithText("state=Idle").assertExists()
    }
}
