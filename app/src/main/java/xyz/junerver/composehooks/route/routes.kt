package xyz.junerver.composehooks.route

import androidx.compose.runtime.Composable
import xyz.junerver.composehooks.HomeScreen
import xyz.junerver.composehooks.example.TestBooleanScreen

/**
 * Description:
 * @author Junerver
 * date: 2024/3/8-8:50
 * Email: junerver@gmail.com
 * Version: v1.0
 */
val routes = arrayOf<Pair<String, @Composable () -> Unit>>(
    "/" to { HomeScreen() },
    "useNetwork" to { TODO() },
    "useRequest" to { TODO() },
    "useBoolean" to { TestBooleanScreen() },
    "useContext" to { TODO() },
    "useCreation" to { TODO() },
    "useDebounce" to { TODO() },
    "useEffect" to { TODO() },
    "useInterval" to { TODO() },
    "useLatest" to { TODO() },
    "useList" to { TODO() },
    "useMap" to { TODO() },
    "useMount" to { TODO() },
    "useNumber" to { TODO() },
    "usePrevious" to { TODO() },
    "useReducer" to { TODO() },
    "useRef" to { TODO() },
    "useState" to { TODO() },
    "useThrottle" to { TODO() },
    "useTimeout" to { TODO() },
    "useToggle" to { TODO() },
    "useUndo" to { TODO() },
    "useUnmount" to { TODO() },
)
