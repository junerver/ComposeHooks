package xyz.junerver.compose.ai.usechat

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/*
  Description: OpenAI API request/response models (multimodal + tool calls support)
  Author: Junerver
  Date: 2026/01/05-11:06
  Email: junerver@gmail.com
  Version: v4.0
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
 * Message format for OpenAI API requests with multimodal and tool support.
 */
@Serializable
internal data class ChatMessageRequest(
    val role: String,
    @Serializable(with = ChatMessageContentSerializer::class)
    val content: ChatMessageContent?,
    @SerialName("tool_calls")
    val toolCalls: List<OpenAIToolCall>? = null,
    @SerialName("tool_call_id")
    val toolCallId: String? = null,
)

/**
 * Tool call in OpenAI format.
 */
@Serializable
internal data class OpenAIToolCall(
    val id: String,
    val type: String = "function",
    val function: OpenAIFunctionCall,
)

/**
 * Function call details.
 */
@Serializable
internal data class OpenAIFunctionCall(
    val name: String,
    val arguments: String, // JSON string
)

/**
 * Content for OpenAI messages - can be text string or array of content parts.
 */
internal sealed class ChatMessageContent {
    data class Text(val text: String) : ChatMessageContent()

    data class Parts(val parts: List<OpenAIContentPart>) : ChatMessageContent()
}

/**
 * Content part types for OpenAI multimodal messages.
 */
internal sealed class OpenAIContentPart {
    data class Text(val text: String) : OpenAIContentPart()

    data class ImageUrl(val url: String, val detail: String = "auto") : OpenAIContentPart()
}

/**
 * Custom serializer for ChatMessageContent.
 * Serializes Text as a plain string, Parts as a JSON array, null as null.
 */
internal object ChatMessageContentSerializer : KSerializer<ChatMessageContent?> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ChatMessageContent")

    override fun serialize(encoder: Encoder, value: ChatMessageContent?) {
        val jsonEncoder = encoder as JsonEncoder
        when (value) {
            null -> jsonEncoder.encodeNull()
            is ChatMessageContent.Text -> jsonEncoder.encodeJsonElement(JsonPrimitive(value.text))
            is ChatMessageContent.Parts -> {
                val jsonArray = buildJsonArray {
                    value.parts.forEach { part ->
                        when (part) {
                            is OpenAIContentPart.Text -> add(
                                buildJsonObject {
                                    put("type", "text")
                                    put("text", part.text)
                                },
                            )
                            is OpenAIContentPart.ImageUrl -> add(
                                buildJsonObject {
                                    put("type", "image_url")
                                    put(
                                        "image_url",
                                        buildJsonObject {
                                            put("url", part.url)
                                            put("detail", part.detail)
                                        },
                                    )
                                },
                            )
                        }
                    }
                }
                jsonEncoder.encodeJsonElement(jsonArray)
            }
        }
    }

    override fun deserialize(decoder: Decoder): ChatMessageContent =
        throw NotImplementedError("Deserialization not needed for request models")
}

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
    @SerialName("tool_calls")
    val toolCalls: List<OpenAIToolCall>? = null,
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
    @SerialName("tool_calls")
    val toolCalls: List<ChunkToolCall>? = null,
)

@Serializable
internal data class ChunkToolCall(
    val index: Int = 0,
    val id: String? = null,
    val type: String? = null,
    val function: ChunkFunctionCall? = null,
)

@Serializable
internal data class ChunkFunctionCall(
    val name: String? = null,
    val arguments: String? = null,
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
 * Converts a list of ChatMessage to ChatMessageRequest format for API calls.
 * Supports multimodal content (text, images) and tool calls.
 */
internal fun List<ChatMessage>.toRequestMessages(): List<ChatMessageRequest> = map { msg ->
    when (msg) {
        is UserMessage -> ChatMessageRequest(
            role = "user",
            content = msg.content.toOpenAIContent(),
        )
        is AssistantMessage -> {
            val toolCalls = msg.toolCalls.takeIf { it.isNotEmpty() }?.map { tc ->
                OpenAIToolCall(
                    id = tc.toolCallId,
                    function = OpenAIFunctionCall(
                        name = tc.toolName,
                        arguments = tc.args.toString(),
                    ),
                )
            }
            ChatMessageRequest(
                role = "assistant",
                content = if (toolCalls != null) {
                    // When there are tool calls, content might be null or just text
                    val text = msg.textContent
                    if (text.isNotEmpty()) ChatMessageContent.Text(text) else null
                } else {
                    msg.content.filterIsInstance<TextPart>()
                        .takeIf { it.isNotEmpty() }
                        ?.let { ChatMessageContent.Text(msg.textContent) }
                },
                toolCalls = toolCalls,
            )
        }
        is SystemMessage -> ChatMessageRequest(
            role = "system",
            content = ChatMessageContent.Text(msg.content),
        )
        is ToolMessage -> ChatMessageRequest(
            role = "tool",
            content = ChatMessageContent.Text(msg.textContent),
            toolCallId = msg.toolCallId,
        )
    }
}

/**
 * Converts UserContentPart list to OpenAI content format.
 */
private fun List<UserContentPart>.toOpenAIContent(): ChatMessageContent {
    // Single text part - use plain string for efficiency
    if (size == 1 && first() is TextPart) {
        return ChatMessageContent.Text((first() as TextPart).text)
    }

    // Multiple parts or multimodal - use array format
    return ChatMessageContent.Parts(
        mapNotNull { part ->
            when (part) {
                is TextPart -> OpenAIContentPart.Text(part.text)
                is ImagePart -> {
                    val url = if (part.isUrl) {
                        part.data
                    } else {
                        "data:${part.mimeType};base64,${part.data}"
                    }
                    OpenAIContentPart.ImageUrl(url)
                }
                is FilePart -> {
                    // OpenAI doesn't support file content directly in chat
                    // Skip file parts
                    null
                }
            }
        },
    )
}

// endregion
