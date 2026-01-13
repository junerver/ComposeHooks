package xyz.junerver.compose.ai.multiprovider

/*
  Description: Aggregate exception for multi-provider failures
  Author: Junerver
  Date: 2026/01/13
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Exception thrown when all providers fail.
 *
 * Contains error information from all attempted providers, allowing
 * for detailed error analysis and debugging.
 *
 * @property errors Map of provider names to their respective errors
 * @property message Aggregated error message
 *
 * Example:
 * ```kotlin
 * try {
 *     // Multi-provider request
 * } catch (e: AggregateException) {
 *     // Check if all errors are rate limit errors
 *     if (e.allErrorsAre<OpenAIException>()) {
 *         println("All providers hit rate limits")
 *     }
 *
 *     // Get specific provider error
 *     val deepSeekError = e.getError("DeepSeek")
 *     println("DeepSeek error: ${deepSeekError?.message}")
 *
 *     // Get last error
 *     val lastError = e.getLastError()
 *     println("Last error: ${lastError?.message}")
 * }
 * ```
 */
class AggregateException(
    val errors: Map<String, Throwable>,
    message: String = buildMessage(errors),
) : Exception(message) {
    /**
     * Gets the last error that occurred.
     *
     * @return The last error, or null if no errors
     */
    fun getLastError(): Throwable? = errors.values.lastOrNull()

    /**
     * Gets the error for a specific provider.
     *
     * @param providerName The name of the provider
     * @return The error for that provider, or null if not found
     */
    fun getError(providerName: String): Throwable? = errors[providerName]

    /**
     * Checks if all errors are of a specific type.
     *
     * @param T The error type to check
     * @return true if all errors are of type T, false otherwise
     */
    inline fun <reified T : Throwable> allErrorsAre(): Boolean = errors.values.all { it is T }

    /**
     * Gets all errors of a specific type.
     *
     * @param T The error type to filter
     * @return Map of provider names to errors of type T
     */
    inline fun <reified T : Throwable> getErrorsOfType(): Map<String, T> = errors.filterValues { it is T }.mapValues { it.value as T }

    /**
     * Gets the number of providers that failed.
     */
    val failedProviderCount: Int
        get() = errors.size

    /**
     * Gets the list of failed provider names.
     */
    val failedProviders: List<String>
        get() = errors.keys.toList()

    companion object {
        private fun buildMessage(errors: Map<String, Throwable>): String {
            if (errors.isEmpty()) {
                return "All providers failed (no error details available)"
            }

            val errorSummary = errors.entries.joinToString(separator = "; ") { (provider, error) ->
                "$provider: ${error.message ?: error::class.simpleName}"
            }

            return "All ${errors.size} provider(s) failed: $errorSummary"
        }
    }
}
