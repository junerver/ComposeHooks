package xyz.junerver.compose.ai

import io.ktor.client.statement.HttpResponse
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import xyz.junerver.compose.ai.usechat.ChatProvider
import xyz.junerver.compose.ai.usechat.Providers

/*
  Description: Base interface for AI options
  Author: Junerver
  Date: 2026/01/06
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Common callback type for HTTP response events.
 */
typealias OnResponseCallback = (response: HttpResponse) -> Unit

/**
 * Common callback type for error events.
 */
typealias OnErrorCallback = (error: Throwable) -> Unit

/**
 * Base interface for AI-related options.
 * Provides common configuration properties shared by useChat and useGenerateObject.
 *
 * @property provider The chat provider to use (includes apiKey, baseUrl, model)
 * @property model Override the provider's default model (null = use provider default)
 * @property systemPrompt Optional system prompt to prepend
 * @property temperature Sampling temperature (0-2), higher values make output more random
 * @property maxTokens Maximum number of tokens to generate
 * @property timeout Request timeout duration
 * @property headers Additional HTTP headers to send with requests
 * @property onResponse Callback when receiving an HTTP response
 * @property onError Callback when an error occurs
 */
interface BaseAIOptions {
    var provider: ChatProvider
    var model: String?
    var systemPrompt: String?
    var temperature: Float?
    var maxTokens: Int?
    var timeout: Duration
    var headers: Map<String, String>
    var onResponse: OnResponseCallback?
    var onError: OnErrorCallback?

    /**
     * The effective model (override or provider default).
     */
    val effectiveModel: String
        get() = model ?: provider.defaultModel
}

/**
 * Default values for BaseAIOptions properties.
 */
object AIOptionsDefaults {
    val DEFAULT_PROVIDER: ChatProvider = Providers.OpenAI(apiKey = "")
    val DEFAULT_TIMEOUT: Duration = 60.seconds
    val DEFAULT_HEADERS: Map<String, String> = emptyMap()
}
