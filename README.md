# ComposeHooks

<picture>
  <img src="art/logo.jpg" width="300">
</picture>

English | [简体中文](https://github.com/junerver/ComposeHooks/blob/master/README.zh-CN.md)

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Version maven-central](https://img.shields.io/maven-central/v/xyz.junerver.compose/hooks2)](https://central.sonatype.com/artifact/xyz.junerver.compose/hooks2)
[![latest releast](https://badgen.net/github/release/junerver/ComposeHooks)](https://github.com/junerver/ComposeHooks/releases/latest)
[![stars](https://badgen.net/github/stars/junerver/ComposeHooks)](https://github.com/junerver/ComposeHooks/releases/latest)
[![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/junerver/ComposeHooks.svg)](http://isitmaintained.com/project/junerver/ComposeHooks "Average time to resolve an issue")
[![Percentage of issues still open](http://isitmaintained.com/badge/open/junerver/ComposeHooks.svg)](http://isitmaintained.com/project/junerver/ComposeHooks "Percentage of issues still open")
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/junerver/ComposeHooks)

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=junerver/ComposeHooks&type=Date)](https://star-history.com/#junerver/ComposeHooks&Date)

## KMP Support

> NOTE: The artifact id is `hooks2`

```kotlin
implementation("xyz.junerver.compose:hooks2:<latest_release>")
```

Currently only limited targets are supported:
- android
- desktop (jvm)
- iosarm64
- iosimulatorarm64
- iosx64


## About

The idea for the project comes from [alibaba](https://github.com/alibaba)/[hooks](https://github.com/alibaba/hooks), which is a very easy-to-use collection of React Hooks.

It encapsulates most common operations as custom hooks, and `useRequest` is the top priority. It is
designed to be very lightweight, highly configurable, and easy to use.

Therefore, based on this design idea, Hooks that can be used in the Compose project were created
using similar API names.

The hooks that have been implemented so far are as follows:

Note: All `use` functions also have the signature of `remember`. If you prefer Compose’s naming method, just use `rememberXxx`!

### Hooks

#### State

| hook name                                                    | description                                                  |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| [useAutoReset](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseAutoResetExample.kt) | A hook which will reset state to the default value after some time. |
| [useBoolean](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseBooleanExample.kt) | Hook to manage boolean state.                                |
| [useContext](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseContextExample.kt) | just like react                                              |
| [useCreation](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseCreationExample.kt) | `useCreation` is the replacement for `useRef`.               |
| [useDebounce](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseDebounceExample.kt) | A hook that deal with the debounced value.                   |
| [Form.useForm](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseFormExample.kt) | A Hook that can easier control headless component `Form`     |
| [useGetState](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseGetStateExample.kt) | A Hooks that handle state using destructuring declaration syntax. |
| [useImmutableList](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseImmutableListExample.kt) | A hook for managing immutable lists in Compose.              |
| [useLastChanged](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseLastChangedExample.kt) | A Hook that  records the Instant of the last change          |
| [useLatest](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseLatestExample.kt) | A Hook that returns the latest value, effectively avoiding the closure problem. |
| [usePersistent](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UsePersistentExample.kt) | A lightweight persistence hook, you need to implement the persistence method yourself (memory persistence is used by default) |
| [usePrevious](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UsePreviousExample.kt) | A Hook to return the previous state.                         |
| [useReducer](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseReducerExample.kt) | just like react                                              |
| [useRef](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseRefExample.kt) | just like react                                              |
| [useResetState](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseResetStateExample.kt) | A hook for managing state with reset functionality.          |
| [useSelectable](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseSelectableExample.kt) | A utility function to help implement select or multi select feature. |
| [`useSelector`/`useDispatch`](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseReduxExample.kt) | easier to management global state，just like use redux-react |
| [useState](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseStateExample.kt) | just like react                                              |
| [useStateMachine](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseStateMachineExample.kt) | A Compose Hook for managing state machines                   |
| [useThrottle](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseThrottleExample.kt) | A hook that deal with the throttled value.                   |
| [useToggle](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseToggleExample.kt) | A hook that toggle states.                                   |

#### Effect

| hook name                                                    | description                                                  |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| `useBackToFrontEffect` & `useFrontToBackEffect`              | Execute effect when app goes to the background or come back to the foreground |
| [useEffect](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseEffectExample.kt) | just like react                                              |
| [useDebounceEffect](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseDebounceExample.kt) | Debounce your `useEffect`.                                   |
| [useThrottleEffect](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseThrottleExample.kt) | Throttle your `useEffect`.                                   |
| [useUpdateEffect](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseUpdateEffectExample.kt) | A hook alike useEffect but skips running the effect for the first time. |
| [usePausableEffect](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UsePausableEffectExample.kt) | A pausable effect hook that provides the ability to pause, resume, and stop effect execution |

#### LifeCycle

| hook name                                                    | description                                                  |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| [useMount](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseMountExample.kt) | A hook that executes a function after the component is mounted. |
| [useUnmount](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseMountExample.kt) | A hook that executes the function right before the component is unmounted. |

#### Time

| hook name                                                    | description                                                  |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| [useInterval](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseIntervalExample.kt) | A hook that handles the `setInterval` timer function.        |
| [useNow](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseNowExample.kt) | A hook that return now date, default format: `yyyy-MM-dd HH:mm:ss` |
| [useTimeAgo](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseLastChangedExample.kt) | Reactive time ago. Automatically update the time ago string when the time changes. |
| [useTimeout](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseTimeoutExample.kt) | A hook that handles the `setTimeout` timer function.         |
| [useTimeoutFn](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseTimeoutFnExample.kt) | A hook for executing a function after a specified delay with controls. |
| [useTimeoutPoll](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseTimeoutPollExample.kt) | Use timeout to poll for content. Triggers the callback after the last task is completed. |
| [useTimestamp](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseTimestampExample.kt) | A hook that return now timestamp as a reactive state.        |

#### Math

| hook name                   | description                     |
| --------------------------- | ------------------------------- |
| Reactive `kotlin.math.abs`. |
| useCeil                     | Reactive `kotlin.math.ceil`     |
| useRound                    | Reactive `kotlin.math.round`    |
| useTrunc                    | Reactive `kotlin.math.truncate` |
| useMin                      | Reactive `kotlin.math.min`      |
| useMax                      | Reactive `kotlin.math.max`      |
| usePow                      | Reactive `kotlin.math.pow`      |
| useSqrt                     | Reactive `kotlin.math.sqrt`     |

#### Utilities

| hook name                                                    | description                                                  |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| [useAsync](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseAsyncExample.kt) | A hook that encapsulates `rememberCoroutineScope` to make it easier to use coroutines |
| [useBiometric](https://github.com/junerver/ComposeHooks/blob/master/app/src/androidMain/kotlin/xyz/junerver/composehooks/example/UseBiometricExample.kt)* | use biometrics conveniently                                  |
| [useBatteryInfo](https://github.com/junerver/ComposeHooks/blob/master/app/src/androidMain/kotlin/xyz/junerver/composehooks/example/UseDeviceInfoExample.kt)* | A hook that can get the battery level and if is charging.    |
| [useBuildInfo](https://github.com/junerver/ComposeHooks/blob/master/app/src/androidMain/kotlin/xyz/junerver/composehooks/example/UseDeviceInfoExample.kt)* | A hook that can get the brand, model, and version of android. |
| [useClipboard](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseClipboardExample.kt) | Easy to use Clipboard                                        |
| [useCountdown](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseCountdownExample.kt) | A hook for manage countdown.                                 |
| [useCounter](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseCounterExample.kt) | A hook that manage counter.                                  |
| [useCycleList](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseCycleListExample.kt) | Cycle through a list of items.                               |
| [useDebounceFn](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseDebounceExample.kt) | A hook that deal with the debounced function.                |
| [useDisableScreenshot](https://github.com/junerver/ComposeHooks/blob/master/app/src/androidMain/kotlin/xyz/junerver/composehooks/example/UseDeviceInfoExample.kt)* | A hook used to handle the prohibition of screenshots on privacy pages. |
| [useEvent](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseEventExample.kt) | Implement lightweight cross-component communication using the subscribe-publish pattern |
| [useFlashlight](https://github.com/junerver/ComposeHooks/blob/master/app/src/androidMain/kotlin/xyz/junerver/composehooks/example/UseDeviceInfoExample.kt)* | A Hook for convenient use of flashlight.                     |
| [useIdel](https://github.com/junerver/ComposeHooks/blob/master/app/src/androidMain/kotlin/xyz/junerver/composehooks/example/UseIdelExample.kt) | Tracks whether the user is being inactive.                   |
| useKeyboard                                                  | A Hook that controls the display and hiding of the soft keyboard. |
| [useNetwork](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseNetworkExample.kt)* | A hook for obtaining network connection status and type.     |
| [useRequest](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseRequestExample.kt) | Manage network requests and implement: [manual and automatic](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/request/Auto%26Manual.kt) triggering; [life cycle callbacks](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/request/Lifecycle.kt); [refresh](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/request/Refresh.kt); [mutate changes](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/request/Mutate.kt); [cancel requests](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/request/Cancel.kt); [polling](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/request/Polling.kt); [Ready](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/request/Ready.kt); [dependency refresh](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/request/DepsRefresh.kt); [debounce](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/request/Debounce.kt), [throttle](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/request/Throttle.kt); [error retry](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/request/ErrorRetry.kt); |
| [useScreenInfo](https://github.com/junerver/ComposeHooks/blob/master/app/src/androidMain/kotlin/xyz/junerver/composehooks/example/UseDeviceInfoExample.kt)* | A hook that obtains information about the screen width, height, horizontal and vertical orientation. |
| [useThrottleFn](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseThrottleExample.kt) | A hook that deal with the throttled function.                |
| [useUndo](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseUndoExample.kt) | A Hook for handling undo and redo.                           |
| [useUpdate](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseUpdateExample.kt) | A hook that returns a function which can be used to force the component to re-render. |
| [useVibrate](https://github.com/junerver/ComposeHooks/blob/master/app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseVibrateExample.kt)* | A hook that make using vibration feedback easy               |

> Functions marked with `*` can only be used on Android

## Add to dependencies

**KMP project**

```
// groovy
implementation 'xyz.junerver.compose:hooks2:<latest_release>'
// kotlin
implementation("xyz.junerver.compose:hooks2:<latest_release>")
```

**Use `hooks2` In Android**

For pure Android projects, please use the following dependencies（Artifact id：`hooks2-android`）：
```kotlin
implementation("xyz.junerver.compose:hooks2-android:<latest_release>")
```

If used in ComposeMultiplatform, use artifact id: `hooks2`

**Old version hooks continue to support**

If your project does not have performance issues due to recompose , you can continue to use the old version in the **Android project**, and the bugfix will be synchronized to the old version in subsequent development.

```kotlin
implementation("xyz.junerver.compose:hooks:2.0.3")
```



## Quick Setup

1. Use `useGetState` to quickly create controlled components

   ```kotlin
   val (name, setName, getName) = useGetState("")
   OutlinedTextField(
       value = getName(), // or `name.value`
       onValueChange = setName,
       label = { Text("Input Name") }
   )
   ```

2. Use `useEffect` to perform component LaunchedEffects

3. Use `useRef` to create object references that are not affected by component recompose

   ```kotlin
   val countRef = useRef(0) // or `val countRef by useRef(0)`
   Button(onClick = {
       countRef.current += 1 // or `countRef += 1`
       println(countRef)
   }) {
       Text(text = "Ref= ${countRef.current}") // or `countRef`
   }
   ```

4. Use `useRequest` to easily manage **network query state**

   ```kotlin
   val (dataState, loadingState, errorState, run) = useRequest(
       requestFn = WebService::login.asRequestFn(), //Encapsulate the corresponding extension functions yourself,to make retrofit friendly
       optionsOf = {
           manual = true
       }
   )
   val data by dataState // obtained `value` through delegate
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
   useRequest organizes code through a plug-in pattern, the core code is extremely simple, and can be easily extended for more advanced features. Current features include:
   - Automatic/manual request
   - Polling
   - Debounce
   - Throttle
   - Error retry
   - Loading delay
   - SWR(stale-while-revalidate)
   - Caching

For more usage, please refer to [wiki](https://github.com/junerver/ComposeHooks/wiki) and examples

## Live Templates

Copy `hooks` in the `Live Templates` directory
File, paste into `C:\Users\<user-name>\AppData\Roaming\Google\AndroidStudio2023.2\templates\`

You can easily create code snippets of `useState` and `useRequest` through `us` and `ur`.

## Open Inlay Hints for Kotlin Type

For hooks like `useRequest`, its return value can deconstruct many objects and functions. It is necessary to enable InlayHint:

Editor - Inlay Hints - Types - Kotlin

## ProGuard
If you are using ProGuard you might need to add the following option:

```
-keep class xyz.junerver.composehooks.** { *; }
-keepclassmembers class xyz.junerver.composehooks.** { *; }
-dontwarn xyz.junerver.composehooks.**
```


## Documentation

- [Easily manage network requests with useRequest](https://junerver.github.io/2024/03/06/%E5%9C%A8Compose%E4%B8%AD%E4%BD%BF%E7%94%A8useRequest%E8%BD%BB%E6%9D%BE%E7%AE%A1%E7%90%86%E7%BD%91%E7%BB%9C%E8%AF%B7%E6%B1%82/)
- [Using state hoisting in Compose? I'll hoisting to Provider](https://junerver.github.io/2024/03/11/%E5%9C%A8Compose%E4%B8%AD%E4%BD%BF%E7%94%A8%E7%8A%B6%E6%80%81%E6%8F%90%E5%8D%87%EF%BC%9F%E6%88%91%E6%8F%90%E5%8D%87%E4%B8%AAP-Provider/)
- [How does a parent component call the function of a child component in Compose?](https://junerver.github.io/2024/03/13/%E5%9C%A8Compose%E4%B8%AD%E7%88%B6%E7%BB%84%E4%BB%B6%E5%A6%82%E4%BD%95%E8%B0%83%E7%94%A8%E5%AD%90%E7%BB%84%E4%BB%B6%E7%9A%84%E5%87%BD%E6%95%B0%EF%BC%9F/)
- [How to use MVI idea conveniently in Compose? Try useReducer!](https://junerver.github.io/2024/03/18/%E5%9C%A8Compose%E4%B8%AD%E6%96%B9%E4%BE%BF%E7%9A%84%E4%BD%BF%E7%94%A8MVI%E6%80%9D%E6%83%B3%EF%BC%9F%E8%AF%95%E8%AF%95useReducer%EF%BC%81/)
- [Easily manage global state in Compose like using redux](https://junerver.github.io/2024/04/01/%E5%9C%A8Compose%E4%B8%AD%E5%83%8F%E4%BD%BF%E7%94%A8redux%E4%B8%80%E6%A0%B7%E8%BD%BB%E6%9D%BE%E7%AE%A1%E7%90%86%E5%85%A8%E5%B1%80%E7%8A%B6%E6%80%81/)
- [Easily use asynchronous dispatch to manage global state in Compose](https://junerver.github.io/2024/04/02/%E5%9C%A8Compose%E4%B8%AD%E8%BD%BB%E6%9D%BE%E4%BD%BF%E7%94%A8%E5%BC%82%E6%AD%A5dispatch%E7%AE%A1%E7%90%86%E5%85%A8%E5%B1%80%E7%8A%B6%E6%80%81/)
- [Managing network requests in Compose is so easy!](https://junerver.github.io/2024/04/03/%E5%9C%A8Compose%E4%B8%AD%E7%AE%A1%E7%90%86%E7%BD%91%E7%BB%9C%E8%AF%B7%E6%B1%82%E7%AB%9F%E7%84%B6%E5%A6%82%E6%AD%A4%E7%AE%80%E5%8D%95%EF%BC%81/)
- [Use debounce and throttle elegantly in Jetpack Compose](https://junerver.github.io/2024/04/11/%E5%9C%A8Jetpack-Compose%E4%B8%AD%E4%BC%98%E9%9B%85%E7%9A%84%E4%BD%BF%E7%94%A8%E9%98%B2%E6%8A%96%E3%80%81%E8%8A%82%E6%B5%81/)

## Todo:

- ~~KMP friendly~~
- ~~CI~~
- Unit Test
- Complete documentation

## 参考/Thanks

1. [alibaba](https://github.com/alibaba)/[hooks](https://github.com/alibaba/hooks)
2. [pavi2410](https://github.com/pavi2410)/[useCompose](https://github.com/pavi2410/useCompose)

## Contributing Guidelines

[Contributing guidelines](https://github.com/junerver/ComposeHooks/blob/master/CONTRIBUTING.md)

## License

[Apache License 2.0](https://choosealicense.com/licenses/apache-2.0/)
