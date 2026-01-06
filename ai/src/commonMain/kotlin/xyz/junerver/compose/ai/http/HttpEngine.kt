package xyz.junerver.compose.ai.http

import kotlinx.coroutines.flow.Flow

/*
  Description: HTTP engine abstraction for AI module
  Author: Junerver
  Date: 2026/01/06
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * HTTP request parameters.
 *
 * @property url The full URL to request
 * @property headers HTTP headers to send
 * @property body Request body (JSON string)
 * @property timeout Request timeout in milliseconds
 */
data class HttpRequest(
    val url: String,
    val headers: Map<String, String> = emptyMap(),
    val body: String? = null,
    val timeout: Long = 60_000L,
)

/**
 * HTTP response result.
 *
 * @property statusCode HTTP status code
 * @property body Response body as string
 */
data class HttpResult(
    val statusCode: Int,
    val body: String,
)

/**
 * Server-Sent Events (SSE) event types.
 */
sealed class SseEvent {
    /**
     * A data line received from SSE stream.
     */
    data class Data(val line: String) : SseEvent()

    /**
     * Stream completed successfully.
     */
    data object Complete : SseEvent()

    /**
     * An error occurred during streaming.
     */
    data class Error(val error: Throwable) : SseEvent()
}

/**
 * HTTP engine abstraction interface.
 *
 * Allows users to provide custom HTTP implementations (e.g., OkHttp)
 * instead of the default Ktor implementation.
 *
 * Example custom implementation:
 * ```kotlin
 * class OkHttpEngine(private val client: OkHttpClient) : HttpEngine {
 *     override suspend fun execute(request: HttpRequest): HttpResult {
 *         // Use OkHttp to execute request
 *     }
 *     override suspend fun executeStream(request: HttpRequest): Flow<SseEvent> = flow {
 *         // Use OkHttp SSE implementation
 *     }
 *     override fun close() {
 *         client.dispatcher.executorService.shutdown()
 *     }
 * }
 * ```
 */
interface HttpEngine {
    /**
     * Executes a standard HTTP request.
     *
     * @param request The HTTP request to execute
     * @return The HTTP response result
     */
    suspend fun execute(request: HttpRequest): HttpResult

    /**
     * Executes an SSE streaming request.
     *
     * The returned Flow emits [SseEvent.Data] for each line received,
     * [SseEvent.Complete] when the stream ends, and [SseEvent.Error] on errors.
     *
     * @param request The HTTP request to execute (with SSE headers)
     * @return A Flow of SSE events
     */
    suspend fun executeStream(request: HttpRequest): Flow<SseEvent>

    /**
     * Closes the HTTP engine and releases resources.
     */
    fun close()
}
