# 副作用与生命周期 Hooks

## 目录

- [useEffect](#useeffect)
- [useMount](#usemount)
- [useUnmount](#useunmount)
- [useUpdateEffect](#useupdateeffect)
- [useDebounceEffect](#usedebounceeffect)
- [useThrottleEffect](#usethrottleeffect)
- [usePausableEffect](#usepausableeffect)
- [useBackToFrontEffect](#usebacktofronteffect)
- [useFrontToBackEffect](#usefronttobackeffect)

---

## useEffect

副作用处理 Hook，类似 React 的 useEffect。

### 无依赖（仅挂载时执行）

```kotlin
useEffect {
    // 组件挂载时执行一次
    initializeData()
}
```

### 单个依赖

```kotlin
val (userId, setUserId) = useState("")

useEffect(userId) {
    // userId 变化时执行
    loadUserData(userId)
}
```

### 多个依赖

```kotlin
val (page, setPage) = useState(1)
val (pageSize, setPageSize) = useState(10)

useEffect(page, pageSize) {
    // page 或 pageSize 变化时执行
    loadPageData(page, pageSize)
}
```

### 注意事项

useEffect 只是协程作用域，**没有卸载清理回调**。需要卸载清理请用 useUnmount。

**说明**：useEffect 是 LaunchedEffect 的别名，内部是协程作用域，可以直接使用 suspend 函数。

---

## useMount

组件挂载时执行，等价于 `useEffect(Unit) { }`。

```kotlin
useMount {
    // 组件首次进入组合时执行
    loadInitialData()
    setupSubscriptions()
}
```

**典型用法**（来自 UseMountExample.kt）：
```kotlin
@Composable
private fun SimpleLifecycleComponent() {
    var mountTime by useState("")

    useMount {
        mountTime = "Mounted at: ${now()}"
        println("SimpleLifecycleComponent: $mountTime")
    }

    useUnmount {
        println("SimpleLifecycleComponent unmounted")
    }

    Text(text = mountTime)
}
```

---

## useUnmount

组件卸载时执行。

```kotlin
useUnmount {
    // 组件从组合中移除时执行
    cleanupResources()
    cancelSubscriptions()
}
```

**典型用法**（来自 UseMountExample.kt）：
```kotlin
@Composable
private fun ResourceComponent(onStatusChange: (String) -> Unit) {
    val resourceId by useCreation { Random.nextInt(1000, 9999) }
    var ready by useRef(true)

    useMount {
        println("Initializing resource #$resourceId")
        onStatusChange("Resource #$resourceId initialized")

        launch(Dispatchers.Main + SupervisorJob()) {
            var counter = 0
            while (ready) {
                counter++
                onStatusChange("Resource #$resourceId active (${counter}s)")
                delay(1.seconds)
            }
        }
    }

    useUnmount {
        ready = false
        println("Cleaning up resource #$resourceId")
        onStatusChange("Resource #$resourceId released")
    }
}
```

---

## useUpdateEffect

跳过首次执行的 Effect，仅在依赖更新时执行。

```kotlin
val (count, setCount) = useState(0)

useUpdateEffect(count) {
    // 首次渲染不执行
    // 仅在 count 变化时执行
    println("Count updated to: $count")
}
```

**对比 useEffect**：
```kotlin
// useEffect: 挂载时执行 + 依赖变化时执行
useEffect(dep) { /* 首次也会执行 */ }

// useUpdateEffect: 仅依赖变化时执行
useUpdateEffect(dep) { /* 首次不执行 */ }
```

---

## useDebounceEffect

防抖 Effect，依赖变化后延迟执行。

```kotlin
val (searchText, setSearchText) = useState("")

useDebounceEffect(searchText, optionsOf = {
    wait = 500.milliseconds
}) {
    // searchText 变化后 500ms 执行
    // 如果 500ms 内再次变化，重新计时
    performSearch(searchText)
}
```

**配置选项**：
```kotlin
useDebounceEffect(dep, optionsOf = {
    wait = 300.milliseconds    // 等待时间
    leading = false            // 是否在延迟开始前执行
    trailing = true            // 是否在延迟结束后执行
    maxWait = 1.seconds        // 最大等待时间
}) {
    // effect
}
```

---

## useThrottleEffect

节流 Effect，限制执行频率。

```kotlin
val (scrollPosition, setScrollPosition) = useState(0)

useThrottleEffect(scrollPosition, optionsOf = {
    wait = 100.milliseconds
}) {
    // 每 100ms 最多执行一次
    updateUI(scrollPosition)
}
```

**配置选项**：
```kotlin
useThrottleEffect(dep, optionsOf = {
    wait = 200.milliseconds    // 节流间隔
    leading = true             // 是否在节流开始时执行
    trailing = true            // 是否在节流结束时执行
}) {
    // effect
}
```

---

## usePausableEffect

可暂停的 Effect。

```kotlin
val (count, setCount) = useState(0)

val (isActive, pause, resume) = usePausableEffect(count) {
    // 可以被暂停的 effect
    println("Count: $count")
}

Button(onClick = pause) { Text("暂停") }
Button(onClick = resume) { Text("恢复") }
Text("Effect 状态: ${if (isActive.value) "活跃" else "暂停"}")
```

---

## useBackToFrontEffect

应用从后台回到前台时执行（仅 Android）。

```kotlin
useBackToFrontEffect {
    // 应用回到前台时执行
    refreshData()
    checkNotifications()
}

// 带依赖
useBackToFrontEffect(userId) {
    // 回到前台且 userId 变化时执行
    syncUserData(userId)
}
```

---

## useFrontToBackEffect

应用进入后台时执行（仅 Android）。

```kotlin
useFrontToBackEffect {
    // 应用进入后台时执行
    saveState()
    pauseOperations()
}
```

---

## 最佳实践

### 1. 选择合适的 Effect

```kotlin
// 初始化数据 -> useMount
useMount { loadData() }

// 响应状态变化 -> useEffect
useEffect(userId) { loadUser(userId) }

// 跳过首次 -> useUpdateEffect
useUpdateEffect(count) { logChange(count) }

// 搜索输入 -> useDebounceEffect
useDebounceEffect(query) { search(query) }

// 滚动处理 -> useThrottleEffect
useThrottleEffect(scroll) { updateHeader(scroll) }
```

### 2. 清理资源

```kotlin
useUnmount {
    // 组件卸载时清理资源
    job.cancel()
    removeListener(listener)
}
```

### 3. 避免无限循环

```kotlin
// 错误：effect 内修改依赖
useEffect(count) {
    setCount(count + 1)  // 无限循环！
}

// 正确：使用条件判断
useEffect(count) {
    if (count < 10) {
        setCount(count + 1)
    }
}
```
