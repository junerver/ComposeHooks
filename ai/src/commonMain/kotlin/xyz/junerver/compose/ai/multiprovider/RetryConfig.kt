package xyz.junerver.compose.ai.multiprovider

/*
  Description: Retry configuration for multi-provider failover
  Author: Junerver
  Date: 2026/01/13
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Configuration for retry behavior in multi-provider mode.
 *
 * @property maxRetries Maximum number of retry attempts (default: 3)
 * @property retryableStatusCodes HTTP status codes that should trigger a retry (default: 429, 500, 502, 503, 504)
 * @property retryDelay Initial delay between retries in milliseconds (default: 1000ms)
 * @property exponentialBackoff Whether to use exponential backoff for retry delays (default: true)
 *
 * Example:
 * ```kotlin
 * // Conservative retry policy
 * RetryConfig(
 *     maxRetries = 2,
 *     retryDelay = 2000L,
 *     exponentialBackoff = true
 * )
 *
 * // Aggressive retry policy
 * RetryConfig(
 *     maxRetries = 5,
 *     retryDelay = 500L,
 *     exponentialBackoff = true
 * )
 * ```
 */
data class RetryConfig(
    val maxRetries: Int = 3,
    val retryableStatusCodes: Set<Int> = setOf(429, 500, 502, 503, 504),
    val retryDelay: Long = 1000L,
    val exponentialBackoff: Boolean = true,
) {
    init {
        require(maxRetries >= 0) { "maxRetries must be non-negative" }
        require(retryDelay >= 0) { "retryDelay must be non-negative" }
    }

    /**
     * Calculates the delay for a specific retry attempt.
     *
     * @param attempt The retry attempt number (0-indexed)
     * @return The delay in milliseconds
     */
    fun calculateDelay(attempt: Int): Long = if (exponentialBackoff) {
        retryDelay * (1 shl attempt) // 2^attempt
    } else {
        retryDelay
    }

    companion object {
        /**
         * Returns the default retry configuration.
         */
        fun default() = RetryConfig()

        /**
         * Returns a conservative retry configuration.
         *
         * - Max retries: 2
         * - Initial delay: 2000ms
         * - Exponential backoff: enabled
         */
        fun conservative() = RetryConfig(
            maxRetries = 2,
            retryDelay = 2000L,
            exponentialBackoff = true,
        )

        /**
         * Returns an aggressive retry configuration.
         *
         * - Max retries: 5
         * - Initial delay: 500ms
         * - Exponential backoff: enabled
         */
        fun aggressive() = RetryConfig(
            maxRetries = 5,
            retryDelay = 500L,
            exponentialBackoff = true,
        )

        /**
         * Returns a no-retry configuration.
         *
         * Useful for testing or when you want immediate failover without retries.
         */
        fun noRetry() = RetryConfig(
            maxRetries = 1,
            retryDelay = 0L,
            exponentialBackoff = false,
        )
    }
}
