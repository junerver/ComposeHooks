package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.seconds
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus
import kotlinx.coroutines.delay
import xyz.junerver.compose.hooks.*
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description:
  Author: Junerver
  Date: 2024/3/11-9:56
  Email: junerver@gmail.com
  Version: v1.0
*/
data class SimpleData(
    val name: String,
    val age: Int,
)

sealed interface SimpleAction {
    data class ChangeName(val newName: String) : SimpleAction
    data object AgeIncrease : SimpleAction
}

val simpleReducer: Reducer<SimpleData, SimpleAction> =
    { prevState: SimpleData, action: SimpleAction ->
        when (action) {
            is SimpleAction.ChangeName -> prevState.copy(name = action.newName)
            is SimpleAction.AgeIncrease -> prevState.copy(age = prevState.age + 1)
        }
    }

@Composable
fun UseReducerExample() {
    val (state, dispatch) = useReducer(
        simpleReducer,
        initialState = SimpleData("default", 18),
        middlewares = arrayOf(
            logMiddleware()
        )
    )
    val (input, setInput) = useGetState("")
    Surface {
        Column {
            Text(text = "User: ${state.value}")
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(value = input.value, onValueChange = setInput.left<String>())
            TButton(text = "changeName") {
                dispatch(SimpleAction.ChangeName(input.value))
            }
            TButton(text = "+1") {
                dispatch(SimpleAction.AgeIncrease)
            }
            TaskApp()
        }
    }
}

// 示例日志中间件
fun <S, A> logMiddleware(): Middleware<S, A> {
    return { dispatch, state ->
        { action ->
            println("Action: $action, PrevState: $state")
            dispatch(action)
        }
    }
}

@Composable
fun TaskList(tasks: List<Task>, onChangeTask: (Task) -> Unit, onDeleteTask: (Int) -> Unit) {
    tasks.forEach { task ->
        key(task.id) {
            TaskItem(task = task, onChange = onChangeTask, onDelete = onDeleteTask)
        }
    }
}

@Composable
fun TaskItem(task: Task, onChange: (Task) -> Unit, onDelete: (Int) -> Unit) {
    var isEditing by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf(task.text) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = task.done,
            onCheckedChange = { checked ->
                onChange(task.copy(done = checked))
            }
        )

        if (isEditing) {
            TextField(
                modifier = Modifier.width(300.dp),
                value = text,
                onValueChange = { newText ->
                    text = newText
                    onChange(task.copy(text = newText))
                }
            )
            Button(onClick = { isEditing = false }) {
                Text("Save")
            }
        } else {
            Text(text = task.text)
            Button(onClick = { isEditing = true }) {
                Text("Edit")
            }
        }

        Button(onClick = { onDelete(task.id) }) {
            Text("Delete")
        }
    }
}

data class Task(val id: Int, val text: String, val done: Boolean)

@Composable
fun AddTask(onAddTask: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    Row {
        TextField(
            modifier = Modifier.width(300.dp),
            value = text,
            onValueChange = { newText ->
                text = newText
            },
            placeholder = { Text("Add task") }
        )
        Button(onClick = {
            onAddTask(text)
            text = ""
        }) {
            Text("Add")
        }
    }
}

@Composable
fun TaskApp() {
    val (tasks, dispatch, dispatchAsync) = useReducer<PersistentList<Task>, TaskAction>(
        { prevState, action ->
            when (action) {
                is TaskAction.Added -> prevState + Task(nextId++, action.text, false)
                is TaskAction.Changed -> prevState.mutate { tasks ->
                    tasks.indexOfFirst { it.id == action.task.id }
                        .takeIf { it != -1 }
                        ?.let { index ->
                            tasks[index] = action.task
                        }
                }

                is TaskAction.Deleted -> prevState.mutate { tasks ->
                    tasks.removeIf { it.id == action.taskId }
                }
            }
        },
        initialTasks,
        arrayOf(
            logMiddleware()
        )
    )

    fun handleAddTask(text: String) {
        dispatchAsync {
            delay(2.seconds)
            TaskAction.Added(text)
        }
    }

    fun handleChangeTask(task: Task) {
        dispatch(TaskAction.Changed(task))
    }

    fun handleDeleteTask(taskId: Int) {
        dispatch(TaskAction.Deleted(taskId))
    }

    Column {
        Text(text = "Day off in Kyoto", style = MaterialTheme.typography.titleLarge)
        AddTask(onAddTask = ::handleAddTask)
        TaskList(
            tasks = tasks.value,
            onChangeTask = ::handleChangeTask,
            onDeleteTask = ::handleDeleteTask
        )
    }
}

var nextId = 3
val initialTasks = persistentListOf(
    Task(id = 0, text = "Philosopher’s Path", done = true),
    Task(id = 1, text = "Visit the temple", done = false),
    Task(id = 2, text = "Drink matcha", done = false)
)

sealed interface TaskAction {
    data class Added(val text: String) : TaskAction
    data class Changed(val task: Task) : TaskAction
    data class Deleted(val taskId: Int) : TaskAction
}
