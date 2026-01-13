package xyz.junerver.compose.ai.usechat

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

/**
 * Tests for OpenAI API models serialization.
 *
 * TDD approach: Test serialization/deserialization logic.
 */
class OpenAIModelsTest {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        explicitNulls = false
    }

    // region Request Serialization Tests

    @Test
    fun testChatCompletionRequestSerialization() {
        val messages = listOf(userMessage("Hello"))
        val request = ChatCompletionRequest(
            model = "gpt-4",
            messages = messages.toRequestMessages(),
            stream = true,
            temperature = 0.7f,
            maxTokens = 100,
        )
        val serialized = json.encodeToString(ChatCompletionRequest.serializer(), request)
        assertTrue(serialized.contains("\"model\":\"gpt-4\""))
        assertTrue(serialized.contains("\"stream\":true"))
        assertTrue(serialized.contains("\"temperature\":0.7"))
        assertTrue(serialized.contains("\"max_tokens\":100"))
    }

    @Test
    fun testChatMessageRequestTextContent() {
        val messages = listOf(userMessage("Hello world"))
        val requestMessages = messages.toRequestMessages()
        assertEquals(1, requestMessages.size)
        assertEquals("user", requestMessages[0].role)
        assertTrue(requestMessages[0].content is ChatMessageContent.Text)
        assertEquals("Hello world", (requestMessages[0].content as ChatMessageContent.Text).text)
    }

    @Test
    fun testChatMessageRequestSystemMessage() {
        val messages = listOf(systemMessage("You are helpful"))
        val requestMessages = messages.toRequestMessages()
        assertEquals(1, requestMessages.size)
        assertEquals("system", requestMessages[0].role)
        assertTrue(requestMessages[0].content is ChatMessageContent.Text)
    }

    @Test
    fun testChatMessageRequestAssistantMessage() {
        val messages = listOf(assistantMessage("I can help"))
        val requestMessages = messages.toRequestMessages()
        assertEquals(1, requestMessages.size)
        assertEquals("assistant", requestMessages[0].role)
    }

    @Test
    fun testChatMessageRequestToolMessage() {
        val messages = listOf(
            toolMessage(
                toolCallId = "call_123",
                toolName = "search",
                result = JsonPrimitive("search result"),
            ),
        )
        val requestMessages = messages.toRequestMessages()
        assertEquals(1, requestMessages.size)
        assertEquals("tool", requestMessages[0].role)
        assertEquals("call_123", requestMessages[0].toolCallId)
    }

    @Test
    fun testChatMessageRequestWithToolCalls() {
        val content = listOf(
            TextPart("Let me search"),
            ToolCallPart(
                toolCallId = "call_456",
                toolName = "web_search",
                args = buildJsonObject {
                    put("query", JsonPrimitive("kotlin"))
                },
            ),
        )
        val messages = listOf(assistantMessage(content))
        val requestMessages = messages.toRequestMessages()
        assertEquals(1, requestMessages.size)
        assertNotNull(requestMessages[0].toolCalls)
        assertEquals(1, requestMessages[0].toolCalls?.size)
        assertEquals("call_456", requestMessages[0].toolCalls?.first()?.id)
        assertEquals("web_search", requestMessages[0].toolCalls?.first()?.function?.name)
    }

    @Test
    fun testMultimodalUserMessage() {
        val content = listOf(
            TextPart("What's in this image?"),
            ImagePart.fromUrl("https://example.com/image.jpg"),
        )
        val messages = listOf(userMessage(content))
        val requestMessages = messages.toRequestMessages()
        assertEquals(1, requestMessages.size)
        assertTrue(requestMessages[0].content is ChatMessageContent.Parts)
        val parts = (requestMessages[0].content as ChatMessageContent.Parts).parts
        assertEquals(2, parts.size)
        assertTrue(parts[0] is OpenAIContentPart.Text)
        assertTrue(parts[1] is OpenAIContentPart.ImageUrl)
    }

    @Test
    fun testImagePartBase64Conversion() {
        val content = listOf(
            TextPart("Describe"),
            ImagePart.fromBase64("base64data", "image/png"),
        )
        val messages = listOf(userMessage(content))
        val requestMessages = messages.toRequestMessages()
        val parts = (requestMessages[0].content as ChatMessageContent.Parts).parts
        val imagePart = parts[1] as OpenAIContentPart.ImageUrl
        assertTrue(imagePart.url.startsWith("data:image/png;base64,"))
        assertTrue(imagePart.url.contains("base64data"))
    }

    // endregion

    // region Response Deserialization Tests

    @Test
    fun testChatCompletionResponseDeserialization() {
        val responseJson =
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
                        "content": "Hello! How can I help?"
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
        val response = json.decodeFromString<ChatCompletionResponse>(responseJson)
        assertEquals("chatcmpl-123", response.id)
        assertEquals("gpt-4", response.model)
        assertEquals(1, response.choices.size)
        assertEquals("Hello! How can I help?", response.choices[0].message.content)
        assertEquals("stop", response.choices[0].finishReason)
        assertNotNull(response.usage)
        assertEquals(10, response.usage?.promptTokens)
        assertEquals(8, response.usage?.completionTokens)
        assertEquals(18, response.usage?.totalTokens)
    }

    @Test
    fun testChatCompletionResponseWithToolCalls() {
        val responseJson =
            """
            {
                "id": "chatcmpl-456",
                "object": "chat.completion",
                "created": 1677652288,
                "model": "gpt-4",
                "choices": [{
                    "index": 0,
                    "message": {
                        "role": "assistant",
                        "content": null,
                        "tool_calls": [{
                            "id": "call_abc",
                            "type": "function",
                            "function": {
                                "name": "get_weather",
                                "arguments": "{\"location\":\"Tokyo\"}"
                            }
                        }]
                    },
                    "finish_reason": "tool_calls"
                }]
            }
            """.trimIndent()
        val response = json.decodeFromString<ChatCompletionResponse>(responseJson)
        assertNull(response.choices[0].message.content)
        assertNotNull(response.choices[0].message.toolCalls)
        assertEquals(1, response.choices[0].message.toolCalls?.size)
        assertEquals("call_abc", response.choices[0].message.toolCalls?.first()?.id)
        assertEquals("get_weather", response.choices[0].message.toolCalls?.first()?.function?.name)
        assertEquals("tool_calls", response.choices[0].finishReason)
    }

    // endregion

    // region Streaming Response Tests

    @Test
    fun testChatCompletionChunkDeserialization() {
        val chunkJson =
            """
            {
                "id": "chatcmpl-123",
                "object": "chat.completion.chunk",
                "created": 1677652288,
                "model": "gpt-4",
                "choices": [{
                    "index": 0,
                    "delta": {
                        "content": "Hello"
                    },
                    "finish_reason": null
                }]
            }
            """.trimIndent()
        val chunk = json.decodeFromString<ChatCompletionChunk>(chunkJson)
        assertEquals("chatcmpl-123", chunk.id)
        assertEquals(1, chunk.choices?.size)
        assertEquals("Hello", chunk.choices?.first()?.delta?.content)
        assertNull(chunk.choices?.first()?.finishReason)
    }

    @Test
    fun testChatCompletionChunkWithRole() {
        val chunkJson =
            """
            {
                "id": "chatcmpl-123",
                "choices": [{
                    "delta": {
                        "role": "assistant",
                        "content": ""
                    }
                }]
            }
            """.trimIndent()
        val chunk = json.decodeFromString<ChatCompletionChunk>(chunkJson)
        assertEquals("assistant", chunk.choices?.first()?.delta?.role)
    }

    @Test
    fun testChatCompletionChunkWithFinishReason() {
        val chunkJson =
            """
            {
                "id": "chatcmpl-123",
                "choices": [{
                    "delta": {},
                    "finish_reason": "stop"
                }]
            }
            """.trimIndent()
        val chunk = json.decodeFromString<ChatCompletionChunk>(chunkJson)
        assertEquals("stop", chunk.choices?.first()?.finishReason)
    }

    @Test
    fun testChatCompletionChunkWithUsage() {
        val chunkJson =
            """
            {
                "id": "chatcmpl-123",
                "choices": [],
                "usage": {
                    "prompt_tokens": 10,
                    "completion_tokens": 20,
                    "total_tokens": 30
                }
            }
            """.trimIndent()
        val chunk = json.decodeFromString<ChatCompletionChunk>(chunkJson)
        assertNotNull(chunk.usage)
        assertEquals(10, chunk.usage?.promptTokens)
        assertEquals(20, chunk.usage?.completionTokens)
        assertEquals(30, chunk.usage?.totalTokens)
    }

    // endregion

    // region Error Response Tests

    @Test
    fun testOpenAIErrorResponseDeserialization() {
        val errorJson =
            """
            {
                "error": {
                    "message": "Invalid API key",
                    "type": "invalid_request_error",
                    "param": null,
                    "code": "invalid_api_key"
                }
            }
            """.trimIndent()
        val errorResponse = json.decodeFromString<OpenAIErrorResponse>(errorJson)
        assertEquals("Invalid API key", errorResponse.error.message)
        assertEquals("invalid_request_error", errorResponse.error.type)
        assertEquals("invalid_api_key", errorResponse.error.code)
    }

    @Test
    fun testOpenAIExceptionCreation() {
        val exception = OpenAIException(
            errorMessage = "Rate limit exceeded",
            errorType = "rate_limit_error",
            errorCode = "rate_limit",
        )
        assertEquals("Rate limit exceeded", exception.message)
        assertEquals("rate_limit_error", exception.errorType)
        assertEquals("rate_limit", exception.errorCode)
    }

    // endregion
}
