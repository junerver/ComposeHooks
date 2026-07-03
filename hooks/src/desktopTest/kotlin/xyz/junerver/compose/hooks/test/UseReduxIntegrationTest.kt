package xyz.junerver.compose.hooks.test

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import xyz.junerver.compose.hooks.useredux.ReduxProvider
import xyz.junerver.compose.hooks.useredux.combineStores
import xyz.junerver.compose.hooks.useredux.createStore
import xyz.junerver.compose.hooks.useredux.plus
import xyz.junerver.compose.hooks.useredux.useDispatch
import xyz.junerver.compose.hooks.useredux.useSelector

private sealed class IntegrationCounterAction {
    data object Increment : IntegrationCounterAction()
    data object Decrement : IntegrationCounterAction()
    data class Set(val value: Int) : IntegrationCounterAction()
}

private val integrationCounterReducer: (Int, IntegrationCounterAction) -> Int = { state, action ->
    when (action) {
        IntegrationCounterAction.Increment -> state + 1
        IntegrationCounterAction.Decrement -> state - 1
        is IntegrationCounterAction.Set -> action.value
    }
}

private sealed class IntegrationTodoAction {
    data class Add(val text: String) : IntegrationTodoAction()
}

private data class IntegrationTodoState(val items: List<String> = emptyList())

private val integrationTodoReducer: (IntegrationTodoState, IntegrationTodoAction) -> IntegrationTodoState = { state, action ->
    when (action) {
        is IntegrationTodoAction.Add -> state.copy(items = state.items + action.text)
    }
}

@Suppress("DEPRECATION")
class UseReduxIntegrationTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun dispatch_updates_selected_state_from_provider() = runComposeUiTest {
        var count by mutableStateOf(0)
        var dispatch: ((IntegrationCounterAction) -> Unit)? = null

        setContent {
            val store = createStore {
                integrationCounterReducer with 0
            }
            ReduxProvider(store = store) {
                val state by useSelector<Int>()
                count = state
                dispatch = useDispatch()
            }
        }

        waitForIdle()
        assertEquals(0, count)

        dispatch!!(IntegrationCounterAction.Increment)
        waitForIdle()
        assertEquals(1, count)

        dispatch!!(IntegrationCounterAction.Set(10))
        waitForIdle()
        assertEquals(10, count)

        dispatch!!(IntegrationCounterAction.Decrement)
        waitForIdle()
        assertEquals(9, count)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun provider_exposes_multiple_registered_slices() = runComposeUiTest {
        var count by mutableStateOf(-1)
        var todoCount by mutableStateOf(-1)

        setContent {
            val store = createStore {
                integrationCounterReducer with 0
                integrationTodoReducer with IntegrationTodoState(listOf("first", "second"))
            }
            ReduxProvider(store = store) {
                val counter by useSelector<Int>()
                val todos by useSelector<IntegrationTodoState>()
                count = counter
                todoCount = todos.items.size
            }
        }

        waitForIdle()
        assertEquals(0, count)
        assertEquals(2, todoCount)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun alias_scopes_selector_and_dispatch_to_named_store() = runComposeUiTest {
        var count by mutableStateOf(0)
        var dispatch: ((IntegrationCounterAction) -> Unit)? = null

        setContent {
            val store = createStore {
                named<Int, IntegrationCounterAction>("counter") {
                    integrationCounterReducer with 0
                }
            }
            ReduxProvider(store = store) {
                val state by useSelector<Int>(alias = "counter")
                count = state
                dispatch = useDispatch(alias = "counter")
            }
        }

        waitForIdle()
        dispatch!!(IntegrationCounterAction.Increment)
        waitForIdle()
        assertEquals(1, count)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun selector_transform_projects_derived_view_state() = runComposeUiTest {
        var display by mutableStateOf("")

        setContent {
            val store = createStore {
                integrationCounterReducer with 42
            }
            ReduxProvider(store = store) {
                val text by useSelector<Int, String> { "Count: $this" }
                display = text
            }
        }

        waitForIdle()
        assertEquals("Count: 42", display)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun combined_store_from_plus_operator_supports_multiple_slices() = runComposeUiTest {
        var count by mutableStateOf(0)
        var todoCount by mutableStateOf(0)

        setContent {
            val counterStore = createStore { integrationCounterReducer with 3 }
            val todoStore = createStore { integrationTodoReducer with IntegrationTodoState(listOf("a")) }
            ReduxProvider(store = counterStore + todoStore) {
                val counter by useSelector<Int>()
                val todos by useSelector<IntegrationTodoState>()
                count = counter
                todoCount = todos.items.size
            }
        }

        waitForIdle()
        assertEquals(3, count)
        assertEquals(1, todoCount)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun combine_stores_function_supports_multiple_slices() = runComposeUiTest {
        var count by mutableStateOf(0)
        var todoCount by mutableStateOf(0)

        setContent {
            val combined = combineStores(
                createStore { integrationCounterReducer with 7 },
                createStore { integrationTodoReducer with IntegrationTodoState(listOf("x", "y")) },
            )
            ReduxProvider(store = combined) {
                val counter by useSelector<Int>()
                val todos by useSelector<IntegrationTodoState>()
                count = counter
                todoCount = todos.items.size
            }
        }

        waitForIdle()
        assertEquals(7, count)
        assertEquals(2, todoCount)
    }
}
