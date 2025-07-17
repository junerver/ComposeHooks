package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus
import xyz.junerver.compose.hooks.Middleware
import xyz.junerver.compose.hooks.Reducer
import xyz.junerver.compose.hooks.useControllable
import xyz.junerver.compose.hooks.useReducer
import xyz.junerver.compose.hooks.useState
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description:
  Author: Junerver
  Date: 2024/3/11-9:56
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Simple data model for demonstrating the basic usage of useReducer
 *
 * In the useReducer pattern, state is designed as an immutable data class containing all state fields to be managed.
 * This design makes the state structure clear, easy to track and debug.
 */
data class SimpleData(
    val name: String,
    val age: Int,
)

/**
 * Define possible Action types for modifying SimpleData state
 *
 * Action is one of the core concepts in the useReducer pattern, describing "intent" rather than "how to implement".
 * By encapsulating state changes as explicit Action types, we achieve the following advantages:
 * 1. Clear state change intent - each Action has a clear meaning and purpose
 * 2. Traceable state changes - all state changes are triggered through Actions, facilitating debugging and logging
 * 3. Centralized business logic - all state handling logic is centralized in the reducer function
 * 4. UI and state logic decoupling - UI components only need to send Actions, without caring about specific handling
 */
sealed interface SimpleAction {
    /**
     * Action to change name
     *
     * Contains the new name to be set as a parameter, so the reducer function can get the information it needs
     */
    data class ChangeName(val newName: String) : SimpleAction

    /**
     * Action to increase age
     *
     * This is a simple Action that doesn't need additional parameters, implemented using data object
     */
    data object AgeIncrease : SimpleAction
}

/**
 * Simple Reducer function that updates state based on different Action types
 *
 * Reducer is the core of the useReducer pattern, it's a pure function that takes the current state and an action, returning a new state.
 * Characteristics of Reducer functions:
 * 1. Pure function - same input always produces same output, no side effects
 * 2. Immutability - doesn't directly modify original state, but returns new state objects
 * 3. Single responsibility - only handles state transformation logic, doesn't process side effects
 *
 * The Reducer function is key to decoupling state and events:
 * - UI components only need to focus on "what Action to send", not "how to handle the Action"
 * - All state handling logic is centralized in the Reducer, making code more maintainable
 * - State changes become predictable and testable
 */
val simpleReducer: Reducer<SimpleData, SimpleAction> =
    { prevState: SimpleData, action: SimpleAction ->
        // Use when expression to execute different state update logic based on action type
        when (action) {
            // When receiving ChangeName Action, update the name
            // Note we use the copy method to create a new state object, maintaining immutability
            is SimpleAction.ChangeName -> prevState.copy(name = action.newName)

            // When receiving AgeIncrease Action, increase age by one
            // Similarly use the copy method to create a new state object
            is SimpleAction.AgeIncrease -> prevState.copy(age = prevState.age + 1)
        }
        // Reducer always returns a new state object, rather than modifying the original state
    }

/**
 * useReducer example page
 *
 * This page demonstrates the usage methods and application scenarios of the useReducer hook. useReducer is a powerful state management solution,
 * particularly suitable for handling complex state logic, based on the following core concepts:
 *
 * 1. Centralized state management - all related states are centralized in one state object
 * 2. Action-driven updates - all state changes are triggered by sending Actions
 * 3. Reducer handling logic - state update logic is centralized in the Reducer function
 *
 * Main advantages of this pattern:
 * - Separation of state logic and UI logic, improving code maintainability
 * - Traceable state change process, facilitating debugging
 * - Complex state management becomes orderly and predictable
 */
@Composable
fun UseReducerExample() {
    Surface {
        ScrollColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Page title
            Text(
                text = "useReducer Examples",
                style = MaterialTheme.typography.headlineMedium,
            )

            // Description text - briefly introduces the purpose of useReducer hook
            Text(
                text = "This hook provides a Redux-like state management solution for Compose, allowing complex state logic to be organized through reducers and actions.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            // Interactive demonstration - shows basic usage of useReducer, including interactive UI elements
            InteractiveReducerDemo()

            // Basic usage example - shows the simplest implementation of useReducer
            ExampleCard(title = "Basic Usage") {
                BasicReducerExample()
            }

            // Task list example - shows the application of useReducer in complex application scenarios
            ExampleCard(title = "Task List Example") {
                TaskApp()
            }
        }
    }
}

/**
 * Example logging middleware, recording action and state changes
 *
 * Middleware is an important extension mechanism in the useReducer pattern, allowing us to:
 * 1. Intercept Actions - process before Actions reach the Reducer
 * 2. Enhance Dispatch - add additional functionality, such as logging, performance monitoring, etc.
 * 3. Handle side effects - process side effects without polluting the pure Reducer function
 *
 * Middleware workflow:
 * 1. Receive the original dispatch function and current state
 * 2. Return an enhanced dispatch function
 * 3. The enhanced dispatch function receives actions and can execute additional logic before and after calling the original dispatch
 *
 * This design ensures that:
 * - Reducer maintains pure function characteristics, focusing on state transformation
 * - Side effects and cross-cutting concerns (like logging, monitoring) can be handled through middleware
 * - State management process can be flexibly extended
 */
fun <S, A> logMiddleware(): Middleware<S, A> = { dispatch, state ->
    // Return an enhanced dispatch function
    { action ->
        // Before calling the original dispatch, log the action and current state
        println("Action: $action, PrevState: $state")

        // Call the original dispatch function, passing the action to the reducer
        dispatch(action)

        // Note: If needed, you can also add post-dispatch logic here
        // For example, logging the updated state: println("NewState: ${state.value}")
    }
}

/**
 * Interactive useReducer demo component
 *
 * This component demonstrates the complete usage process of useReducer in practical applications:
 * 1. Define state and Action
 * 2. Create Reducer function
 * 3. Use useReducer to initialize state and dispatch function
 * 4. Trigger Action dispatch through UI events
 * 5. Use middleware to enhance functionality
 *
 * This example demonstrates the core advantages of useReducer:
 * - Separation of state logic and UI logic - Reducer handles all state update logic
 * - UI components only responsible for rendering and event triggering - no complex state logic
 * - Clear and controllable state update process - explicitly express intent through Action
 */
@Composable
fun InteractiveReducerDemo() {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Interactive Demo",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Using useReducer to manage state
            // This is the core usage of useReducer, it accepts three parameters:
            // 1. reducer function - handles state update logic
            // 2. initialState - initial state
            // 3. middlewares - middleware array, used to enhance dispatch functionality
            val (state, dispatch) = useReducer(
                simpleReducer, // 传入我们定义的 reducer 函数
                initialState = SimpleData("default", 18), // 设置初始状态
                middlewares = arrayOf(
                    logMiddleware(), // 使用日志中间件记录状态变化
                ),
            )
            // useReducer returns:
            // - state: current state as a State<T> object
            // - dispatch: function for dispatching Actions

            // Input field state - using useControllable to manage input field state
            val (input, setInput) = useControllable("")

            // Display current state - show the state content managed by reducer
            Text(
                text = "Current State: ${state.value}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 8.dp),
            )

            // Input field - user can enter a new name
            OutlinedTextField(
                value = input.value,
                onValueChange = setInput,
                label = { Text("Enter a new name") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            )

            // Action buttons - trigger different types of Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // When button is clicked, send Action through dispatch function
                // Note that UI components are only responsible for sending Actions, not handling specific state update logic
                TButton(text = "Change Name") {
                    // Send ChangeName Action, carrying the user input name
                    dispatch(SimpleAction.ChangeName(input.value))
                }

                TButton(text = "Increase Age") {
                    // Send AgeIncrease Action, no additional parameters needed
                    dispatch(SimpleAction.AgeIncrease)
                }
                // After Action is sent, it will be processed by middleware, then handled by reducer function to update state
                // After state update, component will automatically re-render, displaying the latest state
            }
        }
    }
}

/**
 * Basic useReducer usage example
 *
 * This component demonstrates the simplest usage of useReducer, without using middleware, focusing on core functionality:
 * 1. Use useReducer to initialize state and dispatch function
 * 2. Render current state
 * 3. Trigger Action dispatch through buttons
 *
 * This simplified example clearly demonstrates the basic workflow of useReducer:
 * - State is uniformly managed by the reducer function
 * - UI sends Actions through the dispatch function
 * - State updates automatically trigger re-rendering
 */
@Composable
fun BasicReducerExample() {
    // Using useReducer to manage state - simplest form, only providing reducer and initial state
    // Note that no middleware is provided here, demonstrating the most basic usage
    val (state, dispatch) = useReducer(
        simpleReducer, // reducer 函数
        initialState = SimpleData("John", 25), // 初始状态
    )
    // useReducer returns state object and dispatch function

    Column(modifier = Modifier.fillMaxWidth()) {
        // Display current state - read state data directly from state.value
        Text(
            text = "Name: ${state.value.name}, Age: ${state.value.age}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = 8.dp),
        )

        // Action buttons - provide buttons to trigger different Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // When button is clicked, dispatch ChangeName Action, using fixed name 'Alice'
            TButton(text = "Set Name to 'Alice'") {
                dispatch(SimpleAction.ChangeName("Alice"))
            }

            // When button is clicked, dispatch AgeIncrease Action
            TButton(text = "Increase Age") {
                dispatch(SimpleAction.AgeIncrease)
            }
            // Each button click sends Action through dispatch
            // reducer function processes Action and updates state
            // After state update, component automatically re-renders
        }
    }
}

/**
 * Task list component, displaying all task items
 *
 * This component is responsible for rendering the task list, demonstrating how to design child components in the useReducer pattern:
 * 1. Receive data as parameters, rather than directly accessing global state
 * 2. Receive callback functions, rather than directly modifying state
 * 3. Focus on UI rendering, without containing state management logic
 *
 * This design implements separation of concerns:
 * - Parent component (TaskApp) is responsible for state management and Action dispatch
 * - Child component (TaskList) is only responsible for UI rendering and event callbacks
 * - State changes are uniformly handled through Action and reducer
 */
@Composable
fun TaskList(tasks: List<Task>, onChangeTask: (Task) -> Unit, onDeleteTask: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Iterate through task list, rendering a TaskItem for each task
        tasks.forEach { task ->
            // Use key to ensure component correctly recomposes, avoiding state confusion
            key(task.id) {
                // Render single task item, passing task data and callback functions
                // Note: don't directly pass dispatch function, but pass specific callback functions
                // This way child components don't need to know about Actions, just call callback functions
                TaskItem(task = task, onChange = onChangeTask, onDelete = onDeleteTask)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
 * Single task item component, displaying task content and providing edit and delete functionality
 *
 * This component demonstrates how to handle complex UI interactions in the useReducer pattern:
 * 1. Component internally maintains its own local state (edit mode and temporary text)
 * 2. Passes state changes to parent component through callback functions
 * 3. Renders different UI based on state conditions
 *
 * This design implements separation of responsibilities:
 * - Internal component state (such as edit mode) is managed by the component itself
 * - Changes to global state (task data) are passed to the reducer through callback functions
 * - UI rendering logic and interaction logic are handled within the component
 */
@Composable
fun TaskItem(task: Task, onChange: (Task) -> Unit, onDelete: (Int) -> Unit) {
    // Use useState to manage component internal state
    // These states are only used within the component, don't need to be managed through reducer
    var isEditing by useState(false) // Whether in edit mode
    var text by useState(task.text) // Temporary text during editing

    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Task completion status checkbox
            Checkbox(
                checked = task.done,
                onCheckedChange = { checked ->
                    // When checkbox state changes, update task status through callback function
                    // Note: don't directly modify global state, but call callback function
                    // Callback function will eventually trigger Action dispatch and state update
                    onChange(task.copy(done = checked))
                },
            )

            // Render different UI based on edit state condition
            if (isEditing) {
                // Edit mode - display text input field and save button
                TextField(
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    value = text,
                    onValueChange = { newText ->
                        // Update component internal state, don't trigger global state update
                        text = newText
                    },
                    singleLine = true,
                )

                // Save button
                TButton(text = "Save", onClick = {
                    // When saving, check if text is valid
                    if (text.isNotBlank()) {
                        // Update task text through callback function
                        onChange(task.copy(text = text))
                    }
                    // Exit edit mode
                    isEditing = false
                })
            } else {
                // Display mode - show task text and edit button
                Text(
                    text = task.text,
                    // Set different text styles based on task completion status
                    style = if (task.done) {
                        // Completed tasks use faded color display
                        MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.outline)
                    } else {
                        // Uncompleted tasks use normal color
                        MaterialTheme.typography.bodyLarge
                    },
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                )

                // Edit button
                TButton(text = "Edit", onClick = {
                    // Enter edit mode, update component internal state
                    isEditing = true
                })
            }

            // Delete button
            TButton(
                text = "Delete",
                onClick = {
                    // Delete task through callback function
                    // Callback function will eventually trigger TaskAction.Deleted Action
                    onDelete(task.id)
                },
            )
        }
    }
}

/**
 * Task data class
 *
 * In the useReducer pattern, data models are the foundation of state management:
 * 1. Using immutable data classes ensures state immutability
 * 2. Contains all properties that need to be managed (id, text content, completion status)
 * 3. Provides a clear structure, facilitating state updates in the reducer
 *
 * Advantages of immutable data models:
 * - More predictable state changes - by creating new objects rather than modifying existing ones
 * - Easy to debug and track - each state update produces a new object
 * - Supports efficient equality checking - quickly determine changes through reference comparison
 */
data class Task(
    val id: Int, // Task unique identifier
    val text: String, // Task text content
    val done: Boolean, // Task completion status
)

/**
 * Add task component, providing input field and add button
 *
 * This component demonstrates how to handle user input and events in the useReducer pattern:
 * 1. Component internally maintains its own local state (input text)
 * 2. Passes user operations to parent component through callback functions
 * 3. Resets local state after user operations
 *
 * This design implements separation of responsibilities:
 * - Input state is managed by the component itself, without needing to be processed through reducer
 * - Add task logic is passed to parent component through callback functions
 * - Parent component is responsible for converting user operations into Actions and dispatching
 */
@Composable
fun AddTask(onAddTask: (String) -> Unit) {
    // Use useState to manage component internal state (input text)
    // This state is only used within the component, doesn't need to be managed through reducer
    var text by useState("")

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Task input field
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = text,
            onValueChange = { newText ->
                // Update component internal state, don't trigger global state update
                text = newText
            },
            label = { Text("Add task") },
            singleLine = true,
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Add button
        TButton(
            text = "Add",
            onClick = {
                // Check if input is valid
                if (text.isNotBlank()) {
                    // Call callback function, passing input text to parent component
                    // Parent component will convert it to TaskAction.Added Action
                    onAddTask(text)

                    // Reset input text, prepare for next input
                    // Note this is an update of component internal state, not involving global state
                    text = ""
                }
                // If input is empty, don't perform any operation
            },
        )
    }
}

/**
 * Task management application example, demonstrating the application of useReducer in complex scenarios
 *
 * This component demonstrates advanced usage of useReducer in practical applications:
 * 1. Managing complex collection type states (task list)
 * 2. Handling multiple different types of Actions (add, update, delete tasks)
 * 3. Using dispatchAsync to handle asynchronous operations
 * 4. Organizing state management in large applications
 *
 * This example demonstrates the advantages of useReducer in complex applications:
 * - Centralized management of complex state - all task-related state is centralized in one place
 * - Unified state update logic - all task operations are processed through reducer
 * - Support for asynchronous operations - handle asynchronous tasks through dispatchAsync
 * - Good code organization - separation of state logic and UI logic
 */
@Composable
fun TaskApp() {
    // Use useReducer to manage task list state
    // Note that PersistentList<Task> is used as state type, suitable for managing collection type states
    // TaskAction is a sealed interface for all possible operations
    val (tasks, dispatch, dispatchAsync) = useReducer<PersistentList<Task>, TaskAction>(
        // Define reducer function, handling different actions
        { prevState, action ->
            when (action) {
                // Add new task - use immutable collection operations, return new state object
                is TaskAction.Added -> prevState + Task(nextId++, action.text, false)

                // Update existing task - use mutate function to safely modify collection
                is TaskAction.Changed -> prevState.mutate { tasks ->
                    tasks
                        .indexOfFirst { it.id == action.task.id }
                        .takeIf { it != -1 }
                        ?.let { index ->
                            tasks[index] = action.task
                        }
                }

                // Delete task - similarly use mutate function to safely modify collection
                is TaskAction.Deleted -> prevState.mutate { tasks ->
                    tasks.removeAll { it.id == action.taskId }
                }
            }
            // Note: reducer always returns new state object, even when using mutate function
            // mutate function internally creates a new collection object
        },
        // Initial task list
        initialTasks,
        // Middleware array - using log middleware to record all actions
        arrayOf(
            logMiddleware(),
        ),
    )
    // useReducer returns:
    // - tasks: current task list state
    // - dispatch: function for synchronously dispatching Actions
    // - dispatchAsync: function for asynchronously dispatching Actions

    // Handle add task callback function
    fun handleAddTask(text: String) {
        if (text.isNotBlank()) {
            // Use dispatchAsync to handle possible asynchronous operations
            // dispatchAsync receives a suspend function, can execute asynchronous operations within it
            dispatchAsync {
                // Can add asynchronous logic here, for example:
                // delay(2.seconds)  // Simulate network request delay
                // Or call actual API service

                // Finally return the Action to be dispatched
                TaskAction.Added(text)
            }
        }
    }

    // Handle update task callback function
    fun handleChangeTask(task: Task) {
        // Use regular dispatch function to dispatch synchronous Action
        dispatch(TaskAction.Changed(task))
    }

    // Handle delete task callback function
    fun handleDeleteTask(taskId: Int) {
        // Similarly use regular dispatch function to dispatch synchronous Action
        dispatch(TaskAction.Deleted(taskId))
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Task list title
        Text(
            text = "Day off in Kyoto",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Add task component - pass add task callback function
        AddTask(onAddTask = ::handleAddTask)

        Spacer(modifier = Modifier.height(16.dp))

        // Task list - render based on current state condition
        if (tasks.value.isEmpty()) {
            // Empty state prompt
            Text(
                text = "No tasks yet. Add some tasks to get started!",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        } else {
            // Render task list, passing current task list and callback functions
            TaskList(
                tasks = tasks.value,
                onChangeTask = ::handleChangeTask,
                onDeleteTask = ::handleDeleteTask,
            )
        }
        // Note: UI components don't directly modify state, but trigger Actions through callback functions
        // All state update logic is centralized in the reducer function
    }
}

/**
 * Task ID counter, used to generate unique task IDs
 *
 * In practical applications, this type of ID generation would typically be handled by backend services or more complex ID generation strategies.
 * Here we use a simple incrementing counter as an example.
 */
var nextId = 3

/**
 * Initial task list
 *
 * In the useReducer pattern, initial state is an important configuration:
 * 1. Defines initial data when the application starts
 * 2. Uses immutable collections (persistentListOf) to ensure state immutability
 * 3. Provides reasonable initial data, so the application has content to display at startup
 */
val initialTasks = persistentListOf(
    Task(id = 0, text = "Philosopher's Path", done = true),
    Task(id = 1, text = "Visit the temple", done = false),
    Task(id = 2, text = "Drink matcha", done = false),
)

/**
 * Define task-related Action types
 *
 * Action is a core concept in the useReducer pattern, describing "intent" rather than "how to implement":
 * 1. Using sealed interface to define all possible Action types
 * 2. Each Action type contains the data needed to perform the operation
 * 3. Action only describes "what to do", not the logic of "how to do it"
 *
 * Advantages of this design:
 * - Type safety - compiler can check all possible Action types
 * - Clear intent - each Action has a clear meaning and purpose
 * - Centralized state logic - all state handling logic is in the reducer, not scattered everywhere
 * - Easy to test and debug - can record and replay Action sequences
 */
sealed interface TaskAction {
    /**
     * Add task Action
     *
     * This Action only needs to contain task text, other information (such as ID and completion status)
     * will be handled in the reducer function.
     *
     * @param text Task text
     */
    data class Added(
        val text: String,
    ) : TaskAction

    /**
     * Update task Action
     *
     * This Action contains the complete updated task object, the reducer function will
     * find the corresponding task based on task ID and update it.
     *
     * @param task Updated task object
     */
    data class Changed(
        val task: Task,
    ) : TaskAction

    /**
     * Delete task Action
     *
     * This Action only needs to contain the task ID to be deleted, the reducer function will
     * find and delete the corresponding task based on ID.
     *
     * @param taskId Task ID to be deleted
     */
    data class Deleted(
        val taskId: Int,
    ) : TaskAction
}
