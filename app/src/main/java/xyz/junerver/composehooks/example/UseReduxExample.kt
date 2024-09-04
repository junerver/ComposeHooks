package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.seconds
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.Reducer
import xyz.junerver.compose.hooks.useGetState
import xyz.junerver.compose.hooks.useMount
import xyz.junerver.compose.hooks.useRef
import xyz.junerver.compose.hooks.useredux.createStore
import xyz.junerver.compose.hooks.useredux.useDispatch
import xyz.junerver.compose.hooks.useredux.useDispatchAsync
import xyz.junerver.compose.hooks.useredux.useSelector
import xyz.junerver.composehooks.MainActivity
import xyz.junerver.composehooks.net.NetApi
import xyz.junerver.composehooks.ui.component.TButton
import xyz.junerver.composehooks.utils.NanoId
import xyz.junerver.kotlin.Tuple2
import xyz.junerver.kotlin.tuple

data class Todo(val name: String, val id: String)

sealed interface TodoAction
data class AddTodo(val todo: Todo) : TodoAction
data class DelTodo(val id: String) : TodoAction

val todoReducer: Reducer<PersistentList<Todo>, TodoAction> =
    { prevState: PersistentList<Todo>, action: TodoAction ->
        when (action) {
            is AddTodo -> prevState + action.todo

            is DelTodo -> prevState.mutate { mutator ->
                mutator.removeIf { it.id == action.id }
            }
        }
    }
val fetchReducer: Reducer<NetFetchResult<*>, NetFetchResult<*>> = { _, action ->
    action
}

/**
 * 通过使用[createStore]函数创建状态存储对象
 *
 * Create a state store object by using the [createStore] function
 */
val simpleStore = createStore(arrayOf(logMiddleware())) {
    simpleReducer with SimpleData("default", 18)
    todoReducer with persistentListOf()
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
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 20.dp)
            )
            TodosListContainer()
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 20.dp)
            )
            UseReduxFetch()
            HorizontalDivider(
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
    val todos = useSelector<PersistentList<Todo>>()
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
    val (input, setInput) = useGetState("")
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
    val (input, setInput) = useGetState("")
    val dispatch = useDispatch<SimpleAction>()

    /**
     * 使用[useDispatchAsync]你可以获得一个异步的dispatch函数，它允许你在当前组件的协程中执行异步操作，
     * 异步函数的返回值是Action。
     *
     * Using [useDispatchAsync] you can obtain an asynchronous dispatch
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
            TButton(text = "+1") {
                dispatch(SimpleAction.AgeIncrease)
            }
        }
    }
}

sealed interface NetFetchResult<out T> {
    data class Success<T>(val data: T) : NetFetchResult<T>
    data class Error(val msg: Throwable) : NetFetchResult<Nothing>
    data object Idle : NetFetchResult<Nothing>
    data object Loading : NetFetchResult<Nothing>
}

@Composable
fun UseReduxFetch() {
    val (fetchResult, fetch) = useFetchError()
    Column {
        Text(text = "delay 2 seconds, throw error\nresult: $fetchResult")
        TButton(text = "fetch") {
            fetch()
        }
    }
}

@Composable
fun UseReduxFetch2() {
    val (fetchResult, fetch) = useFetchUserInfo()
    Column {
        TButton(text = "fetch2") {
            fetch()
        }
        when (fetchResult) {
            is NetFetchResult.Error -> {
                Text("err: ${fetchResult.msg}")
            }

            NetFetchResult.Idle -> {
                Text(text = "idel")
            }

            NetFetchResult.Loading -> {
                Text(text = "loading")
            }

            is NetFetchResult.Success -> {
                Text(text = "succ: ${fetchResult.data}")
            }
        }
    }
}

typealias ReduxFetch<T> = (block: suspend CoroutineScope.() -> T) -> Unit

/**
 * 封住处理请求前loading、请求错误
 *
 * @param alias
 * @param T
 * @return
 */
@Composable
fun <T> useFetch(alias: String): ReduxFetch<T> {
    val dispatchAsync =
        useDispatchAsync<NetFetchResult<T>>(alias, onBefore = { it(NetFetchResult.Loading) })
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

/**
 * 更加工程化的示例
 *
 * More engineering example
 */

private const val FetchAlias1 = "fetch1"
private const val FetchAlias2 = "fetch2"

private val NetworkFetchAliases = arrayOf(
    FetchAlias1,
    FetchAlias2
)

val fetchStore = createStore {
    NetworkFetchAliases.forEach {
        named(it) {
            fetchReducer with NetFetchResult.Idle
        }
    }
}

@Composable
private fun <T> useFetchAliasFetch(
    alias: String,
    autoFetch: Boolean = false,
    errorRetry: Int = 0,
    block: suspend CoroutineScope.() -> T,
): Tuple2<NetFetchResult<T>, () -> Unit> {
    val fetchResult: NetFetchResult<T> = useSelector(alias)
    val dispatchFetch = useFetch<T>(alias)
    val retryCount = useRef(errorRetry)
    val fetch = {
        dispatchFetch(block)
    }
    // 挂载时自动请求
    useMount {
        if (autoFetch) fetch()
    }
    // 错误重试
    when (fetchResult) {
        is NetFetchResult.Error -> {
            if (retryCount.current > 0) {
                fetch()
                retryCount.current--
            }
        }
        else -> {}
    }
    return tuple(
        first = fetchResult,
        second = fetch
    )
}

@Composable
private fun useFetchError() = useFetchAliasFetch(alias = FetchAlias1, errorRetry = 3) {
    delay(2.seconds)
    error("fetch error")
}

@Composable
private fun useFetchUserInfo(user: String = "junerver") = useFetchAliasFetch(alias = FetchAlias2, autoFetch = true) {
    NetApi.SERVICE.userInfo(user)
}
