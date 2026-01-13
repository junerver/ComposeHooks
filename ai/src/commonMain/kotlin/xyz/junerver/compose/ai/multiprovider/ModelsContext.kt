package xyz.junerver.compose.ai.multiprovider

import xyz.junerver.compose.ai.usechat.ChatProvider
import xyz.junerver.compose.hooks.createContext

/*
  Description: Context for multi-provider configuration
  Author: Junerver
  Date: 2026/01/13
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Context value for multi-provider configuration.
 *
 * Contains the list of providers, load balance strategy, and retry configuration
 * that will be used by [useChat] when in multi-provider mode.
 *
 * @property providers List of chat providers to use
 * @property strategy Load balance strategy for selecting providers
 * @property retryConfig Retry configuration for failover behavior
 */
data class ModelsContextValue(
    val providers: List<ChatProvider>,
    val strategy: LoadBalanceStrategy,
    val retryConfig: RetryConfig,
)

/**
 * Context for multi-provider configuration.
 *
 * This context is used internally by [ModelsProvider] to expose multi-provider
 * configuration to child components using [useChat].
 *
 * Do not use this context directly. Use [ModelsProvider] instead.
 */
@PublishedApi
internal val ModelsContext by lazy {
    createContext(
        ModelsContextValue(
            providers = emptyList(),
            strategy = LoadBalanceStrategy.RoundRobin,
            retryConfig = RetryConfig.default(),
        ),
    )
}
