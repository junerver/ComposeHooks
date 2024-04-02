package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import xyz.junerver.compose.hooks.Reducer
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.useredux.createStore
import xyz.junerver.compose.hooks.useredux.useDispatch
import xyz.junerver.compose.hooks.useredux.useDispatchAsync
import xyz.junerver.compose.hooks.useredux.useSelector
import xyz.junerver.composehooks.MainActivity
import xyz.junerver.composehooks.ui.component.TButton

data class Todo(val name: String, val id: String)

sealed interface TodoAction
data class AddTodo(val todo: Todo) : TodoAction
data class DelTodo(val id: String) : TodoAction

val todoReducer: Reducer<List<Todo>, TodoAction> = { prevState: List<Todo>, action: TodoAction ->
    when (action) {
        is AddTodo -> buildList {
            addAll(prevState)
            add(action.todo)
        }

        is DelTodo -> prevState.filter { it.id != action.id }
    }
}

val store = createStore {
    simpleReducer with SimpleData("default", 18)
    todoReducer with emptyList()
}

@Composable
fun UseReduxExample() {
    /**
     * store provide by root component,see at [MainActivity]
     */
    Surface {
        Column {
            SimpleDataContainer()
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            )
            TodosListContainer()
        }
    }
}

@Composable
private fun TodosListContainer() {
    Column {
        Header()
        TodoList()
    }
}

@Composable
fun TodoList() {
    val todos = useSelector<List<Todo>>()
    Column {
        todos.map {
            TodoItem(item = it)
        }
    }
}

@Composable
private fun Header() {
    val dispatch = useDispatch<TodoAction>()
    val (input, setInput) = useState("")
    Row {
        OutlinedTextField(
            value = input,
            onValueChange = setInput
        )
        TButton(text = "add") {
            dispatch(AddTodo(Todo(input, NanoId.generate())))
            setInput("")
        }
    }
}

@Composable
private fun TodoItem(item: Todo) {
    val dispatch = useDispatch<TodoAction>()
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = item.name)
        TButton(text = "del") {
            dispatch(DelTodo(item.id))
        }
    }
}

@Composable
private fun SimpleDataContainer() {
    Column {
        SubSimpleDataStateText()
        SubSimpleDataStateText2()
        Spacer(modifier = Modifier.height(10.dp))
        SubSimpleDataDispatch()
    }
}

@Composable
private fun SubSimpleDataStateText() {
    val name = useSelector<SimpleData, String> { name }
    Text(text = "User Name: $name")
}

@Composable
private fun SubSimpleDataStateText2() {
    val age = useSelector<SimpleData, String> { "age : $age" }
    Text(text = "User $age")
}

@Composable
private fun SubSimpleDataDispatch() {
    val (input, setInput) = useState("")
    val dispatch = useDispatch<SimpleAction>()
    val asyncDispatch = useDispatchAsync<SimpleAction>()
    Column {
        OutlinedTextField(value = input, onValueChange = setInput)
        Row {
            TButton(text = "changeName") {
                dispatch(SimpleAction.ChangeName(input))
            }
            TButton(text = "Async changeName") {
                asyncDispatch {
                    delay(1.seconds)
                    SimpleAction.ChangeName(input)
                }
            }
        }
        TButton(text = "+1") {
            dispatch(SimpleAction.AgeIncrease)
        }
    }
}

object NanoId {
    private const val ALPHABET = "_~0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val DEFAULT_SIZE = 8
    fun generate(size: Int = DEFAULT_SIZE): String {
        val builder = StringBuilder(size)
        repeat(size) {
            val randomIndex = Random.nextInt(ALPHABET.length)
            builder.append(ALPHABET[randomIndex])
        }
        return builder.toString()
    }
}
