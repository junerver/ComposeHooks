package xyz.junerver.compose.ai.usechat

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import xyz.junerver.compose.ai.http.HttpEngine
import xyz.junerver.compose.ai.http.HttpEngineConfig
import xyz.junerver.compose.ai.http.HttpRequest
import xyz.junerver.compose.ai.http.SseEvent

/*
  Description: HTTP client for multi-provider chat completions
  Author: Junerver
  Date: 2026/01/05-11:06
  Email: junerver@gmail.com
  Version: v3.0
*/

/**
 * Represents a streaming event from the chat API.
 */
sealed class StreamEvent {
    data class Delta(
        val content: String,
        val role: String? = null,
        val finishReason: String? = null,
        val usage: ChatUsage? = null,
    ) : StreamEvent()

    data object Done : StreamEvent()

    data class Error(val error: Throwable) : StreamEvent()
}

/**
 * HTTP client for interacting with chat APIs.
 *
 * Supports multiple providers through the [ChatProvider] abstraction.
 * Uses [HttpEngine] for network requests, allowing custom implementations.
 */
internal class ChatClient(private val options: ChatOptions) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        explicitNulls = false
    }

    private val engine: HttpEngine = options.httpEngine
        ?: HttpEngineConfig.defaultEngineFactory()

    /**
     * Sends a chat completion request and returns a flow of streaming events.
     *
     * @param messages The list of messages to send
     * @return A Flow emitting StreamEvent objects
     */
    suspend fun streamChat(messages: List<ChatMessage>): Flow<StreamEvent> = flow {
        val requestBody = options.buildRequestBody(messages, stream = true)
        val headers = options.buildAuthHeaders() + options.headers

        val request = HttpRequest(
            url = options.buildEndpoint(),
            headers = headers,
            body = requestBody,
            timeout = options.timeout.inWholeMilliseconds,
        )

        engine.executeStream(request).collect { event ->
            when (event) {
                is SseEvent.Data -> {
                    val streamEvent = options.provider.parseStreamLine(event.line)
                    if (streamEvent != null) {
                        emit(streamEvent)
                        if (streamEvent is StreamEvent.Done) return@collect
                    }
                }
                is SseEvent.Complete -> emit(StreamEvent.Done)
                is SseEvent.Error -> {
                    val errorBody = event.error.message ?: "Unknown error"
                    try {
                        val errorResponse = json.decodeFromString<OpenAIErrorResponse>(errorBody)
                        emit(
                            StreamEvent.Error(
                                OpenAIException(
                                    errorMessage = errorResponse.error.message,
                                    errorType = errorResponse.error.type,
                                    errorCode = errorResponse.error.code,
                                ),
                            ),
                        )
                    } catch (e: Exception) {
                        emit(StreamEvent.Error(event.error))
                    }
                }
            }
        }
    }

    /**
     * Sends a non-streaming chat completion request.
     *
     * @param messages The list of messages to send
     * @return The complete assistant message
     */
    suspend fun chat(messages: List<ChatMessage>): AssistantMessage {
        val requestBody = options.buildRequestBody(messages, stream = false)
        val headers = options.buildAuthHeaders() + options.headers

        val request = HttpRequest(
            url = options.buildEndpoint(),
            headers = headers,
            body = requestBody,
            timeout = options.timeout.inWholeMilliseconds,
        )

        val result = engine.execute(request)

        if (result.statusCode !in 200..299) {
            try {
                val errorResponse = json.decodeFromString<OpenAIErrorResponse>(result.body)
                throw OpenAIException(
                    errorMessage = errorResponse.error.message,
                    errorType = errorResponse.error.type,
                    errorCode = errorResponse.error.code,
                )
            } catch (e: OpenAIException) {
                throw e
            } catch (e: Exception) {
                throw Exception("HTTP ${result.statusCode}: ${result.body}")
            }
        }

        val responseBody = result.body
        if (responseBody.isEmpty()) throw Exception("Empty response")

        // Use provider-specific response parsing
        val parseResult = options.provider.parseResponse(responseBody)
        return parseResult.message
    }

    /**
     * Closes the HTTP engine.
     */
    fun close() {
        engine.close()
    }
}
