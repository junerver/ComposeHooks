package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable

/*
  Description: A Compose Hook for managing state machines
  Author: kk
  Date: 2024/6/22-9:30
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
 * val transitions = stateGraphBuilder<State, Event> {
 *     state(State.IDLE) {
 *         on(Event.START) { transitionTo(State.LOADING) }
 *     }
 *     state(State.LOADING) {
 *         on(Event.SUCCESS) { transitionTo(State.SUCCESS) }
 *         on(Event.ERROR) { transitionTo(State.ERROR) }
 *     }
 *     state(State.SUCCESS) {
 *         on(Event.RESET) { transitionTo(State.IDLE) }
 *     }
 *     state(State.ERROR) {
 *         on(Event.RESET) { transitionTo(State.IDLE) }
 *     }
 * }
 *
 * val stateMachine = useStateMachine(State.IDLE, transitions)
 * ```
 */
@Composable
fun <S : Any, E> useStateMachine(
    initialState: S,
    transitions: Map<Pair<S, E>, S>,
    maxHistorySize: Int = 100
): StateMachineHolder<S, E> {
    val (state, setState) = useGetState(initialState)
    val history = useList<S>()

    // Only add to history when state actually changes
    useEffect(state) {
        val currentState = state.value
        if (history.isEmpty() || history.last() != currentState) {
            history.add(currentState)
            // Limit history size
            if (history.size > maxHistorySize) {
                history.removeAt(0)
            }
        }
    }

    val canTransition = { event: E ->
        transitions.containsKey(state.value to event)
    }

    val transition = { event: E ->
        val currentState = state.value
        val nextState = transitions[currentState to event]
        if (nextState != null) {
            setState(nextState)
        }
    }

    val reset = {
        setState(initialState)
    }

    val canGoBack = {
        history.size > 1
    }

    val goBack = {
        if (canGoBack()) {
            // Remove current state
            history.removeAt(history.size - 1)
            // Get previous state
            val previousState = history.last()
            setState(previousState)
        }
    }

    val getAvailableEvents = {
        transitions.keys
            .filter { it.first == state.value }
            .map { it.second }
    }

    return StateMachineHolder(
        currentState = state.value,
        canTransition = canTransition,
        transition = transition,
        history = history.toList(), // Return immutable copy
        reset = reset,
        canGoBack = canGoBack,
        goBack = goBack,
        getAvailableEvents = getAvailableEvents
    )
}

typealias Transition<S, E> = MutableMap<Pair<S, E>, S>

/**
 * State graph builder function that provides a DSL for creating state transitions
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
 * val transitions = stateGraphBuilder<MyState, MyEvent> {
 *     state(MyState.INITIAL) {
 *         on(MyEvent.START) { transitionTo(MyState.PROCESSING) }
 *     }
 *     state(MyState.PROCESSING) {
 *         on(MyEvent.COMPLETE) { transitionTo(MyState.DONE) }
 *         on(MyEvent.ERROR) { transitionTo(MyState.FAILED) }
 *     }
 * }
 * ```
 */
fun <S, E> stateGraphBuilder(
    init: StateGraph<S, E>.() -> Unit
): Transition<S, E> {
    val graph = StateGraph<S, E>()
    graph.init()
    return graph.transitions
}

/**
 * State graph builder class that provides DSL for defining state machine transitions
 *
 * This class enables a fluent API for defining state transitions in a clear and readable way.
 * Each state can define multiple event handlers that specify which state to transition to.
 *
 * @param transitions Internal map storing the state transitions
 */
class StateGraph<S, E>(val transitions: MutableMap<Pair<S, E>, S> = mutableMapOf()) {

    /**
     * Define a state and its possible transitions
     *
     * @param state The state to define transitions for
     * @param descriptionBlock Block that defines the events and their target states
     */
    fun state(
        state: S,
        descriptionBlock: Description<S, E>.() -> Unit
    ) {
        val description = Description<S, E>()
        descriptionBlock(description)
        description.eventMaps.forEach {
            transitions.put(state to it.key, it.value)
        }
    }

    /**
     * Description class for defining event transitions within a state
     *
     * @param eventMaps Internal map storing event to state mappings
     */
    class Description<S, E>(val eventMaps: MutableMap<E, S> = mutableMapOf<E, S>()) {

        /**
         * Define an event handler for the current state
         *
         * @param event The event to handle
         * @param eventBlock Block that defines what to do when this event occurs
         */
        fun <E> on(event: E, eventBlock: E.() -> Unit) {
            eventBlock(event)
        }

        /**
         * Extension function to define state transition for an event
         *
         * @param state The target state to transition to
         */
        fun E.transitionTo(state: S) {
            eventMaps.put(this, state)
        }
    }

}

/**
 * State machine holder containing current state and all control functions
 *
 * @property currentState Current state
 * @property canTransition Check if the specified event transition can be executed
 * @property transition Execute state transition
 * @property history State history records (immutable list)
 * @property reset Reset to initial state
 * @property canGoBack Check if can go back to previous state
 * @property goBack Go back to previous state
 * @property getAvailableEvents Get list of available events for current state
 */
data class StateMachineHolder<S : Any, E>(
    val currentState: S,
    val canTransition: (E) -> Boolean,
    val transition: (E) -> Unit,
    val history: List<S>,
    val reset: () -> Unit,
    val canGoBack: () -> Boolean,
    val goBack: () -> Unit,
    val getAvailableEvents: () -> List<E>
)
