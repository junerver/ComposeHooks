package xyz.junerver.compose.ai.usechat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
  Description: OpenAI API request/response models
  Author: Junerver
  Date: 2024
  Email: junerver@gmail.com
  Version: v1.0
*/

// region Request Models

/**
 * Request body for OpenAI chat completions API.
 */
@Serializable
internal data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessageRequest>,
    val stream: Boolean = true,
    val temperature: Float? = null,
    @SerialName("max_tokens")
    val maxTokens: Int? = null,
)

/**
 * Message format for OpenAI API requests.
 */
@Serializable
internal data class ChatMessageRequest(
    val role: String,
    val content: String,
)

// endregion

// region Response Models (Non-streaming)

/**
 * Response body for non-streaming chat completions.
 */
@Serializable
internal data class ChatCompletionResponse(
    val id: String,
    val `object`: String,
    val created: Long,
    val model: String,
    val choices: List<ChatChoice>,
    val usage: ChatUsage? = null,
)

@Serializable
internal data class ChatChoice(
    val index: Int,
    val message: ChatMessageResponse,
    @SerialName("finish_reason")
    val finishReason: String? = null,
)

@Serializable
internal data class ChatMessageResponse(
    val role: String,
    val content: String? = null,
)

// endregion

// region Streaming Response Models

/**
 * Response chunk for streaming chat completions.
 *
 * OpenAI streaming format:
 * ```
 * data: {"id":"...","object":"chat.completion.chunk","choices":[{"delta":{"content":"Hello"}}]}
 * data: [DONE]
 * ```
 */
@Serializable
internal data class ChatCompletionChunk(
    val id: String? = null,
    val `object`: String? = null,
    val created: Long? = null,
    val model: String? = null,
    val choices: List<ChunkChoice>? = null,
    val usage: ChatUsage? = null,
)

@Serializable
internal data class ChunkChoice(
    val index: Int = 0,
    val delta: ChunkDelta? = null,
    @SerialName("finish_reason")
    val finishReason: String? = null,
)

@Serializable
internal data class ChunkDelta(
    val role: String? = null,
    val content: String? = null,
)

// endregion

// region Error Response

/**
 * Error response from OpenAI API.
 */
@Serializable
internal data class OpenAIErrorResponse(
    val error: OpenAIError,
)

@Serializable
internal data class OpenAIError(
    val message: String,
    val type: String? = null,
    val param: String? = null,
    val code: String? = null,
)

/**
 * Exception representing an OpenAI API error.
 */
class OpenAIException(
    val errorMessage: String,
    val errorType: String? = null,
    val errorCode: String? = null,
) : Exception(errorMessage)

// endregion

// region Internal Helpers

/**
 * Converts a list of Message to ChatMessageRequest format for API calls.
 */
internal fun List<Message>.toRequestMessages(): List<ChatMessageRequest> = map {
    ChatMessageRequest(role = it.role.value, content = it.content)
}

// endregion
