package xyz.junerver.compose.ai.usechat

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import xyz.junerver.compose.ai.useagent.ToolChoice
import xyz.junerver.compose.ai.useagent.tool
import xyz.junerver.compose.ai.useagent.toolText

/**
 * Tests for ChatProvider implementations.
 *
 * TDD approach: Test parsing and building logic without network calls.
 */
class ChatProviderTest {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // region OpenAI Compatible Provider Tests

    @Test
    fun testOpenAIBuildAuthHeaders() {
        val provider = Providers.OpenAI(apiKey = "sk-test-key")
        val headers = provider.buildAuthHeaders()
        assertEquals("Bearer sk-test-key", headers["Authorization"])
    }

    @Test
    fun testOpenAIBuildAuthHeadersEmptyKey() {
        val provider = Providers.OpenAI(apiKey = "")
        val headers = provider.buildAuthHeaders()
        assertTrue(headers.isEmpty())
    }

    @Test
    fun testOpenAIParseStreamLineDone() {
        val provider = Providers.OpenAI(apiKey = "test")
        val result = provider.parseStreamLine("data: [DONE]")
        assertTrue(result is StreamEvent.Done)
    }

    @Test
    fun testOpenAIParseStreamLineBlank() {
        val provider = Providers.OpenAI(apiKey = "test")
        val result = provider.parseStreamLine("")
        assertNull(result)
    }

    @Test
    fun testOpenAIParseStreamLineWithoutDataPrefix() {
        val provider = Providers.OpenAI(apiKey = "test")
        val result = provider.parseStreamLine("event: message")
        assertNull(result)
    }

    @Test
    fun testOpenAIParseStreamLineWithContent() {
        val provider = Providers.OpenAI(apiKey = "test")
        val line = """data: {"id":"chatcmpl-123","object":"chat.completion.chunk","choices":[{"index":0,"delta":{"content":"Hello"},"finish_reason":null}]}"""
        val result = provider.parseStreamLine(line)
        assertTrue(result is StreamEvent.Delta)
        assertEquals("Hello", (result as StreamEvent.Delta).content)
    }

    @Test
    fun testOpenAIParseStreamLineWithRole() {
        val provider = Providers.OpenAI(apiKey = "test")
        val line = """data: {"id":"chatcmpl-123","choices":[{"delta":{"role":"assistant","content":""},"finish_reason":null}]}"""
        val result = provider.parseStreamLine(line)
        assertTrue(result is StreamEvent.Delta)
        assertEquals("assistant", (result as StreamEvent.Delta).role)
    }

    @Test
    fun testOpenAIParseStreamLineWithFinishReason() {
        val provider = Providers.OpenAI(apiKey = "test")
        val line = """data: {"id":"chatcmpl-123","choices":[{"delta":{},"finish_reason":"stop"}]}"""
        val result = provider.parseStreamLine(line)
        assertTrue(result is StreamEvent.Delta)
        assertEquals("stop", (result as StreamEvent.Delta).finishReason)
    }

    @Test
    fun testOpenAIParseStreamLineWithToolCallDelta() {
        val provider = Providers.OpenAI(apiKey = "test")
        val line =
            """data: {"id":"chatcmpl-123","object":"chat.completion.chunk","choices":[{"index":0,"delta":{"tool_calls":[{"index":0,"id":"call_1","type":"function","function":{"name":"get_weather","arguments":"{\"city\":\"Pa\""}}]}}]}"""
        val result = provider.parseStreamLine(line)
        assertTrue(result is StreamEvent.ToolCallDelta)
        val delta = result as StreamEvent.ToolCallDelta
        assertEquals(0, delta.index)
        assertEquals("call_1", delta.toolCallId)
        assertEquals("get_weather", delta.toolName)
        assertTrue(delta.argumentsDelta?.contains("\"city\"") == true)
    }

    @Test
    fun testOpenAIParseStreamLineWithContentAndToolCallsReturnsMulti() {
        val provider = Providers.OpenAI(apiKey = "test")
        val line =
            """data: {"id":"chatcmpl-123","object":"chat.completion.chunk","choices":[{"index":0,"delta":{"content":"Hi","tool_calls":[{"index":0,"id":"call_1","type":"function","function":{"name":"get_weather","arguments":"{\"city\":\"Paris\"}"}}]}}]}"""
        val result = provider.parseStreamLine(line)
        assertTrue(result is StreamEvent.Multi)
        val multi = result as StreamEvent.Multi
        assertTrue(multi.events.any { it is StreamEvent.Delta && (it as StreamEvent.Delta).content == "Hi" })
        assertTrue(multi.events.any { it is StreamEvent.ToolCallDelta && (it as StreamEvent.ToolCallDelta).toolCallId == "call_1" })
    }

    @Test
    fun testOpenAIParseStreamLineMalformedJson() {
        val provider = Providers.OpenAI(apiKey = "test")
        val result = provider.parseStreamLine("data: {invalid json}")
        assertNull(result)
    }

    @Test
    fun testOpenAIParseResponse() {
        val provider = Providers.OpenAI(apiKey = "test")
        val responseBody =
            """
            {
                "id": "chatcmpl-123",
                "object": "chat.completion",
                "created": 1677652288,
                "model": "gpt-4",
                "choices": [{
                    "index": 0,
                    "message": {
                        "role": "assistant",
                        "content": "Hello! How can I help you?"
                    },
                    "finish_reason": "stop"
                }],
                "usage": {
                    "prompt_tokens": 10,
                    "completion_tokens": 8,
                    "total_tokens": 18
                }
            }
            """.trimIndent()
        val result = provider.parseResponse(responseBody)
        assertEquals("Hello! How can I help you?", result.message.textContent)
        assertEquals(FinishReason.Stop, result.finishReason)
        assertNotNull(result.usage)
        assertEquals(10, result.usage?.promptTokens)
        assertEquals(8, result.usage?.completionTokens)
    }

    @Test
    fun testOpenAIParseResponseWithToolCalls() {
        val provider = Providers.OpenAI(apiKey = "test")
        val responseBody =
            """
            {
                "id": "chatcmpl-123",
                "object": "chat.completion",
                "created": 1677652288,
                "model": "gpt-4",
                "choices": [{
                    "index": 0,
                    "message": {
                        "role": "assistant",
                        "content": null,
                        "tool_calls": [{
                            "id": "call_1",
                            "type": "function",
                            "function": {
                                "name": "get_weather",
                                "arguments": "{\"city\":\"Paris\"}"
                            }
                        }]
                    },
                    "finish_reason": "tool_calls"
                }],
                "usage": {
                    "prompt_tokens": 10,
                    "completion_tokens": 8,
                    "total_tokens": 18
                }
            }
            """.trimIndent()
        val result = provider.parseResponse(responseBody)
        assertTrue(result.message.hasToolCalls)
        assertEquals(1, result.message.toolCalls.size)
        assertEquals("get_weather", result.message.toolCalls[0].toolName)
        assertEquals("Paris", result.message.toolCalls[0].args["city"]?.jsonPrimitive?.content)
        assertEquals(FinishReason.ToolCalls, result.finishReason)
    }

    @Test
    fun testOpenAIBuildRequestBody() {
        val provider = Providers.OpenAI(apiKey = "test")
        val messages = listOf(userMessage("Hello"))
        val body = provider.buildRequestBody(
            messages = messages,
            model = "gpt-4",
            stream = true,
            temperature = 0.7f,
            maxTokens = 100,
            systemPrompt = "You are helpful",
        )
        // Verify it's valid JSON
        val parsed = json.parseToJsonElement(body)
        assertTrue(body.contains("\"model\":\"gpt-4\""))
        assertTrue(body.contains("\"stream\":true"))
        assertTrue(body.contains("\"temperature\":0.7"))
        assertTrue(body.contains("\"max_tokens\":100"))
        // System prompt should be in messages
        assertTrue(body.contains("You are helpful"))
    }

    @Test
    fun testOpenAIBuildRequestBodyWithToolsAndSpecificToolChoice() {
        val provider = Providers.OpenAI(apiKey = "test")
        val messages = listOf(userMessage("Hello"))
        val tool = toolText<WeatherParams>(
            name = "get_weather",
            description = "Get the current weather for a city",
            parameters = buildJsonObject {
                put("type", JsonPrimitive("object"))
                put(
                    "properties",
                    buildJsonObject {
                        put(
                            "city",
                            buildJsonObject {
                                put("type", JsonPrimitive("string"))
                            },
                        )
                    },
                )
            },
        ) { _ ->
            "ok"
        }

        val body = provider.buildRequestBody(
            messages = messages,
            model = "gpt-4",
            stream = false,
            temperature = null,
            maxTokens = null,
            systemPrompt = null,
            tools = listOf(tool),
            toolChoice = ToolChoice.Specific(name = "get_weather"),
        )

        val parsed = json.parseToJsonElement(body).jsonObject
        assertNotNull(parsed["tools"])
        val toolChoice = parsed["tool_choice"]
        assertNotNull(toolChoice)
        assertTrue(toolChoice is JsonObject)
        assertEquals("function", toolChoice.jsonObject["type"]?.jsonPrimitive?.content)
        assertEquals("get_weather", toolChoice.jsonObject["function"]?.jsonObject?.get("name")?.jsonPrimitive?.content)
    }

    // endregion

    // region Anthropic Provider Tests

    @Test
    fun testAnthropicBuildAuthHeaders() {
        val provider = Providers.Anthropic(apiKey = "sk-ant-test")
        val headers = provider.buildAuthHeaders()
        assertEquals("sk-ant-test", headers["x-api-key"])
        assertEquals("2023-06-01", headers["anthropic-version"])
    }

    @Test
    fun testAnthropicChatEndpoint() {
        val provider = Providers.Anthropic(apiKey = "test")
        assertEquals("/v1/messages", provider.chatEndpoint)
    }

    @Test
    fun testAnthropicParseStreamLineEventMessageStop() {
        val provider = Providers.Anthropic(apiKey = "test")
        val result = provider.parseStreamLine("event: message_stop")
        assertTrue(result is StreamEvent.Done)
    }

    @Test
    fun testAnthropicParseStreamLineContentBlockDelta() {
        val provider = Providers.Anthropic(apiKey = "test")
        val line = """data: {"type":"content_block_delta","index":0,"delta":{"type":"text_delta","text":"Hello"}}"""
        val result = provider.parseStreamLine(line)
        assertTrue(result is StreamEvent.Delta)
        assertEquals("Hello", (result as StreamEvent.Delta).content)
    }

    @Test
    fun testAnthropicParseStreamLineMessageDelta() {
        val provider = Providers.Anthropic(apiKey = "test")
        val line = """data: {"type":"message_delta","delta":{"stop_reason":"end_turn"},"usage":{"input_tokens":10,"output_tokens":20}}"""
        val result = provider.parseStreamLine(line)
        assertTrue(result is StreamEvent.Delta)
        val delta = result as StreamEvent.Delta
        assertEquals("end_turn", delta.finishReason)
        assertNotNull(delta.usage)
        assertEquals(10, delta.usage?.promptTokens)
        assertEquals(20, delta.usage?.completionTokens)
    }

    @Test
    fun testAnthropicParseStreamLineToolUseStart() {
        val provider = Providers.Anthropic(apiKey = "test")
        val line =
            """data: {"type":"content_block_start","index":0,"content_block":{"type":"tool_use","id":"toolu_1","name":"search"}}"""
        val result = provider.parseStreamLine(line)
        assertTrue(result is StreamEvent.ToolCallDelta)
        val delta = result as StreamEvent.ToolCallDelta
        assertEquals(0, delta.index)
        assertEquals("toolu_1", delta.toolCallId)
        assertEquals("search", delta.toolName)
    }

    @Test
    fun testAnthropicParseStreamLineToolUsePartialJsonDelta() {
        val provider = Providers.Anthropic(apiKey = "test")
        val line =
            """data: {"type":"content_block_delta","index":0,"delta":{"type":"input_json_delta","partial_json":"{\"query\":\"k\""}}"""
        val result = provider.parseStreamLine(line)
        assertTrue(result is StreamEvent.ToolCallDelta)
        val delta = result as StreamEvent.ToolCallDelta
        assertEquals(0, delta.index)
        assertTrue(delta.argumentsDelta?.contains("\"query\"") == true)
    }

    @Test
    fun testAnthropicParseStreamLineThinkingDelta() {
        val provider = Providers.Anthropic(apiKey = "test")
        val line =
            """data: {"type":"content_block_delta","index":0,"delta":{"type":"thinking_delta","thinking":"Let me think"}}"""
        val result = provider.parseStreamLine(line)
        assertTrue(result is StreamEvent.ReasoningDelta)
        assertEquals("Let me think", (result as StreamEvent.ReasoningDelta).text)
    }

    @Test
    fun testAnthropicParseResponse() {
        val provider = Providers.Anthropic(apiKey = "test")
        val responseBody =
            """
            {
                "id": "msg_123",
                "type": "message",
                "role": "assistant",
                "content": [{"type": "text", "text": "Hello from Claude!"}],
                "model": "claude-3-opus",
                "stop_reason": "end_turn",
                "usage": {"input_tokens": 15, "output_tokens": 5}
            }
            """.trimIndent()
        val result = provider.parseResponse(responseBody)
        assertEquals("Hello from Claude!", result.message.textContent)
        assertEquals(FinishReason.Stop, result.finishReason)
        assertNotNull(result.usage)
        assertEquals(15, result.usage?.promptTokens)
        assertEquals(5, result.usage?.completionTokens)
        assertEquals(20, result.usage?.totalTokens)
    }

    @Test
    fun testAnthropicParseResponseWithMultipleBlocks() {
        val provider = Providers.Anthropic(apiKey = "test")
        val responseBody =
            """
            {
                "id": "msg_123",
                "type": "message",
                "role": "assistant",
                "content": [
                    {"type": "thinking", "thinking": "Let me think..."},
                    {"type": "text", "text": "Answer "},
                    {"type": "text", "text": "42"},
                    {"type": "tool_use", "id": "toolu_1", "name": "search", "input": {"query": "kotlin"}}
                ],
                "model": "claude-3-opus",
                "stop_reason": "end_turn",
                "usage": {"input_tokens": 10, "output_tokens": 20}
            }
            """.trimIndent()
        val result = provider.parseResponse(responseBody)
        assertEquals("Answer 42", result.message.textContent)
        assertEquals("Let me think...", result.message.reasoningContent)
        assertTrue(result.message.hasToolCalls)
        assertEquals("search", result.message.toolCalls[0].toolName)
    }

    @Test
    fun testAnthropicBuildRequestBody() {
        val provider = Providers.Anthropic(apiKey = "test")
        val messages = listOf(userMessage("Hello"))
        val body = provider.buildRequestBody(
            messages = messages,
            model = "claude-3-opus",
            stream = true,
            temperature = 0.5f,
            maxTokens = 200,
            systemPrompt = "Be concise",
        )
        // Verify it's valid JSON
        val parsed = json.parseToJsonElement(body)
        assertTrue(body.contains("\"model\":\"claude-3-opus\""))
        assertTrue(body.contains("\"stream\":true"))
        // Anthropic uses separate system field
        assertTrue(body.contains("\"system\":\"Be concise\""))
        // max_tokens is required for Anthropic
        assertTrue(body.contains("\"max_tokens\":200"))
    }

    // endregion

    // region Provider Defaults Tests

    @Test
    fun testDeepSeekDefaults() {
        val provider = Providers.DeepSeek(apiKey = "test")
        assertEquals("DeepSeek", provider.name)
        assertEquals("https://api.deepseek.com", provider.baseUrl)
        assertEquals("deepseek-chat", provider.defaultModel)
    }

    @Test
    fun testMoonshotDefaults() {
        val provider = Providers.Moonshot(apiKey = "test")
        assertEquals("Moonshot", provider.name)
        assertEquals("https://api.moonshot.cn/v1", provider.baseUrl)
        assertEquals("moonshot-v1-8k", provider.defaultModel)
    }

    @Test
    fun testZhipuDefaults() {
        val provider = Providers.Zhipu(apiKey = "test")
        assertEquals("Zhipu", provider.name)
        assertEquals("https://open.bigmodel.cn/api/paas/v4", provider.baseUrl)
        assertEquals("glm-4-flash", provider.defaultModel)
    }

    @Test
    fun testQwenDefaults() {
        val provider = Providers.Qwen(apiKey = "test")
        assertEquals("Qwen", provider.name)
        assertEquals("https://dashscope.aliyuncs.com/compatible-mode/v1", provider.baseUrl)
        assertEquals("qwen-turbo", provider.defaultModel)
    }

    @Test
    fun testGroqDefaults() {
        val provider = Providers.Groq(apiKey = "test")
        assertEquals("Groq", provider.name)
        assertEquals("https://api.groq.com/openai/v1", provider.baseUrl)
        assertEquals("llama-3.1-70b-versatile", provider.defaultModel)
    }

    @Test
    fun testCustomProvider() {
        val provider = Providers.Custom(
            name = "Local LLM",
            baseUrl = "http://localhost:11434/v1",
            defaultModel = "llama3",
            apiKey = "",
        )
        assertEquals("Local LLM", provider.name)
        assertEquals("http://localhost:11434/v1", provider.baseUrl)
        assertEquals("llama3", provider.defaultModel)
        assertTrue(provider.buildAuthHeaders().isEmpty())
    }

    @Test
    fun testAnthropicCompatibleCustomAuthHeaders() {
        val provider = Providers.AnthropicCompatible(
            baseUrl = "https://proxy.example.com",
            defaultModel = "claude-3-opus",
            apiKey = "test-token",
            apiKeyHeader = "Authorization",
            apiKeyPrefix = "Bearer",
            anthropicVersion = null,
        )
        val headers = provider.buildAuthHeaders()
        assertEquals("Bearer test-token", headers["Authorization"])
        assertNull(headers["anthropic-version"])
    }

    // endregion
}

@Serializable
private data class WeatherParams(val city: String)
