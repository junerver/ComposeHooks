package xyz.junerver.composehooks.route

import androidx.compose.runtime.Composable
import xyz.junerver.composehooks.example.*
import xyz.junerver.composehooks.example.request.*
import xyz.junerver.composehooks.example.sub.PersistentSub

/*
  Description:
  Author: Junerver
  Date: 2024/3/8-8:50
  Email: junerver@gmail.com
  Version: v1.0
*/
expect val androidRoutes: Map<String, @Composable () -> Unit>

val routes = mapOf<String, @Composable () -> Unit>(
    "/" to { HomeScreen() },
    "useRequest" to { RequestExampleList() },
    "useAsync" to { UseAsyncExample() },
    "useAutoReset" to { UseAutoResetExample() },
    "useBoolean" to { UseBooleanExample() },
    "useClipboard" to { UseClipboardExample() },
    "useContext" to { UseContextExample() },
    "useCountdown" to { UseCountdownExample() },
    "useCounter" to { UseCounterExample() },
    "useCreation" to { UseCreationExample() },
    "useDebounce" to { UseDebounceExample() },
    "useEffect" to { UseEffectExample() },
    "useEvent" to { UseEventExample() },
    "useForm" to { UseFormExample() },
    "useGetState" to { UseGetStateExample() },
    "useInterval" to { UseIntervalExample() },
    "useLatest" to { UseLatestExample() },
    "useList" to { UseListExample() },
    "useMap" to { UseMapExample() },
    "useMount" to { UseMountExample() },
    "useNow" to { UseNowExample() },
    "useNumber" to { UseNumberExample() },
    "usePersistent" to { UsePersistentExample() },
    "usePrevious" to { UsePreviousExample() },
    "useReducer" to { UseReducerExample() },
    "useRedux" to { UseReduxExample() },
    "useRef" to { UseRefExample() },
    "useResetState" to { UseResetStateExample() },
    "useState" to { UseStateExample() },
    "useThrottle" to { UseThrottleExample() },
    "useTimeout" to { UseTimeoutExample() },
    "useTimestamp" to { UseTimestampExample() },
    "useToggle" to { UseToggleExample() },
    "useUndo" to { UseUndoExample() },
    "useUnmount" to { UseMountExample() },
    "useUpdate" to { UseUpdateExample() },
    "useUpdateEffect" to { UseUpdateEffectExample() }

) + androidRoutes

val subRequestRoutes = mapOf<String, @Composable () -> Unit>(
    "auto&manual" to { AutoManual() },
    "lifecycle" to { Lifecycle() },
    "refresh" to { Refresh() },
    "mutate" to { Mutate() },
    "cancel" to { Cancel() },
    "loadingDelay" to { LoadingDelay() },
    "polling" to { Polling() },
    "ready" to { Ready() },
    "depsRefresh" to { DepsRefresh() },
    "debounce" to { Debounce() },
    "throttle" to { Throttle() },
    "cache&swr" to { Cache() },
    "errorRetry" to { ErrorRetry() }
)

val otherSubRoutes = mapOf<String, @Composable () -> Unit>(
    "PersistentSub" to { PersistentSub() }
)
