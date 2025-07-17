package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.pow
import kotlin.time.Duration.Companion.seconds
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import xyz.junerver.compose.hooks.Reducer
import xyz.junerver.compose.hooks.Tuple2
import xyz.junerver.compose.hooks.getValue
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.setValue
import xyz.junerver.compose.hooks.tuple
import xyz.junerver.compose.hooks.useControllable
import xyz.junerver.compose.hooks.useList
import xyz.junerver.compose.hooks.useMount
import xyz.junerver.compose.hooks.useRef
import xyz.junerver.compose.hooks.useredux.createStore
import xyz.junerver.compose.hooks.useredux.useDispatch
import xyz.junerver.compose.hooks.useredux.useDispatchAsync
import xyz.junerver.compose.hooks.useredux.useSelector
import xyz.junerver.composehooks.example.request.TParams
import xyz.junerver.composehooks.net.NetApi
import xyz.junerver.composehooks.net.bean.UserInfo
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.LogCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.TButton
import xyz.junerver.composehooks.utils.NanoId

/*
  Description: Examples demonstrating the usage of Redux pattern with Compose Hooks
  Author: Junerver
  Date: 2024/7/17-10:30
  Email: junerver@gmail.com
  Version: v1.0
*/

data class Todo(val name: String, val id: String)

sealed interface TodoAction

data class AddTodo(val todo: Todo) : TodoAction

data class DelTodo(val id: String) : TodoAction

val todoReducer: Reducer<PersistentList<Todo>, TodoAction> =
    { prevState: PersistentList<Todo>, action: TodoAction ->
        when (action) {
            is AddTodo -> prevState + action.todo

            is DelTodo -> prevState.mutate { mutator ->
                mutator.removeAll { it.id == action.id }
            }
        }
    }

/**
 * Create a state store object by using the createStore function
 *
 * This demonstrates how to create a centralized store for state management
 */
val simpleStore = createStore(arrayOf(logMiddleware())) {
    simpleReducer with SimpleData("default", 18)
    todoReducer with persistentListOf()
}

/**
 * Main component demonstrating the useRedux hook functionality
 *
 * This page showcases different aspects of Redux pattern implementation in Compose:
 * 1. Basic state management with SimpleData
 * 2. Collection management with Todo list
 * 3. Asynchronous operations with network requests
 */
@Composable
fun UseReduxExample() {
    /** Store is provided by root component, see at MainActivity */
    Surface {
        ScrollColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "useRedux Examples",
                style = MaterialTheme.typography.headlineMedium,
            )

            Text(
                text = "The useRedux hook provides a powerful state management solution based on Redux pattern, with centralized store, actions, and reducers.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            // Interactive Demo
            InteractiveReduxDemo()

            // Basic Usage
            ExampleCard(title = "Basic Usage: Simple Data Management") {
                SimpleDataContainer()
            }

            // Todo List Example
            ExampleCard(title = "Practical Application: Todo List") {
                TodosListContainer()
            }

            // Async Operations
            ExampleCard(title = "Advanced Usage: Complex Asynchronous Requests") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Demonstrates how to build a tool for complex asynchronous requests based on useSelector and useDispatchAsync, featuring automatic requests, request state transitions, error retries, and default parameters.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    UseReduxFetchErrorRetrySample()
                    UseReduxFetchUserInfoSample()
                }
            }
        }
    }
}

/**
 * Interactive demonstration of Redux pattern with real-time updates
 */
@Composable
private fun InteractiveReduxDemo() {
    val dispatch = useDispatch<SimpleAction>()
    val name by useSelector<SimpleData, String> { name }
    val age by useSelector<SimpleData, Int> { age }
    val (input, setInput) = useControllable("")
    val logs = useList<String>()
    val asyncDispatch = useDispatchAsync<SimpleAction>()

    ExampleCard(title = "Interactive Demo") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Current state display
            Text(
                text = "Current State: name='$name', age=$age",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            // Input field for name
            OutlinedTextField(
                value = input.value,
                onValueChange = setInput,
                label = { Text("Enter new name") },
                modifier = Modifier.fillMaxWidth(),
            )

            // Action buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TButton(text = "Change Name") {
                    dispatch(SimpleAction.ChangeName(input.value))
                    logs.add("Changed name to '${input.value}'")
                }

                TButton(text = "Increase Age") {
                    dispatch(SimpleAction.AgeIncrease)
                    logs.add("Increased age to ${age + 1}")
                }

                TButton(text = "Async Change") {
                    asyncDispatch {
                        delay(1.seconds)
                        logs.add("Async changed name after 1 second")
                        SimpleAction.ChangeName(input.value)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Log display
            LogCard(title = "Action Log:", logs = logs)
        }
    }
}

/**
 * Container component for Todo list functionality
 *
 * This component demonstrates how to manage a collection of items using Redux
 */
@Composable
private fun TodosListContainer() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Todo List Example",
            style = MaterialTheme.typography.bodyMedium,
        )

        Text(
            text = "This example shows how to manage a collection of items with Redux",
            style = MaterialTheme.typography.bodySmall,
        )

        Header()
        TodoList()
    }
}

/**
 * Component to display the list of todos from the Redux store
 *
 * This demonstrates how to use [useSelector] to access collection state
 */
@Composable
fun TodoList() {
    /**
     * The corresponding state object saved in the store can be quickly
     * obtained through the [useSelector] function
     */
    val todos by useSelector<PersistentList<Todo>>()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (todos.isEmpty()) {
            Text(
                text = "No todos yet. Add some using the field above.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(vertical = 16.dp),
            )
        } else {
            Text(
                text = "${todos.size} todo(s) in the store",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            todos.forEach {
                TodoItem(item = it)
            }
        }
    }
}

/**
 * Header component for adding new todos
 *
 * This demonstrates how to use [useDispatch] to dispatch actions to the Redux store
 */
@Composable
private fun Header() {
    /**
     * You can quickly obtain the dispatch function corresponding to the Action
     * through [useDispatch]
     */
    val dispatch = useDispatch<TodoAction>()
    val (input, setInput) = useControllable("")

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Add a new todo item",
            style = MaterialTheme.typography.bodyMedium,
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = input.value,
                onValueChange = setInput,
                label = { Text("Enter todo text") },
                modifier = Modifier.weight(1f),
            )

            TButton(
                text = "Add Todo",
                modifier = Modifier.padding(start = 8.dp, top = 8.dp),
            ) {
                if (input.value.isNotEmpty()) {
                    // Dispatch AddTodo action with a new Todo object
                    dispatch(AddTodo(Todo(input.value, NanoId.generate())))
                    // Clear the input field
                    setInput("")
                }
            }
        }

        Text(
            text = "Dispatches AddTodo action with a new Todo object",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

/**
 * Component to display a single todo item with delete functionality
 *
 * This demonstrates how to dispatch actions for a specific item in a collection
 */
@Composable
private fun TodoItem(item: Todo) {
    val dispatch = useDispatch<TodoAction>()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // Display the todo text
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp, top = 8.dp),
        )

        // Delete button with action dispatch
        TButton(text = "Delete") {
            // Dispatch DelTodo action with the item's id
            dispatch(DelTodo(item.id))
        }
    }
}

@Composable
private fun SimpleDataContainer() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Display different ways to access state
        SubSimpleDataStateText()
        SubSimpleDataStateText2()

        HorizontalDivider()
        Spacer(modifier = Modifier.height(10.dp))

        // Display dispatch functionality
        SubSimpleDataDispatch()
    }
}

/**
 * Component demonstrating how to select a specific property from state
 *
 * This shows how to use [useSelector] to extract just the name property
 */
@Composable
private fun SubSimpleDataStateText() {
    /**
     * Using another overload of [useSelector], you can easily transform the
     * state, or only take some attributes of the state object as the state you
     * want to focus on
     */
    val name by useSelector<SimpleData, String> { name }

    Column {
        Text(
            text = "Selected property: name",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
        )
        Text(
            text = "User Name: $name",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

/**
 * Component demonstrating how to transform state with [useSelector]
 *
 * This shows how to use [useSelector] to transform the age property into a formatted string
 */
@Composable
private fun SubSimpleDataStateText2() {
    /**
     * Here we transform the age value into a formatted string directly in the selector
     */
    val age by useSelector<SimpleData, String> { "age: $age" }

    Column {
        Text(
            text = "Transformed property: age",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
        )
        Text(
            text = "User $age",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

/**
 * Component demonstrating different ways to dispatch actions
 *
 * This shows how to use both synchronous and asynchronous dispatch methods
 */
@Composable
private fun SubSimpleDataDispatch() {
    val (input, setInput) = useControllable("")
    val dispatch = useDispatch<SimpleAction>()

    /**
     * Using useDispatchAsync you can obtain an asynchronous dispatch
     * function, which allows you to perform asynchronous operations in the
     * coroutine of the current component. The return value of the asynchronous
     * function is Action.
     */
    val asyncDispatch = useDispatchAsync<SimpleAction>()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Dispatch Methods Demonstration",
            style = MaterialTheme.typography.bodyMedium,
        )

        // Input field for name change
        OutlinedTextField(
            value = input.value,
            onValueChange = setInput,
            label = { Text("Enter new name") },
            modifier = Modifier.fillMaxWidth(),
        )

        // Action buttons with explanations
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column {
                    TButton(text = "Change Name") {
                        dispatch(SimpleAction.ChangeName(input.value))
                    }
                    Text(
                        text = "Synchronous dispatch",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                Column {
                    TButton(text = "Async Change") {
                        asyncDispatch {
                            delay(1.seconds)
                            SimpleAction.ChangeName(input.value)
                        }
                    }
                    Text(
                        text = "Async with 1s delay",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                Column {
                    TButton(text = "Increase Age") {
                        dispatch(SimpleAction.AgeIncrease)
                    }
                    Text(
                        text = "Simple action dispatch",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

/**
 * Component demonstrating error handling in Redux async operations
 *
 * This example intentionally throws an error after a delay to show retry mechanism
 */
@Composable
private fun UseReduxFetchErrorRetrySample() {
    val (fetchResult, fetch) = useFetchError()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Error Handling Example",
            style = MaterialTheme.typography.bodyMedium,
        )

        Text(
            text = "This example will delay for 2 seconds, then throw an error to demonstrate retry mechanism",
            style = MaterialTheme.typography.bodySmall,
        )

        // Display current state
        Text(
            text = "Current state: ${fetchResult::class.simpleName}",
            style = MaterialTheme.typography.bodyMedium,
            color = when (fetchResult) {
                is NetFetchResult.Error -> MaterialTheme.colorScheme.error
                is NetFetchResult.Success -> MaterialTheme.colorScheme.primary
                NetFetchResult.Loading -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.onSurface
            },
        )

        // Show detailed result
        Text(
            text = "Result: $fetchResult",
            style = MaterialTheme.typography.bodySmall,
        )

        // Fetch button
        TButton(text = "Trigger Error Fetch") {
            fetch()
        }
    }
}

/**
 * Component demonstrating successful API fetch with Redux
 *
 * This example shows how to fetch user information from an API
 */
@Composable
private fun UseReduxFetchUserInfoSample() {
    val (fetchResult, fetch) = useFetchUserInfo()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "API Fetch Example",
            style = MaterialTheme.typography.bodyMedium,
        )

        Text(
            text = "This example fetches user information from an API",
            style = MaterialTheme.typography.bodySmall,
        )

        // Fetch button
        TButton(text = "Fetch User Info") {
            fetch()
        }
        val (other, setOther) = useControllable("gaogaotiantian")
        OutlinedTextField(value = other.value, onValueChange = setOther, label = { Text("Enter other user name") })
        TButton(text = "Fetch Other User Info") {
            fetch(other.value)
        }

        // Display result based on state
        when (fetchResult) {
            is NetFetchResult.Error -> {
                Text(
                    text = "Error: ${fetchResult.msg}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            NetFetchResult.Idle -> {
                Text(
                    text = "Idle - No fetch attempted yet",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            NetFetchResult.Loading -> {
                Text(
                    text = "Loading...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }

            is NetFetchResult.Success -> {
                Text(
                    text = "Success! Data: ${fetchResult.data}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

/**
 * Sealed interface representing the different states of a network fetch operation
 */
sealed interface NetFetchResult<out T> {
    /**
     * Represents a successful fetch with data
     */
    data class Success<T>(
        val data: T,
    ) : NetFetchResult<T>

    /**
     * Represents a failed fetch with error message
     */
    data class Error(
        val msg: Throwable,
    ) : NetFetchResult<Nothing>

    /**
     * Represents the initial state before any fetch attempt
     */
    data object Idle : NetFetchResult<Nothing>

    /**
     * Represents the loading state during fetch
     */
    data object Loading : NetFetchResult<Nothing>
}

/**
 * Network fetch reducer that directly passes the action as the new state
 *
 * This reducer is unique in that the action itself becomes the new state. This pattern is used
 * in network request scenarios where the state and action are unified - the action dispatched
 * represents the new state of the network request (Loading, Success, Error, etc.).
 *
 * Key characteristics of this reducer:
 * 1. Identity function - returns the action as the new state without modification
 * 2. State/Action unity - the action dispatched already contains all necessary state information
 * 3. Stateless processing - previous state is ignored (hence the _ parameter)
 *
 * This pattern is particularly useful for network requests where:
 * - Different states of the request (loading, success, error) are represented as different action types
 * - Each action contains all the data needed for that state
 * - The reducer doesn't need to combine previous state with new data
 *
 * @param _ The previous state (ignored in this implementation)
 * @param action The action that also serves as the new state
 * @return The action as the new state
 */
val fetchReducer: Reducer<NetFetchResult<*>, NetFetchResult<*>> = { _, action ->
    action
}

/**
 * More advanced engineering example with named reducers
 *
 * This demonstrates how to create a store with multiple named reducers
 * for different fetch operations
 */

// Constants for fetch operation identifiers, in a formal project, the unique identifier of the interface should be used
private const val FetchAliasErrorRetry = "FetchAliasErrorRetry"
private const val FetchAliasUserInfo = "FetchAliasUserInfo"

// Array of all fetch operation identifiers
private val NetworkFetchAliases = arrayOf(
    FetchAliasErrorRetry,
    FetchAliasUserInfo,
)

// Create a store with named reducers for each fetch operation
val fetchStore = createStore {
    NetworkFetchAliases.forEach {
        named(it) {
            fetchReducer with NetFetchResult.Idle
        }
    }
}

/**
 * Type alias for a function that takes a suspend block and executes a fetch operation
 */
typealias ReduxFetch<T> = (block: suspend CoroutineScope.() -> T) -> Unit

/**
 * Hook to handle network requests with loading state and error handling
 *
 * @param alias The unique identifier for this fetch operation in the Redux store
 * @return A function that takes a suspend block and dispatches appropriate actions
 */
@Composable
fun <T> useFetch(alias: String): ReduxFetch<T> {
    // Get async dispatch with automatic loading state
    val dispatchAsync =
        useDispatchAsync<NetFetchResult<T>>(alias, onBefore = { it(NetFetchResult.Loading) })

    // Return a function that executes the fetch and handles success/error
    return { block ->
        dispatchAsync {
            try {
                // On success, wrap the result in Success state
                NetFetchResult.Success(block())
            } catch (t: Throwable) {
                // On error, wrap the exception in Error state
                NetFetchResult.Error(t)
            }
        }
    }
}

/**
 * Hook that provides fetch functionality with automatic retry on error
 *
 * @param alias Unique identifier for this fetch operation
 * @param autoFetch Whether to fetch automatically on mount
 * @param errorRetry Number of retry attempts on error
 * @param block The suspend function to execute for the fetch
 * @param defaultParams Default parameters for the fetch
 * @return A tuple containing the current fetch result and a function to trigger the fetch
 */
@Composable
private fun <T> useFetchAliasFetch(
    alias: String,
    autoFetch: Boolean = false,
    errorRetry: Int = 0,
    defaultParams: TParams = emptyArray<Any?>(),
    block: suspend CoroutineScope.(TParams) -> T,
): Tuple2<NetFetchResult<T>, (TParams) -> Unit> {
    // Get the current fetch result from the store
    val fetchResult: NetFetchResult<T> by useSelector(alias)

    // Get the fetch function for this alias
    val dispatchFetch = useFetch<T>(alias)

    // Track remaining retry attempts
    var retryCount by useRef(errorRetry)
    var latestParams by useRef(defaultParams)

    // Create the fetch function with exponential backoff
    val fetch = { params: TParams ->
        latestParams = if (defaultParams.size == params.size) {
            params
        } else {
            defaultParams
        }
        val count = errorRetry - retryCount
        dispatchFetch {
            // Calculate delay with exponential backoff, capped at 30 seconds
            delay((1.seconds * 2f.pow(count).toInt()).coerceAtMost(30.seconds))
            block(latestParams)
        }
    }

    // Auto-fetch on mount if enabled
    useMount {
        if (autoFetch && fetchResult is NetFetchResult.Idle) fetch(defaultParams)
    }

    // Handle error retry logic
    when (fetchResult) {
        is NetFetchResult.Error -> {
            if (retryCount > 0) {
                fetch(latestParams)
                retryCount -= 1
            }
        }

        is NetFetchResult.Success -> {
            // Reset retry count on success
            retryCount = errorRetry
        }

        else -> {}
    }

    // Return the current result and fetch function
    return tuple(
        first = fetchResult,
        second = fetch,
    )
}

/**
 * Hook that demonstrates error handling with retry
 *
 * This hook intentionally throws an error after a delay to show the retry mechanism
 */
@Composable
private fun useFetchError() = useFetchAliasFetch(alias = FetchAliasErrorRetry, errorRetry = 3) {
    delay(2.seconds)
    error("fetch error")
}

/**
 * Hook that fetches user information from an API
 *
 * @param user The username to fetch information for
 * @return A tuple containing the fetch result and a function to trigger the fetch
 */
@Composable
private fun useFetchUserInfo(user: String = "junerver") =
    useFetchAliasFetch<UserInfo>(alias = FetchAliasUserInfo, autoFetch = true, defaultParams = arrayOf(user)) { params ->
        NetApi.userInfo(params[0] as String)
    }
