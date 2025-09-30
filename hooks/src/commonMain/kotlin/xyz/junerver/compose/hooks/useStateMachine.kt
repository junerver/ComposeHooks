@file:Suppress("unused")

package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import kotlin.properties.Delegates
import kotlinx.collections.immutable.PersistentList
import kotlinx.coroutines.CoroutineScope

/*
  Description: A Compose Hook for managing state machines
  Author: kk
  Date: 2025/6/22-9:30
  Email: kkneverlie@gmail.com
  Version: v1.0

  Update: 2025/6/24-9:40 by Junerver
  Version: v1.1
  Description: Adds support for action\context

  Update: 2025/6/26-10:11 by Junerver
  Version: v1.1.1
  Description: Refactor action function
*/

/**
 * Type alias for a map of state-event pairs to target states
 *
 * @param S The state type
 * @param E The event type
 */
typealias Transition<S, E> = MutableMap<Pair<S, E>, S>

/**
 * Type alias for a map of state-event pairs to suspend action functions
 *
 * @param S The state type
 * @param E The event type
 * @param CTX The context type
 */
typealias SuspendActions<S, E, CTX> = MutableMap<Pair<S, E>, SuspendAction<CTX, E>>

/**
 * Action function type that updates context during state transitions
 *
 * Similar to a reducer function in Redux, it takes the previous context and event,
 * and returns the new context value. This is a suspend function that runs in a CoroutineScope.
 *
 * @param CTX The type of the context
 * @param E The type of the event
 */
typealias SuspendAction<CTX, E> = suspend CoroutineScope.(prevContext: CTX?, event: E) -> CTX

/**
 * A Compose Hook for managing state machines with context support.
 *
 * @param machineGraph The state machine graph created using [createMachine] function
 * @return [StateMachineHolder] containing current state, context and control functions
 *
 * @example
 * ```kotlin
 * enum class State { IDLE, LOADING, SUCCESS, ERROR }
 * enum class Event { START, SUCCESS, ERROR, RETRY }
 *
 * // Create a state machine with Int context
 * val machineGraph = createMachine<State, Event, Int> {
 *     // Setup initial context
 *     context(1)
 *     // Setup initial state
 *     initial(State.IDLE)
 *
 *     state(State.IDLE) {
 *         on(Event.START) {
 *             target(State.LOADING)
 *             action { ctx, e ->
 *                 // Update context based on event
 *                 ctx + 1
 *             }
 *         }
 *     }
 *
 *     states {
 *         State.LOADING {
 *             on(Event.SUCCESS) {
 *                 target(State.SUCCESS)
 *                 action { ctx, e -> ctx + 1 }
 *             }
 *             on(Event.ERROR) { target(State.ERROR) }
 *         }
 *         State.SUCCESS {
 *             on(Event.START) { target(State.LOADING) }
 *         }
 *         State.ERROR {
 *             on(Event.RETRY) { target(State.LOADING) }
 *         }
 *     }
 * }
 *
 * // Use the state machine with destructuring declaration
 * val (
 *     currentState,
 *     canTransition,
 *     transition,
 *     history,
 *     reset,
 *     canGoBack,
 *     goBack,
 *     getAvailableEvents,
 *     context
 * ) = useStateMachine(machineGraph)
 * ```
 */
@Composable
fun <S : Any, E, CTX> useStateMachine(machineGraph: Ref<MachineGraph<S, E, CTX>>): StateMachineHolder<S, E, CTX> {
    requireNotNull(machineGraph.current.initialState) {
        "must call `createMachine` first, and must call `initial`"
    }
    val (contextState, setContextState) = _useGetState(machineGraph.current.context)
    val (currentState, setState) = useGetState(machineGraph.current.initialState)
    val (undoState, setUndoState, resetUndoState, undo, _, canUndo, _) = useUndo(machineGraph.current.initialState)

    val (runAction, cancelAction, _) = useCancelableAsync()
    val transitionVersionRef = useRef(0L)

    val canTransition = { event: E ->
        machineGraph.current.transitions.containsKey(currentState.value to event)
    }

    val transition = transition@{ event: E ->
        val current = currentState.value
        val nextState = machineGraph.current.transitions[current to event]
        val suspendAction = machineGraph.current.suspendActions[current to event]

        if (nextState == null && suspendAction == null) {
            return@transition
        }

        cancelAction()
        transitionVersionRef.current += 1
        val transitionVersion = transitionVersionRef.current
        val contextBefore = contextState.value

        if (nextState != null) {
            setState(nextState)
            setUndoState(nextState)
        }

        if (suspendAction != null) {
            runAction {
                val newContext = suspendAction(contextBefore, event)
                if (transitionVersionRef.current == transitionVersion) {
                    setContextState(newContext)
                }
            }
        }
    }

    val reset = {
        cancelAction()
        transitionVersionRef.current += 1
        val graph = machineGraph.current
        setState(graph.initialState)
        resetUndoState(graph.initialState)
        setContextState(graph.context)
    }

    val goBack = goBack@{
        if (!canUndo.value) return@goBack
        cancelAction()
        transitionVersionRef.current += 1
        undo()
        setState(undoState.value.present)
    }

    val getAvailableEvents = {
        machineGraph.current.transitions.keys
            .filter { it.first == currentState.value }
            .map { it.second }
    }

    val history = useState {
        undoState.value.past.add(undoState.value.present)
    }

    return remember {
        StateMachineHolder(
            currentState = currentState,
            canTransition = canTransition,
            transition = transition,
            history = history,
            reset = reset,
            canGoBack = canUndo,
            goBack = goBack,
            getAvailableEvents = getAvailableEvents,
            context = contextState,
        )
    }
}

/**
 * State machine graph builder function that provides a DSL for creating state transitions
 *
 * This function offers a more readable and maintainable way to define state machine transitions
 * compared to manually creating a Map. It uses a builder pattern with a DSL that clearly
 * expresses the relationship between states and events.
 *
 * @param init Configuration block for building the state graph
 * @return A MachineGraph that can be used with useStateMachine
 *
 * @example
 * ```kotlin
 * val graph = buildStateMachineGraph<MyState, MyEvent, MyContext> {
 *     context(initialContext) // Optional context
 *     initial(MyState.INITIAL) // Required initial state
 *
 *     state(MyState.INITIAL) {
 *         on(MyEvent.START) {
 *             target(MyState.PROCESSING)
 *             action { ctx, event -> updatedContext } // Optional action
 *         }
 *     }
 *
 *     states {
 *         MyState.PROCESSING {
 *             on(MyEvent.COMPLETE) { target(MyState.DONE) }
 *             on(MyEvent.ERROR) { target(MyState.FAILED) }
 *         }
 *     }
 * }
 * ```
 */
private fun <S : Any, E, CTX> buildStateMachineGraph(init: StateMachineGraphScope<S, E, CTX>.() -> Unit): MachineGraph<S, E, CTX> {
    val graph = StateMachineGraphScope<S, E, CTX>()
    graph.init()
    return graph.build()
}

/**
 * Represents a state machine graph with transitions, initial state, context, and actions.
 *
 * @property transitions Map of state-event pairs to target states
 * @property initialState The initial state of the state machine
 * @property context Optional context data that can be updated during transitions
 * @property suspendActions Map of state-event pairs to action functions that update context
 */
@Stable
data class MachineGraph<S : Any, E, CTX> internal constructor(
    internal val transitions: Transition<S, E>,
    internal val initialState: S,
    internal val context: CTX?,
    internal val suspendActions: SuspendActions<S, E, CTX>,
)

/**
 * Creates a state machine with the given configuration.
 *
 * This is the recommended way to create a state machine for use with useStateMachine.
 *
 * @param init Configuration block for building the state machine
 * @return A reference to the created MachineGraph
 *
 * @example
 * ```kotlin
 * val machineGraph = createMachine<State, Event, Int> {
 *     context(1) // Initial context
 *     initial(State.IDLE) // Initial state
 *
 *     // Define transitions
 *     state(State.IDLE) {
 *         on(Event.START) {
 *             target(State.LOADING)
 *             action { ctx, e -> ctx + 1 }
 *         }
 *     }
 * }
 * ```
 */
@Composable
fun <S : Any, E, CTX> createMachine(init: StateMachineGraphScope<S, E, CTX>.() -> Unit): Ref<MachineGraph<S, E, CTX>> = useCreation {
    buildStateMachineGraph(init)
}

/**
 * State machine graph builder scope that provides DSL for defining state machine transitions
 *
 * This scope enables a fluent API for defining state transitions in a clear and readable way.
 * Each state can define multiple event handlers that specify which state to transition to.
 * It also supports defining context and actions that update the context during transitions.
 */
class StateMachineGraphScope<S : Any, E, CTX> internal constructor() {
    private val transitions: Transition<S, E> = mutableMapOf()
    private val suspendActions: SuspendActions<S, E, CTX> = mutableMapOf()
    private var initState: S by Delegates.notNull()
    private var context: CTX? = null

    private var hasCalledContext = false
    private var hasCalledInitial = false

    /**
     * Sets the initial context for the state machine
     *
     * @param ctx The initial context value
     */
    fun context(ctx: CTX) {
        if (!hasCalledContext) {
            this.context = ctx
            hasCalledContext = true
        } else {
            error("Context already set")
        }
    }

    /**
     * Set the initial state for the state machine
     * @param state The initial state
     */
    fun initial(state: S) {
        if (!hasCalledInitial) {
            this.initState = state
            hasCalledInitial = true
        } else {
            error("Initial state already set")
        }
    }

    /**
     * Define a state and its possible transitions
     *
     * @param state The state to define transitions for
     * @param descriptionBlock Block that defines the events and their target states
     */
    fun state(state: S, descriptionBlock: StateDescriptionScope<S, E, CTX>.() -> Unit) {
        val description = StateDescriptionScope<S, E, CTX>()
        descriptionBlock(description)
        description.eventMaps.forEach {
            transitions[state to it.key] = it.value
        }
        description.actionAsyncMap.forEach {
            suspendActions[state to it.key] = it.value
        }
    }

    /**
     * Define multiple states and their possible transitions
     *
     * @param descriptionBlock Block that defines multiple states and their transitions
     */
    fun states(descriptionBlock: StatesDescriptionScope<S, E, CTX>.() -> Unit) {
        val description = StatesDescriptionScope<S, E, CTX>()
        description.descriptionBlock()
        transitions.putAll(description.transitions)
        suspendActions.putAll(description.suspendActions)
    }

    internal fun build(): MachineGraph<S, E, CTX> {
        require(hasCalledInitial) { "Please call initial() before building the machine" }
        return MachineGraph(transitions, initState, context, suspendActions)
    }
}

/**
 * State description scope for defining event transitions within a state
 *
 * This scope provides two ways to define transitions:
 * 1. Infix function style: `Event.START transitionTo State.LOADING`
 * 2. Block style with on() function: `on(Event.START) { target(State.LOADING) }`
 *
 * The block style also supports defining actions that update context during transitions.
 *
 * @param eventMaps Internal map storing event to state mappings
 * @param actionAsyncMap Internal map storing event to action function mappings
 */
class StateDescriptionScope<S, E, CTX> internal constructor(
    internal val eventMaps: MutableMap<E, S> = mutableMapOf(),
    internal val actionAsyncMap: MutableMap<E, SuspendAction<CTX, E>> = mutableMapOf(),
) {
    /**
     * Infix function to define state transition for an event
     *
     * @receiver The event that triggers the transition
     * @param targetState The target state to transition to
     */
    infix fun E.transitionTo(targetState: S) {
        eventMaps[this] = targetState
    }

    /**
     * Define a transition for an event with a configuration block
     *
     * @param event The event that triggers the transition
     * @param block Configuration block for the event transition
     */
    fun on(event: E, block: EventDescriptionScope<S, E, CTX>.() -> Unit) {
        EventDescriptionScope(eventMaps, event, actionAsyncMap).block()
    }
}

/**
 * Scope for defining multiple states and their transitions in a more concise way
 *
 * This scope allows defining multiple states using the invoke operator syntax:
 * ```kotlin
 * states {
 *     State.IDLE { ... }
 *     State.LOADING { ... }
 * }
 * ```
 *
 * @param transitions Internal map storing state transitions
 * @param suspendActions Internal map storing state action functions
 */
class StatesDescriptionScope<S, E, CTX> internal constructor() {
    internal val transitions: Transition<S, E> = mutableMapOf()
    internal val suspendActions: SuspendActions<S, E, CTX> = mutableMapOf()

    /**
     * Invoke operator that allows using state as a function
     *
     * @param block Configuration block for the state transitions
     */
    operator fun S.invoke(block: StateTransitionScope<S, E, CTX>.() -> Unit) {
        val stateTransition = StateTransitionScope<S, E, CTX>(this, transitions, suspendActions)
        stateTransition.block()
        stateTransition.build()
    }
}

/**
 * Scope for defining transitions from a specific state
 *
 * @param fromState The source state for transitions
 * @param transitions The global transitions map to update
 * @param suspendActions The global actions map to update
 */
class StateTransitionScope<S, E, CTX> internal constructor(
    internal val fromState: S,
    internal val transitions: Transition<S, E>,
    internal val suspendActions: SuspendActions<S, E, CTX>,
) {
    private val eventMaps: MutableMap<E, S> = mutableMapOf()
    private val actionAsyncMap: MutableMap<E, SuspendAction<CTX, E>> = mutableMapOf()

    /**
     * Infix function to define a transition target state
     *
     * @param targetState The target state to transition to
     */
    infix fun E.target(targetState: S) {
        eventMaps[this] = targetState
    }

    /**
     * Define a transition with an event and optional action
     *
     * @param event The event that triggers the transition
     * @param block Configuration block for the event transition
     */
    fun on(event: E, block: EventDescriptionScope<S, E, CTX>.() -> Unit) {
        EventDescriptionScope(eventMaps, event, actionAsyncMap).block()
    }

    internal fun build() {
        eventMaps.forEach {
            transitions[fromState to it.key] = it.value
        }
        actionAsyncMap.forEach {
            suspendActions[fromState to it.key] = it.value
        }
    }
}

/**
 * Scope for defining an event transition with target state and optional action
 *
 * This scope provides methods to configure what happens when an event occurs,
 * including which state to transition to and what actions to perform.
 *
 * @param S The state type
 * @param E The event type
 * @param CTX The context type
 * @param eventMaps Map to store event to target state mappings
 * @param event The current event being configured
 * @param actionAsyncMap Map to store event to action function mappings
 */
class EventDescriptionScope<S, E, CTX> internal constructor(
    internal val eventMaps: MutableMap<E, S>,
    internal val event: E,
    internal val actionAsyncMap: MutableMap<E, SuspendAction<CTX, E>>,
) {
    /**
     * Sets the target state for the current event
     *
     * @param targetState The state to transition to when the event occurs
     */
    fun target(targetState: S) {
        eventMaps[event] = targetState
    }

    /**
     * Sets the suspend action function for the current event
     *
     * The suspend action function is called during transition and can update the context.
     */
    fun action(action: SuspendAction<CTX, E>) {
        actionAsyncMap[event] = action
    }
}

/**
 * State machine holder containing current state, context and all control functions
 *
 * @property currentState Current state as a deferred read State
 * @property canTransition Check if the specified event transition can be executed
 * @property transition Execute state transition with the given event
 * @property history State history records as a deferred read State of PersistentList
 * @property reset Reset to initial state and clear history
 * @property canGoBack Check if can go back to previous state
 * @property goBack Go back to previous state
 * @property getAvailableEvents Get list of available events for current state
 * @property context Current context value as a deferred read State
 */
@Stable
data class StateMachineHolder<S : Any, E, CTX>(
    val currentState: State<S>,
    val canTransition: (E) -> Boolean,
    val transition: (E) -> Unit,
    val history: State<PersistentList<S>>,
    val reset: () -> Unit,
    val canGoBack: State<Boolean>,
    val goBack: () -> Unit,
    val getAvailableEvents: () -> List<E>,
    val context: State<CTX?>,
)
