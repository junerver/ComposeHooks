package xyz.junerver.compose.ai.usechat

import kotlinx.serialization.json.Json

/*
  Description: Chat provider abstraction for multi-vendor support
  Author: Junerver
  Date: 2024
  Email: junerver@gmail.com
  Version: v2.0
*/

/**
 * Chat provider interface defining vendor-specific configurations.
 *
 * Different AI providers (OpenAI, Anthropic, DeepSeek, etc.) have different:
 * - API endpoints
 * - Authentication methods
 * - Request/response formats
 *
 * This interface abstracts these differences, allowing [useChat] to work
 * with any compatible provider.
 */
interface ChatProvider {
    /** Provider display name */
    val name: String

    /** Base URL for API requests */
    val baseUrl: String

    /** API key for authentication */
    val apiKey: String

    /** Default model for this provider */
    val defaultModel: String

    /** Chat completions endpoint path */
    val chatEndpoint: String get() = "/chat/completions"

    /**
     * Builds authentication headers for API requests.
     *
     * @return Map of HTTP headers
     */
    fun buildAuthHeaders(): Map<String, String>

    /**
     * Builds the request body for chat completions.
     *
     * @param messages List of messages to send
     * @param model Model identifier
     * @param stream Whether to use streaming
     * @param temperature Sampling temperature
     * @param maxTokens Maximum tokens to generate
     * @param systemPrompt Optional system prompt
     * @return Serialized JSON request body
     */
    fun buildRequestBody(
        messages: List<Message>,
        model: String,
        stream: Boolean,
        temperature: Float?,
        maxTokens: Int?,
        systemPrompt: String?,
    ): String

    /**
     * Parses a streaming response line into a [StreamEvent].
     *
     * @param line Raw line from SSE stream
     * @return Parsed event or null if line should be skipped
     */
    fun parseStreamLine(line: String): StreamEvent?

    /**
     * Parses a non-streaming response body into a [Message].
     *
     * @param body Response body JSON
     * @return Parsed assistant message
     */
    fun parseResponse(body: String): ChatResponseResult
}

/**
 * Result of parsing a chat response.
 */
data class ChatResponseResult(
    val message: Message,
    val usage: ChatUsage? = null,
    val finishReason: FinishReason? = null,
)

/**
 * Pre-configured chat providers.
 *
 * Most providers are OpenAI-compatible, using the same API format.
 * Some providers (like Anthropic) require custom implementations.
 *
 * ## Usage
 * ```kotlin
 * // Create a provider instance with API key
 * val provider = Providers.DeepSeek(apiKey = "sk-xxx")
 *
 * useChat {
 *     this.provider = provider
 * }
 * ```
 */
sealed class Providers : ChatProvider {
    companion object {
        internal val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        }
    }

    // region OpenAI Compatible Providers

    /**
     * Base class for OpenAI-compatible providers.
     *
     * Most providers follow the OpenAI API format with Bearer token authentication.
     */
    abstract class OpenAICompatible(
        override val name: String,
        override val baseUrl: String,
        override val apiKey: String,
        override val defaultModel: String,
    ) : Providers() {
        override fun buildAuthHeaders(): Map<String, String> = if (apiKey.isNotBlank()) {
            mapOf("Authorization" to "Bearer $apiKey")
        } else {
            emptyMap()
        }

        override fun buildRequestBody(
            messages: List<Message>,
            model: String,
            stream: Boolean,
            temperature: Float?,
            maxTokens: Int?,
            systemPrompt: String?,
        ): String {
            val allMessages = buildList {
                systemPrompt?.let { add(Message.system(it)) }
                addAll(messages)
            }
            val request = ChatCompletionRequest(
                model = model,
                messages = allMessages.toRequestMessages(),
                stream = stream,
                temperature = temperature,
                maxTokens = maxTokens,
            )
            return json.encodeToString(ChatCompletionRequest.serializer(), request)
        }

        override fun parseStreamLine(line: String): StreamEvent? {
            if (line.isBlank()) return null
            if (!line.startsWith("data: ")) return null

            val data = line.removePrefix("data: ").trim()
            if (data == "[DONE]") return StreamEvent.Done

            return try {
                val chunk = json.decodeFromString<ChatCompletionChunk>(data)
                val choice = chunk.choices?.firstOrNull()
                val delta = choice?.delta
                val content = delta?.content ?: ""
                val role = delta?.role
                val finishReason = choice?.finishReason

                if (content.isNotEmpty() || role != null || finishReason != null) {
                    StreamEvent.Delta(
                        content = content,
                        role = role,
                        finishReason = finishReason,
                        usage = chunk.usage,
                    )
                } else {
                    null
                }
            } catch (e: Exception) {
                null // Skip malformed JSON
            }
        }

        override fun parseResponse(body: String): ChatResponseResult {
            val response = json.decodeFromString<ChatCompletionResponse>(body)
            val choice = response.choices.firstOrNull()
                ?: throw Exception("No choices in response")
            return ChatResponseResult(
                message = Message.assistant(content = choice.message.content ?: ""),
                usage = response.usage,
                finishReason = choice.finishReason?.let { FinishReason.fromString(it) },
            )
        }
    }

    /** OpenAI official API */
    data class OpenAI(
        override val apiKey: String,
        override val baseUrl: String = "https://api.openai.com/v1",
        override val defaultModel: String = "gpt-4o-mini",
    ) : OpenAICompatible(
            name = "OpenAI",
            baseUrl = baseUrl,
            apiKey = apiKey,
            defaultModel = defaultModel,
        )

    /** DeepSeek - OpenAI compatible */
    data class DeepSeek(
        override val apiKey: String,
        override val baseUrl: String = "https://api.deepseek.com",
        override val defaultModel: String = "deepseek-chat",
    ) : OpenAICompatible(
            name = "DeepSeek",
            baseUrl = baseUrl,
            apiKey = apiKey,
            defaultModel = defaultModel,
        )

    /** Moonshot (月之暗面) - OpenAI compatible */
    data class Moonshot(
        override val apiKey: String,
        override val baseUrl: String = "https://api.moonshot.cn/v1",
        override val defaultModel: String = "moonshot-v1-8k",
    ) : OpenAICompatible(
            name = "Moonshot",
            baseUrl = baseUrl,
            apiKey = apiKey,
            defaultModel = defaultModel,
        )

    /** Zhipu GLM (智谱) - OpenAI compatible */
    data class Zhipu(
        override val apiKey: String,
        override val baseUrl: String = "https://open.bigmodel.cn/api/paas/v4",
        override val defaultModel: String = "glm-4-flash",
    ) : OpenAICompatible(
            name = "Zhipu",
            baseUrl = baseUrl,
            apiKey = apiKey,
            defaultModel = defaultModel,
        )

    /** Qwen (通义千问) - OpenAI compatible */
    data class Qwen(
        override val apiKey: String,
        override val baseUrl: String = "https://dashscope.aliyuncs.com/compatible-mode/v1",
        override val defaultModel: String = "qwen-turbo",
    ) : OpenAICompatible(
            name = "Qwen",
            baseUrl = baseUrl,
            apiKey = apiKey,
            defaultModel = defaultModel,
        )

    /** Groq - OpenAI compatible, fast inference */
    data class Groq(
        override val apiKey: String,
        override val baseUrl: String = "https://api.groq.com/openai/v1",
        override val defaultModel: String = "llama-3.1-70b-versatile",
    ) : OpenAICompatible(
            name = "Groq",
            baseUrl = baseUrl,
            apiKey = apiKey,
            defaultModel = defaultModel,
        )

    /** Together AI - OpenAI compatible */
    data class Together(
        override val apiKey: String,
        override val baseUrl: String = "https://api.together.xyz/v1",
        override val defaultModel: String = "meta-llama/Llama-3-70b-chat-hf",
    ) : OpenAICompatible(
            name = "Together",
            baseUrl = baseUrl,
            apiKey = apiKey,
            defaultModel = defaultModel,
        )

    /** Xiaomi MiMo - OpenAI compatible */
    data class MiMo(
        override val apiKey: String,
        override val baseUrl: String = "https://api.xiaomimimo.com/v1",
        override val defaultModel: String = "mimo-v2-flash",
    ) : OpenAICompatible(
            name = "MiMo",
            baseUrl = baseUrl,
            apiKey = apiKey,
            defaultModel = defaultModel,
        )

    // endregion

    // region Anthropic

    /**
     * Anthropic Claude API.
     *
     * Uses different authentication and message format from OpenAI.
     * - Auth: `x-api-key` header instead of Bearer token
     * - Messages: `system` is a separate field, not in messages array
     * - Streaming: Different SSE event format
     */
    data class Anthropic(
        override val apiKey: String,
        override val baseUrl: String = "https://api.anthropic.com",
        override val defaultModel: String = "claude-sonnet-4-20250514",
    ) : Providers() {
        override val name: String = "Anthropic"
        override val chatEndpoint: String = "/v1/messages"

        override fun buildAuthHeaders(): Map<String, String> = mapOf(
            "x-api-key" to apiKey,
            "anthropic-version" to "2023-06-01",
        )

        override fun buildRequestBody(
            messages: List<Message>,
            model: String,
            stream: Boolean,
            temperature: Float?,
            maxTokens: Int?,
            systemPrompt: String?,
        ): String {
            // Anthropic: filter out system messages, they go in separate field
            val userMessages = messages.filter { it.role != Role.System }
            val request = AnthropicRequest(
                model = model,
                messages = userMessages.map { AnthropicMessage(it.role.value, it.content) },
                stream = stream,
                system = systemPrompt,
                temperature = temperature,
                maxTokens = maxTokens ?: 4096, // Anthropic requires max_tokens
            )
            return json.encodeToString(AnthropicRequest.serializer(), request)
        }

        override fun parseStreamLine(line: String): StreamEvent? {
            if (line.isBlank()) return null

            // Anthropic SSE format:
            // event: message_start
            // data: {...}
            // event: content_block_delta
            // data: {"type":"content_block_delta","index":0,"delta":{"type":"text_delta","text":"Hello"}}
            // event: message_stop

            if (line.startsWith("event: ")) {
                val eventType = line.removePrefix("event: ").trim()
                return when (eventType) {
                    "message_stop" -> StreamEvent.Done
                    else -> null // Wait for data line
                }
            }

            if (!line.startsWith("data: ")) return null

            val data = line.removePrefix("data: ").trim()
            return try {
                val event = json.decodeFromString<AnthropicStreamEvent>(data)
                when (event.type) {
                    "content_block_delta" -> {
                        val text = event.delta?.text ?: ""
                        if (text.isNotEmpty()) {
                            StreamEvent.Delta(content = text)
                        } else {
                            null
                        }
                    }
                    "message_delta" -> {
                        val usage = event.usage?.let {
                            ChatUsage(
                                promptTokens = it.inputTokens ?: 0,
                                completionTokens = it.outputTokens ?: 0,
                                totalTokens = (it.inputTokens ?: 0) + (it.outputTokens ?: 0),
                            )
                        }
                        val finishReason = event.delta?.stopReason
                        if (usage != null || finishReason != null) {
                            StreamEvent.Delta(
                                content = "",
                                finishReason = finishReason,
                                usage = usage,
                            )
                        } else {
                            null
                        }
                    }
                    else -> null
                }
            } catch (e: Exception) {
                null
            }
        }

        override fun parseResponse(body: String): ChatResponseResult {
            val response = json.decodeFromString<AnthropicResponse>(body)
            val content = response.content.firstOrNull()?.text ?: ""
            return ChatResponseResult(
                message = Message.assistant(content = content),
                usage = ChatUsage(
                    promptTokens = response.usage.inputTokens,
                    completionTokens = response.usage.outputTokens,
                    totalTokens = response.usage.inputTokens + response.usage.outputTokens,
                ),
                finishReason = response.stopReason?.let { FinishReason.fromString(it) },
            )
        }
    }

    // endregion

    // region Custom Provider

    /**
     * Custom provider for OpenAI-compatible APIs.
     *
     * Use this for:
     * - Local LLM servers (Ollama, LM Studio, etc.)
     * - Proxy services
     * - Other OpenAI-compatible APIs not in the preset list
     *
     * ## Usage
     * ```kotlin
     * useChat {
     *     provider = Providers.Custom(
     *         name = "Local Ollama",
     *         baseUrl = "http://localhost:11434/v1",
     *         defaultModel = "llama3",
     *         apiKey = "", // Local may not need API key
     *     )
     * }
     * ```
     */
    data class Custom(
        override val name: String,
        override val baseUrl: String,
        override val defaultModel: String,
        override val apiKey: String = "",
        override val chatEndpoint: String = "/chat/completions",
    ) : OpenAICompatible(name, baseUrl, apiKey, defaultModel)

    // endregion
}
