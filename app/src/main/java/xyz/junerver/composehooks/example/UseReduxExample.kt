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
import xyz.junerver.compose.hooks.Reducer
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.useredux.createStore
import xyz.junerver.compose.hooks.useredux.useDispatch
import xyz.junerver.compose.hooks.useredux.useSelector
import xyz.junerver.composehooks.MainActivity
import xyz.junerver.composehooks.ui.component.TButton

/**
 * Description:
 *
 * @author Junerver date: 2024/3/11-9:56 Email: junerver@gmail.com Version:
 *     v1.0
 */
data class OtherData(
    val name: String,
    val age: Int,
)

sealed interface OtherAction {
    data class ChangeName(val newName: String) : OtherAction
    data object AgeIncrease : OtherAction
}

val otherReducer: Reducer<OtherData, OtherAction> = { prevState: OtherData, action: OtherAction ->
    when (action) {
        is OtherAction.ChangeName -> prevState.copy(name = action.newName)
        is OtherAction.AgeIncrease -> prevState.copy(age = prevState.age + 1)
    }
}

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
    otherReducer with OtherData("default", 18)
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
            Divider(modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp))
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
            onValueChange = setInput,
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
        Spacer(modifier = Modifier.height(10.dp))
        SubSimpleDataDispatch()
    }
}

@Composable
private fun SubSimpleDataStateText() {
    val state = useSelector<OtherData>()
    Text(text = "User: $state")
}

@Composable
private fun SubSimpleDataDispatch() {
    val (input, setInput) = useState("")
    val dispatch = useDispatch<OtherAction>()
    Column {
        OutlinedTextField(value = input, onValueChange = setInput)
        TButton(text = "changeName") {
            dispatch(OtherAction.ChangeName(input))
        }
        TButton(text = "+1") {
            dispatch(OtherAction.AgeIncrease)
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
