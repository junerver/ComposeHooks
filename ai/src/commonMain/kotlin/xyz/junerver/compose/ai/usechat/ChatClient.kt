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
  Description: Ktor-based HTTP client for OpenAI chat completions
  Author: Junerver
  Date: 2024
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Represents a streaming event from the chat API.
 */
internal sealed class StreamEvent {
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
 * HTTP client for interacting with OpenAI-compatible chat APIs.
 */
internal class ChatClient(private val options: ChatOptions) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
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
    suspend fun streamChat(messages: List<Message>): Flow<StreamEvent> = flow {
        try {
            val requestBody = ChatCompletionRequest(
                model = options.model,
                messages = messages.toRequestMessages(),
                stream = true,
                temperature = options.temperature,
                maxTokens = options.maxTokens,
            )

            httpClient.preparePost(options.buildEndpoint()) {
                // SSE streams need longer/no timeout
                timeout {
                    requestTimeoutMillis = Long.MAX_VALUE
                    socketTimeoutMillis = Long.MAX_VALUE
                }
                contentType(ContentType.Application.Json.withCharset(Charsets.UTF_8))
                header(HttpHeaders.Authorization, "Bearer ${options.apiKey}")
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
                                )
                            )
                        )
                    } catch (e: Exception) {
                        emit(StreamEvent.Error(Exception("HTTP ${response.status.value}: $errorBody")))
                    }
                    return@execute
                }

                val channel = response.bodyAsChannel()
                while (!channel.isClosedForRead) {
                    val line = channel.readUTF8Line() ?: continue

                    if (line.isBlank()) continue

                    if (!line.startsWith("data: ")) continue

                    val data = line.removePrefix("data: ").trim()

                    if (data == "[DONE]") {
                        emit(StreamEvent.Done)
                        break
                    }

                    try {
                        val chunk = json.decodeFromString<ChatCompletionChunk>(data)
                        val choice = chunk.choices?.firstOrNull()
                        val delta = choice?.delta
                        val content = delta?.content ?: ""
                        val role = delta?.role
                        val finishReason = choice?.finishReason

                        if (content.isNotEmpty() || role != null || finishReason != null) {
                            emit(
                                StreamEvent.Delta(
                                    content = content,
                                    role = role,
                                    finishReason = finishReason,
                                    usage = chunk.usage,
                                )
                            )
                        }
                    } catch (e: Exception) {
                        // Skip malformed JSON chunks
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
    suspend fun chat(messages: List<Message>): Message {
        val requestBody = ChatCompletionRequest(
            model = options.model,
            messages = messages.toRequestMessages(),
            stream = false,
            temperature = options.temperature,
            maxTokens = options.maxTokens,
        )

        val response: HttpResponse = httpClient.post(options.buildEndpoint()) {
            contentType(ContentType.Application.Json.withCharset(Charsets.UTF_8))
            header(HttpHeaders.Authorization, "Bearer ${options.apiKey}")
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
        val completionResponse = json.decodeFromString<ChatCompletionResponse>(responseBody)
        val choice = completionResponse.choices.firstOrNull()
            ?: throw Exception("No choices in response")

        return Message.assistant(content = choice.message.content ?: "")
    }

    /**
     * Closes the HTTP client.
     */
    fun close() {
        httpClient.close()
    }
}
