# SSE 流式连接 Hooks

## 目录

- [useSse 基础](#usesse-基础)
- [自动连接与手动连接](#自动连接与手动连接)
- [生命周期回调](#生命周期回调)
- [流控制](#流控制)
- [错误处理与重试](#错误处理与重试)

---

## useSse 基础

useSse 是用于管理 Server-Sent Events (SSE) 和流式连接的 Hook，是 useRequest 的流式对应物。
useRequest 处理一次性 HTTP 请求（suspend -> T），useSse 处理流式连接（suspend -> Flow<T>）。

### 基本结构

```kotlin
val (dataState, streamingState, errorState, params, send, cancel, refresh) = useSse(
    streamFn = { params -> api.subscribe(params) },
    optionsOf = {
        // 配置选项
    }
)

val data by dataState        // 最新事件数据
val isStreaming by streamingState  // 是否正在流式传输
val error by errorState      // 错误信息
```

### 返回值说明

| 返回值 | 类型 | 说明 |
|--------|------|------|
| dataState | State<TEvent?> | 最新事件数据 |
| streamingState | State<Boolean> | 是否正在流式传输 |
| errorState | State<Throwable?> | 错误信息 |
| params | State<TParams?> | 当前参数 |
| send | (TParams?) -> Unit | 手动发起流 |
| cancel | () -> Unit | 取消当前流 |
| refresh | () -> Unit | 使用上次参数重新连接 |

---

## 自动连接与手动连接

### 自动连接

组件挂载时自动发起流式连接。

```kotlin
val (lastEvent, isStreaming, error) = useSse(
    streamFn = { topic: String -> sseService.subscribe(topic) },
    optionsOf = {
        defaultParams = "news"  // 必须设置默认参数
        onEvent = { event ->
            println("收到事件: $event")
        }
    }
)
```

### 手动连接

需要手动触发流式连接。

```kotlin
val (lastEvent, isStreaming, error, _, send, cancel) = useSse(
    streamFn = { url: String -> sseClient.connect(url) },
    optionsOf = {
        manual = true
    }
)

Button(onClick = { send("https://api.example.com/events") }) {
    Text("开始监听")
}

Button(onClick = cancel) {
    Text("停止")
}
```

---

## 生命周期回调

```kotlin
val (data, isStreaming, error) = useSse(
    streamFn = { params -> api.subscribe(params) },
    optionsOf = {
        defaultParams = "topic"

        onBefore = { params ->
            println("流开始: $params")
        }

        onEvent = { event ->
            // 处理每个事件（包括累积场景）
            println("事件: $event")
        }

        onError = { error ->
            println("流错误: ${error.message}")
        }

        onFinally = {
            println("流结束")
        }
    }
)
```

---

## 流控制

### 取消流

```kotlin
val (data, isStreaming, _, _, _, cancel) = useSse(...)

Button(onClick = cancel) {
    Text("停止监听")
}
```

### 刷新流

```kotlin
val (data, isStreaming, _, _, _, _, refresh) = useSse(...)

Button(onClick = refresh) {
    Text("重新连接")
}
```

---

## 错误处理与重试

```kotlin
val (data, isStreaming, error) = useSse(
    streamFn = { params -> api.subscribe(params) },
    optionsOf = {
        defaultParams = "topic"
        retryCount = 3              // 重试次数
        retryInterval = 2.seconds   // 重试间隔
    }
)

if (error != null) {
    Text("错误: ${error.message}")
}
```

---

## 完整配置参考

```kotlin
optionsOf = {
    // 基础配置
    manual = false                    // 是否手动触发
    defaultParams = params            // 默认参数

    // 生命周期
    onBefore = { params -> }
    onEvent = { event -> }           // 每个事件回调
    onError = { error -> }
    onFinally = { }

    // 重试
    retryCount = 0                    // 重试次数
    retryInterval = 0.seconds        // 重试间隔
}
```

---

## 依据

- useSse: hooks/src/commonMain/kotlin/xyz/junerver/compose/hooks/usses/useSse.kt
