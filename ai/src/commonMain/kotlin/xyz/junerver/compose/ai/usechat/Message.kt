package xyz.junerver.compose.ai.usechat

import androidx.compose.runtime.Immutable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/*
  Description: Chat message models inspired by Vercel AI SDK
  Author: Junerver
  Date: 2026/01/05-11:06
  Email: junerver@gmail.com
  Version: v4.0

  Reference: https://github.com/vercel/ai
*/

@OptIn(ExperimentalTime::class)
internal fun currentTimeMillis() = Clock.System.now().toEpochMilliseconds()

private var messageCounter = 0L

internal fun generateMessageId(): String = "msg_${currentTimeMillis()}_${messageCounter++}"

// region Message Types

/**
 * Base interface for all chat messages.
 * Inspired by Vercel AI SDK's ModelMessage design.
 */
@Serializable
sealed interface ChatMessage {
    val id: String
    val createdAt: Long
    val metadata: Map<String, String>?

    /** Gets the text content of the message. */
    val textContent: String
}

/**
 * User message containing user input.
 * Content can include text, images, and files.
 */
@Immutable
@Serializable
@SerialName("user")
data class UserMessage(
    override val id: String = generateMessageId(),
    val content: List<UserContentPart>,
    override val createdAt: Long = currentTimeMillis(),
    override val metadata: Map<String, String>? = null,
) : ChatMessage {
    override val textContent: String
        get() = content.filterIsInstance<TextPart>().joinToString("") { it.text }

    val hasImages: Boolean get() = content.any { it is ImagePart }
    val hasFiles: Boolean get() = content.any { it is FilePart }
    val isTextOnly: Boolean get() = content.all { it is TextPart }
}

/**
 * Assistant message containing AI response.
 * Content can include text, files, reasoning, and tool calls.
 */
@Immutable
@Serializable
@SerialName("assistant")
data class AssistantMessage(
    override val id: String = generateMessageId(),
    val content: List<AssistantContentPart>,
    override val createdAt: Long = currentTimeMillis(),
    override val metadata: Map<String, String>? = null,
    val model: String? = null,
    val usage: ChatUsage? = null,
    val finishReason: FinishReason? = null,
) : ChatMessage {
    override val textContent: String
        get() = content.filterIsInstance<TextPart>().joinToString("") { it.text }

    val reasoningContent: String?
        get() = content.filterIsInstance<ReasoningPart>().takeIf { it.isNotEmpty() }
            ?.joinToString("") { it.text }

    val toolCalls: List<ToolCallPart>
        get() = content.filterIsInstance<ToolCallPart>()

    val hasToolCalls: Boolean get() = content.any { it is ToolCallPart }
    val hasReasoning: Boolean get() = content.any { it is ReasoningPart }
}

/**
 * System message containing system instructions.
 * Content is always plain text.
 */
@Immutable
@Serializable
@SerialName("system")
data class SystemMessage(
    override val id: String = generateMessageId(),
    val content: String,
    override val createdAt: Long = currentTimeMillis(),
    override val metadata: Map<String, String>? = null,
) : ChatMessage {
    override val textContent: String get() = content
}

/**
 * Tool message containing tool execution results.
 */
@Immutable
@Serializable
@SerialName("tool")
data class ToolMessage(
    override val id: String = generateMessageId(),
    val toolCallId: String,
    val content: List<ToolResultPart>,
    override val createdAt: Long = currentTimeMillis(),
    override val metadata: Map<String, String>? = null,
) : ChatMessage {
    override val textContent: String
        get() = content.joinToString("\n") { it.result.toString() }
}

// Type alias for backward compatibility
typealias Message = ChatMessage

// endregion

// region Content Parts

/**
 * Content part that can be used in user messages.
 */
@Serializable
sealed interface UserContentPart

/**
 * Content part that can be used in assistant messages.
 */
@Serializable
sealed interface AssistantContentPart

/**
 * Text content part. Can be used in both user and assistant messages.
 */
@Immutable
@Serializable
@SerialName("text")
data class TextPart(
    val text: String,
) : UserContentPart, AssistantContentPart

/**
 * Image content part. Only for user messages.
 *
 * @param data Base64-encoded image data or image URL
 * @param mimeType MIME type (image/jpeg, image/png, image/gif, image/webp)
 * @param isUrl Whether data is a URL (true) or base64-encoded data (false)
 */
@Immutable
@Serializable
@SerialName("image")
data class ImagePart(
    val data: String,
    val mimeType: String = "image/jpeg",
    val isUrl: Boolean = false,
) : UserContentPart {
    companion object {
        fun fromBase64(base64: String, mimeType: String = "image/jpeg") = ImagePart(data = base64, mimeType = mimeType, isUrl = false)

        fun fromUrl(url: String) = ImagePart(data = url, mimeType = "", isUrl = true)
    }
}

/**
 * File content part. Can be used in both user and assistant messages.
 *
 * @param data Base64-encoded file data
 * @param mimeType MIME type (application/pdf, etc.)
 * @param fileName Optional file name
 */
@Immutable
@Serializable
@SerialName("file")
data class FilePart(
    val data: String,
    val mimeType: String,
    val fileName: String? = null,
) : UserContentPart, AssistantContentPart

/**
 * Reasoning/thinking content part. Only for assistant messages.
 * Used for models that expose their thinking process (e.g., Claude's thinking blocks).
 */
@Immutable
@Serializable
@SerialName("reasoning")
data class ReasoningPart(
    val text: String,
) : AssistantContentPart

/**
 * Tool call content part. Only for assistant messages.
 * Represents a request from the model to call a tool/function.
 *
 * @param toolCallId Unique identifier for this tool call
 * @param toolName Name of the tool to call
 * @param args Arguments to pass to the tool as JSON object
 */
@Immutable
@Serializable
@SerialName("tool_call")
data class ToolCallPart(
    val toolCallId: String,
    val toolName: String,
    val args: JsonObject,
) : AssistantContentPart

/**
 * Tool result content part. Only for tool messages.
 * Represents the result of a tool execution.
 *
 * @param toolCallId ID of the tool call this result is for
 * @param toolName Name of the tool that was called
 * @param result The result of the tool execution
 * @param isError Whether the result is an error
 */
@Immutable
@Serializable
@SerialName("tool_result")
data class ToolResultPart(
    val toolCallId: String,
    val toolName: String,
    val result: JsonElement,
    val isError: Boolean = false,
)

// endregion

// region Factory Functions

/**
 * Creates a user message with text content.
 */
fun userMessage(text: String, id: String = generateMessageId(), metadata: Map<String, String>? = null) = UserMessage(
    id = id,
    content = listOf(TextPart(text)),
    metadata = metadata,
)

/**
 * Creates a user message with multiple content parts.
 */
fun userMessage(content: List<UserContentPart>, id: String = generateMessageId(), metadata: Map<String, String>? = null) = UserMessage(
    id = id,
    content = content,
    metadata = metadata,
)

/**
 * Creates a user message with text and image.
 */
fun userMessageWithImage(
    text: String,
    imageBase64: String,
    mimeType: String = "image/jpeg",
    id: String = generateMessageId(),
) = UserMessage(
    id = id,
    content = listOf(
        TextPart(text),
        ImagePart.fromBase64(imageBase64, mimeType),
    ),
)

/**
 * Creates a user message with text and image URL.
 */
fun userMessageWithImageUrl(text: String, imageUrl: String, id: String = generateMessageId()) = UserMessage(
    id = id,
    content = listOf(
        TextPart(text),
        ImagePart.fromUrl(imageUrl),
    ),
)

/**
 * Creates a user message with text and file.
 */
fun userMessageWithFile(
    text: String,
    fileBase64: String,
    mimeType: String,
    fileName: String? = null,
    id: String = generateMessageId(),
) = UserMessage(
    id = id,
    content = listOf(
        TextPart(text),
        FilePart(fileBase64, mimeType, fileName),
    ),
)

/**
 * Creates an assistant message with text content.
 */
fun assistantMessage(
    text: String,
    id: String = generateMessageId(),
    model: String? = null,
    usage: ChatUsage? = null,
    finishReason: FinishReason? = null,
    metadata: Map<String, String>? = null,
) = AssistantMessage(
    id = id,
    content = listOf(TextPart(text)),
    model = model,
    usage = usage,
    finishReason = finishReason,
    metadata = metadata,
)

/**
 * Creates an assistant message with multiple content parts.
 */
fun assistantMessage(
    content: List<AssistantContentPart>,
    id: String = generateMessageId(),
    model: String? = null,
    usage: ChatUsage? = null,
    finishReason: FinishReason? = null,
    metadata: Map<String, String>? = null,
) = AssistantMessage(
    id = id,
    content = content,
    model = model,
    usage = usage,
    finishReason = finishReason,
    metadata = metadata,
)

/**
 * Creates a system message.
 */
fun systemMessage(content: String, id: String = generateMessageId(), metadata: Map<String, String>? = null) = SystemMessage(
    id = id,
    content = content,
    metadata = metadata,
)

/**
 * Creates a tool message with results.
 */
fun toolMessage(
    toolCallId: String,
    results: List<ToolResultPart>,
    id: String = generateMessageId(),
    metadata: Map<String, String>? = null,
) = ToolMessage(
    id = id,
    toolCallId = toolCallId,
    content = results,
    metadata = metadata,
)

/**
 * Creates a tool message with a single result.
 */
fun toolMessage(
    toolCallId: String,
    toolName: String,
    result: JsonElement,
    isError: Boolean = false,
    id: String = generateMessageId(),
) = ToolMessage(
    id = id,
    toolCallId = toolCallId,
    content = listOf(ToolResultPart(toolCallId, toolName, result, isError)),
)

// endregion

// region Usage and Finish Reason

/**
 * Represents the usage statistics from an API response.
 */
@Immutable
@Serializable
data class ChatUsage(
    @SerialName("prompt_tokens")
    val promptTokens: Int = 0,
    @SerialName("completion_tokens")
    val completionTokens: Int = 0,
    @SerialName("total_tokens")
    val totalTokens: Int = 0,
)

/**
 * Represents the finish reason for a chat completion.
 */
@Serializable
enum class FinishReason {
    @SerialName("stop")
    Stop,

    @SerialName("length")
    Length,

    @SerialName("content_filter")
    ContentFilter,

    @SerialName("tool_calls")
    ToolCalls,

    @SerialName("function_call")
    FunctionCall,
    ;

    companion object {
        fun fromString(value: String?): FinishReason? = when (value?.lowercase()) {
            "stop", "end_turn" -> Stop
            "length", "max_tokens" -> Length
            "content_filter" -> ContentFilter
            "tool_calls", "tool_use" -> ToolCalls
            "function_call" -> FunctionCall
            else -> null
        }
    }
}

// endregion
