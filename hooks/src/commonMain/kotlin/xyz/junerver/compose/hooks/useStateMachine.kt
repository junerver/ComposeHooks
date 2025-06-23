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
fun <S : Any, E, CTX> useStateMachine(machineGraph: Ref<MachineGraph<S, E, CTX>>, maxHistorySize: Int = 100): StateMachineHolder<S, E> {
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
        val action = machineGraph.current.actions[current to event]
        if (action != null) {
            action(event)
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
        getAvailableEvents = getAvailableEvents
    )
}

typealias Transition<S, E> = MutableMap<Pair<S, E>, S>
typealias Actions<S, E> = MutableMap<Pair<S, E>, Action<E>>

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
fun <S : Any, E, CTX> buildStateMachineGraph(init: StateMachineGraphScope<S, E, CTX>.() -> Unit): MachineGraph<S, E, CTX> {
    val graph = StateMachineGraphScope<S, E, CTX>()
    graph.init()
    return graph.build()
}

@Stable
data class MachineGraph<S : Any, E, CTX>(
    val transitions: Transition<S, E>,
    val initialState: S,
    val context: CTX?,
    val actions: Actions<S, E>,
)

@Composable
fun <S : Any, E, CTX> createMachine(init: StateMachineGraphScope<S, E, CTX>.() -> Unit): Ref<MachineGraph<S, E, CTX>> = useCreation {
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
class StateMachineGraphScope<S : Any, E, CTX>() {
    val transitions: Transition<S, E> = mutableMapOf()
    val actions: Actions<S, E> = mutableMapOf()
    var initState: S by Delegates.notNull<S>()
    var context: CTX? = null

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
    fun state(state: S, descriptionBlock: StateDescriptionScope<S, E>.() -> Unit) {
        val description = StateDescriptionScope<S, E>()
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
    fun states(descriptionBlock: StatesDescriptionScope<S, E>.() -> Unit) {
        val description = StatesDescriptionScope<S, E>()
        description.descriptionBlock()
        transitions.putAll(description.transitions)
        actions.putAll(description.actions)
    }

    internal fun build(): MachineGraph<S, E, CTX> = MachineGraph(transitions, initState, context, actions)
}

/**
 * State description scope for defining event transitions within a state
 *
 * This scope provides an infix function style for defining transitions that reads naturally:
 * `Event.START transitionTo State.LOADING`
 *
 * @param eventMaps Internal map storing event to state mappings
 */
class StateDescriptionScope<S, E>(
    val eventMaps: MutableMap<E, S> = mutableMapOf(),
    val actionMaps: MutableMap<E, Action<E>> = mutableMapOf(),
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

    fun on(event: E, block: EventDescriptionScope<S, E>.() -> Unit) {
        EventDescriptionScope(eventMaps, event, actionMaps).block()
    }
}

class StatesDescriptionScope<S, E>() {
    internal val transitions: Transition<S, E> = mutableMapOf()
    internal val actions: Actions<S, E> = mutableMapOf()

    operator fun S.invoke(block: StateTransitionScope<S, E>.() -> Unit) {
        val stateTransition = StateTransitionScope(this, transitions, actions)
        stateTransition.block()
        stateTransition.build()
    }
}

class StateTransitionScope<S, E>(val fromState: S, val transitions: Transition<S, E>, val actions: Actions<S, E>) {
    private val eventMaps: MutableMap<E, S> = mutableMapOf()
    private val actionMaps: MutableMap<E, Action<E>> = mutableMapOf()

    infix fun E.target(targetState: S) {
        eventMaps[this] = targetState
    }

    fun on(event: E, block: EventDescriptionScope<S, E>.() -> Unit) {
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

typealias Action<E> = (E) -> Unit

class EventDescriptionScope<S, E>(val eventMaps: MutableMap<E, S>, val event: E, val actionMaps: MutableMap<E, Action<E>>) {
    fun target(targetState: S) {
        eventMaps[event] = targetState
    }

    fun action(action: (E) -> Unit) {
        actionMaps[event] = action
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
