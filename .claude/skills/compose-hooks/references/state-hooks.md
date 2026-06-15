# 状态管理 Hooks

## 目录

- [useState](#usestate)
- [useStateAsync](#usestateasync)
- [useGetState](#usegetstate)
- [useControllable](#usecontrollable)
- [useBoolean](#useboolean)
- [useToggle](#usetoggle)
- [useReducer](#usereducer)
- [useRef](#useref)
- [useLatest](#uselatest)
- [useLastChanged](#uselastchanged)
- [useList](#uselist)
- [useMap](#usemap)
- [useImmutableList](#useimmutablelist)
- [useSorted](#usesorted)
- [useCycleList](#usecyclelist)
- [useSelectable](#useselectable)
- [useResetState](#useresetstate)
- [useAutoReset](#useautoreset)
- [usePrevious](#useprevious)
- [useCreation](#usecreation)
- [usePersistent](#usepersistent)
- [useContext](#usecontext)
- [useSelector/useDispatch](#useselectorusedispatch)
- [useStateMachine](#usestatemachine)
- [数值类型 Hooks](#数值类型-hooks)

---

## useState

基础状态管理 Hook。

**重要**：useState 解构使用时存在闭包问题和快速更新丢失问题，推荐：
- 需要解构时使用 `useGetState`
- 使用 `by` 委托访问
- 需要异步初始化时使用 `useStateAsync`

```kotlin
// 推荐：使用 by 委托
var state by useState("initial")
state = "new value"

// 直接访问 MutableState
val state = useState("initial")
state.value = "new value"

// 解构使用（有闭包问题，不推荐在协程中使用）
val (state, setState) = useState("initial")
setState("new value")
```

**闭包问题示例**：
```kotlin
// 错误：协程中解构值被捕获，不会更新
val (state, setState) = useState("initial")
LaunchedEffect(Unit) {
    repeat(10) {
        delay(1.seconds)
        setState("$state.")  // state 始终是 "initial"
    }
}

// 正确：使用 by 委托
var state by useState("initial")
LaunchedEffect(Unit) {
    repeat(10) {
        delay(1.seconds)
        state += "."  // 正常工作
    }
}
```

### 派生状态重载

`useState` 有一个接受 `keys` + `factory` 的重载，内部封装了 `derivedStateOf`，返回只读 `State<T>`。它是项目的计算属性，等价于 `remember(keys) { derivedStateOf(factory) }`。

```kotlin
// 基础用法：依赖变化时重新计算
val fullName by useState(firstName, lastName) {
    "$firstName $lastName"
}

// 列表过滤/派生
val filteredList by useState(searchText, sourceList) {
    if (searchText.isEmpty()) sourceList
    else sourceList.filter { it.contains(searchText) }
}
```

**适用场景**：
- 替代直接使用 `derivedStateOf`（项目规范要求用 `useState` 代替）
- 当需要基于其他状态派生出新的只读状态时
- 与 `useEffect` 不同，派生状态是同步计算、惰性求值的，不执行副作用

**注意**：此重载返回 `State<T>`（只读），不是 `MutableState`，不支持 `by` 赋值。

---

## useStateAsync

异步初始化状态，适用于需要从数据库或网络获取初始值的场景。

```kotlin
val state = useStateAsync {
    // 在协程中异步获取初始值
    database.getDefaultValue()
}

// state.value 在初始化完成前为 null，完成后为实际值
Text("值: ${state.value ?: "加载中..."}")
```

---

## useGetState

增强版 useState，解决闭包问题和快速更新问题。

```kotlin
val (state, setState, getState) = useGetState(0)

// 直接设置值
setState(5)

// 函数式更新（推荐）
setState { current -> current + 1 }

// 获取最新值（在闭包中使用）
val currentValue = getState()
```

**适用场景**：
- 协程/回调中需要访问最新状态
- 快速连续更新状态

---

## useControllable

受控/非受控组件模式。当父组件传入值时使用受控模式，否则使用内部状态。

```kotlin
// 受控模式：父组件传入 value
@Composable
fun MyTextField(
    value: String? = null,  // 可选，不传则为非受控模式
    onValueChange: (String) -> Unit = {}
) {
    val (state, setValue, getState) = useControllable(value ?: "")
    
    OutlinedTextField(
        value = state.value,
        onValueChange = { newValue ->
            setValue(newValue)
            onValueChange(newValue)
        }
    )
}

// 非受控模式
MyTextField()  // 使用内部状态

// 受控模式
MyTextField(value = externalValue, onValueChange = { updateExternal(it) })
```

---

## useBoolean

布尔状态管理，提供便捷的切换方法。

```kotlin
val (state, toggle, setValue, setTrue, setFalse) = useBoolean(false)

toggle()        // 切换状态
setTrue()       // 设置为 true
setFalse()      // 设置为 false
setValue(true)  // 直接设置值
```

**典型用法**（来自 UseBooleanExample.kt）：
```kotlin
val (darkMode, toggleDarkMode, _, _, _) = useBoolean(false)
val (notifications, toggleNotifications, _, _, _) = useBoolean(true)

Row(verticalAlignment = Alignment.CenterVertically) {
    Text(text = "Dark Mode")
    Spacer(modifier = Modifier.weight(1f))
    Switch(checked = darkMode.value, onCheckedChange = { toggleDarkMode() })
}
```

---

## useToggle

在两个值之间切换。

```kotlin
// 基础用法
val (state, toggle, setValue) = useToggle("A", "B")
toggle()  // A -> B -> A

// 不同类型切换
val (state, toggle, setValue) = useToggleEither("left", 100)

// 可见性切换（单内容）
val (content, toggle) = useToggleVisible(content = { Text("Hello") })
toggle()  // 显示/隐藏

// 可见性切换（双内容）
val (content, toggle) = useToggleVisible(
    content1 = { Text("内容1") },
    content2 = { Text("内容2") }
)
toggle()  // 切换内容
```

---

## useReducer

Redux 风格状态管理，适合复杂状态逻辑。

```kotlin
// 1. 定义 State
data class CounterState(val count: Int)

// 2. 定义 Action
sealed interface CounterAction {
    data object Increment : CounterAction
    data object Decrement : CounterAction
    data class SetValue(val value: Int) : CounterAction
}

// 3. 定义 Reducer
val counterReducer: Reducer<CounterState, CounterAction> = { state, action ->
    when (action) {
        is CounterAction.Increment -> state.copy(count = state.count + 1)
        is CounterAction.Decrement -> state.copy(count = state.count - 1)
        is CounterAction.SetValue -> state.copy(count = action.value)
    }
}

// 4. 使用
val (state, dispatch, dispatchAsync) = useReducer(
    counterReducer,
    initialState = CounterState(0),
    middlewares = arrayOf(logMiddleware())  // 可选中间件
)

dispatch(CounterAction.Increment)

// 异步 dispatch
dispatchAsync {
    delay(1.seconds)
    CounterAction.SetValue(100)
}
```

**中间件示例**：
```kotlin
fun <S, A> logMiddleware(): Middleware<S, A> = { dispatch, state ->
    { action ->
        println("Action: $action, State: $state")
        dispatch(action)
    }
}
```

---

## useRef

创建不触发重组的可变引用。

```kotlin
val countRef = useRef(0)

// 读写不触发重组
countRef.current += 1
println(countRef.current)

// 使用 by 委托
var count by useRef(0)
count += 1
```

**适用场景**：
- 存储不需要触发 UI 更新的值
- 保存定时器 ID、DOM 引用等

---

## useLatest

始终返回最新值的引用，解决闭包问题。

```kotlin
val (state, setState) = useState(0)
val stateRef = useLatestRef(state)

useEffect {
    delay(1.seconds)
    // stateRef.current 始终是最新值
    println(stateRef.current)
}
```

### useLatestState

始终返回最新 State 值的引用（State 版本）。

```kotlin
val state = useState(0)
val latestState = useLatestState(state)

useEffect {
    delay(1.seconds)
    println(latestState.current.value)
}
```

---

## useLastChanged

追踪值的最后变更时间。

```kotlin
val (count, setCount) = useState(0)
val lastChanged = useLastChanged(count)

// count 变化时，lastChanged 自动更新为当前时间
Text("最后修改: ${lastChanged.value}")

Button(onClick = { setCount { it + 1 } }) {
    Text("增加")
}
```

---

## useList

响应式列表状态管理。

```kotlin
val list = useList(1, 2, 3)
// 或
val list = useList(listOf(1, 2, 3))

// 操作方法
list.add(4)              // 添加
list.add(0, 0)           // 插入
list.removeAt(0)         // 删除
list.removeLast()        // 删除最后一个
list[0] = 10             // 修改
list.clear()             // 清空
list.shuffle()           // 打乱

// 配合 useListReduce
val sum by useListReduce(list) { a, b -> a + b }
```

---

## useMap

响应式 Map 状态管理。

```kotlin
val map = useMap("key1" to "value1", "key2" to "value2")

// 操作方法
map["key3"] = "value3"   // 添加/修改
map.remove("key1")       // 删除
map.clear()              // 清空
```

---

## useImmutableList

不可变列表状态管理，使用 kotlinx.collections.immutable。

```kotlin
val (list, mutate) = useImmutableList(1, 2, 3)

// 添加元素
mutate { it.add(4) }

// 删除元素
mutate { it.removeAll { it > 2 } }

// 更新元素
mutate { it.replaceAll { it * 2 } }

// 访问当前列表
val currentList = list.value
```

---

## useSorted

列表排序 Hook。

```kotlin
val list = listOf(3, 1, 4, 1, 5)
val sorted by useSorted(list) { a, b -> a.compareTo(b) }

// 或使用配置
val sorted by useSorted(list) {
    compareFn = { a, b -> a.compareTo(b) }
    dirty = false  // 是否修改原列表
}
```

---

## useCycleList

循环列表，在列表元素间循环切换。

```kotlin
val (current, next, prev, go) = useCycleList(listOf("A", "B", "C"))

// current.value = "A"
next()  // "B"
next()  // "C"
next()  // "A" (循环)
prev()  // "C"
go(1)   // "B" (跳转到索引)
```

---

## useSelectable

选择/多选功能。

```kotlin
data class Item(val id: Int, val name: String)
val items = listOf(Item(1, "A"), Item(2, "B"), Item(3, "C"))

// 单选
val (selected, select, isSelected) = useSelectable(
    selectionMode = SelectionMode.Single,
    items = items,
    keyProvider = { it.id }
)

// 多选
val (selected, toggle, isSelected, selectAll, clearAll) = useSelectable(
    selectionMode = SelectionMode.Multiple,
    items = items,
    keyProvider = { it.id }
)
```

---

## useResetState

带重置功能的状态管理。

```kotlin
val (state, setState, reset) = useResetState("initial")

setState("changed")
reset()  // 重置为 "initial"
```

---

## useAutoReset

自动重置状态，在指定时间后恢复默认值。

```kotlin
val (state, setState) = useAutoReset("default", 3.seconds)

setState("temporary")  // 3秒后自动恢复为 "default"
```

---

## usePrevious

获取状态的前一个值。

```kotlin
val (state, setState) = useState(0)
val previous = usePrevious(state)

// state = 5, previous.value = 0
// state = 10, previous.value = 5
```

---

## useCreation

创建复杂对象，类似 useMemo 但更可靠。

```kotlin
// 无依赖，只创建一次
val service = useCreation {
    ExpensiveService()
}

// 有依赖，依赖变化时重新创建
val (userId) = useState("")
val userProfile = useCreation(userId) {
    UserProfile(userId)
}
```

---

## usePersistent

轻量级持久化状态。

```kotlin
// 需要先配置持久化方法
HooksConfig.persistentSaver = { key, value -> /* 保存 */ }
HooksConfig.persistentReader = { key -> /* 读取 */ }

val (state, setState) = usePersistent("key", "default")
```

---

## useContext

类似 React 的 Context，跨组件共享状态。

```kotlin
// 1. 创建 Context
val ThemeContext = createContext("light")

// 2. 提供值
ThemeContext.Provider("dark") {
    ChildComponent()
}

// 3. 消费值
@Composable
fun ChildComponent() {
    val theme = useContext(ThemeContext)
    // theme = "dark"
}
```

---

## useSelector/useDispatch

全局状态管理，类似 Redux-React。

```kotlin
// 1. 定义 Store
data class AppState(val count: Int, val user: String)
sealed interface AppAction { /* ... */ }

val appStore = createStore(
    reducer = appReducer,
    initialState = AppState(0, "")
)

// 2. 提供 Store
StoreProvider(appStore) {
    App()
}

// 3. 使用
@Composable
fun Counter() {
    val count by useSelector<AppState, Int> { count }
    val dispatch = useDispatch<AppAction>()

    Button(onClick = { dispatch(AppAction.Increment) }) {
        Text("Count: $count")
    }
}
```

### useDispatchAsync

异步 dispatch，用于在 reducer 中执行异步操作。

```kotlin
@Composable
fun AsyncCounter() {
    val dispatchAsync = useDispatchAsync<AppAction>()

    Button(onClick = {
        dispatchAsync {
            delay(1.seconds)  // 异步操作
            AppAction.SetValue(100)
        }
    }) {
        Text("异步更新")
    }
}
```

---

## useStateMachine

状态机管理。

```kotlin
// 1. 定义状态和事件
sealed class State {
    object Idle : State()
    object Loading : State()
    data class Success(val data: String) : State()
    data class Error(val message: String) : State()
}

sealed class Event {
    object Load : Event()
    data class LoadSuccess(val data: String) : Event()
    data class LoadError(val message: String) : Event()
}

// 2. 定义状态机
val machineGraph = useRef(
    MachineGraph<State, Event, Unit>(State.Idle) {
        state<State.Idle> {
            on<Event.Load> { transitionTo(State.Loading) }
        }
        state<State.Loading> {
            on<Event.LoadSuccess> { transitionTo(State.Success(it.data)) }
            on<Event.LoadError> { transitionTo(State.Error(it.message)) }
        }
    }
)

// 3. 使用
val (currentState, send) = useStateMachine(machineGraph)

send(Event.Load)
```

---

## 数值类型 Hooks

专用于特定数值类型的轻量状态 Hook，避免装箱开销。

### useInt

```kotlin
val count = useInt(0)
count.intValue++
Text("Count: ${count.intValue}")
```

### useLong

```kotlin
val id = useLong(0L)
id.longValue = 123456789L
Text("ID: ${id.longValue}")
```

### useFloat

```kotlin
val ratio = useFloat(0f)
ratio.floatValue = 0.5f
Text("Ratio: ${ratio.floatValue}")
```

### useDouble

```kotlin
val price = useDouble(0.0)
price.doubleValue = 99.99
Text("Price: ${price.doubleValue}")
```

**说明**：这些 Hooks 返回 Compose 的 `MutableIntState`/`MutableLongState`/`MutableFloatState`/`MutableDoubleState`，适用于对性能要求较高的数值场景。
