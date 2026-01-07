package xyz.junerver.compose.ai.usechat

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/*
  Description: Anthropic API request/response models (multimodal + tool use support)
  Author: Junerver
  Date: 2026/01/05-11:06
  Email: junerver@gmail.com
  Version: v4.0
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
    val tools: List<AnthropicTool>? = null,
    @SerialName("tool_choice")
    val toolChoice: JsonObject? = null,
)

/**
 * Tool definition in Anthropic messages API format.
 */
@Serializable
internal data class AnthropicTool(
    val name: String,
    val description: String,
    @SerialName("input_schema")
    val inputSchema: JsonObject,
)

/**
 * Message format for Anthropic API with multimodal and tool support.
 */
@Serializable
internal data class AnthropicMessage(
    val role: String,
    @Serializable(with = AnthropicMessageContentSerializer::class)
    val content: AnthropicMessageContent,
)

/**
 * Content for Anthropic messages - can be text string or array of content blocks.
 */
internal sealed class AnthropicMessageContent {
    data class Text(val text: String) : AnthropicMessageContent()

    data class Parts(val parts: List<AnthropicContentPart>) : AnthropicMessageContent()
}

/**
 * Content block types for Anthropic multimodal messages.
 */
internal sealed class AnthropicContentPart {
    data class Text(val text: String) : AnthropicContentPart()

    data class Image(val source: AnthropicImageSource) : AnthropicContentPart()

    data class Document(val source: AnthropicDocumentSource, val cacheControl: AnthropicCacheControl? = null) : AnthropicContentPart()

    data class ToolUse(val id: String, val name: String, val input: JsonObject) : AnthropicContentPart()

    data class ToolResult(val toolUseId: String, val content: String, val isError: Boolean = false) : AnthropicContentPart()

    data class Thinking(val thinking: String) : AnthropicContentPart()
}

/**
 * Image source for Anthropic API.
 */
internal data class AnthropicImageSource(
    val type: String, // "base64" or "url"
    val mediaType: String = "",
    val data: String = "",
    val url: String = "",
) {
    companion object {
        fun fromBase64(base64: String, mediaType: String) = AnthropicImageSource(type = "base64", mediaType = mediaType, data = base64)

        fun fromUrl(url: String) = AnthropicImageSource(type = "url", url = url)
    }
}

/**
 * Document source for Anthropic API (PDF support).
 */
internal data class AnthropicDocumentSource(
    val type: String, // "base64"
    val mediaType: String, // "application/pdf"
    val data: String,
)

/**
 * Cache control for Anthropic API.
 */
internal data class AnthropicCacheControl(
    val type: String = "ephemeral",
)

/**
 * Custom serializer for AnthropicMessageContent.
 * Serializes Text as a plain string, Parts as a JSON array of content blocks.
 */
internal object AnthropicMessageContentSerializer : KSerializer<AnthropicMessageContent> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("AnthropicMessageContent")

    override fun serialize(encoder: Encoder, value: AnthropicMessageContent) {
        val jsonEncoder = encoder as JsonEncoder
        when (value) {
            is AnthropicMessageContent.Text -> jsonEncoder.encodeJsonElement(JsonPrimitive(value.text))
            is AnthropicMessageContent.Parts -> {
                val jsonArray = buildJsonArray {
                    value.parts.forEach { part ->
                        when (part) {
                            is AnthropicContentPart.Text -> add(
                                buildJsonObject {
                                    put("type", "text")
                                    put("text", part.text)
                                },
                            )
                            is AnthropicContentPart.Image -> add(
                                buildJsonObject {
                                    put("type", "image")
                                    put(
                                        "source",
                                        buildJsonObject {
                                            put("type", part.source.type)
                                            if (part.source.type == "base64") {
                                                put("media_type", part.source.mediaType)
                                                put("data", part.source.data)
                                            } else {
                                                put("url", part.source.url)
                                            }
                                        },
                                    )
                                },
                            )
                            is AnthropicContentPart.Document -> add(
                                buildJsonObject {
                                    put("type", "document")
                                    put(
                                        "source",
                                        buildJsonObject {
                                            put("type", part.source.type)
                                            put("media_type", part.source.mediaType)
                                            put("data", part.source.data)
                                        },
                                    )
                                    part.cacheControl?.let {
                                        put(
                                            "cache_control",
                                            buildJsonObject {
                                                put("type", it.type)
                                            },
                                        )
                                    }
                                },
                            )
                            is AnthropicContentPart.ToolUse -> add(
                                buildJsonObject {
                                    put("type", "tool_use")
                                    put("id", part.id)
                                    put("name", part.name)
                                    put("input", part.input)
                                },
                            )
                            is AnthropicContentPart.ToolResult -> add(
                                buildJsonObject {
                                    put("type", "tool_result")
                                    put("tool_use_id", part.toolUseId)
                                    put("content", part.content)
                                    if (part.isError) {
                                        put("is_error", true)
                                    }
                                },
                            )
                            is AnthropicContentPart.Thinking -> add(
                                buildJsonObject {
                                    put("type", "thinking")
                                    put("thinking", part.thinking)
                                },
                            )
                        }
                    }
                }
                jsonEncoder.encodeJsonElement(jsonArray)
            }
        }
    }

    override fun deserialize(decoder: Decoder): AnthropicMessageContent =
        throw NotImplementedError("Deserialization not needed for request models")
}

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
    val id: String? = null,
    val name: String? = null,
    val input: JsonObject? = null,
    val thinking: String? = null,
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
    val thinking: String? = null,
    @SerialName("partial_json")
    val partialJson: String? = null,
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

/**
 * Exception representing an Anthropic API error.
 */
class AnthropicException(
    val errorMessage: String,
    val errorType: String,
) : Exception(errorMessage)

// endregion

// region Internal Helpers

/**
 * Converts a list of ChatMessage to AnthropicMessage format for API calls.
 * Filters out system messages (handled separately) and supports multimodal content and tools.
 */
internal fun List<ChatMessage>.toAnthropicMessages(): List<AnthropicMessage> = filter { it !is SystemMessage }
    .map { msg ->
        when (msg) {
            is UserMessage -> AnthropicMessage(
                role = "user",
                content = msg.content.toAnthropicContent(),
            )
            is AssistantMessage -> {
                val parts = mutableListOf<AnthropicContentPart>()

                // Add reasoning/thinking blocks first
                msg.content.filterIsInstance<ReasoningPart>().forEach {
                    parts.add(AnthropicContentPart.Thinking(it.text))
                }

                // Add text parts
                msg.content.filterIsInstance<TextPart>().forEach {
                    parts.add(AnthropicContentPart.Text(it.text))
                }

                // Add tool calls
                msg.toolCalls.forEach { tc ->
                    parts.add(
                        AnthropicContentPart.ToolUse(
                            id = tc.toolCallId,
                            name = tc.toolName,
                            input = tc.args,
                        ),
                    )
                }

                AnthropicMessage(
                    role = "assistant",
                    content = if (parts.size == 1 && parts.first() is AnthropicContentPart.Text) {
                        AnthropicMessageContent.Text((parts.first() as AnthropicContentPart.Text).text)
                    } else {
                        AnthropicMessageContent.Parts(parts)
                    },
                )
            }
            is ToolMessage -> AnthropicMessage(
                role = "user",
                content = AnthropicMessageContent.Parts(
                    msg.content.map { result ->
                        AnthropicContentPart.ToolResult(
                            toolUseId = result.toolCallId,
                            content = result.result.toString(),
                            isError = result.isError,
                        )
                    },
                ),
            )
            is SystemMessage -> throw IllegalStateException("System messages should be filtered out")
        }
    }

/**
 * Converts UserContentPart list to Anthropic content format.
 */
private fun List<UserContentPart>.toAnthropicContent(): AnthropicMessageContent {
    // Single text part - use plain string for efficiency
    if (size == 1 && first() is TextPart) {
        return AnthropicMessageContent.Text((first() as TextPart).text)
    }

    // Multiple parts or multimodal - use array format
    return AnthropicMessageContent.Parts(
        map { part ->
            when (part) {
                is TextPart -> AnthropicContentPart.Text(part.text)
                is ImagePart -> {
                    val source = if (part.isUrl) {
                        AnthropicImageSource.fromUrl(part.data)
                    } else {
                        AnthropicImageSource.fromBase64(part.data, part.mimeType)
                    }
                    AnthropicContentPart.Image(source)
                }
                is FilePart -> {
                    // Anthropic supports PDF documents
                    AnthropicContentPart.Document(
                        source = AnthropicDocumentSource(
                            type = "base64",
                            mediaType = part.mimeType,
                            data = part.data,
                        ),
                    )
                }
            }
        },
    )
}

// endregion
