package xyz.junerver.compose.ai.multiprovider

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlinx.coroutines.test.runTest
import xyz.junerver.compose.ai.test.FakeChatProvider
import xyz.junerver.compose.ai.usechat.ChatMessage
import xyz.junerver.compose.ai.usechat.ChatOptions

/*
  Description: Unit tests for MultiProviderChatClient
  Author: Junerver
  Date: 2026/01/13
  Email: junerver@gmail.com
  Version: v1.0
*/

private val noMessages: List<ChatMessage> = listOf()

class MultiProviderChatClientTest {

    @Test
    fun testClientCreation() = runTest {
        val provider1 = FakeChatProvider(name = "Provider1")
        val provider2 = FakeChatProvider(name = "Provider2")

        val client = MultiProviderChatClient(
            providers = listOf(provider1, provider2),
            strategy = LoadBalanceStrategy.RoundRobin,
            retryConfig = RetryConfig.default(),
            baseOptions = ChatOptions.optionOf {
                stream = false
            },
        )

        // Verify client was created successfully
        assertTrue(client != null)
    }

    @Test
    fun testRoundRobinStrategyType() {
        val strategy = LoadBalanceStrategy.RoundRobin
        assertTrue(strategy is LoadBalanceStrategy.RoundRobin)
    }

    @Test
    fun testRandomStrategyType() {
        val strategy = LoadBalanceStrategy.Random
        assertTrue(strategy is LoadBalanceStrategy.Random)
    }

    @Test
    fun testWeightedStrategyType() {
        val weights = mapOf("Provider1" to 3, "Provider2" to 2)
        val strategy = LoadBalanceStrategy.Weighted(weights)
        assertTrue(strategy is LoadBalanceStrategy.Weighted)
        assertEquals(weights, strategy.weights)
    }

    @Test
    fun testSmartStrategyType() {
        val strategy = LoadBalanceStrategy.Smart
        assertTrue(strategy is LoadBalanceStrategy.Smart)
    }

    @Test
    fun testRetryConfigDefault() {
        val config = RetryConfig.default()
        assertEquals(3, config.maxRetries)
        assertEquals(1000L, config.retryDelay)
        assertTrue(config.exponentialBackoff)
    }

    @Test
    fun testRetryConfigConservative() {
        val config = RetryConfig.conservative()
        assertEquals(2, config.maxRetries)
        assertEquals(2000L, config.retryDelay)
        assertTrue(config.exponentialBackoff)
    }

    @Test
    fun testRetryConfigAggressive() {
        val config = RetryConfig.aggressive()
        assertEquals(5, config.maxRetries)
        assertEquals(500L, config.retryDelay)
        assertTrue(config.exponentialBackoff)
    }

    @Test
    fun testRetryConfigNoRetry() {
        val config = RetryConfig.noRetry()
        assertEquals(1, config.maxRetries)
        assertEquals(0L, config.retryDelay)
        assertEquals(false, config.exponentialBackoff)
    }

    @Test
    fun testMultipleProvidersCreation() = runTest {
        val providers = listOf(
            FakeChatProvider(name = "Provider1"),
            FakeChatProvider(name = "Provider2"),
            FakeChatProvider(name = "Provider3"),
        )

        val client = MultiProviderChatClient(
            providers = providers,
            strategy = LoadBalanceStrategy.RoundRobin,
            retryConfig = RetryConfig.default(),
            baseOptions = ChatOptions.optionOf {
                stream = false
            },
        )

        assertTrue(client != null)
    }

    @Test
    fun testWeightedStrategyWithCustomWeights() {
        val weights = mapOf(
            "DeepSeek" to 3,
            "OpenAI" to 2,
            "Anthropic" to 1,
        )

        val strategy = LoadBalanceStrategy.Weighted(weights)
        assertEquals(3, strategy.weights.size)
        assertEquals(3, strategy.weights["DeepSeek"])
        assertEquals(2, strategy.weights["OpenAI"])
        assertEquals(1, strategy.weights["Anthropic"])
    }
}
