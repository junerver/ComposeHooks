package xyz.junerver.compose.ai

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import xyz.junerver.compose.ai.usechat.ChatUsage
import xyz.junerver.compose.hooks.createContext
import xyz.junerver.compose.hooks.useContext
import xyz.junerver.compose.hooks.useCreation
import xyz.junerver.compose.hooks.useEffect
import xyz.junerver.compose.hooks.useRef

/*
  Description: Token Usage Statistics System
  Author: Junerver
  Date: 2026/06/13
  Email: junerver@gmail.com
  Version: v1.0

  A global token usage tracking system for all AI hooks.
  Provides:
  - Per-session cumulative token usage (prompt, completion, total)
  - Per-request token usage history
  - Current window/context token estimation
*/

/**
 * Represents a single token usage record for a request.
 *
 * @property requestId Unique identifier for the request (or message ID)
 * @property provider The provider name (e.g., "OpenAI", "DeepSeek")
 * @property model The model used for this request
 * @property usage The token usage statistics
 * @property timestamp When this request was made
 */
@Stable
data class TokenUsageRecord(
    val requestId: String,
    val provider: String,
    val model: String,
    val usage: ChatUsage,
    val timestamp: Long = currentTimestamp(),
)

/**
 * Aggregated token usage statistics.
 *
 * @property totalPromptTokens Total input/prompt tokens consumed
 * @property totalCompletionTokens Total output/completion tokens consumed
 * @property totalTokens Total tokens consumed (prompt + completion)
 * @property requestCount Number of requests made
 * @property records List of individual usage records
 */
@Stable
data class TokenUsageStats(
    val totalPromptTokens: Int = 0,
    val totalCompletionTokens: Int = 0,
    val totalTokens: Int = 0,
    val requestCount: Int = 0,
    val records: ImmutableList<TokenUsageRecord> = persistentListOf(),
) {
    companion object {
        val EMPTY = TokenUsageStats()
    }
}

/**
 * Estimated window token usage based on message content.
 *
 * @property messageCount Number of messages in the current window
 * @property estimatedTokens Estimated token count for the current window
 * @property maxTokens Maximum tokens configured for the model (if known)
 * @property remainingTokens Estimated remaining tokens in the window
 */
@Stable
data class WindowTokenUsage(
    val messageCount: Int = 0,
    val estimatedTokens: Int = 0,
    val maxTokens: Int? = null,
    val remainingTokens: Int? = null,
)

/**
 * Token usage tracker that manages cumulative statistics across a session.
 *
 * This is designed to be used as a Context value, allowing all AI hooks
 * within a provider to share and contribute to the same statistics.
 *
 * Uses MutableState for reactive updates in Compose.
 */
@Stable
class TokenUsageTracker {
    private val _statsState = mutableStateOf(TokenUsageStats.EMPTY)

    /**
     * Current aggregated token usage statistics.
     * This is a reactive state that will trigger recomposition when updated.
     */
    val stats: TokenUsageStats get() = _statsState.value

    /**
     * Record a new token usage from a completed request.
     *
     * @param requestId Unique identifier for the request
     * @param provider Provider name
     * @param model Model name
     * @param usage Token usage from the response
     */
    fun recordUsage(requestId: String, provider: String, model: String, usage: ChatUsage) {
        val record = TokenUsageRecord(
            requestId = requestId,
            provider = provider,
            model = model,
            usage = usage,
        )
        _statsState.value = _statsState.value.copy(
            totalPromptTokens = _statsState.value.totalPromptTokens + usage.promptTokens,
            totalCompletionTokens = _statsState.value.totalCompletionTokens + usage.completionTokens,
            totalTokens = _statsState.value.totalTokens + usage.totalTokens,
            requestCount = _statsState.value.requestCount + 1,
            records = (_statsState.value.records + record).toImmutableList(),
        )
    }

    /**
     * Reset all accumulated statistics.
     */
    fun reset() {
        _statsState.value = TokenUsageStats.EMPTY
    }
}

/**
 * Context for sharing token usage statistics across AI hooks.
 * Uses a sentinel value to detect if context was explicitly provided.
 */
private val DefaultTokenUsageTracker = TokenUsageTracker()
private var isTokenUsageContextProvided = false

val TokenUsageContext by lazy { createContext(DefaultTokenUsageTracker) }

/**
 * Hook to access the token usage tracker from the current context.
 *
 * @return The [TokenUsageTracker] instance from the nearest provider
 */
@Composable
fun useTokenUsage(): TokenUsageTracker = useContext(TokenUsageContext)

/**
 * Hook to get reactive token usage statistics.
 *
 * This hook subscribes to the tracker and returns the current stats.
 * Since the tracker doesn't have built-in reactivity, this returns
 * the stats directly. Use inside a composable that recomposes when
 * the chat state changes.
 *
 * @return The current [TokenUsageStats]
 */
@Composable
fun useTokenStats(): TokenUsageStats {
    val tracker = useTokenUsage()
    return tracker.stats
}

/**
 * Hook to estimate current window/context token usage based on messages.
 *
 * Uses a simple heuristic: ~4 characters per token for English, ~2 for CJK.
 * This is a rough estimate; actual tokenization depends on the model.
 *
 * @param messages Current messages in the conversation
 * @param maxContextTokens Maximum context window size (if known)
 * @return [WindowTokenUsage] with estimated token counts
 */
@Composable
fun useWindowTokens(
    messages: List<Any>, // Accept any message type with textContent
    maxContextTokens: Int? = null,
): WindowTokenUsage {
    val windowUsageRef = useRef(WindowTokenUsage())

    useEffect(messages) {
        val totalChars = messages.sumOf { msg ->
            // Try to get text content via reflection or common interface
            when (msg) {
                is xyz.junerver.compose.ai.usechat.ChatMessage -> msg.textContent.length
                else -> msg.toString().length
            }
        }
        // Rough estimation: 1 token ≈ 3-4 chars for mixed content
        val estimatedTokens = totalChars / 3
        val remaining = maxContextTokens?.let { it - estimatedTokens }

        windowUsageRef.current = WindowTokenUsage(
            messageCount = messages.size,
            estimatedTokens = estimatedTokens,
            maxTokens = maxContextTokens,
            remainingTokens = remaining,
        )
    }

    return windowUsageRef.current
}

/**
 * Provider composable that wraps AI hooks with token usage tracking.
 *
 * All AI hooks within this provider will automatically report their
 * token usage to the shared tracker.
 *
 * @param content Composable content containing AI hooks
 *
 * @example
 * ```kotlin
 * TokenUsageProvider {
 *     val chat = useChat { ... }
 *     val stats = useTokenStats()
 *     Text("Total tokens: ${stats.totalTokens}")
 * }
 * ```
 */
@Composable
fun TokenUsageProvider(content: @Composable () -> Unit) {
    val tracker = useCreation { TokenUsageTracker() }.current
    isTokenUsageContextProvided = true
    TokenUsageContext.Provider(tracker) {
        content()
    }
}

/**
 * Remember a TokenUsageTracker if available in the context.
 *
 * This function safely retrieves the tracker from the context.
 * If no TokenUsageProvider is present, returns null.
 *
 * @return The [TokenUsageTracker] instance or null if not in a provider
 */
@Composable
fun rememberTokenTracker(): TokenUsageTracker? {
    // Only return tracker if TokenUsageProvider is active
    if (!isTokenUsageContextProvided) return null
    val tracker = useContext(TokenUsageContext)
    // Check if it's the default tracker (not explicitly provided)
    return if (tracker === DefaultTokenUsageTracker) null else tracker
}

/**
 * Utility function to get current timestamp in milliseconds.
 * Platform-independent implementation.
 */
internal expect fun currentTimestamp(): Long
