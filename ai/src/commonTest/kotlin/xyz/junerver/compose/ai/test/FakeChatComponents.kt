package xyz.junerver.compose.ai.test

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import xyz.junerver.compose.ai.http.HttpEngine
import xyz.junerver.compose.ai.http.HttpRequest
import xyz.junerver.compose.ai.http.HttpResult
import xyz.junerver.compose.ai.http.SseEvent
import xyz.junerver.compose.ai.usechat.AssistantMessage
import xyz.junerver.compose.ai.usechat.ChatMessage
import xyz.junerver.compose.ai.usechat.ChatProvider
import xyz.junerver.compose.ai.usechat.ChatResponseResult
import xyz.junerver.compose.ai.usechat.ChatUsage
import xyz.junerver.compose.ai.usechat.FinishReason
import xyz.junerver.compose.ai.usechat.StreamEvent
import xyz.junerver.compose.ai.usechat.assistantMessage

/**
 * Lightweight provider used in tests to avoid dealing with vendor specific formats.
 *
 * The provider interprets streaming lines in a simplified protocol:
 * - `delta:<text>` emits a [StreamEvent.Delta] with the provided content
 * - `finish:<reason>` emits a [StreamEvent.Delta] carrying only the finish reason
 * - `done` emits [StreamEvent.Done]
 * - `error:<message>` emits [StreamEvent.Error]
 */
internal class FakeChatProvider(
    override val name: String = "Fake",
    override val baseUrl: String = "https://fake.local",
    override val apiKey: String = "test-api-key",
    override val defaultModel: String = "fake-model",
) : ChatProvider {

    var lastRequestMessages: List<ChatMessage>? = null
    var lastStreamFlag: Boolean? = null
    var providedUsage: ChatUsage? = null
    var nextResponse: ChatResponseResult? = null

    override fun buildAuthHeaders(): Map<String, String> = mapOf("Authorization" to "Bearer $apiKey")

    override fun buildRequestBody(
        messages: List<ChatMessage>,
        model: String,
        stream: Boolean,
        temperature: Float?,
        maxTokens: Int?,
        systemPrompt: String?,
    ): String {
        lastRequestMessages = messages
        lastStreamFlag = stream
        return buildString {
            append("model=").append(model)
            append("|stream=").append(stream)
            temperature?.let { append("|temp=").append(it) }
            maxTokens?.let { append("|max=").append(it) }
            systemPrompt?.let { append("|system=").append(it) }
        }
    }

    override fun parseStreamLine(line: String): StreamEvent? = when {
        line.startsWith("delta:") -> StreamEvent.Delta(content = line.removePrefix("delta:").trim())
        line.startsWith("finish:") -> StreamEvent.Delta(
            content = "",
            finishReason = line.removePrefix("finish:").trim(),
            usage = providedUsage,
        )
        line.trim() == "done" -> StreamEvent.Done
        line.startsWith("error:") -> StreamEvent.Error(IllegalStateException(line.removePrefix("error:").trim()))
        else -> null
    }

    override fun parseResponse(body: String): ChatResponseResult {
        nextResponse?.let { return it.also { nextResponse = null } }

        val parts = body.split("|")
        val text = parts.firstOrNull { it.startsWith("text:") }?.removePrefix("text:") ?: body
        val finish = parts.firstOrNull { it.startsWith("finish:") }?.removePrefix("finish:")
        return ChatResponseResult(
            message = assistantMessage(text = text),
            usage = providedUsage,
            finishReason = finish?.let { FinishReason.fromString(it) },
        )
    }
}

/**
 * Fake HTTP engine that serves queued responses/flows to keep tests deterministic.
 */
internal class FakeHttpEngine : HttpEngine {
    private val streamQueue = ArrayDeque<Flow<SseEvent>>()
    private val responseQueue = ArrayDeque<HttpResult>()

    val recordedRequests = mutableListOf<HttpRequest>()

    fun enqueueStream(vararg events: SseEvent) {
        streamQueue += flow {
            events.forEach { emit(it) }
        }
    }

    fun enqueueResponse(result: HttpResult) {
        responseQueue += result
    }

    override suspend fun execute(request: HttpRequest): HttpResult {
        recordedRequests += request
        return responseQueue.removeFirstOrNull()
            ?: throw IllegalStateException("No queued HttpResult available")
    }

    override suspend fun executeStream(request: HttpRequest): Flow<SseEvent> {
        recordedRequests += request
        return streamQueue.removeFirstOrNull()
            ?: throw IllegalStateException("No queued stream Flow available")
    }

    override fun close() = Unit
}
