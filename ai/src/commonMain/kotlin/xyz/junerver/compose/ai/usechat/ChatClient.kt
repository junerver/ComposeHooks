package xyz.junerver.compose.ai.usechat

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.plugins.timeout
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.http.withCharset
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

/*
  Description: Ktor-based HTTP client for multi-provider chat completions
  Author: Junerver
  Date: 2026/01/05-11:06
  Email: junerver@gmail.com
  Version: v2.0
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
 */
internal class ChatClient(private val options: ChatOptions) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        explicitNulls = false
    }

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
        install(HttpTimeout) {
            requestTimeoutMillis = options.timeout.inWholeMilliseconds
            connectTimeoutMillis = options.timeout.inWholeMilliseconds
            socketTimeoutMillis = options.timeout.inWholeMilliseconds
        }
        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.ALL
        }
    }

    /**
     * Sends a chat completion request and returns a flow of streaming events.
     *
     * @param messages The list of messages to send
     * @return A Flow emitting StreamEvent objects
     */
    suspend fun streamChat(messages: List<ChatMessage>): Flow<StreamEvent> = flow {
        try {
            val requestBody = options.buildRequestBody(messages, stream = true)

            httpClient.preparePost(options.buildEndpoint()) {
                // SSE streams need longer/no timeout
                timeout {
                    requestTimeoutMillis = Long.MAX_VALUE
                    socketTimeoutMillis = Long.MAX_VALUE
                }
                contentType(ContentType.Application.Json.withCharset(Charsets.UTF_8))
                // Use provider-specific auth headers
                options.buildAuthHeaders().forEach { (key, value) ->
                    header(key, value)
                }
                header(HttpHeaders.Accept, "text/event-stream")
                header(HttpHeaders.CacheControl, "no-cache")
                header(HttpHeaders.Connection, "keep-alive")
                options.headers.forEach { (key, value) ->
                    header(key, value)
                }
                setBody(requestBody)
            }.execute { response ->
                options.onResponse?.invoke(response)

                if (!response.status.isSuccess()) {
                    val errorBody = response.bodyAsChannel().readUTF8Line() ?: "Unknown error"
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
                        emit(StreamEvent.Error(Exception("HTTP ${response.status.value}: $errorBody")))
                    }
                    return@execute
                }

                val channel = response.bodyAsChannel()
                while (!channel.isClosedForRead) {
                    val line = channel.readUTF8Line() ?: continue

                    // Use provider-specific stream parsing
                    val event = options.provider.parseStreamLine(line)
                    if (event != null) {
                        emit(event)
                        if (event is StreamEvent.Done) break
                    }
                }
            }
        } catch (e: Exception) {
            emit(StreamEvent.Error(e))
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

        val response: HttpResponse = httpClient.post(options.buildEndpoint()) {
            contentType(ContentType.Application.Json.withCharset(Charsets.UTF_8))
            // Use provider-specific auth headers
            options.buildAuthHeaders().forEach { (key, value) ->
                header(key, value)
            }
            options.headers.forEach { (key, value) ->
                header(key, value)
            }
            setBody(requestBody)
        }

        options.onResponse?.invoke(response)

        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsChannel().readUTF8Line() ?: "Unknown error"
            try {
                val errorResponse = json.decodeFromString<OpenAIErrorResponse>(errorBody)
                throw OpenAIException(
                    errorMessage = errorResponse.error.message,
                    errorType = errorResponse.error.type,
                    errorCode = errorResponse.error.code,
                )
            } catch (e: OpenAIException) {
                throw e
            } catch (e: Exception) {
                throw Exception("HTTP ${response.status.value}: $errorBody")
            }
        }

        val responseBody = response.bodyAsChannel().readUTF8Line() ?: throw Exception("Empty response")
        // Use provider-specific response parsing
        val result = options.provider.parseResponse(responseBody)
        return result.message
    }

    /**
     * Closes the HTTP client.
     */
    fun close() {
        httpClient.close()
    }
}
