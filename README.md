# ComposeHooks

<picture>
  <img src="art/logo.jpg" width="300">
</picture>

English | [简体中文](https://github.com/junerver/ComposeHooks/blob/master/README.zh-CN.md)

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Version maven-central](https://img.shields.io/maven-central/v/xyz.junerver.compose/hooks)](https://central.sonatype.com/artifact/xyz.junerver.compose/hooks)
[![latest releast](https://badgen.net/github/release/junerver/ComposeHooks)](https://github.com/junerver/ComposeHooks/releases/latest) 
[![stars](https://badgen.net/github/stars/junerver/ComposeHooks)](https://github.com/junerver/ComposeHooks/releases/latest)
[![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/junerver/ComposeHooks.svg)](http://isitmaintained.com/project/junerver/ComposeHooks "Average time to resolve an issue")
[![Percentage of issues still open](http://isitmaintained.com/badge/open/junerver/ComposeHooks.svg)](http://isitmaintained.com/project/junerver/ComposeHooks "Percentage of issues still open")

## About

The idea for the project comes from [alibaba](https://github.com/alibaba)/[hooks](https://github.com/alibaba/hooks), which is a very easy-to-use collection of React Hooks.

It encapsulates most common operations as custom hooks, and `useRequest` is the top priority. It is
designed to be very lightweight, highly configurable, and easy to use.

Therefore, based on this design idea, Hooks that can be used in the Compose project were created
using similar API names.

The hooks that have been implemented so far are as follows:

| hook name                                                    | effect                                                       |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| [useRequest](https://github.com/junerver/ComposeHooks/blob/master/app/src/main/java/xyz/junerver/composehooks/example/UseRequestExample.kt) | Manage network requests and implement: [manual and automatic](https://github.com/junerver/ComposeHooks/blob/master/app/src/main/java/xyz/junerver/composehooks/example/request/Auto%26Manual.kt) triggering; [life cycle callbacks](https://github.com/junerver/ComposeHooks/blob/master/app/src/main/java/xyz/junerver/composehooks/example/request/Lifecycle.kt); [refresh](https://github.com/junerver/ComposeHooks/blob/master/app/src/main/java/xyz/junerver/composehooks/example/request/Refresh.kt); [mutate changes](https://github.com/junerver/ComposeHooks/blob/master/app/src/main/java/xyz/junerver/composehooks/example/request/Mutate.kt); [cancel requests](https://github.com/junerver/ComposeHooks/blob/master/app/src/main/java/xyz/junerver/composehooks/example/request/Cancel.kt); [polling](https://github.com/junerver/ComposeHooks/blob/master/app/src/main/java/xyz/junerver/composehooks/example/request/Polling.kt); [Ready](https://github.com/junerver/ComposeHooks/blob/master/app/src/main/java/xyz/junerver/composehooks/example/request/Ready.kt); [dependency refresh](https://github.com/junerver/ComposeHooks/blob/master/app/src/main/java/xyz/junerver/composehooks/example/request/DepsRefresh.kt); [debounce](https://github.com/junerver/ComposeHooks/blob/master/app/src/main/java/xyz/junerver/composehooks/example/request/Debounce.kt), [throttle](https://github.com/junerver/ComposeHooks/blob/master/app/src/main/java/xyz/junerver/composehooks/example/request/Throttle.kt); [error retry](https://github.com/junerver/ComposeHooks/blob/master/app/src/main/java/xyz/junerver/composehooks/example/request/ErrorRetry.kt); |
| [useBoolean](https://github.com/junerver/ComposeHooks/blob/master/app/src/main/java/xyz/junerver/composehooks/example/UseBooleanExample.kt) | Hook to manage boolean state.                                |
| [useContext](https://github.com/junerver/ComposeHooks/blob/master/app/src/main/java/xyz/junerver/composehooks/example/UseContextExample.kt) | just like react                                              |
| [useCreation](https://github.com/junerver/ComposeHooks/blob/master/app/src/main/java/xyz/junerver/composehooks/example/UseCreationExample.kt) | `useCreation` is the replacement for `useRef`.               |
| [useDebounce](https://github.com/junerver/ComposeHooks/blob/master/app/src/main/java/xyz/junerver/composehooks/example/UseDebounceExample.kt) | A hook that deal with the debounced value.                   |
| [useDebounceFn](https://github.com/junerver/ComposeHooks/blob/master/app/src/main/java/xyz/junerver/composehooks/example/UseDebounceExample.kt) | A hook that deal with the debounced function.                |
| [useEffect](https://github.com/junerver/ComposeHooks/blob/master/app/src/main/java/xyz/junerver/composehooks/example/UseEffectExample.kt) | just like react                                              |
| [useEvent](https://github.com/junerver/ComposeHooks/blob/master/app/src/main/java/xyz/junerver/composehooks/example/UseEventExample.kt) | Implement lightweight cross-component communication using the subscribe-publish pattern |
| [useInterval](https://github.com/junerver/ComposeHooks/blob/master/app/src/main/java/xyz/junerver/composehooks/example/UseIntervalExample.kt) | A hook that handles the `setInterval` timer function.        |
| [useLatest](https://github.com/junerver/ComposeHooks/blob/master/app/src/main/java/xyz/junerver/composehooks/example/UseLatestExample.kt) | A Hook that returns the latest value, effectively avoiding the closure problem. |
| [useMount](https://github.com/junerver/ComposeHooks/blob/master/app/src/main/java/xyz/junerver/composehooks/example/UseMountExample.kt) | A hook that executes a function after the component is mounted. |
| [useNetwork](https://github.com/junerver/ComposeHooks/blob/master/app/src/main/java/xyz/junerver/composehooks/example/UseNetworkExample.kt) | A hook for obtaining network connection status and type.     |
| [usePrevious](https://github.com/junerver/ComposeHooks/blob/master/app/src/main/java/xyz/junerver/composehooks/example/UsePreviousExample.kt) | A Hook to return the previous state.                         |
| [useReducer](https://github.com/junerver/ComposeHooks/blob/master/app/src/main/java/xyz/junerver/composehooks/example/UseReducerExample.kt) | just like react                                              |
| [useRef](https://github.com/junerver/ComposeHooks/blob/master/app/src/main/java/xyz/junerver/composehooks/example/UseRefExample.kt) | just like react                                              |
| [useState](https://github.com/junerver/ComposeHooks/blob/master/app/src/main/java/xyz/junerver/composehooks/example/UseStateExample.kt) | just like react                                              |
| [useThrottle](https://github.com/junerver/ComposeHooks/blob/master/app/src/main/java/xyz/junerver/composehooks/example/UseThrottleExample.kt) | A hook that deal with the throttled value.                   |
| [useThrottleFn](https://github.com/junerver/ComposeHooks/blob/master/app/src/main/java/xyz/junerver/composehooks/example/UseThrottleExample.kt) | A hook that deal with the throttled function.                |
| [useToggle](https://github.com/junerver/ComposeHooks/blob/master/app/src/main/java/xyz/junerver/composehooks/example/UseToggleExample.kt) | A hook that toggle states.                                   |
| [useTimeout](https://github.com/junerver/ComposeHooks/blob/master/app/src/main/java/xyz/junerver/composehooks/example/UseTimeoutExample.kt) | A hook that handles the `setTimeout` timer function.         |
| [useUndo](https://github.com/junerver/ComposeHooks/blob/master/app/src/main/java/xyz/junerver/composehooks/example/UseUndoExample.kt) | A Hook for handling undo and redo.                           |
| [useUnmount](https://github.com/junerver/ComposeHooks/blob/master/app/src/main/java/xyz/junerver/composehooks/example/UseMountExample.kt) | A hook that executes the function right before the component is unmounted. |
| [useUpdate](https://github.com/junerver/ComposeHooks/blob/master/app/src/main/java/xyz/junerver/composehooks/example/UseUpdateExample.kt) | A hook that returns a function which can be used to force the component to re-render. |
| [useUpdateEffect](https://github.com/junerver/ComposeHooks/blob/master/app/src/main/java/xyz/junerver/composehooks/example/UseUpdateEffectExample.kt) | A hook alike useEffect but skips running the effect for the first time. |
## Add to dependencies

```groovy
implementation 'xyz.junerver.compose:hooks:<latest_release>'
```

```kotlin
implementation("xyz.junerver.compose:hooks:<latest_release>")
```

## Quick Setup

1. Use `useState` to quickly create controlled components

   ```kotlin
   val (name, setName) = useState("")
   OutlinedTextField(
       value = name,
       onValueChange = setName,
       label = { Text("Input Name") }
   )
   ```

2. Use `useEffect` to perform component LaunchedEffects

3. Use `useRef` to create object references that are not affected by component recompose

   ```kotlin
   val countRef = useRef(0)
   Button(onClick = {
       countRef.current += 1
       println(countRef)
   }) {
       Text(text = "Ref= ${countRef.current}")
   }
   ```

4. Use `useRequest` to easily manage **network query state**

   ```kotlin
   val (data, loading, error, run) = useRequest(
       requestFn = WebService::login.asRequestFn(), //Encapsulate the corresponding extension functions yourself,to make retrofit friendly
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
   useRequest organizes code through a plug-in pattern, the core code is extremely simple, and can be easily extended for more advanced features. Current features include:
   - Automatic/manual request
   - Polling
   - Debounce
   - Throttle
   - Error retry
   - Loading delay
   - SWR(stale-while-revalidate)
   - Caching



## Live Templates

Copy `hooks` in the `Live Templates` directory
File, paste into `C:\Users\<user-name>\AppData\Roaming\Google\AndroidStudio2023.2\templates\`

You can easily create code snippets of `useState` and `useRequest` through `us` and `ur`.

## Open Inlay Hints for Kotlin Type

For hooks like `useRequest`, its return value can deconstruct many objects and functions. It is necessary to enable InlayHint:

Editor - Inlay Hints - Types - Kotlin

## Documentation

- [Easily manage network requests with useRequest](https://junerver.xyz/2024/03/06/%E5%9C%A8Compose%E4%B8%AD%E4%BD%BF%E7%94%A8useRequest%E8%BD%BB%E6%9D%BE%E7%AE%A1%E7%90%86%E7%BD%91%E7%BB%9C%E8%AF%B7%E6%B1%82/)
- [Using state hoisting in Compose? I'll hoisting to Provider](https://junerver.xyz/2024/03/11/%E5%9C%A8Compose%E4%B8%AD%E4%BD%BF%E7%94%A8%E7%8A%B6%E6%80%81%E6%8F%90%E5%8D%87%EF%BC%9F%E6%88%91%E6%8F%90%E5%8D%87%E4%B8%AAP-Provider/)

## Todo:

- KMP friendly
- Unit Test
- CI
- Complete documentation

## 参考/Thanks

1. [alibaba](https://github.com/alibaba)/[hooks](https://github.com/alibaba/hooks)
2. [pavi2410](https://github.com/pavi2410)/[useCompose](https://github.com/pavi2410/useCompose)

## License

[Apache License 2.0](https://choosealicense.com/licenses/apache-2.0/)
