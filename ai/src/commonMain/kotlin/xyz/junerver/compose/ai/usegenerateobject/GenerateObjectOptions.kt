package xyz.junerver.compose.ai.usegenerateobject

import androidx.compose.runtime.Stable
import io.ktor.client.statement.HttpResponse
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import xyz.junerver.compose.ai.usechat.ChatProvider
import xyz.junerver.compose.ai.usechat.ChatUsage
import xyz.junerver.compose.ai.usechat.Providers

/*
  Description: Configuration options for useGenerateObject hook
  Author: Junerver
  Date: 2026/01/05
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Callback type definitions for generate object events
 */
typealias OnObjectFinishCallback<T> = (obj: T, usage: ChatUsage?) -> Unit
typealias OnObjectErrorCallback = (error: Throwable) -> Unit
typealias OnObjectResponseCallback = (response: HttpResponse) -> Unit

/**
 * Configuration options for the useGenerateObject hook.
 *
 * @property provider The chat provider to use
 * @property model Override the provider's default model (null = use provider default)
 * @property systemPrompt Optional system prompt to prepend
 * @property temperature Sampling temperature (0-2), higher values make output more random
 * @property maxTokens Maximum number of tokens to generate
 * @property timeout Request timeout duration
 * @property headers Additional HTTP headers to send with requests
 * @property onResponse Callback when receiving an HTTP response
 * @property onFinish Callback when object generation is complete
 * @property onError Callback when an error occurs
 */
@Stable
data class GenerateObjectOptions<T> internal constructor(
    var provider: ChatProvider = Providers.OpenAI(apiKey = ""),
    var model: String? = null,
    var systemPrompt: String? = null,
    var temperature: Float? = null,
    var maxTokens: Int? = null,
    var timeout: Duration = 60.seconds,
    var headers: Map<String, String> = emptyMap(),
    // Callbacks
    var onResponse: OnObjectResponseCallback? = null,
    var onFinish: OnObjectFinishCallback<T>? = null,
    var onError: OnObjectErrorCallback? = null,
) {
    companion object {
        fun <T> optionOf(block: GenerateObjectOptions<T>.() -> Unit): GenerateObjectOptions<T> = GenerateObjectOptions<T>().apply(block)
    }

    /**
     * The effective model (override or provider default).
     */
    val effectiveModel: String
        get() = model ?: provider.defaultModel
}
