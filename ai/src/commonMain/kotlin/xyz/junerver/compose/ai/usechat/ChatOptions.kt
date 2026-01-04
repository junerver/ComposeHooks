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
  Version: v2.0
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
 * @property provider The chat provider to use (includes apiKey, baseUrl, model)
 * @property model Override the provider's default model (null = use provider default)
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
    var provider: ChatProvider = Providers.OpenAI(apiKey = ""),
    var model: String? = null,
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
     * The effective model (override or provider default).
     */
    val effectiveModel: String
        get() = model ?: provider.defaultModel

    /**
     * Builds the full API endpoint URL for chat completions.
     */
    internal fun buildEndpoint(): String {
        val base = provider.baseUrl.trimEnd('/')
        return "$base${provider.chatEndpoint}"
    }

    /**
     * Builds authentication headers using the provider.
     */
    internal fun buildAuthHeaders(): Map<String, String> = provider.buildAuthHeaders()

    /**
     * Builds the request body using the provider.
     */
    internal fun buildRequestBody(messages: List<Message>, stream: Boolean): String = provider.buildRequestBody(
        messages = messages,
        model = effectiveModel,
        stream = stream,
        temperature = temperature,
        maxTokens = maxTokens,
        systemPrompt = systemPrompt,
    )
}
