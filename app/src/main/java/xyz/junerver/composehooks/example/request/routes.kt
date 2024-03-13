package xyz.junerver.composehooks.example.request

import androidx.compose.runtime.Composable
import xyz.junerver.composehooks.example.RequestExampleList

/**
 * Description:
 * @author Junerver
 * date: 2024/3/12-8:25
 * Email: junerver@gmail.com
 * Version: v1.0
 */
val subRoutes = mapOf<String, @Composable () -> Unit>(
    "/" to { RequestExampleList() },
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
    "cache&swr" to { TODO() },
    "errorRetry" to { ErrorRetry() }
)
