package xyz.junerver.compose.ai.multiprovider

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/*
  Description: Unit tests for RetryConfig
  Author: Junerver
  Date: 2026/01/13
  Email: junerver@gmail.com
  Version: v1.0
*/

class RetryConfigTest {

    @Test
    fun testDefaultConfig() {
        val config = RetryConfig.default()

        assertEquals(3, config.maxRetries)
        assertEquals(setOf(429, 500, 502, 503, 504), config.retryableStatusCodes)
        assertEquals(1000L, config.retryDelay)
        assertTrue(config.exponentialBackoff)
    }

    @Test
    fun testConservativeConfig() {
        val config = RetryConfig.conservative()

        assertEquals(2, config.maxRetries)
        assertEquals(2000L, config.retryDelay)
        assertTrue(config.exponentialBackoff)
    }

    @Test
    fun testAggressiveConfig() {
        val config = RetryConfig.aggressive()

        assertEquals(5, config.maxRetries)
        assertEquals(500L, config.retryDelay)
        assertTrue(config.exponentialBackoff)
    }

    @Test
    fun testNoRetryConfig() {
        val config = RetryConfig.noRetry()

        assertEquals(1, config.maxRetries)
        assertEquals(0L, config.retryDelay)
        assertEquals(false, config.exponentialBackoff)
    }

    @Test
    fun testCalculateDelayWithExponentialBackoff() {
        val config = RetryConfig(
            maxRetries = 5,
            retryDelay = 1000L,
            exponentialBackoff = true,
        )

        // 2^0 * 1000 = 1000
        assertEquals(1000L, config.calculateDelay(0))
        // 2^1 * 1000 = 2000
        assertEquals(2000L, config.calculateDelay(1))
        // 2^2 * 1000 = 4000
        assertEquals(4000L, config.calculateDelay(2))
        // 2^3 * 1000 = 8000
        assertEquals(8000L, config.calculateDelay(3))
        // 2^4 * 1000 = 16000
        assertEquals(16000L, config.calculateDelay(4))
    }

    @Test
    fun testCalculateDelayWithoutExponentialBackoff() {
        val config = RetryConfig(
            maxRetries = 5,
            retryDelay = 1000L,
            exponentialBackoff = false,
        )

        // All delays should be the same
        assertEquals(1000L, config.calculateDelay(0))
        assertEquals(1000L, config.calculateDelay(1))
        assertEquals(1000L, config.calculateDelay(2))
        assertEquals(1000L, config.calculateDelay(3))
        assertEquals(1000L, config.calculateDelay(4))
    }

    @Test
    fun testNegativeMaxRetriesThrowsException() {
        assertFailsWith<IllegalArgumentException> {
            RetryConfig(maxRetries = -1)
        }
    }

    @Test
    fun testNegativeRetryDelayThrowsException() {
        assertFailsWith<IllegalArgumentException> {
            RetryConfig(retryDelay = -1L)
        }
    }

    @Test
    fun testCustomRetryableStatusCodes() {
        val config = RetryConfig(
            retryableStatusCodes = setOf(408, 429, 503),
        )

        assertTrue(config.retryableStatusCodes.contains(408))
        assertTrue(config.retryableStatusCodes.contains(429))
        assertTrue(config.retryableStatusCodes.contains(503))
        assertEquals(false, config.retryableStatusCodes.contains(500))
    }

    @Test
    fun testZeroMaxRetriesIsValid() {
        val config = RetryConfig(maxRetries = 0)
        assertEquals(0, config.maxRetries)
    }
}
