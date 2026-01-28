# 工具 Hooks

## 目录

- [useDebounce / useDebounceFn](#usedebounce--usedebouncefn)
- [useThrottle / useThrottleFn](#usethrottle--usethrottlefn)
- [useInterval](#useinterval)
- [useTimeoutFn](#usetimeoutfn)
- [useTimeoutPoll](#usetimeoutpoll)
- [useCountdown](#usecountdown)
- [useCounter](#usecounter)
- [useUndo](#useundo)
- [useAsync](#useasync)
- [useUpdate](#useupdate)
- [useEvent](#useevent)
- [useClipboard](#useclipboard)
- [useCycleList](#usecyclelist)
- [useSelectable](#useselectable)
- [useSorted](#usesorted)
- [useNow / useTimestamp](#usenow--usetimestamp)
- [useDateFormat / useTimeAgo](#usedateformat--usetimeago)
- [useMath](#usemath)

---

## useDebounce / useDebounceFn

防抖处理。

### useDebounce - 防抖值

```kotlin
val (input, setInput) = useState("")
val debouncedInput = useDebounce(input) {
    wait = 500.milliseconds
}

// input 变化后 500ms，debouncedInput 才更新
useEffect(debouncedInput) {
    search(debouncedInput.value)
}
```

### useDebounceFn - 防抖函数

```kotlin
val debouncedSearch = useDebounceFn<String>(
    fn = { query -> performSearch(query) },
    optionsOf = { wait = 300.milliseconds }
)

// 调用
debouncedSearch.run("keyword")
debouncedSearch.cancel()  // 取消
debouncedSearch.flush()   // 立即执行
```

**配置选项**：
```kotlin
optionsOf = {
    wait = 300.milliseconds    // 等待时间
    leading = false            // 延迟开始前执行
    trailing = true            // 延迟结束后执行
    maxWait = 1.seconds        // 最大等待时间
}
```

---

## useThrottle / useThrottleFn

节流处理。

### useThrottle - 节流值

```kotlin
val (scrollY, setScrollY) = useState(0)
val throttledScrollY = useThrottle(scrollY) {
    wait = 100.milliseconds
}

// 每 100ms 最多更新一次
useEffect(throttledScrollY) {
    updateHeader(throttledScrollY.value)
}
```

### useThrottleFn - 节流函数

```kotlin
val throttledLog = useThrottleFn<Int>(
    fn = { value -> println("Value: $value") },
    optionsOf = { wait = 200.milliseconds }
)

throttledLog.run(42)
```

---

## useInterval

定时器 Hook。

```kotlin
// 基础用法
useInterval(optionsOf = {
    period = 1.seconds
}) {
    tick()
}

// 带控制
val (count, setCount) = useState(0)
val (isRunning, toggle) = useBoolean(true)

useInterval(
    optionsOf = { period = 1.seconds },
    ready = isRunning.value
) {
    setCount { it + 1 }
}

Button(onClick = toggle) {
    Text(if (isRunning.value) "暂停" else "继续")
}
```

**配置选项**：
```kotlin
optionsOf = {
    period = 1.seconds         // 间隔时间
    initialDelay = 0.seconds   // 初始延迟
}
```

---

## useTimeoutFn

延时执行。

```kotlin
val (isPending, start, stop) = useTimeoutFn(
    fn = { showNotification() },
    interval = 3.seconds
)

start()      // 开始计时
stop()       // 取消
// isPending.value 表示是否在等待中
```

**配置选项**：
```kotlin
useTimeoutFn(fn, interval, optionsOf = {
    immediate = false  // 是否立即开始
    immediateCallback = false // 是否在开始时立即执行
})
```

---

## useTimeoutPoll

超时轮询，上一次任务完成后才开始下一次。

```kotlin
val timeoutPoll = useTimeoutPoll(
    fn = { fetchData() },
    interval = 5.seconds
)

// timeoutPoll.isActive.value / timeoutPoll.pause() / timeoutPoll.resume()

// 自动开始
useTimeoutPoll(
    fn = { fetchData() },
    interval = 5.seconds,
    immediate = true
)
```

---

## useCountdown

倒计时 Hook。

```kotlin
val (countdown, formattedRes) = useCountdown {
    targetDate = Clock.System.now() + 10.minutes // kotlin.time.Clock
    interval = 1.seconds
    onEnd = { showComplete() }
}

// countdown: 剩余毫秒数
// formattedRes: FormattedRes(days, hours, minutes, seconds, milliseconds)

Text("剩余: ${formattedRes.minutes}:${formattedRes.seconds}")
```

**配置选项**：
```kotlin
optionsOf = {
    targetDate = targetInstant     // 目标时间
    leftTime = 60.seconds          // 或指定剩余时间
    interval = 1.seconds           // 更新间隔
    onEnd = { /* 结束回调 */ }
}
```

---

## useCounter

计数器 Hook。

```kotlin
val (count, inc, dec, set, reset) = useCounter(
    initialValue = 0,
    optionsOf = {
        min = 0
        max = 100
    }
)

inc()       // +1
inc(5)      // +5
dec()       // -1
dec(3)      // -3
set(50)     // 设置为 50
reset()     // 重置为初始值
```

---

## useUndo

撤销/重做 Hook。

```kotlin
val (state, set, reset, undo, redo, canUndo, canRedo) = useUndo("initial")

set("change 1")
set("change 2")

undo()  // 回到 "change 1"
redo()  // 回到 "change 2"
reset() // 重置为 "initial"

// canUndo.value, canRedo.value 表示是否可操作
```

---

## useAsync

简化协程使用。

```kotlin
// 方式1: 直接执行
useAsync {
    val data = fetchData()
    processData(data)
}

// 方式2: 获取执行函数
val asyncRun = useAsync()
Button(onClick = {
    asyncRun {
        val result = longOperation()
        updateUI(result)
    }
}) { Text("执行") }

// 方式3: 可取消
val (run, cancel) = useCancelableAsync()
run { longOperation() }
cancel()  // 取消执行
```

---

## useUpdate

强制重组。

```kotlin
val forceUpdate = useUpdate()

Button(onClick = forceUpdate) {
    Text("强制刷新")
}
```

---

## useEvent

发布-订阅模式的跨组件通信。

```kotlin
// 定义事件类型
data class UserLoggedIn(val userId: String)

// 订阅
useEventSubscribe<UserLoggedIn> { event ->
    println("User logged in: ${event.userId}")
}

// 发布
val publish = useEventPublish<UserLoggedIn>()
publish(UserLoggedIn("user123"))
```

---

## useClipboard

剪贴板操作。

```kotlin
val (text, setText, clear) = useClipboard()

// 读取剪贴板
Text("剪贴板内容: ${text.value}")

// 写入剪贴板
setText("复制的文本")

// 清空
clear()
```

---

## useCycleList

循环列表。

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

## useSorted

列表排序。

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

## useNow / useTimestamp

当前时间。

### useNow

```kotlin
val now = useNow {
    interval = 1.seconds
}
// now.value = "2024-01-15 10:30:45"

// 自定义格式
val now = useNow {
    interval = 1.seconds
    format = { epochMillis -> epochMillis.toString() }
}
```

### useTimestamp

```kotlin
val timestamp = useTimestamp {
    interval = 100.milliseconds
}
// timestamp.value = 1705301445123 (毫秒)
```

---

## useDateFormat / useTimeAgo

日期格式化。

### useDateFormat

```kotlin
val formatted = useDateFormat(instant) {
    formatStr = "YYYY-MM-DD HH:mm:ss"
}
```

### useTimeAgo

```kotlin
val timeAgo = useTimeAgo(pastInstant) {
    updateInterval = 1.seconds
}
// timeAgo.value = "5 minutes ago"
```

---

## useMath

响应式数学运算。

```kotlin
val (value, setValue) = useState(3.7)

val abs = useAbs(value)       // 绝对值
val ceil = useCeil(value)     // 向上取整
val floor = useFloor(value)   // 向下取整
val round = useRound(value)   // 四舍五入

val (a, setA) = useState(3.0)
val (b, setB) = useState(4.0)

val min = useMin(a, b)        // 最小值
val max = useMax(a, b)        // 最大值
val pow = usePow(a, b)        // a^b
val sqrt = useSqrt(a)         // 平方根
```
