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
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue
import xyz.junerver.compose.hooks.createMachine
import xyz.junerver.compose.hooks.rememberStateMachine
import xyz.junerver.compose.hooks.useRef
import xyz.junerver.compose.hooks.useStateMachine
import xyz.junerver.compose.hooks.usestatemachine.StateMachineHolder

/*
  Description: Tests for useStateMachine
  Author: Junerver
  Date: 2026/1/8
  Email: junerver@gmail.com
  Version: v1.0
*/

@Suppress("DEPRECATION")
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
                    0 -> {
                        stepRef.current = 1
                        holder.transition(MachineEvent.Log)
                    }
                    1 -> {
                        stepRef.current = 2
                        holder.transition(MachineEvent.Log)
                    }
                    2 -> {
                        stepRef.current = 3
                        holder.transition(MachineEvent.Log)
                    }
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

    // ========== P0: Async Cancellation Tests ==========

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun cancelPreviousActionOnNewTransition() = runComposeUiTest {
        setContent {
            val graph = createMachine<MachineState, MachineEvent, Int> {
                context(0)
                initial(MachineState.Idle)

                state(MachineState.Idle) {
                    on(MachineEvent.Start) {
                        target(MachineState.Loading)
                        action { ctx, _ ->
                            kotlinx.coroutines.delay(200)
                            (ctx ?: 0) + 100
                        }
                    }
                }
                state(MachineState.Loading) {
                    on(MachineEvent.Complete) {
                        target(MachineState.Success)
                        action { ctx, _ -> (ctx ?: 0) + 1 }
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
                        // Immediately trigger another transition before first action completes
                        holder.transition(MachineEvent.Complete)
                    }
                }
            }

            Text(text = "state=${holder.currentState.value} ctx=${holder.context.value}")
        }

        // Wait for async actions to complete
        waitForIdle()
        Thread.sleep(300)
        waitForIdle()

        // Context should be 1 (from Complete), not 101 (from cancelled Start action)
        onNodeWithText("state=Success ctx=1").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun resetCancelsInFlightAction() = runComposeUiTest {
        setContent {
            val graph = createMachine<MachineState, MachineEvent, Int> {
                context(100)
                initial(MachineState.Idle)

                state(MachineState.Idle) {
                    on(MachineEvent.Start) {
                        target(MachineState.Loading)
                        action { ctx, _ ->
                            kotlinx.coroutines.delay(200)
                            (ctx ?: 0) + 50
                        }
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
                        // Reset immediately after transition
                        holder.reset()
                    }
                }
            }

            Text(text = "state=${holder.currentState.value} ctx=${holder.context.value}")
        }

        waitForIdle()
        Thread.sleep(300)
        waitForIdle()

        // Should be initial state/context, not affected by cancelled action
        onNodeWithText("state=Idle ctx=100").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun goBackCancelsInFlightAction() = runComposeUiTest {
        setContent {
            val graph = createMachine<MachineState, MachineEvent, Int> {
                context(0)
                initial(MachineState.Idle)

                state(MachineState.Idle) {
                    on(MachineEvent.Start) { target(MachineState.Loading) }
                }
                state(MachineState.Loading) {
                    on(MachineEvent.Complete) {
                        target(MachineState.Success)
                        action { ctx, _ ->
                            kotlinx.coroutines.delay(200)
                            (ctx ?: 0) + 100
                        }
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
                    2 -> {
                        stepRef.current = 3
                        // Go back immediately after transition with slow action
                        holder.goBack()
                    }
                }
            }

            Text(text = "state=${holder.currentState.value} ctx=${holder.context.value}")
        }

        waitForIdle()
        Thread.sleep(300)
        waitForIdle()

        // Should be Loading state with context=0, not affected by cancelled action
        onNodeWithText("state=Loading ctx=0").assertExists()
    }

    // ========== P0: Concurrent Event Tests ==========

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun transition_chain_reaches_expected_state_across_recompositions() = runComposeUiTest {
        setContent {
            val graph = createMachine<MachineState, MachineEvent, Int> {
                context(0)
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
                        holder.transition(MachineEvent.Start)
                    }
                    1 -> {
                        stepRef.current = 2
                        holder.transition(MachineEvent.Complete)
                    }
                }
            }

            Text(text = "state=${holder.currentState.value} step=${stepRef.current}")
        }

        waitForIdle()
        onNodeWithText("state=Success step=2").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun state_machine_remains_usable_after_multiple_sequential_transitions() = runComposeUiTest {
        setContent {
            val graph = createMachine<MachineState, MachineEvent, Int> {
                context(0)
                initial(MachineState.Idle)

                state(MachineState.Idle) {
                    on(MachineEvent.Start) { target(MachineState.Loading) }
                }
                state(MachineState.Loading) {
                    on(MachineEvent.Complete) { target(MachineState.Success) }
                    on(MachineEvent.Fail) { target(MachineState.Error) }
                }
                state(MachineState.Success) {
                    on(MachineEvent.Start) { target(MachineState.Loading) }
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
                    2 -> {
                        stepRef.current = 3
                        holder.transition(MachineEvent.Start)
                    }
                }
            }

            Text(text = "state=${holder.currentState.value} step=${stepRef.current}")
        }

        waitForIdle()
        onNodeWithText("state=Loading step=3").assertExists()
    }

    // ========== P0: Exception Handling Tests ==========

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun actionThrowsDoesNotCorruptState() = runComposeUiTest {
        setContent {
            val graph = createMachine<MachineState, MachineEvent, Int> {
                context(0)
                initial(MachineState.Idle)

                state(MachineState.Idle) {
                    on(MachineEvent.Start) {
                        target(MachineState.Loading)
                        action { _, _ ->
                            throw RuntimeException("Test exception")
                        }
                    }
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
                        holder.transition(MachineEvent.Start)
                    }
                    1 -> {
                        stepRef.current = 2
                        // State should have changed despite action failure
                        assertEquals(MachineState.Loading, holder.currentState.value)
                        // Context should remain unchanged
                        assertEquals(0, holder.context.value)
                        // State machine should still be usable
                        holder.transition(MachineEvent.Complete)
                    }
                }
            }

            Text(text = "state=${holder.currentState.value} ctx=${holder.context.value}")
        }

        waitForIdle()
        Thread.sleep(100)
        waitForIdle()

        onNodeWithText("state=Success ctx=0").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun actionCancelledDoesNotCrash() = runComposeUiTest {
        setContent {
            val graph = createMachine<MachineState, MachineEvent, Int> {
                context(0)
                initial(MachineState.Idle)

                state(MachineState.Idle) {
                    on(MachineEvent.Start) {
                        target(MachineState.Loading)
                        action { ctx, _ ->
                            kotlinx.coroutines.delay(1000)
                            (ctx ?: 0) + 1
                        }
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
                        // Cancel by resetting
                        holder.reset()
                    }
                }
            }

            Text(text = "state=${holder.currentState.value}")
        }

        waitForIdle()
        // Should not crash
        onNodeWithText("state=Idle").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun exceptionInActionKeepsStateMachineUsable() = runComposeUiTest {
        setContent {
            val graph = createMachine<MachineState, MachineEvent, Int> {
                context(0)
                initial(MachineState.Idle)

                state(MachineState.Idle) {
                    on(MachineEvent.Start) {
                        target(MachineState.Loading)
                        action { _, _ -> throw IllegalStateException("Boom") }
                    }
                    on(MachineEvent.Log) {
                        action { ctx, _ -> (ctx ?: 0) + 1 }
                    }
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
                        holder.transition(MachineEvent.Start)
                    }
                    1 -> {
                        stepRef.current = 2
                        // Try another transition after exception
                        holder.transition(MachineEvent.Complete)
                    }
                    2 -> {
                        stepRef.current = 3
                        holder.reset()
                    }
                    3 -> {
                        stepRef.current = 4
                        // Try action-only event
                        holder.transition(MachineEvent.Log)
                    }
                }
            }

            Text(text = "state=${holder.currentState.value} ctx=${holder.context.value}")
        }

        waitForIdle()
        Thread.sleep(100)
        waitForIdle()

        onNodeWithText("state=Idle ctx=1").assertExists()
    }

    // ========== P0: Configuration Validation Tests ==========

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun invalidMachineMissingInitialThrows() = runComposeUiTest {
        var exceptionMessage = ""

        // Test outside of setContent since createMachine is not a composable
        val result = runCatching {
            val scope = xyz.junerver.compose.hooks.StateMachineGraphScope<MachineState, MachineEvent, Unit>()
            scope.apply {
                // Missing initial() call
                state(MachineState.Idle) {
                    on(MachineEvent.Start) { target(MachineState.Loading) }
                }
            }
            scope.build()
        }

        exceptionMessage = if (result.isFailure) {
            result.exceptionOrNull()?.message ?: "unknown"
        } else {
            "no_exception"
        }

        setContent {
            Text(text = "result=$exceptionMessage")
        }

        waitForIdle()
        // Should contain "initial" in error message
        assertTrue(exceptionMessage.contains("initial", ignoreCase = true))
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun duplicateInitialCallThrows() = runComposeUiTest {
        var exceptionMessage = ""

        val result = runCatching {
            val scope = xyz.junerver.compose.hooks.StateMachineGraphScope<MachineState, MachineEvent, Unit>()
            scope.apply {
                initial(MachineState.Idle)
                initial(MachineState.Loading) // Duplicate call
            }
        }

        exceptionMessage = if (result.isFailure) {
            result.exceptionOrNull()?.message ?: "unknown"
        } else {
            "no_exception"
        }

        setContent {
            Text(text = "result=$exceptionMessage")
        }

        waitForIdle()
        assertTrue(exceptionMessage.contains("already set", ignoreCase = true))
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun duplicateContextCallThrows() = runComposeUiTest {
        var exceptionMessage = ""

        val result = runCatching {
            val scope = xyz.junerver.compose.hooks.StateMachineGraphScope<MachineState, MachineEvent, Int>()
            scope.apply {
                initial(MachineState.Idle)
                context(1)
                context(2) // Duplicate call
            }
        }

        exceptionMessage = if (result.isFailure) {
            result.exceptionOrNull()?.message ?: "unknown"
        } else {
            "no_exception"
        }

        setContent {
            Text(text = "result=$exceptionMessage")
        }

        waitForIdle()
        assertTrue(exceptionMessage.contains("already set", ignoreCase = true))
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun emptyStateMachineIsValid(): Unit = runComposeUiTest {
        setContent {
            val graph = createMachine<MachineState, MachineEvent, Unit> {
                initial(MachineState.Idle)
                // No state definitions
            }

            val holder = useStateMachine(graph)

            Text(text = "state=${holder.currentState.value}")
        }

        waitForIdle()
        onNodeWithText("state=Idle").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rapid_transitions_in_same_frame_use_latest_state() = runComposeUiTest {
        lateinit var holder: StateMachineHolder<MachineState, MachineEvent, Int>

        setContent {
            val graph = createMachine<MachineState, MachineEvent, Int> {
                context(0)
                initial(MachineState.Idle)

                state(MachineState.Idle) {
                    on(MachineEvent.Start) {
                        target(MachineState.Loading)
                        action { ctx, _ -> (ctx ?: 0) + 1 }
                    }
                }
                state(MachineState.Loading) {
                    on(MachineEvent.Complete) {
                        target(MachineState.Success)
                        action { ctx, _ -> (ctx ?: 0) + 10 }
                    }
                }
            }

            holder = useStateMachine(graph)
            Text(text = "state=${holder.currentState.value}")
        }

        waitForIdle()
        runOnIdle {
            holder.transition(MachineEvent.Start)
            holder.transition(MachineEvent.Complete)
            assertEquals(MachineState.Success, holder.currentState.value)
        }
        onNodeWithText("state=Success").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun history_updates_after_transitions_and_backtracking() = runComposeUiTest {
        lateinit var holder: StateMachineHolder<MachineState, MachineEvent, Unit>

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

            holder = useStateMachine(graph)
            Text(text = "history=${holder.history.value.joinToString(">")} state=${holder.currentState.value}")
        }

        waitForIdle()
        runOnIdle {
            holder.transition(MachineEvent.Start)
            holder.transition(MachineEvent.Complete)
        }
        waitForIdle()
        assertEquals(listOf(MachineState.Idle, MachineState.Loading, MachineState.Success), holder.history.value)

        runOnIdle { holder.goBack() }
        waitForIdle()
        assertEquals(listOf(MachineState.Idle, MachineState.Loading), holder.history.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun remember_state_machine_alias_keeps_holder_and_action_identity() = runComposeUiTest {
        var marker by mutableStateOf(0)
        lateinit var holder: StateMachineHolder<MachineState, MachineEvent, Unit>
        var initialHolder: StateMachineHolder<MachineState, MachineEvent, Unit>? = null

        setContent {
            val graph = createMachine<MachineState, MachineEvent, Unit> {
                initial(MachineState.Idle)
                state(MachineState.Idle) {
                    on(MachineEvent.Start) { target(MachineState.Loading) }
                }
            }

            holder = rememberStateMachine(graph)
            if (initialHolder == null) initialHolder = holder
            Text(text = "marker=$marker state=${holder.currentState.value}")
        }

        waitForIdle()
        val first = initialHolder ?: error("holder was not composed")
        val initialTransition = first.transition
        val initialCanTransition = first.canTransition
        val initialReset = first.reset

        marker = 1
        waitForIdle()

        assertSame(first, holder)
        assertSame(initialTransition, holder.transition)
        assertSame(initialCanTransition, holder.canTransition)
        assertSame(initialReset, holder.reset)
        assertSame(first.currentState, holder.currentState)
        assertSame(first.history, holder.history)
    }
}
