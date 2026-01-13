package xyz.junerver.compose.ai.multiprovider

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import xyz.junerver.compose.ai.test.FakeChatProvider
import xyz.junerver.compose.ai.usechat.ChatOptions

/*
  Description: Integration tests for multi-provider functionality
  Author: Junerver
  Date: 2026/01/13
  Email: junerver@gmail.com
  Version: v1.0
*/

class ModelsProviderIntegrationTest {
    @Test
    fun testModelsContextValueCreation() {
        val providers = listOf(
            FakeChatProvider(name = "Provider1"),
            FakeChatProvider(name = "Provider2"),
        )

        val contextValue = ModelsContextValue(
            providers = providers,
            strategy = LoadBalanceStrategy.RoundRobin,
            retryConfig = RetryConfig.default(),
        )

        assertEquals(2, contextValue.providers.size)
        assertTrue(contextValue.strategy is LoadBalanceStrategy.RoundRobin)
        assertEquals(3, contextValue.retryConfig.maxRetries)
    }

    @Test
    fun testModelsContextValueWithDifferentStrategies() {
        val providers = listOf(
            FakeChatProvider(name = "Provider1"),
            FakeChatProvider(name = "Provider2"),
            FakeChatProvider(name = "Provider3"),
        )

        // Test with RoundRobin
        val roundRobinContext = ModelsContextValue(
            providers = providers,
            strategy = LoadBalanceStrategy.RoundRobin,
            retryConfig = RetryConfig.default(),
        )
        assertTrue(roundRobinContext.strategy is LoadBalanceStrategy.RoundRobin)

        // Test with Random
        val randomContext = ModelsContextValue(
            providers = providers,
            strategy = LoadBalanceStrategy.Random,
            retryConfig = RetryConfig.default(),
        )
        assertTrue(randomContext.strategy is LoadBalanceStrategy.Random)

        // Test with Weighted
        val weightedContext = ModelsContextValue(
            providers = providers,
            strategy = LoadBalanceStrategy.Weighted(
                mapOf(
                    "Provider1" to 3,
                    "Provider2" to 2,
                    "Provider3" to 1,
                ),
            ),
            retryConfig = RetryConfig.default(),
        )
        assertTrue(weightedContext.strategy is LoadBalanceStrategy.Weighted)

        // Test with Smart
        val smartContext = ModelsContextValue(
            providers = providers,
            strategy = LoadBalanceStrategy.Smart,
            retryConfig = RetryConfig.default(),
        )
        assertTrue(smartContext.strategy is LoadBalanceStrategy.Smart)
    }

    @Test
    fun testModelsContextValueWithDifferentRetryConfigs() {
        val providers = listOf(
            FakeChatProvider(name = "Provider1"),
        )

        // Test with default config
        val defaultContext = ModelsContextValue(
            providers = providers,
            strategy = LoadBalanceStrategy.RoundRobin,
            retryConfig = RetryConfig.default(),
        )
        assertEquals(3, defaultContext.retryConfig.maxRetries)
        assertEquals(1000L, defaultContext.retryConfig.retryDelay)

        // Test with conservative config
        val conservativeContext = ModelsContextValue(
            providers = providers,
            strategy = LoadBalanceStrategy.RoundRobin,
            retryConfig = RetryConfig.conservative(),
        )
        assertEquals(2, conservativeContext.retryConfig.maxRetries)
        assertEquals(2000L, conservativeContext.retryConfig.retryDelay)

        // Test with aggressive config
        val aggressiveContext = ModelsContextValue(
            providers = providers,
            strategy = LoadBalanceStrategy.RoundRobin,
            retryConfig = RetryConfig.aggressive(),
        )
        assertEquals(5, aggressiveContext.retryConfig.maxRetries)
        assertEquals(500L, aggressiveContext.retryConfig.retryDelay)

        // Test with no retry config
        val noRetryContext = ModelsContextValue(
            providers = providers,
            strategy = LoadBalanceStrategy.RoundRobin,
            retryConfig = RetryConfig.noRetry(),
        )
        assertEquals(1, noRetryContext.retryConfig.maxRetries)
        assertEquals(0L, noRetryContext.retryConfig.retryDelay)
    }

    @Test
    fun testMultiProviderClientIntegrationWithContext() {
        val providers = listOf(
            FakeChatProvider(name = "DeepSeek"),
            FakeChatProvider(name = "OpenAI"),
            FakeChatProvider(name = "Anthropic"),
        )

        val contextValue = ModelsContextValue(
            providers = providers,
            strategy = LoadBalanceStrategy.Smart,
            retryConfig = RetryConfig.default(),
        )

        // Create client using context values
        val client = MultiProviderChatClient(
            providers = contextValue.providers,
            strategy = contextValue.strategy,
            retryConfig = contextValue.retryConfig,
            baseOptions = ChatOptions.optionOf {
                stream = false
            },
        )

        assertTrue(client != null)
    }

    @Test
    fun testProviderMetricsIntegration() {
        val metrics1 = ProviderMetrics("Provider1")
        val metrics2 = ProviderMetrics("Provider2")
        val metrics3 = ProviderMetrics("Provider3")

        // Simulate different performance characteristics
        // Provider1: Fast and reliable
        metrics1.recordSuccess(100L)
        metrics1.recordSuccess(120L)
        metrics1.recordSuccess(110L)

        // Provider2: Slower but reliable
        metrics2.recordSuccess(200L)
        metrics2.recordSuccess(220L)
        metrics2.recordSuccess(210L)

        // Provider3: Fast but unreliable
        metrics3.recordSuccess(90L)
        metrics3.recordFailure()
        metrics3.recordFailure()

        // Calculate scores
        val maxResponseTime = maxOf(
            metrics1.averageResponseTime,
            metrics2.averageResponseTime,
            metrics3.averageResponseTime,
        )

        val score1 = metrics1.score(maxResponseTime)
        val score2 = metrics2.score(maxResponseTime)
        val score3 = metrics3.score(maxResponseTime)

        // Provider1 should have the best score (fast and reliable)
        assertTrue(score1 > score2)
        assertTrue(score1 > score3)

        // Provider2 should have better score than Provider3 (more reliable)
        assertTrue(score2 > score3)
    }

    @Test
    fun testRetryConfigCalculateDelayIntegration() {
        val config = RetryConfig(
            maxRetries = 5,
            retryDelay = 100L,
            exponentialBackoff = true,
        )

        // Test exponential backoff sequence
        val delays = (0 until 5).map { config.calculateDelay(it) }

        assertEquals(listOf(100L, 200L, 400L, 800L, 1600L), delays)

        // Verify each delay is double the previous
        for (i in 1 until delays.size) {
            assertEquals(delays[i - 1] * 2, delays[i])
        }
    }

    @Test
    fun testAggregateExceptionIntegration() {
        val errors = linkedMapOf(
            "Provider1" to RuntimeException("Connection timeout"),
            "Provider2" to IllegalStateException("Rate limit exceeded"),
            "Provider3" to IllegalArgumentException("Invalid API key"),
        )

        val exception = AggregateException(errors)

        // Test error retrieval
        assertEquals(3, exception.failedProviderCount)
        assertEquals(listOf("Provider1", "Provider2", "Provider3"), exception.failedProviders)

        // Test getError
        assertTrue(exception.getError("Provider1") is RuntimeException)
        assertTrue(exception.getError("Provider2") is IllegalStateException)
        assertTrue(exception.getError("Provider3") is IllegalArgumentException)

        // Test getLastError
        assertTrue(exception.getLastError() is IllegalArgumentException)

        // Test getErrorsOfType
        val runtimeErrors = exception.getErrorsOfType<RuntimeException>()
        assertEquals(3, runtimeErrors.size) // All are RuntimeException subclasses

        val illegalStateErrors = exception.getErrorsOfType<IllegalStateException>()
        assertEquals(1, illegalStateErrors.size)
    }
}
