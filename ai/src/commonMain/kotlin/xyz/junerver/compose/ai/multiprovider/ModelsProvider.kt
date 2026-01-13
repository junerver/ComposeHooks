@file:Suppress("FunctionNaming") // Composable functions should start with uppercase

package xyz.junerver.compose.ai.multiprovider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import xyz.junerver.compose.ai.usechat.ChatProvider

/*
  Description: Provider component for multi-provider support
  Author: Junerver
  Date: 2026/01/13
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Provider component for multi-provider support.
 *
 * This component enables automatic failover and load balancing across multiple
 * AI providers. When [useChat] is used within this provider, it will automatically
 * use the multi-provider logic instead of single-provider mode.
 *
 * ## Features
 * - **Automatic Failover**: Switches to another provider when one fails
 * - **Load Balancing**: Distributes requests across providers using various strategies
 * - **Retry Logic**: Configurable retry behavior with exponential backoff
 * - **Metrics Tracking**: Tracks success rate and response time for smart selection
 *
 * ## Usage
 * ```kotlin
 * ModelsProvider(
 *     providers = listOf(
 *         Providers.DeepSeek(apiKey = "sk-xxx"),
 *         Providers.OpenAI(apiKey = "sk-yyy"),
 *         Providers.Anthropic(apiKey = "sk-zzz"),
 *     ),
 *     strategy = LoadBalanceStrategy.Smart,
 *     retryConfig = RetryConfig.default()
 * ) {
 *     // useChat will automatically use multi-provider mode
 *     val chatHolder = useChat {
 *         systemPrompt = "You are a helpful assistant."
 *     }
 *
 *     // Use chatHolder as normal
 *     chatHolder.sendText("Hello!")
 * }
 * ```
 *
 * ## Load Balance Strategies
 * - **RoundRobin**: Selects providers in sequential order
 * - **Random**: Randomly selects a provider for each request
 * - **Weighted**: Selects providers based on configured weights
 * - **Smart**: Dynamically selects the best provider based on metrics
 *
 * ## Backward Compatibility
 * If you explicitly specify a provider in [useChat], it will use single-provider
 * mode even when inside [ModelsProvider]:
 * ```kotlin
 * ModelsProvider(providers = listOf(...)) {
 *     // This uses multi-provider mode
 *     useChat { }
 *
 *     // This uses single-provider mode (explicit provider)
 *     useChat {
 *         provider = Providers.Anthropic(apiKey = "sk-zzz")
 *     }
 * }
 * ```
 *
 * @param providers List of chat providers to use
 * @param strategy Load balance strategy (default: RoundRobin)
 * @param retryConfig Retry configuration (default: RetryConfig.default())
 * @param content Child components that will have access to multi-provider support
 */
@Suppress("FunctionNaming") // Composable functions should start with uppercase
@Composable
fun ModelsProvider(
    providers: List<ChatProvider>,
    strategy: LoadBalanceStrategy = LoadBalanceStrategy.RoundRobin,
    retryConfig: RetryConfig = RetryConfig.default(),
    content: @Composable () -> Unit,
) {
    require(providers.isNotEmpty()) { "At least one provider must be specified" }

    val contextValue = remember(providers, strategy, retryConfig) {
        ModelsContextValue(
            providers = providers,
            strategy = strategy,
            retryConfig = retryConfig,
        )
    }

    ModelsContext.Provider(value = contextValue, content = content)
}

/**
 * Provider component for multi-provider support (Map overload).
 *
 * This overload accepts a map of provider names to providers, which can be
 * more convenient when you want to reference providers by name.
 *
 * Example:
 * ```kotlin
 * ModelsProvider(
 *     providers = mapOf(
 *         "deepseek" to Providers.DeepSeek(apiKey = "sk-xxx"),
 *         "openai" to Providers.OpenAI(apiKey = "sk-yyy"),
 *     ),
 *     strategy = LoadBalanceStrategy.Weighted(
 *         weights = mapOf(
 *             "DeepSeek" to 3,  // 60% of requests
 *             "OpenAI" to 2     // 40% of requests
 *         )
 *     )
 * ) {
 *     // ...
 * }
 * ```
 *
 * @param providers Map of provider names to providers
 * @param strategy Load balance strategy (default: RoundRobin)
 * @param retryConfig Retry configuration (default: RetryConfig.default())
 * @param content Child components that will have access to multi-provider support
 */
@Suppress("FunctionNaming") // Composable functions should start with uppercase
@Composable
fun ModelsProvider(
    providers: Map<String, ChatProvider>,
    strategy: LoadBalanceStrategy = LoadBalanceStrategy.RoundRobin,
    retryConfig: RetryConfig = RetryConfig.default(),
    content: @Composable () -> Unit,
) {
    ModelsProvider(
        providers = providers.values.toList(),
        strategy = strategy,
        retryConfig = retryConfig,
        content = content,
    )
}
