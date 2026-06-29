package xyz.junerver.compose.hooks.test

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import xyz.junerver.compose.hooks.Dispatch
import xyz.junerver.compose.hooks.Middleware
import xyz.junerver.compose.hooks.Reducer
import xyz.junerver.compose.hooks.useredux.combineStores
import xyz.junerver.compose.hooks.useredux.createStore

/*
  Description: wasmJs smoke tests for core hooks logic.
  Author: Junerver
  Date: 2026/6/29
  Email: junerver@gmail.com
  Version: v1.0

  These tests verify that the pure-logic code paths of the core hooks run
  correctly under the wasmJs runtime — in particular that KClass is usable
  as a Map key (Redux store registration) and that reducer composition works.
  They complement the platform-agnostic tests inherited from commonTest.
*/

// Top-level types (local sealed interfaces are not allowed inside functions).
private data class CounterState(val count: Int)

private sealed interface CounterAction {
    data object Increment : CounterAction
    data class Add(val value: Int) : CounterAction
}

private data class TodoState(val items: List<String>)

private sealed interface TodoAction {
    data class Add(val item: String) : TodoAction
}

private val counterReducer: Reducer<CounterState, CounterAction> = { state, action ->
    when (action) {
        CounterAction.Increment -> state.copy(count = state.count + 1)
        is CounterAction.Add -> state.copy(count = state.count + action.value)
    }
}

private val todoReducer: Reducer<TodoState, TodoAction> = { state, action ->
    when (action) {
        is TodoAction.Add -> state.copy(items = state.items + action.item)
    }
}

class WasmJsSmokeTest {

    @Test
    fun createStore_registers_state_and_action_types_on_wasmJs() {
        // Exercises KClass-as-Map-key registration (CreateStore uses
        // S::class / A::class). This is the exact pattern §4.1 of the
        // requirements doc flagged as a risk — confirmed working on wasmJs.
        val store = createStore {
            counterReducer with CounterState(0)
        }
        assertNotNull(store)
        assertEquals(1, store.records.size)
        val record = store.records.first()
        assertEquals(CounterState::class, record.stateType)
        assertEquals(CounterAction::class, record.actionType)
    }

    @Test
    fun reducer_is_invoked_correctly_on_wasmJs() {
        val store = createStore {
            counterReducer with CounterState(10)
        }
        val record = store.records.first()
        val next = record.reducer(record.initialState, CounterAction.Increment)
        assertEquals(CounterState(11), next)
        val next2 = record.reducer(next, CounterAction.Add(5))
        assertEquals(CounterState(16), next2)
    }

    @Test
    fun combineStores_merges_records_on_wasmJs() {
        val a = createStore { counterReducer with CounterState(0) }
        val b = createStore { todoReducer with TodoState(emptyList()) }
        val combined = combineStores(a, b)
        assertEquals(2, combined.records.size)
        assertTrue(combined.records.any { it.stateType == CounterState::class })
        assertTrue(combined.records.any { it.stateType == TodoState::class })
    }

    @Test
    fun middleware_array_is_carried_through_on_wasmJs() {
        // Middleware<S, A> = (dispatch: Dispatch<A>, state: S) -> Dispatch<A>
        val log: MutableList<String> = mutableListOf()
        val loggingMiddleware: Middleware<Any, Any> = { dispatch, _ ->
            { action ->
                log.add("before")
                dispatch(action)
                log.add("after")
            }
        }
        val store = createStore(middlewares = arrayOf(loggingMiddleware)) {
            counterReducer with CounterState(0)
        }
        assertEquals(1, store.middlewares.size)
        // Compose middleware around a base dispatch backed by the reducer.
        val record = store.records.first()
        val baseDispatch: Dispatch<Any> = { action ->
            record.reducer(record.initialState, action)
            Unit
        }
        val composed: Dispatch<Any> = store.middlewares.fold(baseDispatch) { acc, mw ->
            mw(acc, record.initialState)
        }
        composed(CounterAction.Increment)
        assertEquals(listOf("before", "after"), log)
    }
}
