package xyz.junerver.compose.ai.usechat

import androidx.compose.runtime.Immutable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
  Description: Chat message model for useChat hook
  Author: Junerver
  Date: 2024
  Email: junerver@gmail.com
  Version: v2.0
*/

@OptIn(ExperimentalTime::class)
internal fun currentTimeMillis() = Clock.System.now().toEpochMilliseconds()

/**
 * Represents the role of a message sender in a chat conversation.
 */
@Serializable
enum class Role(val value: String) {
    @SerialName("system")
    System("system"),

    @SerialName("user")
    User("user"),

    @SerialName("assistant")
    Assistant("assistant"),

    @SerialName("tool")
    Tool("tool"),
    ;

    companion object {
        fun fromString(value: String): Role = when (value.lowercase()) {
            "system" -> System
            "user" -> User
            "assistant" -> Assistant
            "tool" -> Tool
            else -> User
        }
    }
}

/**
 * Represents a single message in a chat conversation.
 *
 * @property id Unique identifier for the message
 * @property role The role of the message sender (user, assistant, or system)
 * @property content The text content of the message
 * @property createdAt Timestamp when the message was created (milliseconds since epoch)
 */
@Immutable
@Serializable
data class Message(
    val id: String = generateId(),
    val role: Role,
    val content: String,
    val createdAt: Long = currentTimeMillis(),
) {
    companion object {
        private var counter = 0L

        private fun generateId(): String = "msg_${currentTimeMillis()}_${counter++}"

        fun user(content: String, id: String = generateId()) = Message(id = id, role = Role.User, content = content)

        fun assistant(content: String, id: String = generateId()) = Message(id = id, role = Role.Assistant, content = content)

        fun system(content: String, id: String = generateId()) = Message(id = id, role = Role.System, content = content)
    }
}

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
            "stop" -> Stop
            "length" -> Length
            "content_filter" -> ContentFilter
            "tool_calls" -> ToolCalls
            "function_call" -> FunctionCall
            else -> null
        }
    }
}
