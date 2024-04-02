package xyz.junerver.composehooks.route

import androidx.compose.runtime.Composable
import xyz.junerver.composehooks.HomeScreen
import xyz.junerver.composehooks.example.UseAsyncExample
import xyz.junerver.composehooks.example.UseBooleanExample
import xyz.junerver.composehooks.example.UseClipboardExample
import xyz.junerver.composehooks.example.UseContextExample
import xyz.junerver.composehooks.example.UseCreationExample
import xyz.junerver.composehooks.example.UseDebounceExample
import xyz.junerver.composehooks.example.UseEffectExample
import xyz.junerver.composehooks.example.UseEventExample
import xyz.junerver.composehooks.example.UseFormExample
import xyz.junerver.composehooks.example.UseIntervalExample
import xyz.junerver.composehooks.example.UseLatestExample
import xyz.junerver.composehooks.example.UseListExample
import xyz.junerver.composehooks.example.UseMapExample
import xyz.junerver.composehooks.example.UseMountExample
import xyz.junerver.composehooks.example.UseNetworkExample
import xyz.junerver.composehooks.example.UseNowExample
import xyz.junerver.composehooks.example.UseNumberExample
import xyz.junerver.composehooks.example.UsePreviousExample
import xyz.junerver.composehooks.example.UseReducerExample
import xyz.junerver.composehooks.example.UseReduxExample
import xyz.junerver.composehooks.example.UseRefExample
import xyz.junerver.composehooks.example.UseRequestExample
import xyz.junerver.composehooks.example.UseSensorExample
import xyz.junerver.composehooks.example.UseStateExample
import xyz.junerver.composehooks.example.UseThrottleExample
import xyz.junerver.composehooks.example.UseTimeoutExample
import xyz.junerver.composehooks.example.UseTimestampExample
import xyz.junerver.composehooks.example.UseToggleExample
import xyz.junerver.composehooks.example.UseUndoExample
import xyz.junerver.composehooks.example.UseUpdateEffectExample
import xyz.junerver.composehooks.example.UseUpdateExample

/**
 * Description:
 * @author Junerver
 * date: 2024/3/8-8:50
 * Email: junerver@gmail.com
 * Version: v1.0
 */
val routes = mapOf<String, @Composable () -> Unit>(
    "/" to { HomeScreen() },
    "useRequest" to { UseRequestExample() },
    "useAsync" to { UseAsyncExample() },
    "useBoolean" to { UseBooleanExample() },
    "useClipboard" to { UseClipboardExample() },
    "useContext" to { UseContextExample() },
    "useCreation" to { UseCreationExample() },
    "useDebounce" to { UseDebounceExample() },
    "useEffect" to { UseEffectExample() },
    "useEvent" to { UseEventExample() },
    "useForm" to { UseFormExample() },
    "useInterval" to { UseIntervalExample() },
    "useLatest" to { UseLatestExample() },
    "useList" to { UseListExample() },
    "useMap" to { UseMapExample() },
    "useMount" to { UseMountExample() },
    "useNetwork" to { UseNetworkExample() },
    "useNow" to { UseNowExample() },
    "useNumber" to { UseNumberExample() },
    "usePrevious" to { UsePreviousExample() },
    "useReducer" to { UseReducerExample() },
    "useRedux" to { UseReduxExample() },
    "useRef" to { UseRefExample() },
    "useSensor" to { UseSensorExample() },
    "useState" to { UseStateExample() },
    "useThrottle" to { UseThrottleExample() },
    "useTimeout" to { UseTimeoutExample() },
    "useTimestamp" to { UseTimestampExample() },
    "useToggle" to { UseToggleExample() },
    "useUndo" to { UseUndoExample() },
    "useUnmount" to { UseMountExample() },
    "useUpdate" to { UseUpdateExample() },
    "useUpdateEffect" to { UseUpdateEffectExample() }
)
