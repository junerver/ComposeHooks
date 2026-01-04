package xyz.junerver.compose.ai.usechat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
  Description: Anthropic API request/response models
  Author: Junerver
  Date: 2024
  Email: junerver@gmail.com
  Version: v2.0
*/

// region Request Models

/**
 * Request body for Anthropic messages API.
 *
 * @see <a href="https://docs.anthropic.com/en/api/messages">Anthropic Messages API</a>
 */
@Serializable
internal data class AnthropicRequest(
    val model: String,
    val messages: List<AnthropicMessage>,
    @SerialName("max_tokens")
    val maxTokens: Int = 4096,
    val stream: Boolean = true,
    val system: String? = null,
    val temperature: Float? = null,
)

/**
 * Message format for Anthropic API.
 */
@Serializable
internal data class AnthropicMessage(
    val role: String,
    val content: String,
)

// endregion

// region Response Models (Non-streaming)

/**
 * Response body for Anthropic messages API.
 */
@Serializable
internal data class AnthropicResponse(
    val id: String,
    val type: String,
    val role: String,
    val content: List<AnthropicContentBlock>,
    val model: String,
    @SerialName("stop_reason")
    val stopReason: String? = null,
    @SerialName("stop_sequence")
    val stopSequence: String? = null,
    val usage: AnthropicUsage,
)

/**
 * Content block in Anthropic response.
 */
@Serializable
internal data class AnthropicContentBlock(
    val type: String,
    val text: String? = null,
)

/**
 * Token usage in Anthropic response.
 */
@Serializable
internal data class AnthropicUsage(
    @SerialName("input_tokens")
    val inputTokens: Int = 0,
    @SerialName("output_tokens")
    val outputTokens: Int = 0,
)

// endregion

// region Streaming Response Models

/**
 * Streaming event from Anthropic API.
 *
 * Anthropic uses Server-Sent Events with different event types:
 * - `message_start`: Initial message metadata
 * - `content_block_start`: Start of a content block
 * - `content_block_delta`: Text delta in a content block
 * - `content_block_stop`: End of a content block
 * - `message_delta`: Final message metadata (stop_reason, usage)
 * - `message_stop`: End of message
 *
 * @see <a href="https://docs.anthropic.com/en/api/messages-streaming">Anthropic Streaming</a>
 */
@Serializable
internal data class AnthropicStreamEvent(
    val type: String,
    val index: Int? = null,
    val delta: AnthropicDelta? = null,
    val usage: AnthropicStreamUsage? = null,
    @SerialName("content_block")
    val contentBlock: AnthropicContentBlock? = null,
    val message: AnthropicStreamMessage? = null,
)

/**
 * Delta content in streaming response.
 */
@Serializable
internal data class AnthropicDelta(
    val type: String? = null,
    val text: String? = null,
    @SerialName("stop_reason")
    val stopReason: String? = null,
    @SerialName("stop_sequence")
    val stopSequence: String? = null,
)

/**
 * Usage in streaming response.
 */
@Serializable
internal data class AnthropicStreamUsage(
    @SerialName("input_tokens")
    val inputTokens: Int? = null,
    @SerialName("output_tokens")
    val outputTokens: Int? = null,
)

/**
 * Message metadata in message_start event.
 */
@Serializable
internal data class AnthropicStreamMessage(
    val id: String? = null,
    val type: String? = null,
    val role: String? = null,
    val model: String? = null,
    val usage: AnthropicStreamUsage? = null,
)

// endregion

// region Error Response

/**
 * Error response from Anthropic API.
 */
@Serializable
internal data class AnthropicErrorResponse(
    val type: String,
    val error: AnthropicError,
)

@Serializable
internal data class AnthropicError(
    val type: String,
    val message: String,
)

// endregion
