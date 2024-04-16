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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.Reducer
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.useredux.createStore
import xyz.junerver.compose.hooks.useredux.useDispatch
import xyz.junerver.compose.hooks.useredux.useDispatchAsync
import xyz.junerver.compose.hooks.useredux.useSelector
import xyz.junerver.composehooks.MainActivity
import xyz.junerver.composehooks.net.NetApi
import xyz.junerver.composehooks.net.bean.UserInfo
import xyz.junerver.composehooks.ui.component.TButton
import xyz.junerver.composehooks.utils.NanoId

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
val fetchReducer: Reducer<NetFetchResult, NetFetchResult> = { _, action ->
    action
}

/**
 * 通过使用[createStore]函数创建状态存储对象
 *
 * Create a state store object by using the [createStore] function
 */
val store = createStore(arrayOf(logMiddleware())) {
    simpleReducer with SimpleData("default", 18)
    todoReducer with emptyList()
    arrayOf("fetch1", "fetch2").forEach {
        named(it) {
            fetchReducer with NetFetchResult.Idle
        }
    }
}

@Composable
fun UseReduxExample() {
    /** store provide by root component,see at [MainActivity] */
    Surface {
        Column(
            modifier = Modifier
                .padding(20.dp)
        ) {
            SimpleDataContainer()
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 20.dp)
            )
            TodosListContainer()
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 20.dp)
            )
            UseReduxFetch()
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 20.dp)
            )
            UseReduxFetch2()
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
    /**
     * 通过[useSelector]函数可以快速获取 store 中保存的对应的状态对象；
     *
     * The corresponding state object saved in the store can be quickly
     * obtained through the [useSelector] function;
     */
    val todos = useSelector<List<Todo>>()
    Column {
        todos.map {
            TodoItem(item = it)
        }
    }
}

@Composable
private fun Header() {
    /**
     * 通过[useDispatch]可以快速获取对应Action的 dispatch 函数
     *
     * You can quickly obtain the dispatch function corresponding to the Action
     * through [useDispatch]
     */
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
    /**
     * 使用[useSelector]的另一个重载，你可以轻松的对状态进行变形，或者只取状态对象的部分属性作为你要关注的状态；
     *
     * Using another overload of [use Selector], you can easily transform the
     * state, or only take some attributes of the state object as the state you
     * want to focus on;
     */
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

    /**
     * 使用[useDispatchAsync]你可以活动一个异步的dispatch函数，它允许你在当前组件的协程中执行异步操作，
     * 异步函数的返回值是Action。
     *
     * Using [useDispatchAsync] you can activate an asynchronous dispatch
     * function, which allows you to perform asynchronous operations in the
     * coroutine of the current component. The return value of the asynchronous
     * function is Action.
     */
    val asyncDispatch = useDispatchAsync<SimpleAction>()
    Column {
        OutlinedTextField(value = input, onValueChange = setInput)
        Row {
            val scope = rememberCoroutineScope()
            TButton(text = "changeName") {
                scope.launch {
                    // 异步任务
                    delay(1.seconds)
                    dispatch(SimpleAction.ChangeName(input))
                }
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

sealed interface NetFetchResult {
    data class Success<T>(val data: T) : NetFetchResult
    data class Error(val msg: Throwable) : NetFetchResult
    data object Idle : NetFetchResult
    data object Loading : NetFetchResult
}

@Composable
fun UseReduxFetch() {
    val fetchResult: NetFetchResult = useSelector("fetch1")
    val dispatchFetch = useFetch<String>("fetch1")
    Column {
        Text(text = "result: $fetchResult")
        TButton(text = "fetch") {
            dispatchFetch {
                delay(2.seconds)
                error("fetch error")
            }
        }
    }
}

@Composable
fun UseReduxFetch2() {
    val fetchResult: NetFetchResult = useSelector("fetch2")
    val dispatchFetch = useFetch<UserInfo>("fetch2")
    Column {
        Text(text = "result: $fetchResult")
        TButton(text = "fetch2") {
            dispatchFetch {
                NetApi.SERVICE.userInfo("junerver")
            }
        }
    }
}

typealias ReduxFetch<T> = (block: suspend CoroutineScope.() -> T) -> Unit

@Composable
fun <T> useFetch(alias: String): ReduxFetch<T> {
    val dispatchAsync =
        useDispatchAsync<NetFetchResult>(alias, onBefore = { it(NetFetchResult.Loading) })
    return { block ->
        dispatchAsync {
            try {
                NetFetchResult.Success(block())
            } catch (t: Throwable) {
                NetFetchResult.Error(t)
            }
        }
    }
}
