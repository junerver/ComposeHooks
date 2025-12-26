package xyz.junerver.compose.ai.usechat

import androidx.compose.runtime.Stable
import io.ktor.client.statement.HttpResponse
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import xyz.junerver.compose.hooks.Options

/*
  Description: Configuration options for useChat hook
  Author: Junerver
  Date: 2024
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Callback type definitions for chat events
 */
typealias OnResponseCallback = (response: HttpResponse) -> Unit
typealias OnFinishCallback = (message: Message, usage: ChatUsage?, finishReason: FinishReason?) -> Unit
typealias OnErrorCallback = (error: Throwable) -> Unit
typealias OnStreamCallback = (delta: String) -> Unit

/**
 * Configuration options for the useChat hook.
 *
 * @property baseUrl The base URL of the OpenAI-compatible API endpoint
 * @property apiKey The API key for authentication
 * @property model The model to use for chat completions
 * @property systemPrompt Optional system prompt to prepend to conversations
 * @property initialMessages Initial messages to populate the chat
 * @property temperature Sampling temperature (0-2), higher values make output more random
 * @property maxTokens Maximum number of tokens to generate
 * @property timeout Request timeout duration
 * @property stream Whether to use streaming responses (default: true)
 * @property headers Additional HTTP headers to send with requests
 * @property onResponse Callback when receiving an HTTP response
 * @property onFinish Callback when a message generation is complete
 * @property onError Callback when an error occurs
 * @property onStream Callback for each streaming delta
 */
@Stable
data class ChatOptions internal constructor(
    var baseUrl: String = "https://api.openai.com/v1",
    var apiKey: String = "",
    var model: String = "gpt-3.5-turbo",
    var systemPrompt: String? = null,
    var initialMessages: List<Message> = emptyList(),
    var temperature: Float? = null,
    var maxTokens: Int? = null,
    var timeout: Duration = 60.seconds,
    var stream: Boolean = true,
    var headers: Map<String, String> = emptyMap(),
    // Callbacks
    var onResponse: OnResponseCallback? = null,
    var onFinish: OnFinishCallback? = null,
    var onError: OnErrorCallback? = null,
    var onStream: OnStreamCallback? = null,
) {
    companion object : Options<ChatOptions>(::ChatOptions)

    /**
     * Builds the full API endpoint URL for chat completions.
     */
    internal fun buildEndpoint(): String {
        val base = baseUrl.trimEnd('/')
        return "$base/chat/completions"
    }
}
