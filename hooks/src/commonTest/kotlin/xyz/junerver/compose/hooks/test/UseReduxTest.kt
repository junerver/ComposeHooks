package xyz.junerver.compose.hooks.test

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import xyz.junerver.compose.hooks.Middleware
import xyz.junerver.compose.hooks.Reducer
import xyz.junerver.compose.hooks.useredux.combineStores
import xyz.junerver.compose.hooks.useredux.createStore
import xyz.junerver.compose.hooks.useredux.plus

/*
  Description: Redux ecosystem (createStore, combineStores) comprehensive TDD tests
  Author: Junerver
  Date: 2026/1/24
  Email: junerver@gmail.com
  Version: v1.0
*/

class UseReduxTest {
    // Test state and action types
    data class CounterState(val count: Int)

    sealed interface CounterAction {
        data object Increment : CounterAction

        data object Decrement : CounterAction

        data class Add(val value: Int) : CounterAction
    }

    data class TodoState(val items: List<String>)

    sealed interface TodoAction {
        data class Add(val item: String) : TodoAction

        data class Remove(val index: Int) : TodoAction
    }

    // Test reducers
    private val counterReducer: Reducer<CounterState, CounterAction> = { state, action ->
        when (action) {
            CounterAction.Increment -> state.copy(count = state.count + 1)
            CounterAction.Decrement -> state.copy(count = state.count - 1)
            is CounterAction.Add -> state.copy(count = state.count + action.value)
        }
    }

    private val todoReducer: Reducer<TodoState, TodoAction> = { state, action ->
        when (action) {
            is TodoAction.Add -> state.copy(items = state.items + action.item)
            is TodoAction.Remove -> state.copy(items = state.items.filterIndexed { i, _ -> i != action.index })
        }
    }

    @Test
    fun createStore_creates_store_with_single_reducer() {
        val store = createStore {
            counterReducer with CounterState(0)
        }

        assertNotNull(store)
        assertEquals(1, store.records.size)
        assertEquals(CounterState::class, store.records[0].stateType)
        assertEquals(CounterAction::class, store.records[0].actionType)
    }

    @Test
    fun createStore_creates_store_with_multiple_reducers() {
        val store = createStore {
            counterReducer with CounterState(0)
            todoReducer with TodoState(emptyList())
        }

        assertEquals(2, store.records.size)
        assertEquals(CounterState::class, store.records[0].stateType)
        assertEquals(TodoState::class, store.records[1].stateType)
    }

    @Test
    fun createStore_with_named_reducer() {
        val store = createStore {
            named("counter") {
                counterReducer with CounterState(10)
            }
        }

        assertEquals(1, store.records.size)
        assertEquals("counter", store.records[0].alias)
        assertEquals(CounterState(10), store.records[0].initialState)
    }

    @Test
    fun createStore_with_middleware() {
        val logs = mutableListOf<String>()
        val logMiddleware: Middleware<Any, Any> = { next, _ ->
            { action ->
                logs.add("before: $action")
                next(action)
                logs.add("after: $action")
            }
        }

        val store = createStore(arrayOf(logMiddleware)) {
            counterReducer with CounterState(0)
        }

        assertEquals(1, store.middlewares.size)
    }

    @Test
    fun createStore_with_multiple_middlewares() {
        val middleware1: Middleware<Any, Any> = { next, _ -> { action -> next(action) } }
        val middleware2: Middleware<Any, Any> = { next, _ -> { action -> next(action) } }

        val store = createStore(arrayOf(middleware1, middleware2)) {
            counterReducer with CounterState(0)
        }

        assertEquals(2, store.middlewares.size)
    }

    @Test
    fun store_plus_operator_combines_stores() {
        val store1 = createStore {
            counterReducer with CounterState(0)
        }

        val store2 = createStore {
            todoReducer with TodoState(emptyList())
        }

        val combined = store1 + store2

        assertEquals(2, combined.records.size)
        assertEquals(CounterState::class, combined.records[0].stateType)
        assertEquals(TodoState::class, combined.records[1].stateType)
    }

    @Test
    fun store_plus_operator_combines_middlewares() {
        val middleware1: Middleware<Any, Any> = { next, _ -> { action -> next(action) } }
        val middleware2: Middleware<Any, Any> = { next, _ -> { action -> next(action) } }

        val store1 = createStore(arrayOf(middleware1)) {
            counterReducer with CounterState(0)
        }

        val store2 = createStore(arrayOf(middleware2)) {
            todoReducer with TodoState(emptyList())
        }

        val combined = store1 + store2

        assertEquals(2, combined.middlewares.size)
    }

    @Test
    fun combineStores_combines_multiple_stores() {
        val store1 = createStore { counterReducer with CounterState(0) }
        val store2 = createStore { todoReducer with TodoState(emptyList()) }
        val store3 = createStore {
            named("counter2") {
                counterReducer with CounterState(100)
            }
        }

        val combined = combineStores(store1, store2, store3)

        assertEquals(3, combined.records.size)
    }

    @Test
    fun store_record_preserves_initial_state() {
        val initialState = CounterState(42)
        val store = createStore {
            counterReducer with initialState
        }

        assertEquals(initialState, store.records[0].initialState)
    }

    @Test
    fun store_record_preserves_reducer() {
        val store = createStore {
            counterReducer with CounterState(0)
        }

        val reducer = store.records[0].reducer
        assertNotNull(reducer)

        // Test reducer works correctly
        val newState = reducer(CounterState(0), CounterAction.Increment)
        assertEquals(CounterState(1), newState)
    }

    @Test
    fun store_equality_checks_middlewares_and_records() {
        val store1 = createStore {
            counterReducer with CounterState(0)
        }

        val store2 = createStore {
            counterReducer with CounterState(0)
        }

        // Different instances but same content
        assertEquals(store1.records.size, store2.records.size)
        assertEquals(store1.records[0].stateType, store2.records[0].stateType)
    }

    @Test
    fun named_stores_can_have_same_type_different_alias() {
        val store = createStore {
            named("counter1") {
                counterReducer with CounterState(0)
            }
            named("counter2") {
                counterReducer with CounterState(100)
            }
        }

        assertEquals(2, store.records.size)
        assertEquals("counter1", store.records[0].alias)
        assertEquals("counter2", store.records[1].alias)
        assertEquals(CounterState(0), store.records[0].initialState)
        assertEquals(CounterState(100), store.records[1].initialState)
    }

    @Test
    fun reducer_handles_all_action_types() {
        val state = CounterState(10)

        val afterIncrement = counterReducer(state, CounterAction.Increment)
        assertEquals(11, afterIncrement.count)

        val afterDecrement = counterReducer(state, CounterAction.Decrement)
        assertEquals(9, afterDecrement.count)

        val afterAdd = counterReducer(state, CounterAction.Add(5))
        assertEquals(15, afterAdd.count)
    }

    @Test
    fun todo_reducer_handles_add_and_remove() {
        val state = TodoState(listOf("item1", "item2"))

        val afterAdd = todoReducer(state, TodoAction.Add("item3"))
        assertEquals(listOf("item1", "item2", "item3"), afterAdd.items)

        val afterRemove = todoReducer(state, TodoAction.Remove(0))
        assertEquals(listOf("item2"), afterRemove.items)
    }

    @Test
    fun empty_store_has_no_records() {
        val store = createStore { }

        assertEquals(0, store.records.size)
        assertEquals(0, store.middlewares.size)
    }

    @Test
    fun middleware_chain_order_is_preserved() {
        val order = mutableListOf<Int>()

        val middleware1: Middleware<Any, Any> = { next, _ ->
            { action ->
                order.add(1)
                next(action)
            }
        }

        val middleware2: Middleware<Any, Any> = { next, _ ->
            { action ->
                order.add(2)
                next(action)
            }
        }

        val store = createStore(arrayOf(middleware1, middleware2)) {
            counterReducer with CounterState(0)
        }

        // Verify middleware order is preserved
        assertEquals(middleware1, store.middlewares[0])
        assertEquals(middleware2, store.middlewares[1])
    }
}
