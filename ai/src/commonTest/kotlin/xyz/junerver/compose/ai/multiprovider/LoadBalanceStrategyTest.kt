package xyz.junerver.compose.ai.multiprovider

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/*
  Description: Unit tests for LoadBalanceStrategy
  Author: Junerver
  Date: 2026/01/13
  Email: junerver@gmail.com
  Version: v1.0
*/

class LoadBalanceStrategyTest {

    @Test
    fun testRoundRobinStrategy() {
        val strategy = LoadBalanceStrategy.RoundRobin
        assertTrue(strategy is LoadBalanceStrategy.RoundRobin)
    }

    @Test
    fun testRandomStrategy() {
        val strategy = LoadBalanceStrategy.Random
        assertTrue(strategy is LoadBalanceStrategy.Random)
    }

    @Test
    fun testWeightedStrategy() {
        val weights = mapOf(
            "Provider1" to 3,
            "Provider2" to 2,
            "Provider3" to 1,
        )
        val strategy = LoadBalanceStrategy.Weighted(weights)

        assertTrue(strategy is LoadBalanceStrategy.Weighted)
        assertEquals(weights, strategy.weights)
    }

    @Test
    fun testSmartStrategy() {
        val strategy = LoadBalanceStrategy.Smart
        assertTrue(strategy is LoadBalanceStrategy.Smart)
    }

    @Test
    fun testWeightedStrategyWithEmptyWeights() {
        val strategy = LoadBalanceStrategy.Weighted(emptyMap())
        assertTrue(strategy.weights.isEmpty())
    }

    @Test
    fun testWeightedStrategyWithSingleProvider() {
        val weights = mapOf("Provider1" to 10)
        val strategy = LoadBalanceStrategy.Weighted(weights)

        assertEquals(1, strategy.weights.size)
        assertEquals(10, strategy.weights["Provider1"])
    }

    @Test
    fun testWeightedStrategyEquality() {
        val weights1 = mapOf("Provider1" to 3, "Provider2" to 2)
        val weights2 = mapOf("Provider1" to 3, "Provider2" to 2)

        val strategy1 = LoadBalanceStrategy.Weighted(weights1)
        val strategy2 = LoadBalanceStrategy.Weighted(weights2)

        assertEquals(strategy1, strategy2)
    }
}
