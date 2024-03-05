# ComposeHooks

<picture>
  <img src="art/logo.jpg" width="300">
</picture>
[English](https://github.com/junerver/ComposeHooks/blob/master/README.md) | 简体中文

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0) ![release](https://img.shields.io/badge/release-v1.0.0-blue)

## 简介

项目的 idea 来自 [alibaba](https://github.com/alibaba)/[hooks](https://github.com/alibaba/hooks)，这是一个非常好用的React Hooks 集合。

它封装了大多数的常用操作作为自定义钩子，而 `useRequest` 则是重中之重，它设计的非常轻量化、可配置性高，使用简单。

于是，参照这一设计思想，采用类似的 API 名称创建了可以用在 Compose 项目的 Hooks。

目前已经实现的钩子如下：

| 函数名称      | 效果                                                         |
| ------------- | ------------------------------------------------------------ |
| useRequest    | 管理网络请求，实现了：手动、自动触发；生命周期回调；刷新；mutate变更；取消请求；轮询；Ready；依赖刷新；防抖、节流；错误重试； |
| useBoolean    | 管理 boolean 状态的 Hook。                                   |
| useContext    | just like react                                              |
| useCreation   | 用来替换 useRef                                              |
| useDebounce   | 用来处理防抖值的 Hook。                                      |
| useDebounceFn | 用来处理防抖函数的 Hook。                                    |
| useEffect     | just like react                                              |
| useInterval   | 一个可以处理 setInterval 的 Hook。                           |
| useLatest     | 返回当前最新值的 Hook，可以避免在使用解构写法时的闭包问题。  |
| useMount      | 只在组件初始化时执行的 Hook。                                |
| useNetwork    | 获取网络连接状态、类型                                       |
| usePrevious   | 保存上一次状态的 Hook。                                      |
| useReducer    | 一个可以在组件内使用的极简 redux                             |
| useRef        | just like react                                              |
| useState      | just like react                                              |
| useThrottle   | 用来处理节流值的 Hook。                                      |
| useThrottleFn | 用来处理函数节流的 Hook。                                    |
| useToggle     | 用于在两个状态值间切换的 Hook。                              |
| useTimeout    | 用于执行定时任务                                             |
| useUndo       | 用于处理撤销、重做的 Hook。                                  |
| useUnmount    | 在组件卸载（unmount）时执行的 Hook。                         |

## 添加依赖

```groovy
implementation 'xyz.junerver.compose:hooks:1.0.0'
```

```kotlin
implementation("xyz.junerver.compose:hooks:1.0.0")
```

## 快速开始

1. 使用 `useState` 快速创建受控组件

   ```kotlin
   val (name, setName) = useState("")
   OutlinedTextField(
       value = name,
       onValueChange = setName,
       label = { Text("Input Name") }
   )
   ```

2. 使用 `useEffect` 执行组件副作用

3. 使用 `useRef` 创建不受组件重组影响的对象引用

   ```kotlin
   val countRef = useRef(0)
   Button(onClick = {
       countRef.current += 1
       println(countRef)
   }) {
       Text(text = "Ref= ${countRef.current}")
   }
   ```

4. 使用 `useRequest` 轻松管理**网络状态**

   ```kotlin
   val (data, loading, error, run) = useRequest(
       requestFn = WebService::login.asRequestFn(),
       optionsOf {
           manual = true
       }
   )
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

   

## 使用 Live Templates

复制`Live Templates`目录下的`hooks`
文件，粘贴到`C:\Users\<user-name>\AppData\Roaming\Google\AndroidStudio2023.2\templates\`

你可以方便的通过 `us`、`ur` 来创建 `useState`、`useRequest`的代码片段。

## Todo:

- KMP friendly
- Unit Test
- CI
- Complete documentation



## 参考

1. [alibaba](https://github.com/alibaba)/[hooks](https://github.com/alibaba/hooks)
2. [pavi2410](https://github.com/pavi2410)/[useCompose](https://github.com/pavi2410/useCompose)