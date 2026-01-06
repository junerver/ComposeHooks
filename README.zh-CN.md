# ComposeHooks

<picture>
  <img src="art/logo.jpg" width="300">
</picture>

[English](https://github.com/junerver/ComposeHooks/blob/master/README.md) | 简体中文

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Version maven-central](https://img.shields.io/maven-central/v/xyz.junerver.compose/hooks2)](https://central.sonatype.com/artifact/xyz.junerver.compose/hooks2)
[![latest releast](https://badgen.net/github/release/junerver/ComposeHooks)](https://github.com/junerver/ComposeHooks/releases/latest)
[![stars](https://badgen.net/github/stars/junerver/ComposeHooks)](https://github.com/junerver/ComposeHooks/releases/latest)
[![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/junerver/ComposeHooks.svg)](http://isitmaintained.com/project/junerver/ComposeHooks "Average time to resolve an issue")
[![Percentage of issues still open](http://isitmaintained.com/badge/open/junerver/ComposeHooks.svg)](http://isitmaintained.com/project/junerver/ComposeHooks "Percentage of issues still open")
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/junerver/ComposeHooks)
[![klibs.io](https://img.shields.io/badge/KLIBS_IO-blueviolet?logo=kotlin&logoColor=white)](https://klibs.io/project/junerver/ComposeHooks)

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=junerver/ComposeHooks&type=Date)](https://star-history.com/#junerver/ComposeHooks&Date)

## KMP 支持

> 注意：工件id为 `hooks2`

```kotlin
implementation("xyz.junerver.compose:hooks2:<latest_release>")
```

目前只支持有限的 target：
- android
- desktop（jvm）
- iosarm64
- iosimulatorarm64
- iosx64

## 简介

项目的 idea 来自 [alibaba](https://github.com/alibaba)/[hooks](https://github.com/alibaba/hooks)，这是一个非常好用的React Hooks 集合。

它封装了大多数的常用操作作为自定义钩子，而 `useRequest` 则是重中之重，它设计的非常轻量化、可配置性高，使用简单。

于是，参照这一设计思想，采用类似的 API 名称创建了可以用在 Compose 项目的 Hooks。

目前已经实现的钩子如下：

注意：所有 `use` 函数同样有 `remember` 的签名，如果你更喜欢 Compose 的命名方式，只需要使用 `rememberXxx` 即可！

### Hooks

#### State

| Hook 名称                                                                                                                                           | 描述                                                           |
| :-------------------------------------------------------------------------------------------------------------------------------------------------- | :------------------------------------------------------------- |
| [useAutoReset](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseAutoResetExample.kt) | 一个会在一段时间后将状态重置为默认值的 Hook。                  |
| [useBoolean](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseBooleanExample.kt) | 用于管理布尔状态的 Hook。                                      |
| [useContext](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseContextExample.kt) | 类似于 React 的 `useContext`。                                 |
| [useCreation](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseCreationExample.kt) | `useCreation` 是 `useRef` 的替代品。                           |
| [useDebounce](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseDebounceExample.kt) | 处理防抖值的 Hook。                                            |
| [Form.useForm](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseFormExample.kt) | 一个可以更轻松地控制无头组件 `Form` 的 Hook。                   |
| [useGetState](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseGetStateExample.kt) | 使用解构声明语法处理状态的 Hook。                              |
| [useImmutableList](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseImmutableListExample.kt) | 一个用于在 Compose 中管理不可变列表的 Hook。                   |
| [useLastChanged](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseLastChangedExample.kt) | 一个记录上次更改时间的 Hook。                                  |
| [useLatest](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseLatestExample.kt) | 一个返回最新值，有效避免闭包问题的 Hook。                      |
| [usePersistent](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UsePersistentExample.kt) | 一个轻量级持久化 Hook，你需要自己实现持久化方法（默认使用内存持久化）。 |
| [usePrevious](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UsePreviousExample.kt) | 一个返回前一个状态的 Hook。                                    |
| [useReducer](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseReducerExample.kt) | 类似于 React 的 `useReducer`。                                 |
| [useRef](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseRefExample.kt)           | 类似于 React 的 `useRef`。                                     |
| [useResetState](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseResetStateExample.kt) | 一个用于管理带重置功能状态的 Hook。                            |
| [useSelectable](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseSelectableExample.kt) | 一个帮助实现选择或多选功能的实用函数。                         |
| [`useSelector`/`useDispatch`](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseReduxExample.kt) | 更容易管理全局状态，类似于 Redux-React。                       |
| [useState](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseStateExample.kt)     | 类似于 React 的 `useState`。                                   |
| [useStateMachine](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseStateMachineExample.kt) | 一个用于管理状态机的 Compose Hook。                            |
| [useThrottle](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseThrottleExample.kt) | 处理节流值的 Hook。                                            |
| [useToggle](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseToggleExample.kt)   | 一个用于切换状态的 Hook。                                      |

---

#### Effect

| Hook 名称                                                                                                                                                                               | 描述                                             |
| :-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | :----------------------------------------------- |
| `useBackToFrontEffect` & `useFrontToBackEffect`                                                                                                                                        | 当应用进入后台或回到前台时执行 Effect。          |
| [useEffect](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseEffectExample.kt)                                      | 类似于 React 的 `useEffect`。                    |
| [useDebounceEffect](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseDebounceExample.kt)                           | 对 `useEffect` 进行防抖。                        |
| [useThrottleEffect](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseThrottleExample.kt)                           | 对 `useEffect` 进行节流。                        |
| [useUpdateEffect](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseUpdateEffectExample.kt)                           | 一个类似于 `useEffect` 但跳过第一次运行的 Hook。 |
| [usePausableEffect](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UsePausableEffectExample.kt) | 一个可暂停的 Effect Hook，提供暂停、恢复和停止 Effect 执行的能力。 |

---

#### LifeCycle

| Hook 名称                                                                                                                                       | 描述                                 |
| :---------------------------------------------------------------------------------------------------------------------------------------------- | :----------------------------------- |
| [useMount](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseMountExample.kt) | 一个在组件挂载后执行函数的 Hook。    |
| [useUnmount](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseMountExample.kt) | 一个在组件卸载前执行函数的 Hook。    |

---

#### Time

| Hook 名称                                                    | 描述                                                         |
| :----------------------------------------------------------- | :----------------------------------------------------------- |
| [useDateFormat](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseDateFormatExample.kt) | 受[dayjs](https://github.com/iamkun/dayjs)启发，根据传递的令牌对日期进行格式化的 Hook。 |
| [useInterval](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseIntervalExample.kt) | 一个处理 `setInterval` 定时器函数的 Hook。                   |
| [useNow](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseNowExample.kt) | 一个返回当前日期时间的 Hook，默认格式为 `yyyy-MM-dd HH:mm:ss`。 |
| [useTimeAgo](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseLastChangedExample.kt) | 响应式时间戳。当时间变化时自动更新“距离现在多久”的字符串。   |
| [useTimeout](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseTimeoutExample.kt) | 一个处理 `setTimeout` 定时器函数的 Hook。                    |
| [useTimeoutFn](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseTimeoutFnExample.kt) | 一个用于在指定延迟后执行函数并提供控制功能的 Hook。          |
| [useTimeoutPoll](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseTimeoutPollExample.kt) | 使用超时来轮询内容。在最后一个任务完成后触发回调。           |
| [useTimestamp](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseTimestampExample.kt) | 一个返回当前时间戳作为响应式状态的 Hook。                    |

---

#### Math

| Hook 名称     | 描述                               |
| :------------ | :--------------------------------- |
| `useAbs`      | 响应式 `kotlin.math.abs`。         |
| `useCeil`     | 响应式 `kotlin.math.ceil`。        |
| `useRound`    | 响应式 `kotlin.math.round`。       |
| `useTrunc`    | 响应式 `kotlin.math.truncate`。    |
| `useMin`      | 响应式 `kotlin.math.min`。         |
| `useMax`      | 响应式 `kotlin.math.max`。         |
| `usePow`      | 响应式 `kotlin.math.pow`。         |
| `useSqrt`     | 响应式 `kotlin.math.sqrt`。        |

---

#### Utilities

| Hook 名称                                                    | 描述                                                         |
| :----------------------------------------------------------- | :----------------------------------------------------------- |
| [useAsync](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseAsyncExample.kt) | 一个封装 `rememberCoroutineScope` 以简化协程使用的 Hook。    |
| [useBiometric](https://github.com/junerver/ComposeHooks/blob/master/app/src/androidMain/kotlin/xyz/junerver/composehooks/example/UseBiometricExample.kt)\* | 方便地使用生物识别功能。                                     |
| [useBatteryInfo](https://github.com/junerver/ComposeHooks/blob/master/app/src/androidMain/kotlin/xyz/junerver/composehooks/example/UseDeviceInfoExample.kt)\* | 一个可以获取电池电量和是否正在充电的 Hook。                  |
| [useBuildInfo](https://github.com/junerver/ComposeHooks/blob/master/app/src/androidMain/kotlin/xyz/junerver/composehooks/example/UseDeviceInfoExample.kt)\* | 一个可以获取 Android 品牌、型号和版本的 Hook。               |
| [useClipboard](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseClipboardExample.kt) | 易于使用的剪贴板 Hook。                                      |
| [useCountdown](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseCountdownExample.kt) | 一个用于管理倒计时的 Hook。                                  |
| [useCounter](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseCounterExample.kt) | 一个用于管理计数器的 Hook。                                  |
| [useCycleList](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseCycleListExample.kt) | 循环遍历列表项的 Hook。                                      |
| [useDebounceFn](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseDebounceExample.kt) | 处理防抖函数的 Hook。                                        |
| [useDisableScreenshot](https://github.com/junerver/ComposeHooks/blob/master/app/src/androidMain/kotlin/xyz/junerver/composehooks/example/UseDeviceInfoExample.kt)\* | 一个用于处理隐私页面禁止截图的 Hook。                        |
| [useEvent](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseEventExample.kt) | 使用订阅-发布模式实现轻量级跨组件通信。                      |
| [useFlashlight](https://github.com/junerver/ComposeHooks/blob/master/app/src/androidMain/kotlin/xyz/junerver/composehooks/example/UseDeviceInfoExample.kt)\* | 一个方便使用手电筒的 Hook。                                  |
| [useIdel](https://github.com/junerver/ComposeHooks/blob/master/app/src/androidMain/kotlin/xyz/junerver/composehooks/example/UseIdelExample.kt) | 跟踪用户是否处于非活动状态。                                 |
| `useKeyboard`                                                | 一个控制软键盘显示和隐藏的 Hook。                            |
| [useNetwork](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseNetworkExample.kt)\* | 一个用于获取网络连接状态和类型的 Hook。                      |
| [useRequest](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseRequestExample.kt) | 管理网络请求并实现：[手动和自动](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/request/Auto%26Manual.kt) 触发；[生命周期回调](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/request/Lifecycle.kt)；[刷新](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/request/Refresh.kt)；[修改变化](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/request/Mutate.kt)；[取消请求](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/request/Cancel.kt); [轮询](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/request/Polling.kt); [Ready](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/request/Ready.kt); [依赖刷新](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/request/DepsRefresh.kt); [防抖](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/request/Debounce.kt), [节流](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/request/Throttle.kt); [错误重试](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/request/ErrorRetry.kt); |
| [useScreenInfo](https://github.com/junerver/ComposeHooks/blob/master/app/src/androidMain/kotlin/xyz/junerver/composehooks/example/UseDeviceInfoExample.kt)* | 一个获取屏幕宽度、高度、横向和纵向方向信息的 Hook。          |
| [useSorted](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseSortedExample.kt) | 一个处理列表排序的 Hook。                                    |
| [useThrottleFn](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseThrottleExample.kt) | 一个处理节流函数的 Hook。                                    |
| [useUndo](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseUndoExample.kt) | 一个用于处理撤销和重做的 Hook。                              |
| [useUpdate](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseUpdateExample.kt) | 一个返回可以强制组件重新渲染的函数的 Hook。                  |
| [useVibrate](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseVibrateExample.kt)* | 一个让振动反馈变得简单的 Hook。                              |

> 注意，标记 `*` 的函数，只可以在 Android 中使用

### AI 模块

独立的 AI 模块，提供与 OpenAI 兼容 API 进行 AI 聊天和结构化数据生成的 Hook。

**添加 AI 模块依赖：**
```kotlin
implementation("xyz.junerver.compose:hai:<latest_release>")
```

| Hook 名称 | 描述 |
| --------- | ---- |
| [useChat](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseChatExample.kt) | 用于管理与 OpenAI 兼容 API 聊天对话的 Hook，支持流式响应的打字机效果。 |
| [useGenerateObject](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseGenerateObjectExample.kt) | 用于从 AI 响应生成结构化数据对象的 Hook，支持多模态输入（文本 + 图片）。 |

**功能特性：**
- 流式响应 (SSE)，实时打字机效果
- 多模态输入支持（文本、图片、文件）
- 类型安全的结构化数据生成
- 消息状态管理
- 加载和错误状态
- 控制函数（发送、停止、重新加载）
- 可配置选项（temperature、maxTokens、timeout 等）
- 生命周期回调（onFinish、onError、onStream）

**示例 - 聊天对话：**
```kotlin
val (messages, isLoading, error, sendMessage, _, _, reload, stop) = useChat {
    provider = Providers.OpenAI(apiKey = "your-api-key")
    model = "gpt-3.5-turbo"
    systemPrompt = "你是一个乐于助人的助手。"
    onFinish = { message, usage, reason ->
        println("完成: ${message.content}")
    }
}

// 发送消息
sendMessage("你好！")

// 显示消息（带流式效果）
messages.value.forEach { message ->
    Text("${message.role}: ${message.content}")
}
```

**示例 - 生成结构化数据：**
```kotlin
@Serializable
data class Recipe(val name: String, val ingredients: List<String>)

val (recipe, rawJson, isLoading, error, submit, stop) = useGenerateObject<Recipe>(
    schemaString = Recipe::class.jsonSchemaString,
) {
    provider = Providers.OpenAI(apiKey = "your-api-key")
    systemPrompt = "你是一位专业的厨师。"
}

// 生成结构化数据
submit("生成一道意大利面的食谱")

// 使用结果
recipe.value?.let { r ->
    Text("食谱: ${r.name}")
    r.ingredients.forEach { Text("- $it") }
}
```

## 添加依赖

**KMP项目**

```
// groovy
implementation 'xyz.junerver.compose:hooks2:<latest_release>'
// kotlin
implementation("xyz.junerver.compose:hooks2:<latest_release>")
```

**纯 Android 项目引入 hooks2**

纯Android项目请使用如下依赖（即工件id：`hooks2-android`）：

```kotlin
implementation("xyz.junerver.compose:hooks2-android:<latest_release>")
```

**旧版本 hooks 继续支持**

如果你的项目并没有出现因为重组导致性能问题，你可以继续在 **Android项目** 中使用旧版本，后续开发中会同步 bugfix 到旧版本。

```kotlin
implementation("xyz.junerver.compose:hooks:2.0.3")
```



## 快速开始

1. 使用 `useGetState` 快速创建受控组件

   ```kotlin
   val (name, setName, getState) = useGetState("")
   OutlinedTextField(
       value = getName(), // or `name.value`
       onValueChange = setName,
       label = { Text("Input Name") }
   )
   ```

2. 使用 `useEffect` 执行组件副作用

3. 使用 `useRef` 创建不受组件重组影响的对象引用

   ```kotlin
   val countRef = useRef(0) // or `val countRef by useRef(0)`
   Button(onClick = {
       countRef.current += 1 // or `countRef += 1`
       println(countRef)
   }) {
       Text(text = "Ref= ${countRef.current}") // or `countRef`
   }
   ```

4. 使用 `useRequest` 轻松管理**网络状态**

   ```kotlin
   val (dataState, loadingState, errorState, run) = useRequest(
       requestFn = WebService::login.asRequestFn(), //自行封装相应扩展函数
       optionsOf {
           manual = true
       }
   )
   val data by dataState // 使用委托获取状态的值
   val loading  by loadingState
   val error by errorState
   if (loading) {
       Text(text = "loading ....")
   }
   if (data != null) {
       Text(text = "resp: $data")
   }
   if (error != null) {
       Text(text = "error: $error")
   }
   Button(onClick = { run(arrayOf(requestBody)) }) {
       Text(text = "Login")
   }
   ```

   `useRequest` 通过插件式组织代码，核心代码极其简单，并且可以很方便的扩展出更高级的功能。目前已有能力包括：
   - 自动请求/手动请求
   - 轮询
   - 防抖
   - 节流
   - 错误重试
   - loading delay
   - SWR(stale-while-revalidate)
   - 缓存

更多用法请查看[wiki](https://github.com/junerver/ComposeHooks/wiki)、工程示例

## 使用 Live Templates

复制`Live Templates`目录下的`hooks`
文件，粘贴到`C:\Users\<user-name>\AppData\Roaming\Google\AndroidStudio2023.2\templates\`

你可以方便的通过 `us`、`ur` 来创建 `useState`、`useRequest`的代码片段。

## 开启类型的内嵌提示

像`useRequest`这样的钩子，它的返回值可以解构出很多对象、函数，开启 InlayHint 很有必要：

Editor - Inlay Hints - Types - Kotlin

## 混淆
如果你的项目需要使用 ProGuard，请加入下面的混淆规则:

```
-keep class xyz.junerver.composehooks.** { *; }
-keepclassmembers class xyz.junerver.composehooks.** { *; }
-dontwarn xyz.junerver.composehooks.**
```

## 文档

- [在Compose中使用useRequest轻松管理网络请求](https://junerver.github.io/2024/03/06/%E5%9C%A8Compose%E4%B8%AD%E4%BD%BF%E7%94%A8useRequest%E8%BD%BB%E6%9D%BE%E7%AE%A1%E7%90%86%E7%BD%91%E7%BB%9C%E8%AF%B7%E6%B1%82/)
- [在Compose中使用状态提升？我提升个P...Provider](https://junerver.github.io/2024/03/11/%E5%9C%A8Compose%E4%B8%AD%E4%BD%BF%E7%94%A8%E7%8A%B6%E6%80%81%E6%8F%90%E5%8D%87%EF%BC%9F%E6%88%91%E6%8F%90%E5%8D%87%E4%B8%AAP-Provider/)
- [在Compose中父组件如何调用子组件的函数？](https://junerver.github.io/2024/03/13/%E5%9C%A8Compose%E4%B8%AD%E7%88%B6%E7%BB%84%E4%BB%B6%E5%A6%82%E4%BD%95%E8%B0%83%E7%94%A8%E5%AD%90%E7%BB%84%E4%BB%B6%E7%9A%84%E5%87%BD%E6%95%B0%EF%BC%9F/)
- [在Compose中方便的使用MVI思想？试试useReducer！](https://junerver.github.io/2024/03/18/%E5%9C%A8Compose%E4%B8%AD%E6%96%B9%E4%BE%BF%E7%9A%84%E4%BD%BF%E7%94%A8MVI%E6%80%9D%E6%83%B3%EF%BC%9F%E8%AF%95%E8%AF%95useReducer%EF%BC%81/)
- [在Compose中像使用redux一样轻松管理全局状态](https://junerver.github.io/2024/04/01/%E5%9C%A8Compose%E4%B8%AD%E5%83%8F%E4%BD%BF%E7%94%A8redux%E4%B8%80%E6%A0%B7%E8%BD%BB%E6%9D%BE%E7%AE%A1%E7%90%86%E5%85%A8%E5%B1%80%E7%8A%B6%E6%80%81/)
- [在Compose中轻松使用异步dispatch管理全局状态](https://junerver.github.io/2024/04/02/%E5%9C%A8Compose%E4%B8%AD%E8%BD%BB%E6%9D%BE%E4%BD%BF%E7%94%A8%E5%BC%82%E6%AD%A5dispatch%E7%AE%A1%E7%90%86%E5%85%A8%E5%B1%80%E7%8A%B6%E6%80%81/)
- [在Compose中管理网络请求竟然如此简单！](https://junerver.github.io/2024/04/03/%E5%9C%A8Compose%E4%B8%AD%E7%AE%A1%E7%90%86%E7%BD%91%E7%BB%9C%E8%AF%B7%E6%B1%82%E7%AB%9F%E7%84%B6%E5%A6%82%E6%AD%A4%E7%AE%80%E5%8D%95%EF%BC%81/)
- [在Jetpack Compose中优雅的使用防抖、节流](https://junerver.github.io/2024/04/11/%E5%9C%A8Jetpack-Compose%E4%B8%AD%E4%BC%98%E9%9B%85%E7%9A%84%E4%BD%BF%E7%94%A8%E9%98%B2%E6%8A%96%E3%80%81%E8%8A%82%E6%B5%81/)

## Todo:

- ~~KMP friendly~~
- ~~CI~~
- Unit Test
- Complete documentation

## 参考

1. [alibaba](https://github.com/alibaba)/[hooks](https://github.com/alibaba/hooks)
2. [pavi2410](https://github.com/pavi2410)/[useCompose](https://github.com/pavi2410/useCompose)
3. [vueuse](https://github.com/vueuse)/[vueuse](https://github.com/vueuse/vueuse)

## 贡献指南

[Contributing guidelines](https://github.com/junerver/ComposeHooks/blob/master/CONTRIBUTING.md)

## License

[Apache License 2.0](https://choosealicense.com/licenses/apache-2.0/)
