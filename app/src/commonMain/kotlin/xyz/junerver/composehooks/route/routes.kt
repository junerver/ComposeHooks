package xyz.junerver.composehooks.route

import androidx.compose.runtime.Composable
import xyz.junerver.composehooks.example.DeferReads
import xyz.junerver.composehooks.example.HomeScreen
import xyz.junerver.composehooks.example.RequestExampleList
import xyz.junerver.composehooks.example.UseAgentExample
import xyz.junerver.composehooks.example.UseAsyncExample
import xyz.junerver.composehooks.example.UseAutoResetExample
import xyz.junerver.composehooks.example.UseBooleanExample
import xyz.junerver.composehooks.example.UseChatExample
import xyz.junerver.composehooks.example.UseClipboardExample
import xyz.junerver.composehooks.example.UseContextExample
import xyz.junerver.composehooks.example.UseCountdownExample
import xyz.junerver.composehooks.example.UseCounterExample
import xyz.junerver.composehooks.example.UseCreationExample
import xyz.junerver.composehooks.example.UseCycleListExample
import xyz.junerver.composehooks.example.UseDateFormatExample
import xyz.junerver.composehooks.example.UseDebounceExample
import xyz.junerver.composehooks.example.UseEffectExample
import xyz.junerver.composehooks.example.UseEventExample
import xyz.junerver.composehooks.example.UseFormExample
import xyz.junerver.composehooks.example.UseGenerateObjectExample
import xyz.junerver.composehooks.example.UseGetStateExample
import xyz.junerver.composehooks.example.UseImmutableListExample
import xyz.junerver.composehooks.example.UseIntervalExample
import xyz.junerver.composehooks.example.UseLastChangedExample
import xyz.junerver.composehooks.example.UseLatestExample
import xyz.junerver.composehooks.example.UseListExample
import xyz.junerver.composehooks.example.UseMapExample
import xyz.junerver.composehooks.example.UseMountExample
import xyz.junerver.composehooks.example.UseNowExample
import xyz.junerver.composehooks.example.UseNumberExample
import xyz.junerver.composehooks.example.UsePausableEffectExample
import xyz.junerver.composehooks.example.UsePersistentExample
import xyz.junerver.composehooks.example.UsePreviousExample
import xyz.junerver.composehooks.example.UseReducerExample
import xyz.junerver.composehooks.example.UseReduxExample
import xyz.junerver.composehooks.example.UseRefExample
import xyz.junerver.composehooks.example.UseResetStateExample
import xyz.junerver.composehooks.example.UseSelectableExample
import xyz.junerver.composehooks.example.UseSortedExample
import xyz.junerver.composehooks.example.UseStateExample
import xyz.junerver.composehooks.example.UseStateMachineExample
import xyz.junerver.composehooks.example.UseTableExample
import xyz.junerver.composehooks.example.UseTableRequestExample
import xyz.junerver.composehooks.example.UseThrottleExample
import xyz.junerver.composehooks.example.UseTimeAgoExample
import xyz.junerver.composehooks.example.UseTimeoutExample
import xyz.junerver.composehooks.example.UseTimeoutFnExample
import xyz.junerver.composehooks.example.UseTimeoutPollExample
import xyz.junerver.composehooks.example.UseTimestampExample
import xyz.junerver.composehooks.example.UseToggleExample
import xyz.junerver.composehooks.example.UseUndoExample
import xyz.junerver.composehooks.example.UseUpdateEffectExample
import xyz.junerver.composehooks.example.UseUpdateExample
import xyz.junerver.composehooks.example.request.AutoManual
import xyz.junerver.composehooks.example.request.Cache
import xyz.junerver.composehooks.example.request.Cancel
import xyz.junerver.composehooks.example.request.Debounce
import xyz.junerver.composehooks.example.request.DepsRefresh
import xyz.junerver.composehooks.example.request.ErrorRetry
import xyz.junerver.composehooks.example.request.Lifecycle
import xyz.junerver.composehooks.example.request.LoadingDelay
import xyz.junerver.composehooks.example.request.Mutate
import xyz.junerver.composehooks.example.request.Polling
import xyz.junerver.composehooks.example.request.Ready
import xyz.junerver.composehooks.example.request.Refresh
import xyz.junerver.composehooks.example.request.Throttle
import xyz.junerver.composehooks.example.sub.PersistentSub

/*
  Description:
  Author: Junerver
  Date: 2024/3/8-8:50
  Email: junerver@gmail.com
  Version: v1.0
*/

expect fun getPlatformSpecialRoutes(): Map<String, @Composable () -> Unit>

val platformSpecialRoutes: Map<String, @Composable () -> Unit> by lazy { getPlatformSpecialRoutes() }

val routes = mapOf<String, @Composable () -> Unit>(
    "/" to { HomeScreen() },
    "DeferReads" to { DeferReads() },
    "useRequest" to { RequestExampleList() },
    "useAsync" to { UseAsyncExample() },
    "useAutoReset" to { UseAutoResetExample() },
    "useAgent" to { UseAgentExample() },
    "useBoolean" to { UseBooleanExample() },
    "useChat" to { UseChatExample() },
    "useClipboard" to { UseClipboardExample() },
    "useContext" to { UseContextExample() },
    "useCountdown" to { UseCountdownExample() },
    "useCounter" to { UseCounterExample() },
    "useCreation" to { UseCreationExample() },
    "useDateFormat" to { UseDateFormatExample() },
    "useDebounce" to { UseDebounceExample() },
    "useEffect" to { UseEffectExample() },
    "useEvent" to { UseEventExample() },
    "useForm" to { UseFormExample() },
    "useGenerateObject" to { UseGenerateObjectExample() },
    "useGetState" to { UseGetStateExample() },
    "useImmutableList" to { UseImmutableListExample() },
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
    "useUpdateEffect" to { UseUpdateEffectExample() },
    "useSelectable" to { UseSelectableExample() },
    "useStateMachine" to { UseStateMachineExample() },
    "useTimeAgo" to { UseTimeAgoExample() },
    "useLastChanged" to { UseLastChangedExample() },
    "useTimeoutFn" to { UseTimeoutFnExample() },
    "useTimeoutPoll" to { UseTimeoutPollExample() },
    "usePausableEffect" to { UsePausableEffectExample() },
    "useCycleList" to { UseCycleListExample() },
    "useSorted" to { UseSortedExample() },
    "useTable" to { UseTableExample() },
    "useTableRequest" to { UseTableRequestExample() },
) + platformSpecialRoutes

expect fun getSubRequestRoutes(): Map<String, @Composable () -> Unit>

val extRequestRoutes: Map<String, @Composable () -> Unit> by lazy { getSubRequestRoutes() }
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
    "errorRetry" to { ErrorRetry() },
) + extRequestRoutes

val otherSubRoutes = mapOf<String, @Composable () -> Unit>(
    "PersistentSub" to { PersistentSub() },
)
