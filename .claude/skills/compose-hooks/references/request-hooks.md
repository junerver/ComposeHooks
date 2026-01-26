# 网络请求 Hooks

## 目录

- [useRequest 基础](#userequest-基础)
- [自动请求与手动请求](#自动请求与手动请求)
- [生命周期回调](#生命周期回调)
- [刷新与变更](#刷新与变更)
- [轮询](#轮询)
- [防抖与节流](#防抖与节流)
- [错误重试](#错误重试)
- [依赖刷新](#依赖刷新)
- [缓存与 SWR](#缓存与-swr)
- [Loading Delay](#loading-delay)
- [自定义插件](#自定义插件)

---

## useRequest 基础

useRequest 是一个强大的网络请求管理 Hook，通过插件架构提供丰富功能。

### 基本结构

```kotlin
val (dataState, loadingState, errorState, request, mutate, refresh, cancel) = useRequest(
    requestFn = { params -> api.getData(params) },
    optionsOf = {
        // 配置选项
    }
)

val data by dataState
val loading by loadingState
val error by errorState
```

### 返回值说明

| 返回值 | 类型 | 说明 |
|--------|------|------|
| dataState | State<T?> | 请求返回的数据 |
| loadingState | State<Boolean> | 加载状态 |
| errorState | State<Throwable?> | 错误信息 |
| request | (params) -> Unit | 手动触发请求 |
| mutate | (T?) -> Unit | 直接修改数据 |
| refresh | () -> Unit | 使用上次参数重新请求 |
| cancel | () -> Unit | 取消请求 |

---

## 自动请求与手动请求

### 自动请求

组件挂载时自动发起请求。

```kotlin
val (data, loading, error) = useRequest(
    requestFn = { NetApi.getUserInfo(it) },
    optionsOf = {
        defaultParams = "userId123"  // 必须设置默认参数
    }
)

if (loading) {
    CircularProgressIndicator()
}
data?.let { UserCard(it) }
```

### 手动请求

需要手动触发请求。

```kotlin
val (data, loading, error, request) = useRequest(
    requestFn = { NetApi.login(it) },
    optionsOf = {
        manual = true
    }
)

Button(onClick = { request(loginParams) }) {
    Text("登录")
}
```

### 多参数请求

使用 Tuple 传递多个参数。

```kotlin
val (data, loading, error, request) = useRequest(
    requestFn = { params: Tuple2<String, Int> ->
        NetApi.search(params.first, params.second)
    },
    optionsOf = {
        manual = true
        defaultParams = tuple("keyword", 1)
    }
)

request(tuple("new keyword", 2))
```

---

## 生命周期回调

```kotlin
val (data, loading, error) = useRequest(
    requestFn = { NetApi.getData(it) },
    optionsOf = {
        defaultParams = "param"

        onBefore = { params ->
            println("请求开始: $params")
        }

        onSuccess = { data, params ->
            println("请求成功: $data")
        }

        onError = { error, params ->
            println("请求失败: ${error.message}")
        }

        onFinally = { params, data, error ->
            println("请求结束")
        }
    }
)
```

---

## 刷新与变更

### refresh - 重新请求

```kotlin
val (data, loading, error, _, _, refresh) = useRequest(
    requestFn = { NetApi.getData(it) },
    optionsOf = {
        defaultParams = "param"
    }
)

Button(onClick = refresh) {
    Text("刷新")
}
```

### mutate - 直接修改数据

```kotlin
val (data, loading, error, _, mutate) = useRequest(
    requestFn = { NetApi.getData(it) },
    optionsOf = {
        defaultParams = "param"
    }
)

// 乐观更新
Button(onClick = {
    mutate(data?.copy(liked = true))
}) {
    Text("点赞")
}
```

---

## 轮询

```kotlin
val (data, loading, error) = useRequest(
    requestFn = { NetApi.getStatus(it) },
    optionsOf = {
        defaultParams = "taskId"
        pollingInterval = 3.seconds  // 每3秒轮询一次
        pollingWhenHidden = false    // 页面隐藏时停止轮询
    }
)
```

---

## 防抖与节流

### 防抖请求

```kotlin
val (data, loading, error, request) = useRequest(
    requestFn = { NetApi.search(it) },
    optionsOf = {
        manual = true
        debounceWait = 500.milliseconds
    }
)

// 快速调用只会执行最后一次
TextField(
    value = query,
    onValueChange = {
        setQuery(it)
        request(it)  // 防抖处理
    }
)
```

### 节流请求

```kotlin
val (data, loading, error, request) = useRequest(
    requestFn = { NetApi.log(it) },
    optionsOf = {
        manual = true
        throttleWait = 1.seconds
    }
)
```

---

## 错误重试

```kotlin
val (data, loading, error) = useRequest(
    requestFn = { NetApi.getData(it) },
    optionsOf = {
        defaultParams = "param"
        retryCount = 3              // 重试次数
        retryInterval = 1.seconds  // 重试间隔
    }
)
```

---

## 依赖刷新

依赖变化时自动重新请求。

```kotlin
val (userId, setUserId) = useState("user1")

val (data, loading, error) = useRequest(
    requestFn = { NetApi.getUserInfo(it) },
    optionsOf = {
        defaultParams = userId
        refreshDeps = arrayOf(userId)  // userId 变化时自动刷新
    }
)

// 切换用户
Button(onClick = { setUserId("user2") }) {
    Text("切换用户")
}
```

---

## 缓存与 SWR

### 基础缓存

```kotlin
val (data, loading, error) = useRequest(
    requestFn = { NetApi.getData(it) },
    optionsOf = {
        defaultParams = "param"
        cacheKey = "unique-cache-key"
        cacheTime = 5.minutes  // 缓存时间
    }
)
```

### SWR (Stale-While-Revalidate)

先返回缓存数据，同时后台刷新。

```kotlin
val (data, loading, error) = useRequest(
    requestFn = { NetApi.getData(it) },
    optionsOf = {
        defaultParams = "param"
        cacheKey = "swr-key"
        staleTime = 1.minutes  // 数据新鲜时间
        // staleTime 内不会重新请求
        // 超过 staleTime 会先返回缓存，同时后台刷新
    }
)
```

---

## Loading Delay

避免 loading 闪烁。

```kotlin
val (data, loading, error) = useRequest(
    requestFn = { NetApi.getData(it) },
    optionsOf = {
        defaultParams = "param"
        loadingDelay = 300.milliseconds
        // 请求在 300ms 内完成不会显示 loading
    }
)
```

---

## Ready

条件满足时才发起请求。

```kotlin
val (isLoggedIn, setLoggedIn) = useState(false)

val (data, loading, error) = useRequest(
    requestFn = { NetApi.getUserData(it) },
    optionsOf = {
        defaultParams = "param"
        ready = isLoggedIn  // 登录后才请求
    }
)
```

---

## 自定义插件

扩展 useRequest 功能。

```kotlin
// 定义插件
@Composable
fun <TParams, TData : Any> myPlugin(
    options: UseRequestOptions<TParams, TData>
): Plugin<TParams, TData> {
    return object : Plugin<TParams, TData>() {
        override fun onBefore(params: TParams?): BeforeReturn<TData>? {
            println("My plugin: before request")
            return null
        }

        override fun onSuccess(data: TData, params: TParams?) {
            println("My plugin: success")
        }

        override fun onError(e: Throwable, params: TParams?) {
            println("My plugin: error")
        }
    }
}

// 使用插件
val (data, loading, error) = useRequest(
    requestFn = { NetApi.getData(it) },
    optionsOf = {
        defaultParams = "param"
    },
    plugins = arrayOf(::myPlugin)
)
```

---

## 完整配置参考

```kotlin
optionsOf = {
    // 基础配置
    manual = false                    // 是否手动触发
    defaultParams = params            // 默认参数
    ready = true                      // 是否准备好

    // 生命周期
    onBefore = { params -> }
    onSuccess = { data, params -> }
    onError = { error, params -> }
    onFinally = { params, data, error -> }

    // 轮询
    pollingInterval = 0.seconds       // 轮询间隔
    pollingWhenHidden = true          // 隐藏时是否轮询
    pollingErrorRetryCount = -1       // 轮询错误重试次数

    // 防抖节流
    debounceWait = 0.milliseconds     // 防抖等待时间
    throttleWait = 0.milliseconds     // 节流等待时间

    // 重试
    retryCount = 0                    // 重试次数
    retryInterval = 0.seconds         // 重试间隔

    // 缓存
    cacheKey = ""                     // 缓存键
    cacheTime = 5.minutes             // 缓存时间
    staleTime = 0.seconds             // 数据新鲜时间

    // 其他
    loadingDelay = 0.milliseconds     // loading 延迟
    refreshDeps = emptyArray()        // 刷新依赖
}
```
