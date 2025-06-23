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
*/

/**
 *
 * @param initialState The initial state
 * @param transitions State transition mapping table, defining transitions from (current state, event) to new state
 * @param maxHistorySize Maximum length of history records, defaults to 100
 * @return [StateMachineHolder] containing current state and control functions
 *
 * @example
 * ```kotlin
 * enum class State { IDLE, LOADING, SUCCESS, ERROR }
 * enum class Event { START, SUCCESS, ERROR, RESET }
 *
 * // Using traditional Map approach
 * val transitions = mapOf(
 *     (State.IDLE to Event.START) to State.LOADING,
 *     (State.LOADING to Event.SUCCESS) to State.SUCCESS,
 *     (State.LOADING to Event.ERROR) to State.ERROR,
 *     (State.SUCCESS to Event.RESET) to State.IDLE,
 *     (State.ERROR to Event.RESET) to State.IDLE
 * )
 *
 * // Or using the more readable DSL approach
 * val transitions = buildStateMachineGraph<State, Event> {
 *     state(State.IDLE) {
 *         Event.START transitionTo State.LOADING
 *     }
 *     state(State.LOADING) {
 *         Event.SUCCESS transitionTo State.SUCCESS
 *         Event.ERROR transitionTo State.ERROR
 *     }
 *     state(State.SUCCESS) {
 *         Event.RESET transitionTo State.IDLE
 *     }
 *     state(State.ERROR) {
 *         Event.RESET transitionTo State.IDLE
 *     }
 * }
 *
 * val stateMachine = useStateMachine(State.IDLE, transitions)
 * ```
 */
@Composable
fun <S : Any, E> useStateMachine(machineGraph: Ref<MachineGraph<S, E>>, maxHistorySize: Int = 100): StateMachineHolder<S, E> {
    requireNotNull(machineGraph.current.initialState) {
        "must call `createMachine` first, and must call `initial`"
    }
    val (currentState, setState) = useGetState(machineGraph.current.initialState)
    val (undoState, setUndoState, resetUndoState, undo, _, canUndo, _) = useUndo(machineGraph.current.initialState)

    val canTransition = { event: E ->
        machineGraph.current.transitions.containsKey(currentState.value to event)
    }

    val transition = { event: E ->
        val current = currentState.value
        val nextState = machineGraph.current.transitions[current to event]
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
        getAvailableEvents = getAvailableEvents
    )
}

typealias Transition<S, E> = MutableMap<Pair<S, E>, S>

/**
 * State machine graph builder function that provides a DSL for creating state transitions
 *
 * This function offers a more readable and maintainable way to define state machine transitions
 * compared to manually creating a Map. It uses a builder pattern with a DSL that clearly
 * expresses the relationship between states and events.
 *
 * @param init Configuration block for building the state graph
 * @return A transition map that can be used with useStateMachine
 *
 * @example
 * ```kotlin
 * val transitions = buildStateMachineGraph<MyState, MyEvent> {
 *     state(MyState.INITIAL) {
 *         MyEvent.START transitionTo MyState.PROCESSING
 *     }
 *     state(MyState.PROCESSING) {
 *         MyEvent.COMPLETE transitionTo MyState.DONE
 *         MyEvent.ERROR transitionTo MyState.FAILED
 *     }
 * }
 * ```
 */
fun <S : Any, E> buildStateMachineGraph(init: StateMachineGraphScope<S, E>.() -> Unit): MachineGraph<S, E> {
    val graph = StateMachineGraphScope<S, E>()
    graph.init()
    return graph.build()
}

@Stable
data class MachineGraph<S : Any, E>(val transitions: Transition<S, E>, val initialState: S)

@Composable
fun <S : Any, E> createMachine(init: StateMachineGraphScope<S, E>.() -> Unit): Ref<MachineGraph<S, E>> = useCreation {
    buildStateMachineGraph(init)
}

/**
 * State machine graph builder scope that provides DSL for defining state machine transitions
 *
 * This scope enables a fluent API for defining state transitions in a clear and readable way.
 * Each state can define multiple event handlers that specify which state to transition to.
 *
 * @param transitions Internal map storing the state transitions
 */
class StateMachineGraphScope<S : Any, E>() {
    val transitions: MutableMap<Pair<S, E>, S> = mutableMapOf()
    var initState: S by Delegates.notNull<S>()

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
    fun state(state: S, descriptionBlock: StateDescriptionScope<S, E>.() -> Unit) {
        val description = StateDescriptionScope<S, E>()
        descriptionBlock(description)
        description.eventMaps.forEach {
            transitions[state to it.key] = it.value
        }
    }

    /**
     * Define multiple states and their possible transitions
     */
    fun states(descriptionBlock: StatesDescriptionScope<S, E>.() -> Unit) {
        val description = StatesDescriptionScope<S, E>()
        description.descriptionBlock()
        transitions.putAll(description.transitions)
    }

    internal fun build(): MachineGraph<S, E> = MachineGraph(transitions, initState)
}

/**
 * State description scope for defining event transitions within a state
 *
 * This scope provides an infix function style for defining transitions that reads naturally:
 * `Event.START transitionTo State.LOADING`
 *
 * @param eventMaps Internal map storing event to state mappings
 */
class StateDescriptionScope<S, E>(val eventMaps: MutableMap<E, S> = mutableMapOf()) {
    /**
     * Infix function to define state transition for an event
     *
     * @receiver The event that triggers the transition
     * @param targetState The target state to transition to
     */
    infix fun E.transitionTo(targetState: S) {
        eventMaps[this] = targetState
    }

    fun on(event: E, block: EventDescriptionScope<S, E>.() -> Unit) {
        EventDescriptionScope(eventMaps, event).block()
    }
}

class StatesDescriptionScope<S, E>() {
    internal val transitions: MutableMap<Pair<S, E>, S> = mutableMapOf()

    operator fun S.invoke(block: StateTransitionScope<S, E>.() -> Unit) {
        val stateTransition = StateTransitionScope(this, transitions)
        stateTransition.block()
        stateTransition.build()
    }
}

class StateTransitionScope<S, E>(val fromState: S, val transitions: MutableMap<Pair<S, E>, S>) {
    private val eventMaps: MutableMap<E, S> = mutableMapOf()

    infix fun E.target(targetState: S) {
        eventMaps[this] = targetState
    }

    fun on(event: E, block: EventDescriptionScope<S, E>.() -> Unit) {
        EventDescriptionScope(eventMaps, event).block()
    }

    internal fun build() {
        eventMaps.forEach {
            transitions[fromState to it.key] = it.value
        }
    }
}

class EventDescriptionScope<S, E>(val eventMaps: MutableMap<E, S>, val event: E) {
    fun target(targetState: S) {
        eventMaps[event] = targetState
    }
}

/**
 * State machine holder containing current state and all control functions
 *
 * @property currentState Current state as a deferred read State
 * @property canTransition Check if the specified event transition can be executed
 * @property transition Execute state transition
 * @property history State history records as a deferred read State of PersistentList
 * @property reset Reset to initial state and clear history
 * @property canGoBack Check if can go back to previous state
 * @property goBack Go back to previous state
 * @property getAvailableEvents Get list of available events for current state
 */
data class StateMachineHolder<S : Any, E>(
    val currentState: State<S>,
    val canTransition: (E) -> Boolean,
    val transition: (E) -> Unit,
    val history: State<PersistentList<S>>,
    val reset: () -> Unit,
    val canGoBack: State<Boolean>,
    val goBack: () -> Unit,
    val getAvailableEvents: () -> List<E>,
)
