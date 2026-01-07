package xyz.junerver.compose.ai.http

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for HTTP data classes.
 *
 * TDD approach: Test data class behavior and defaults.
 */
class HttpModelsTest {

    // region HttpRequest Tests

    @Test
    fun testHttpRequestDefaults() {
        val request = HttpRequest(url = "https://api.example.com/chat")
        assertEquals("https://api.example.com/chat", request.url)
        assertTrue(request.headers.isEmpty())
        assertEquals(null, request.body)
        assertEquals(60_000L, request.timeout)
    }

    @Test
    fun testHttpRequestWithAllParams() {
        val request = HttpRequest(
            url = "https://api.example.com/chat",
            headers = mapOf("Authorization" to "Bearer token"),
            body = """{"message": "hello"}""",
            timeout = 120_000L,
        )
        assertEquals("https://api.example.com/chat", request.url)
        assertEquals("Bearer token", request.headers["Authorization"])
        assertEquals("""{"message": "hello"}""", request.body)
        assertEquals(120_000L, request.timeout)
    }

    @Test
    fun testHttpRequestCopy() {
        val original = HttpRequest(url = "https://api.example.com/chat")
        val modified = original.copy(timeout = 30_000L)
        assertEquals(60_000L, original.timeout)
        assertEquals(30_000L, modified.timeout)
        assertEquals(original.url, modified.url)
    }

    // endregion

    // region HttpResult Tests

    @Test
    fun testHttpResultSuccess() {
        val result = HttpResult(
            statusCode = 200,
            body = """{"response": "ok"}""",
        )
        assertEquals(200, result.statusCode)
        assertEquals("""{"response": "ok"}""", result.body)
    }

    @Test
    fun testHttpResultError() {
        val result = HttpResult(
            statusCode = 401,
            body = """{"error": "Unauthorized"}""",
        )
        assertEquals(401, result.statusCode)
        assertTrue(result.body.contains("Unauthorized"))
    }

    @Test
    fun testHttpResultEmptyBody() {
        val result = HttpResult(statusCode = 204, body = "")
        assertEquals(204, result.statusCode)
        assertEquals("", result.body)
    }

    // endregion

    // region SseEvent Tests

    @Test
    fun testSseEventData() {
        val event = SseEvent.Data("data: {\"content\": \"hello\"}")
        assertTrue(event is SseEvent.Data)
        assertEquals("data: {\"content\": \"hello\"}", event.line)
    }

    @Test
    fun testSseEventComplete() {
        val event = SseEvent.Complete
        assertTrue(event is SseEvent.Complete)
    }

    @Test
    fun testSseEventError() {
        val exception = Exception("Connection failed")
        val event = SseEvent.Error(exception)
        assertTrue(event is SseEvent.Error)
        assertEquals("Connection failed", event.error.message)
    }

    @Test
    fun testSseEventDataEquality() {
        val event1 = SseEvent.Data("line1")
        val event2 = SseEvent.Data("line1")
        val event3 = SseEvent.Data("line2")
        assertEquals(event1, event2)
        assertTrue(event1 != event3)
    }

    @Test
    fun testSseEventCompleteIsSingleton() {
        val event1 = SseEvent.Complete
        val event2 = SseEvent.Complete
        assertTrue(event1 === event2)
    }

    // endregion

    // region HttpRequest Headers Tests

    @Test
    fun testHttpRequestMultipleHeaders() {
        val request = HttpRequest(
            url = "https://api.example.com/chat",
            headers = mapOf(
                "Authorization" to "Bearer token",
                "Content-Type" to "application/json",
                "X-Custom-Header" to "custom-value",
            ),
        )
        assertEquals(3, request.headers.size)
        assertEquals("Bearer token", request.headers["Authorization"])
        assertEquals("application/json", request.headers["Content-Type"])
        assertEquals("custom-value", request.headers["X-Custom-Header"])
    }

    @Test
    fun testHttpRequestHeadersCaseSensitive() {
        val request = HttpRequest(
            url = "https://api.example.com/chat",
            headers = mapOf(
                "Authorization" to "value1",
                "authorization" to "value2",
            ),
        )
        // Map keys are case-sensitive
        assertEquals(2, request.headers.size)
        assertEquals("value1", request.headers["Authorization"])
        assertEquals("value2", request.headers["authorization"])
    }

    // endregion

    // region HttpResult Status Code Tests

    @Test
    fun testHttpResultStatusCodes() {
        val successCodes = listOf(200, 201, 204)
        val clientErrorCodes = listOf(400, 401, 403, 404)
        val serverErrorCodes = listOf(500, 502, 503)

        successCodes.forEach { code ->
            val result = HttpResult(statusCode = code, body = "")
            assertTrue(result.statusCode in 200..299, "Expected $code to be success")
        }

        clientErrorCodes.forEach { code ->
            val result = HttpResult(statusCode = code, body = "")
            assertTrue(result.statusCode in 400..499, "Expected $code to be client error")
        }

        serverErrorCodes.forEach { code ->
            val result = HttpResult(statusCode = code, body = "")
            assertTrue(result.statusCode in 500..599, "Expected $code to be server error")
        }
    }

    // endregion
}
