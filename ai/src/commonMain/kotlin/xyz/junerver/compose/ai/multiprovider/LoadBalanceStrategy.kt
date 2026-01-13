package xyz.junerver.compose.ai.multiprovider

/*
  Description: Load balance strategies for multi-provider support
  Author: Junerver
  Date: 2026/01/13
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Load balance strategy for selecting providers.
 *
 * Defines how requests are distributed across multiple providers.
 */
sealed class LoadBalanceStrategy {
    /**
     * Round-robin strategy: Selects providers in sequential order.
     *
     * Simple and efficient, ensures even distribution of requests.
     */
    data object RoundRobin : LoadBalanceStrategy()

    /**
     * Random strategy: Randomly selects a provider for each request.
     *
     * Provides good distribution with minimal overhead.
     */
    data object Random : LoadBalanceStrategy()

    /**
     * Weighted strategy: Selects providers based on configured weights.
     *
     * Allows prioritizing certain providers over others.
     *
     * @property weights Map of provider names to their weights (higher = more likely to be selected)
     *
     * Example:
     * ```kotlin
     * LoadBalanceStrategy.Weighted(
     *     weights = mapOf(
     *         "DeepSeek" to 3,  // 60% of requests
     *         "OpenAI" to 2     // 40% of requests
     *     )
     * )
     * ```
     */
    data class Weighted(val weights: Map<String, Int>) : LoadBalanceStrategy()

    /**
     * Smart strategy: Dynamically selects the best provider based on metrics.
     *
     * Considers success rate and response time to choose the optimal provider.
     * Uses a scoring algorithm: success_rate * 0.7 + normalized_response_time * 0.3
     *
     * Note: Requires some initial requests to collect metrics. Initially behaves like RoundRobin.
     */
    data object Smart : LoadBalanceStrategy()
}
