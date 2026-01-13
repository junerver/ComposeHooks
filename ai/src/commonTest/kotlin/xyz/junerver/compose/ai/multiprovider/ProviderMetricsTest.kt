package xyz.junerver.compose.ai.multiprovider

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/*
  Description: Unit tests for ProviderMetrics
  Author: Junerver
  Date: 2026/01/13
  Email: junerver@gmail.com
  Version: v1.0
*/

class ProviderMetricsTest {
    @Test
    fun testInitialState() {
        val metrics = ProviderMetrics("TestProvider")

        assertEquals("TestProvider", metrics.providerName)
        assertEquals(0L, metrics.totalRequests)
        assertEquals(0L, metrics.successfulRequests)
        assertEquals(0L, metrics.failedRequests)
        assertEquals(0L, metrics.totalResponseTime)
        assertEquals(0.0, metrics.successRate)
        assertEquals(Long.MAX_VALUE, metrics.averageResponseTime)
    }

    @Test
    fun testRecordSuccess() {
        val metrics = ProviderMetrics("TestProvider")

        metrics.recordSuccess(100L)

        assertEquals(1L, metrics.totalRequests)
        assertEquals(1L, metrics.successfulRequests)
        assertEquals(0L, metrics.failedRequests)
        assertEquals(100L, metrics.totalResponseTime)
        assertEquals(1.0, metrics.successRate)
        assertEquals(100L, metrics.averageResponseTime)
    }

    @Test
    fun testRecordFailure() {
        val metrics = ProviderMetrics("TestProvider")

        metrics.recordFailure()

        assertEquals(1L, metrics.totalRequests)
        assertEquals(0L, metrics.successfulRequests)
        assertEquals(1L, metrics.failedRequests)
        assertEquals(0L, metrics.totalResponseTime)
        assertEquals(0.0, metrics.successRate)
        assertEquals(Long.MAX_VALUE, metrics.averageResponseTime)
    }

    @Test
    fun testMultipleRecords() {
        val metrics = ProviderMetrics("TestProvider")

        metrics.recordSuccess(100L)
        metrics.recordSuccess(200L)
        metrics.recordFailure()
        metrics.recordSuccess(300L)

        assertEquals(4L, metrics.totalRequests)
        assertEquals(3L, metrics.successfulRequests)
        assertEquals(1L, metrics.failedRequests)
        assertEquals(600L, metrics.totalResponseTime)
        assertEquals(0.75, metrics.successRate) // 3/4
        assertEquals(200L, metrics.averageResponseTime) // 600/3
    }

    @Test
    fun testScore() {
        val metrics = ProviderMetrics("TestProvider")

        // Record some successful requests
        metrics.recordSuccess(100L)
        metrics.recordSuccess(200L)

        // Success rate: 1.0 (2/2)
        // Average response time: 150ms (300/2)
        // If max response time is 300ms:
        // Normalized time: 1.0 - (150/300) = 0.5
        // Score: 1.0 * 0.7 + 0.5 * 0.3 = 0.7 + 0.15 = 0.85
        val score = metrics.score(maxResponseTime = 300L)
        assertEquals(0.85, score, 0.001)
    }

    @Test
    fun testScoreWithFailures() {
        val metrics = ProviderMetrics("TestProvider")

        metrics.recordSuccess(100L)
        metrics.recordFailure()

        // Success rate: 0.5 (1/2)
        // Average response time: 100ms (100/1)
        // If max response time is 200ms:
        // Normalized time: 1.0 - (100/200) = 0.5
        // Score: 0.5 * 0.7 + 0.5 * 0.3 = 0.35 + 0.15 = 0.5
        val score = metrics.score(maxResponseTime = 200L)
        assertEquals(0.5, score, 0.001)
    }

    @Test
    fun testScoreWithNoSuccessfulRequests() {
        val metrics = ProviderMetrics("TestProvider")

        metrics.recordFailure()
        metrics.recordFailure()

        // Success rate: 0.0 (0/2)
        // Average response time: Long.MAX_VALUE (no successful requests)
        // Score should be very low
        val score = metrics.score(maxResponseTime = 1000L)
        assertTrue(score < 0.1)
    }

    @Test
    fun testScoreWithZeroMaxResponseTime() {
        val metrics = ProviderMetrics("TestProvider")

        metrics.recordSuccess(100L)

        // Success rate: 1.0
        // Max response time: 0 (edge case)
        // Normalized time: 0.0
        // Score: 1.0 * 0.7 + 0.0 * 0.3 = 0.7
        val score = metrics.score(maxResponseTime = 0L)
        assertEquals(0.7, score, 0.001)
    }
}
