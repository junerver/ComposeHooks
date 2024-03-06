# ComposeHooks

<picture>
  <img src="art/logo.jpg" width="300">
</picture>

English | [简体中文](https://github.com/junerver/ComposeHooks/blob/master/README.zh-CN.md)

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0) ![release](https://img.shields.io/badge/release-v1.0.1-blue)


## About

The idea for the project comes
from [alibaba](https://github.com/alibaba)/[hooks](https://github.com/alibaba/hooks), which is a
very easy-to-use collection of React Hooks.

It encapsulates most common operations as custom hooks, and `useRequest` is the top priority. It is
designed to be very lightweight, highly configurable, and easy to use.

Therefore, based on this design idea, Hooks that can be used in the Compose project were created
using similar API names.

The hooks that have been implemented so far are as follows:

| hook name     | effect                                                       |
| ------------- | ------------------------------------------------------------ |
| useRequest    | Manage network requests and implement: manual and automatic triggering; life cycle callbacks; refresh; mutate changes; cancel requests; polling; Ready; dependency refresh; debounce, throttle; error retry; |
| useBoolean    | Hook to manage boolean state.                                |
| useContext    | just like react                                              |
| useCreation   | `useCreation` is the replacement for `useRef`.               |
| useDebounce   | A hook that deal with the debounced value.                   |
| useDebounceFn | A hook that deal with the debounced function.                |
| useEffect     | just like react                                              |
| useInterval   | A hook that handles the `setInterval` timer function.        |
| useLatest     | A Hook that returns the latest value, effectively avoiding the closure problem. |
| useMount      | A hook that executes a function after the component is mounted. |
| useNetwork    | A hook for obtaining network connection status and type.     |
| usePrevious   | A Hook to return the previous state.                         |
| useReducer    | just like react                                              |
| useRef        | just like react                                              |
| useState      | just like react                                              |
| useThrottle   | A hook that deal with the throttled value.                   |
| useThrottleFn | A hook that deal with the throttled function.                |
| useToggle     | A hook that toggle states.                                   |
| useTimeout    | A hook that handles the `setTimeout` timer function.         |
| useUndo       | A Hook for handling undo and redo.                           |
| useUnmount    | A hook that executes the function right before the component is unmounted. |

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
       requestFn = WebService::login.asRequestFn(), //retrofit friendl
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
