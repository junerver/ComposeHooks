package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import kotlin.properties.Delegates
import kotlinx.collections.immutable.PersistentList

/*
  Description: A Compose Hook for managing state machines
  Author: kk
  Date: 2025/6/22-9:30
  Email: kkneverlie@gmail.com
  Version: v1.0

  Update: 2025/6/24-9:40 by Junerver
  Version: v1.1
  Description: Adds support for action\context
*/

/**
 * A Compose Hook for managing state machines with context support.
 *
 * @param machineGraph The state machine graph created using [createMachine] function
 * @param maxHistorySize Maximum length of history records, defaults to 100
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
fun <S : Any, E, CTX> useStateMachine(
    machineGraph: Ref<MachineGraph<S, E, CTX>>,
    maxHistorySize: Int = 100,
): StateMachineHolder<S, E, CTX> {
    requireNotNull(machineGraph.current.initialState) {
        "must call `createMachine` first, and must call `initial`"
    }
    val (contextState, setContextState) = _useGetState(machineGraph.current.context)
    val (currentState, setState) = useGetState(machineGraph.current.initialState)
    val (undoState, setUndoState, resetUndoState, undo, _, canUndo, _) = useUndo(machineGraph.current.initialState)

    val canTransition = { event: E ->
        machineGraph.current.transitions.containsKey(currentState.value to event)
    }

    val transition = { event: E ->
        val current = currentState.value
        val nextState = machineGraph.current.transitions[current to event]
        val action = machineGraph.current.actions[current to event]
        if (action != null) {
            setContextState(action(contextState.value, event))
        }
        if (nextState != null) {
            setState(nextState)
            setUndoState(nextState)
        }
    }

    val reset = {
        setState(machineGraph.current.initialState)
        resetUndoState(machineGraph.current.initialState)
    }

    val goBack = {
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

    return StateMachineHolder(
        currentState = currentState,
        canTransition = canTransition,
        transition = transition,
        history = history,
        reset = reset,
        canGoBack = canUndo,
        goBack = goBack,
        getAvailableEvents = getAvailableEvents,
        context = contextState
    )
}

typealias Transition<S, E> = MutableMap<Pair<S, E>, S>
typealias Actions<S, E, CTX> = MutableMap<Pair<S, E>, Action<CTX, E>>

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
fun <S : Any, E, CTX> buildStateMachineGraph(init: StateMachineGraphScope<S, E, CTX>.() -> Unit): MachineGraph<S, E, CTX> {
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
 * @property actions Map of state-event pairs to action functions that update context
 */
@Stable
data class MachineGraph<S : Any, E, CTX>(
    val transitions: Transition<S, E>,
    val initialState: S,
    val context: CTX?,
    val actions: Actions<S, E, CTX>,
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
class StateMachineGraphScope<S : Any, E, CTX>() {
    val transitions: Transition<S, E> = mutableMapOf()
    val actions: Actions<S, E, CTX> = mutableMapOf()
    var initState: S by Delegates.notNull<S>()
    var context: CTX? = null

    /**
     * Sets the initial context for the state machine
     *
     * @param ctx The initial context value
     */
    fun context(ctx: CTX) {
        this.context = ctx
    }

    /**
     * Set the initial state for the state machine
     * @param state The initial state
     */
    fun initial(state: S) {
        this.initState = state
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
        description.actionMaps.forEach {
            actions[state to it.key] = it.value
        }
    }

    /**
     * Define multiple states and their possible transitions
     */
    fun states(descriptionBlock: StatesDescriptionScope<S, E, CTX>.() -> Unit) {
        val description = StatesDescriptionScope<S, E, CTX>()
        description.descriptionBlock()
        transitions.putAll(description.transitions)
        actions.putAll(description.actions)
    }

    internal fun build(): MachineGraph<S, E, CTX> = MachineGraph(transitions, initState, context, actions)
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
 * @param actionMaps Internal map storing event to action function mappings
 */
class StateDescriptionScope<S, E, CTX>(
    val eventMaps: MutableMap<E, S> = mutableMapOf(),
    val actionMaps: MutableMap<E, Action<CTX, E>> = mutableMapOf(),
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

    fun on(event: E, block: EventDescriptionScope<S, E, CTX>.() -> Unit) {
        EventDescriptionScope(eventMaps, event, actionMaps).block()
    }
}

/**
 * Scope for defining multiple states and their transitions in a more concise way
 *
 * This scope allows defining multiple states using the invoke operator syntax:
 * ```
 * states {
 *     State.IDLE { ... }
 *     State.LOADING { ... }
 * }
 * ```
 */
class StatesDescriptionScope<S, E, CTX>() {
    internal val transitions: Transition<S, E> = mutableMapOf()
    internal val actions: Actions<S, E, CTX> = mutableMapOf()

    /**
     * Invoke operator that allows using state as a function
     *
     * @param block Configuration block for the state transitions
     */
    operator fun S.invoke(block: StateTransitionScope<S, E, CTX>.() -> Unit) {
        val stateTransition = StateTransitionScope<S, E, CTX>(this, transitions, actions)
        stateTransition.block()
        stateTransition.build()
    }
}

/**
 * Scope for defining transitions from a specific state
 *
 * @param fromState The source state for transitions
 * @param transitions The global transitions map to update
 * @param actions The global actions map to update
 */
class StateTransitionScope<S, E, CTX>(val fromState: S, val transitions: Transition<S, E>, val actions: Actions<S, E, CTX>) {
    private val eventMaps: MutableMap<E, S> = mutableMapOf()
    private val actionMaps: MutableMap<E, Action<CTX, E>> = mutableMapOf()

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
        EventDescriptionScope(eventMaps, event, actionMaps).block()
    }

    internal fun build() {
        eventMaps.forEach {
            transitions[fromState to it.key] = it.value
        }
        actionMaps.forEach {
            actions[fromState to it.key] = it.value
        }
    }
}

/**
 * Action function type that updates context during state transitions
 *
 * Similar to a reducer function in Redux, it takes the previous context and event,
 * and returns the new context value.
 */
typealias Action<CTX, E> = (prevContext: CTX?, event: E) -> CTX
typealias SuspendAction<CTX, E> = suspend (prevContext: CTX?, event: E) -> CTX


/**
 * Scope for defining an event transition with target state and optional action
 *
 * @param eventMaps Map to store event to target state mappings
 * @param event The current event being configured
 * @param actionMaps Map to store event to action function mappings
 */
class EventDescriptionScope<S, E, CTX>(val eventMaps: MutableMap<E, S>, val event: E, val actionMaps: MutableMap<E, Action<CTX, E>>) {
    /**
     * Sets the target state for the current event
     *
     * @param targetState The state to transition to when the event occurs
     */
    fun target(targetState: S) {
        eventMaps[event] = targetState
    }

    /**
     * Sets the action function for the current event
     *
     * The action function is called during transition and can update the context.
     *
     * @param action The action function that updates context
     */
    fun action(action: Action<CTX, E>) {
        actionMaps[event] = action
    }

    fun action(action:SuspendAction<CTX, E>): EventDescriptionScope<S, E, CTX> {
        TODO("don't support suspend function")
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
