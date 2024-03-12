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
    "lifecycle" to { TODO() },
    "refresh" to { TODO() },
    "mutate" to { TODO() },
    "cancel" to { TODO() },
    "loadingDelay" to { TODO() },
    "polling" to { TODO() },
    "ready" to { TODO() },
    "depsRefresh" to { TODO() },
    "debounce" to { TODO() },
    "throttle" to { TODO() },
    "cache&swr" to { TODO() },
    "errorRetry" to { TODO() }
)
