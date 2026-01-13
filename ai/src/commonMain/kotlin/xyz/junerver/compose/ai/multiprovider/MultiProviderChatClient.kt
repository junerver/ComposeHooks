package xyz.junerver.compose.ai.multiprovider

import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import xyz.junerver.compose.ai.http.HttpEngine
import xyz.junerver.compose.ai.http.HttpEngineConfig
import xyz.junerver.compose.ai.usechat.AnthropicException
import xyz.junerver.compose.ai.usechat.ChatClient
import xyz.junerver.compose.ai.usechat.ChatMessage
import xyz.junerver.compose.ai.usechat.ChatOptions
import xyz.junerver.compose.ai.usechat.ChatProvider
import xyz.junerver.compose.ai.usechat.ChatResponseResult
import xyz.junerver.compose.ai.usechat.OpenAIException
import xyz.junerver.compose.ai.usechat.StreamEvent

/*
  Description: Multi-provider chat client with failover and load balancing
  Author: Junerver
  Date: 2026/01/13
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Chat client that supports multiple providers with automatic failover and load balancing.
 *
 * This client wraps multiple [ChatClient] instances and provides:
 * - Automatic failover when a provider fails
 * - Load balancing across providers using various strategies
 * - Retry logic with exponential backoff
 * - Metrics tracking for smart provider selection
 *
 * @property providers List of chat providers to use
 * @property strategy Load balance strategy for selecting providers
 * @property retryConfig Retry configuration for failover behavior
 * @property baseOptions Base chat options (provider will be overridden)
 */
@OptIn(ExperimentalTime::class)
internal class MultiProviderChatClient(
    private val providers: List<ChatProvider>,
    private val strategy: LoadBalanceStrategy,
    private val retryConfig: RetryConfig,
    private val baseOptions: ChatOptions,
) {
    private var providerIndex = 0
    private val metricsMap = mutableMapOf<String, ProviderMetrics>()
    private val engine: HttpEngine = baseOptions.httpEngine ?: HttpEngineConfig.defaultEngineFactory()
    private val shouldCloseEngine: Boolean = baseOptions.httpEngine == null

    init {
        // Initialize metrics for all providers
        providers.forEach { provider ->
            metricsMap[provider.name] = ProviderMetrics(provider.name)
        }
    }

    /**
     * Selects a provider based on the configured strategy.
     */
    private fun selectProvider(): ChatProvider = when (strategy) {
        is LoadBalanceStrategy.RoundRobin -> {
            val index = providerIndex % providers.size
            providerIndex++
            providers[index]
        }

        is LoadBalanceStrategy.Random -> {
            providers.random()
        }

        is LoadBalanceStrategy.Weighted -> {
            selectWeightedProvider(providers, strategy.weights)
        }

        is LoadBalanceStrategy.Smart -> {
            selectSmartProvider()
        }
    }

    /**
     * Selects a provider based on weights.
     */
    private fun selectWeightedProvider(providers: List<ChatProvider>, weights: Map<String, Int>): ChatProvider {
        val totalWeight = providers.sumOf { weights[it.name] ?: 1 }
        var random = Random.nextInt(totalWeight)

        for (provider in providers) {
            val weight = weights[provider.name] ?: 1
            random -= weight
            if (random < 0) {
                return provider
            }
        }
        return providers.last()
    }

    /**
     * Selects the best provider based on metrics (success rate and response time).
     */
    private fun selectSmartProvider(): ChatProvider {
        val maxResponseTime = metricsMap.values.maxOfOrNull { it.averageResponseTime } ?: 1L

        return providers.maxByOrNull { provider ->
            metricsMap[provider.name]?.score(maxResponseTime) ?: 0.0
        } ?: providers.first()
    }

    /**
     * Records metrics for a provider.
     */
    private fun recordMetrics(providerName: String, success: Boolean, responseTime: Long) {
        metricsMap[providerName]?.let { metrics ->
            if (success) {
                metrics.recordSuccess(responseTime)
            } else {
                metrics.recordFailure()
            }
        }
    }

    /**
     * Checks if an error is retryable.
     */
    private fun isRetryable(error: Throwable): Boolean = when (error) {
        is OpenAIException -> {
            // Retry on rate limit and server errors
            error.errorCode in listOf("rate_limit", "server_error") ||
                error.errorType in listOf("rate_limit_error", "server_error")
        }

        is AnthropicException -> {
            // Retry on rate limit and overloaded errors
            error.errorType in listOf("rate_limit_error", "overloaded_error")
        }

        else -> {
            // Try to extract status code from error message
            val statusCode = extractStatusCode(error)
            statusCode in retryConfig.retryableStatusCodes
        }
    }

    /**
     * Extracts HTTP status code from error message.
     */
    private fun extractStatusCode(error: Throwable): Int? {
        val message = error.message ?: return null
        // Try to extract "HTTP 429" or "429" from error message
        val regex = Regex("""HTTP\s+(\d{3})|^(\d{3})""")
        val match = regex.find(message)
        return match?.groupValues?.firstOrNull { it.toIntOrNull() != null }?.toIntOrNull()
    }

    /**
     * Sends a non-streaming chat completion request with automatic failover.
     *
     * @param messages The list of messages to send
     * @return The complete assistant message
     * @throws AggregateException if all providers fail
     */
    suspend fun chat(messages: List<ChatMessage>): ChatResponseResult {
        val errors = mutableMapOf<String, Throwable>()

        repeat(retryConfig.maxRetries) { attempt ->
            val provider = selectProvider()

            // Skip if we already tried this provider (unless it's the only one)
            if (provider.name in errors && providers.size > 1) {
                return@repeat
            }

            val startTime = Clock.System.now().toEpochMilliseconds()
            try {
                val client = ChatClient(options = baseOptions.copy(provider = provider), engine = engine, shouldCloseEngine = false)
                val result = client.chat(messages)
                val responseTime = Clock.System.now().toEpochMilliseconds() - startTime

                // Record success metrics
                recordMetrics(provider.name, success = true, responseTime)
                return result
            } catch (e: Exception) {
                val responseTime = Clock.System.now().toEpochMilliseconds() - startTime
                errors[provider.name] = e

                // Record failure metrics
                recordMetrics(provider.name, success = false, responseTime)

                // Don't retry if error is not retryable
                if (!isRetryable(e)) {
                    throw e
                }

                // Apply delay before next retry
                if (attempt < retryConfig.maxRetries - 1) {
                    delay(retryConfig.calculateDelay(attempt))
                }
            }
        }

        // All providers failed, throw aggregate exception
        throw AggregateException(errors)
    }

    /**
     * Sends a streaming chat completion request with automatic failover.
     *
     * If a stream fails after it has started emitting events, it will not retry
     * to avoid duplicate content. Only failures before the stream starts are retried.
     *
     * @param messages The list of messages to send
     * @return A Flow emitting StreamEvent objects
     */
    suspend fun streamChat(messages: List<ChatMessage>): Flow<StreamEvent> = flow {
        val errors = mutableMapOf<String, Throwable>()
        var completed = false

        repeat(retryConfig.maxRetries) { attempt ->
            if (completed) return@repeat

            val provider = selectProvider()

            // Skip if we already tried this provider (unless it's the only one)
            if (provider.name in errors && providers.size > 1) {
                return@repeat
            }

            val startTime = Clock.System.now().toEpochMilliseconds()
            var streamStarted = false
            var streamError: Throwable? = null
            var shouldStopRetrying = false

            try {
                val client = ChatClient(options = baseOptions.copy(provider = provider), engine = engine, shouldCloseEngine = false)
                client.streamChat(messages).collect { event ->
                    when (event) {
                        is StreamEvent.Error -> {
                            streamError = event.error
                            errors[provider.name] = event.error

                            if (!isRetryable(event.error)) {
                                // Non-retryable error, emit and stop
                                emit(event)
                                shouldStopRetrying = true
                                completed = true
                            }
                            // Retryable error, don't emit, will continue to retry
                        }

                        is StreamEvent.Done -> {
                            val responseTime = Clock.System.now().toEpochMilliseconds() - startTime
                            recordMetrics(provider.name, success = true, responseTime)
                            emit(event)
                            completed = true
                        }

                        else -> {
                            // Mark stream as started once we emit any event
                            streamStarted = true
                            emit(event)
                        }
                    }
                }

                // Stream ended normally without Done event
                if (!completed && streamError == null) {
                    val responseTime = Clock.System.now().toEpochMilliseconds() - startTime
                    recordMetrics(provider.name, success = true, responseTime)
                    completed = true
                }
            } catch (e: Exception) {
                val responseTime = Clock.System.now().toEpochMilliseconds() - startTime
                errors[provider.name] = e
                recordMetrics(provider.name, success = false, responseTime)

                if (!isRetryable(e)) {
                    emit(StreamEvent.Error(e))
                    completed = true
                }
            }

            // If completed or should stop retrying, exit
            if (completed || shouldStopRetrying) {
                return@flow
            }

            // If stream already started, don't retry (avoid duplicate content)
            if (streamStarted) {
                emit(StreamEvent.Error(streamError ?: Exception("Stream failed after starting")))
                return@flow
            }

            // Apply delay before next retry
            if (attempt < retryConfig.maxRetries - 1) {
                delay(retryConfig.calculateDelay(attempt))
            }
        }

        // All providers failed, emit aggregate exception
        if (!completed) {
            emit(StreamEvent.Error(AggregateException(errors)))
        }
    }

    /**
     * Closes all resources.
     */
    fun close() {
        if (shouldCloseEngine) {
            engine.close()
        }
    }
}
