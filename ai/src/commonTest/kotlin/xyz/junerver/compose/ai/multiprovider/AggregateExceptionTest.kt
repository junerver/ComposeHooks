package xyz.junerver.compose.ai.multiprovider

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/*
  Description: Unit tests for AggregateException
  Author: Junerver
  Date: 2026/01/13
  Email: junerver@gmail.com
  Version: v1.0
*/

class AggregateExceptionTest {

    @Test
    fun testEmptyErrors() {
        val exception = AggregateException(emptyMap())

        assertTrue(exception.errors.isEmpty())
        assertNull(exception.getLastError())
        assertTrue(exception.message?.contains("All providers failed") == true)
    }

    @Test
    fun testSingleError() {
        val error = RuntimeException("Test error")
        val exception = AggregateException(mapOf("Provider1" to error))

        assertEquals(1, exception.errors.size)
        assertEquals(error, exception.getLastError())
        assertEquals(error, exception.getError("Provider1"))
        assertNull(exception.getError("Provider2"))
    }

    @Test
    fun testMultipleErrors() {
        val error1 = RuntimeException("Error 1")
        val error2 = IllegalStateException("Error 2")
        val error3 = IllegalArgumentException("Error 3")

        val exception = AggregateException(
            mapOf(
                "Provider1" to error1,
                "Provider2" to error2,
                "Provider3" to error3,
            ),
        )

        assertEquals(3, exception.errors.size)
        assertEquals(error1, exception.getError("Provider1"))
        assertEquals(error2, exception.getError("Provider2"))
        assertEquals(error3, exception.getError("Provider3"))
    }

    @Test
    fun testGetLastError() {
        val error1 = RuntimeException("Error 1")
        val error2 = IllegalStateException("Error 2")

        // LinkedHashMap preserves insertion order
        val exception = AggregateException(
            linkedMapOf(
                "Provider1" to error1,
                "Provider2" to error2,
            ),
        )

        // Last error should be error2
        assertEquals(error2, exception.getLastError())
    }

    @Test
    fun testAllErrorsAre() {
        val error1 = RuntimeException("Error 1")
        val error2 = RuntimeException("Error 2")

        val exception = AggregateException(
            mapOf(
                "Provider1" to error1,
                "Provider2" to error2,
            ),
        )

        assertTrue(exception.allErrorsAre<RuntimeException>())
        assertFalse(exception.allErrorsAre<IllegalStateException>())
    }

    @Test
    fun testAllErrorsAreWithMixedTypes() {
        val error1 = IllegalArgumentException("Error 1")
        val error2 = IllegalStateException("Error 2")

        val exception = AggregateException(
            mapOf(
                "Provider1" to error1,
                "Provider2" to error2,
            ),
        )

        // Both are RuntimeException subclasses
        assertTrue(exception.allErrorsAre<RuntimeException>())
        // Not all are IllegalArgumentException
        assertFalse(exception.allErrorsAre<IllegalArgumentException>())
        // Not all are IllegalStateException
        assertFalse(exception.allErrorsAre<IllegalStateException>())
        // Both are Exceptions
        assertTrue(exception.allErrorsAre<Exception>())
    }

    @Test
    fun testMessageFormat() {
        val error1 = RuntimeException("Connection timeout")
        val error2 = IllegalStateException("Rate limit exceeded")

        val exception = AggregateException(
            linkedMapOf(
                "DeepSeek" to error1,
                "OpenAI" to error2,
            ),
        )

        val message = exception.message ?: ""
        // Check that message contains provider count
        assertTrue(message.contains("2 provider"))
        // Check that message contains error information
        assertTrue(message.contains("DeepSeek") && message.contains("Connection timeout"))
        assertTrue(message.contains("OpenAI") && message.contains("Rate limit exceeded"))
    }

    @Test
    fun testCustomMessage() {
        val error = RuntimeException("Test error")
        val customMessage = "Custom failure message"

        val exception = AggregateException(
            mapOf("Provider1" to error),
            customMessage,
        )

        assertEquals(customMessage, exception.message)
    }
}
