package xyz.junerver.compose.ai.usechat

import androidx.compose.runtime.Stable
import kotlin.time.Duration
import xyz.junerver.compose.ai.AIOptionsDefaults
import xyz.junerver.compose.ai.BaseAIOptions
import xyz.junerver.compose.ai.OnErrorCallback
import xyz.junerver.compose.ai.OnResponseCallback
import xyz.junerver.compose.hooks.Options

/*
  Description: Configuration options for useChat hook
  Author: Junerver
  Date: 2026/01/05-11:06
  Email: junerver@gmail.com
  Version: v2.0
*/

/**
 * Callback type definitions for chat-specific events
 */
typealias OnFinishCallback = (message: ChatMessage, usage: ChatUsage?, finishReason: FinishReason?) -> Unit
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
    override var provider: ChatProvider = AIOptionsDefaults.DEFAULT_PROVIDER,
    override var model: String? = null,
    override var systemPrompt: String? = null,
    var initialMessages: List<ChatMessage> = emptyList(),
    override var temperature: Float? = null,
    override var maxTokens: Int? = null,
    override var timeout: Duration = AIOptionsDefaults.DEFAULT_TIMEOUT,
    var stream: Boolean = true,
    override var headers: Map<String, String> = AIOptionsDefaults.DEFAULT_HEADERS,
    // Callbacks
    override var onResponse: OnResponseCallback? = null,
    var onFinish: OnFinishCallback? = null,
    override var onError: OnErrorCallback? = null,
    var onStream: OnStreamCallback? = null,
) : BaseAIOptions {
    companion object : Options<ChatOptions>(::ChatOptions)

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
    internal fun buildRequestBody(messages: List<ChatMessage>, stream: Boolean): String = provider.buildRequestBody(
        messages = messages,
        model = effectiveModel,
        stream = stream,
        temperature = temperature,
        maxTokens = maxTokens,
        systemPrompt = systemPrompt,
    )
}
