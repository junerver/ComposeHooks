package xyz.junerver.compose.ai.multiprovider

/*
  Description: Provider metrics for smart load balancing
  Author: Junerver
  Date: 2026/01/13
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Metrics for tracking provider performance.
 *
 * Used by [LoadBalanceStrategy.Smart] to dynamically select the best provider
 * based on success rate and response time.
 *
 * @property providerName The name of the provider
 * @property totalRequests Total number of requests sent to this provider
 * @property successfulRequests Number of successful requests
 * @property failedRequests Number of failed requests
 * @property totalResponseTime Total response time in milliseconds (for successful requests)
 */
internal data class ProviderMetrics(
    val providerName: String,
    var totalRequests: Long = 0,
    var successfulRequests: Long = 0,
    var failedRequests: Long = 0,
    var totalResponseTime: Long = 0, // milliseconds
) {
    /**
     * Success rate (0.0 to 1.0).
     *
     * Returns 0.0 if no requests have been made yet.
     */
    val successRate: Double
        get() = if (totalRequests > 0) successfulRequests.toDouble() / totalRequests else 0.0

    /**
     * Average response time in milliseconds.
     *
     * Returns Long.MAX_VALUE if no successful requests have been made yet.
     */
    val averageResponseTime: Long
        get() = if (successfulRequests > 0) totalResponseTime / successfulRequests else Long.MAX_VALUE

    /**
     * Calculates a composite score for this provider.
     *
     * The score is calculated as:
     * - Success rate * 0.7 (70% weight)
     * - Normalized response time * 0.3 (30% weight)
     *
     * Higher scores indicate better performance.
     *
     * @param maxResponseTime The maximum average response time across all providers
     * @return A score between 0.0 and 1.0
     */
    fun score(maxResponseTime: Long): Double {
        val normalizedTime = if (maxResponseTime > 0) {
            1.0 - (averageResponseTime.toDouble() / maxResponseTime)
        } else {
            0.0
        }
        return successRate * 0.7 + normalizedTime.coerceAtLeast(0.0) * 0.3
    }

    /**
     * Records a successful request.
     *
     * @param responseTime Response time in milliseconds
     */
    fun recordSuccess(responseTime: Long) {
        totalRequests++
        successfulRequests++
        totalResponseTime += responseTime
    }

    /**
     * Records a failed request.
     */
    fun recordFailure() {
        totalRequests++
        failedRequests++
    }
}
