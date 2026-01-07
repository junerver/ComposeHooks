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
 * Tests for Anthropic API models serialization.
 *
 * TDD approach: Test serialization/deserialization logic.
 */
class AnthropicModelsTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        explicitNulls = false
    }

    // region Request Conversion Tests

    @Test
    fun testUserMessageToAnthropicFormat() {
        val messages = listOf(userMessage("Hello"))
        val anthropicMessages = messages.toAnthropicMessages()
        assertEquals(1, anthropicMessages.size)
        assertEquals("user", anthropicMessages[0].role)
        assertTrue(anthropicMessages[0].content is AnthropicMessageContent.Text)
        assertEquals("Hello", (anthropicMessages[0].content as AnthropicMessageContent.Text).text)
    }

    @Test
    fun testAssistantMessageToAnthropicFormat() {
        val messages = listOf(assistantMessage("I can help"))
        val anthropicMessages = messages.toAnthropicMessages()
        assertEquals(1, anthropicMessages.size)
        assertEquals("assistant", anthropicMessages[0].role)
    }

    @Test
    fun testSystemMessageFiltered() {
        val messages = listOf(
            systemMessage("You are helpful"),
            userMessage("Hello"),
        )
        val anthropicMessages = messages.toAnthropicMessages()
        // System messages should be filtered out
        assertEquals(1, anthropicMessages.size)
        assertEquals("user", anthropicMessages[0].role)
    }

    @Test
    fun testToolMessageToAnthropicFormat() {
        val messages = listOf(
            toolMessage(
                toolCallId = "call_123",
                toolName = "search",
                result = JsonPrimitive("result"),
            ),
        )
        val anthropicMessages = messages.toAnthropicMessages()
        assertEquals(1, anthropicMessages.size)
        assertEquals("user", anthropicMessages[0].role) // Tool results go as user messages
        assertTrue(anthropicMessages[0].content is AnthropicMessageContent.Parts)
    }

    @Test
    fun testMultimodalUserMessageToAnthropicFormat() {
        val content = listOf(
            TextPart("What's in this image?"),
            ImagePart.fromUrl("https://example.com/image.jpg"),
        )
        val messages = listOf(userMessage(content))
        val anthropicMessages = messages.toAnthropicMessages()
        assertEquals(1, anthropicMessages.size)
        assertTrue(anthropicMessages[0].content is AnthropicMessageContent.Parts)
        val parts = (anthropicMessages[0].content as AnthropicMessageContent.Parts).parts
        assertEquals(2, parts.size)
        assertTrue(parts[0] is AnthropicContentPart.Text)
        assertTrue(parts[1] is AnthropicContentPart.Image)
    }

    @Test
    fun testAssistantMessageWithToolCalls() {
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
        val anthropicMessages = messages.toAnthropicMessages()
        assertEquals(1, anthropicMessages.size)
        assertTrue(anthropicMessages[0].content is AnthropicMessageContent.Parts)
        val parts = (anthropicMessages[0].content as AnthropicMessageContent.Parts).parts
        assertTrue(parts.any { it is AnthropicContentPart.Text })
        assertTrue(parts.any { it is AnthropicContentPart.ToolUse })
    }

    @Test
    fun testAssistantMessageWithReasoning() {
        val content = listOf(
            ReasoningPart("Let me think..."),
            TextPart("The answer is 42"),
        )
        val messages = listOf(assistantMessage(content))
        val anthropicMessages = messages.toAnthropicMessages()
        assertEquals(1, anthropicMessages.size)
        assertTrue(anthropicMessages[0].content is AnthropicMessageContent.Parts)
        val parts = (anthropicMessages[0].content as AnthropicMessageContent.Parts).parts
        assertTrue(parts.any { it is AnthropicContentPart.Thinking })
        assertTrue(parts.any { it is AnthropicContentPart.Text })
    }

    // endregion

    // region Request Serialization Tests

    @Test
    fun testAnthropicRequestSerialization() {
        val messages = listOf(userMessage("Hello"))
        val request = AnthropicRequest(
            model = "claude-3-opus",
            messages = messages.toAnthropicMessages(),
            maxTokens = 4096,
            stream = true,
            system = "You are helpful",
            temperature = 0.7f,
        )
        val serialized = json.encodeToString(AnthropicRequest.serializer(), request)
        assertTrue(serialized.contains("\"model\":\"claude-3-opus\""))
        assertTrue(serialized.contains("\"max_tokens\":4096"))
        assertTrue(serialized.contains("\"stream\":true"))
        assertTrue(serialized.contains("\"system\":\"You are helpful\""))
        assertTrue(serialized.contains("\"temperature\":0.7"))
    }

    // endregion

    // region Response Deserialization Tests

    @Test
    fun testAnthropicResponseDeserialization() {
        val responseJson = """
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
        val response = json.decodeFromString<AnthropicResponse>(responseJson)
        assertEquals("msg_123", response.id)
        assertEquals("assistant", response.role)
        assertEquals("claude-3-opus", response.model)
        assertEquals("end_turn", response.stopReason)
        assertEquals(1, response.content.size)
        assertEquals("text", response.content[0].type)
        assertEquals("Hello from Claude!", response.content[0].text)
        assertEquals(15, response.usage.inputTokens)
        assertEquals(5, response.usage.outputTokens)
    }

    @Test
    fun testAnthropicResponseWithToolUse() {
        val responseJson = """
            {
                "id": "msg_456",
                "type": "message",
                "role": "assistant",
                "content": [
                    {"type": "text", "text": "Let me search for that."},
                    {"type": "tool_use", "id": "call_abc", "name": "search", "input": {"query": "kotlin"}}
                ],
                "model": "claude-3-opus",
                "stop_reason": "tool_use",
                "usage": {"input_tokens": 20, "output_tokens": 10}
            }
        """.trimIndent()
        val response = json.decodeFromString<AnthropicResponse>(responseJson)
        assertEquals(2, response.content.size)
        assertEquals("text", response.content[0].type)
        assertEquals("tool_use", response.content[1].type)
        assertEquals("call_abc", response.content[1].id)
        assertEquals("search", response.content[1].name)
        assertNotNull(response.content[1].input)
        assertEquals("tool_use", response.stopReason)
    }

    @Test
    fun testAnthropicResponseWithThinking() {
        val responseJson = """
            {
                "id": "msg_789",
                "type": "message",
                "role": "assistant",
                "content": [
                    {"type": "thinking", "thinking": "Let me analyze this..."},
                    {"type": "text", "text": "The answer is 42."}
                ],
                "model": "claude-3-opus",
                "stop_reason": "end_turn",
                "usage": {"input_tokens": 10, "output_tokens": 15}
            }
        """.trimIndent()
        val response = json.decodeFromString<AnthropicResponse>(responseJson)
        assertEquals(2, response.content.size)
        assertEquals("thinking", response.content[0].type)
        assertEquals("Let me analyze this...", response.content[0].thinking)
        assertEquals("text", response.content[1].type)
        assertEquals("The answer is 42.", response.content[1].text)
    }

    // endregion

    // region Streaming Response Tests

    @Test
    fun testAnthropicStreamEventContentBlockDelta() {
        val eventJson = """
            {
                "type": "content_block_delta",
                "index": 0,
                "delta": {"type": "text_delta", "text": "Hello"}
            }
        """.trimIndent()
        val event = json.decodeFromString<AnthropicStreamEvent>(eventJson)
        assertEquals("content_block_delta", event.type)
        assertEquals(0, event.index)
        assertEquals("text_delta", event.delta?.type)
        assertEquals("Hello", event.delta?.text)
    }

    @Test
    fun testAnthropicStreamEventMessageDelta() {
        val eventJson = """
            {
                "type": "message_delta",
                "delta": {"stop_reason": "end_turn"},
                "usage": {"input_tokens": 10, "output_tokens": 20}
            }
        """.trimIndent()
        val event = json.decodeFromString<AnthropicStreamEvent>(eventJson)
        assertEquals("message_delta", event.type)
        assertEquals("end_turn", event.delta?.stopReason)
        assertNotNull(event.usage)
        assertEquals(10, event.usage?.inputTokens)
        assertEquals(20, event.usage?.outputTokens)
    }

    @Test
    fun testAnthropicStreamEventMessageStart() {
        val eventJson = """
            {
                "type": "message_start",
                "message": {
                    "id": "msg_123",
                    "type": "message",
                    "role": "assistant",
                    "model": "claude-3-opus",
                    "usage": {"input_tokens": 10}
                }
            }
        """.trimIndent()
        val event = json.decodeFromString<AnthropicStreamEvent>(eventJson)
        assertEquals("message_start", event.type)
        assertNotNull(event.message)
        assertEquals("msg_123", event.message?.id)
        assertEquals("assistant", event.message?.role)
        assertEquals("claude-3-opus", event.message?.model)
    }

    @Test
    fun testAnthropicStreamEventContentBlockStart() {
        val eventJson = """
            {
                "type": "content_block_start",
                "index": 0,
                "content_block": {"type": "text", "text": ""}
            }
        """.trimIndent()
        val event = json.decodeFromString<AnthropicStreamEvent>(eventJson)
        assertEquals("content_block_start", event.type)
        assertEquals(0, event.index)
        assertNotNull(event.contentBlock)
        assertEquals("text", event.contentBlock?.type)
    }

    // endregion

    // region Error Response Tests

    @Test
    fun testAnthropicErrorResponseDeserialization() {
        val errorJson = """
            {
                "type": "error",
                "error": {
                    "type": "invalid_request_error",
                    "message": "Invalid API key"
                }
            }
        """.trimIndent()
        val errorResponse = json.decodeFromString<AnthropicErrorResponse>(errorJson)
        assertEquals("error", errorResponse.type)
        assertEquals("invalid_request_error", errorResponse.error.type)
        assertEquals("Invalid API key", errorResponse.error.message)
    }

    // endregion

    // region Image Source Tests

    @Test
    fun testAnthropicImageSourceFromBase64() {
        val source = AnthropicImageSource.fromBase64("base64data", "image/png")
        assertEquals("base64", source.type)
        assertEquals("image/png", source.mediaType)
        assertEquals("base64data", source.data)
        assertEquals("", source.url)
    }

    @Test
    fun testAnthropicImageSourceFromUrl() {
        val source = AnthropicImageSource.fromUrl("https://example.com/image.jpg")
        assertEquals("url", source.type)
        assertEquals("https://example.com/image.jpg", source.url)
        assertEquals("", source.mediaType)
        assertEquals("", source.data)
    }

    // endregion
}
