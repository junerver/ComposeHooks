package xyz.junerver.compose.ai.usegenerateobject

import androidx.compose.runtime.Stable
import kotlin.time.Duration
import xyz.junerver.compose.ai.AIOptionsDefaults
import xyz.junerver.compose.ai.BaseAIOptions
import xyz.junerver.compose.ai.OnErrorCallback
import xyz.junerver.compose.ai.OnResponseCallback
import xyz.junerver.compose.ai.http.HttpEngine
import xyz.junerver.compose.ai.usechat.ChatProvider
import xyz.junerver.compose.ai.usechat.ChatUsage

/*
  Description: Configuration options for useGenerateObject hook
  Author: Junerver
  Date: 2026/01/05
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Callback type definition for generate object completion event.
 */
typealias OnObjectFinishCallback<T> = (obj: T, usage: ChatUsage?) -> Unit

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
 * @property httpEngine Custom HTTP engine (null = use global default)
 * @property stream Whether to use streaming responses (default true)
 * @property enableIncrementalParsing Whether to parse partial JSON during streaming (default true)
 * @property enableJsonHealing Enable JSON repair mechanism (default true)
 * @property onResponse Callback when receiving an HTTP response
 * @property onFinish Callback when object generation is complete
 * @property onError Callback when an error occurs
 */
@Stable
data class GenerateObjectOptions<T> internal constructor(
    override var provider: ChatProvider = AIOptionsDefaults.DEFAULT_PROVIDER,
    override var model: String? = null,
    override var systemPrompt: String? = null,
    override var temperature: Float? = null,
    override var maxTokens: Int? = null,
    override var timeout: Duration = AIOptionsDefaults.DEFAULT_TIMEOUT,
    override var headers: Map<String, String> = AIOptionsDefaults.DEFAULT_HEADERS,
    override var httpEngine: HttpEngine? = null,
    var stream: Boolean = true,
    var enableIncrementalParsing: Boolean = true,
    /**
     * Enable JSON repair mechanism, default is true.
     *
     * When enabled, automatically handles:
     * - Extract embedded JSON from text
     * - Clean format issues (comments, quotes, trailing commas)
     * - Repair incomplete JSON (balance brackets)
     *
     * When false, only performs basic markdown cleaning.
     */
    var enableJsonHealing: Boolean = true,
    // Callbacks
    override var onResponse: OnResponseCallback? = null,
    var onFinish: OnObjectFinishCallback<T>? = null,
    override var onError: OnErrorCallback? = null,
) : BaseAIOptions {
    companion object {
        fun <T> optionOf(block: GenerateObjectOptions<T>.() -> Unit): GenerateObjectOptions<T> = GenerateObjectOptions<T>().apply(block)
    }
}
