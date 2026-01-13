package xyz.junerver.compose.ai.usechat

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

/**
 * Tests for Message models and factory functions.
 *
 * TDD approach: Define expected behavior first, then verify implementation.
 */
class MessageTest {
    // region UserMessage Tests

    @Test
    fun testUserMessageWithText() {
        val msg = userMessage("Hello, world!")
        assertEquals("Hello, world!", msg.textContent)
        assertTrue(msg.isTextOnly)
        assertFalse(msg.hasImages)
        assertFalse(msg.hasFiles)
        assertEquals(1, msg.content.size)
        assertTrue(msg.content.first() is TextPart)
    }

    @Test
    fun testUserMessageWithCustomId() {
        val msg = userMessage("Test", id = "custom-id-123")
        assertEquals("custom-id-123", msg.id)
    }

    @Test
    fun testUserMessageWithMetadata() {
        val metadata = mapOf("key" to "value", "source" to "test")
        val msg = userMessage("Test", metadata = metadata)
        assertEquals(metadata, msg.metadata)
    }

    @Test
    fun testUserMessageWithImage() {
        val msg = userMessageWithImage(
            text = "What's in this image?",
            imageBase64 = "base64data",
            mimeType = "image/png",
        )
        assertEquals("What's in this image?", msg.textContent)
        assertTrue(msg.hasImages)
        assertFalse(msg.isTextOnly)
        assertEquals(2, msg.content.size)
    }

    @Test
    fun testUserMessageWithImageUrl() {
        val msg = userMessageWithImageUrl(
            text = "Describe this",
            imageUrl = "https://example.com/image.jpg",
        )
        assertTrue(msg.hasImages)
        val imagePart = msg.content.filterIsInstance<ImagePart>().first()
        assertTrue(imagePart.isUrl)
        assertEquals("https://example.com/image.jpg", imagePart.data)
    }

    @Test
    fun testUserMessageWithFile() {
        val msg = userMessageWithFile(
            text = "Analyze this PDF",
            fileBase64 = "pdfdata",
            mimeType = "application/pdf",
            fileName = "document.pdf",
        )
        assertTrue(msg.hasFiles)
        val filePart = msg.content.filterIsInstance<FilePart>().first()
        assertEquals("application/pdf", filePart.mimeType)
        assertEquals("document.pdf", filePart.fileName)
    }

    @Test
    fun testUserMessageWithMultipleParts() {
        val content = listOf(
            TextPart("First text"),
            ImagePart.fromUrl("https://example.com/1.jpg"),
            TextPart("Second text"),
            ImagePart.fromBase64("base64", "image/jpeg"),
        )
        val msg = userMessage(content)
        assertEquals("First textSecond text", msg.textContent)
        assertTrue(msg.hasImages)
        assertFalse(msg.isTextOnly)
    }

    // endregion

    // region AssistantMessage Tests

    @Test
    fun testAssistantMessageWithText() {
        val msg = assistantMessage("I'm here to help!")
        assertEquals("I'm here to help!", msg.textContent)
        assertFalse(msg.hasToolCalls)
        assertFalse(msg.hasReasoning)
        assertNull(msg.reasoningContent)
    }

    @Test
    fun testAssistantMessageWithUsage() {
        val usage = ChatUsage(promptTokens = 10, completionTokens = 20, totalTokens = 30)
        val msg = assistantMessage("Response", usage = usage)
        assertNotNull(msg.usage)
        assertEquals(10, msg.usage?.promptTokens)
        assertEquals(20, msg.usage?.completionTokens)
        assertEquals(30, msg.usage?.totalTokens)
    }

    @Test
    fun testAssistantMessageWithFinishReason() {
        val msg = assistantMessage("Done", finishReason = FinishReason.Stop)
        assertEquals(FinishReason.Stop, msg.finishReason)
    }

    @Test
    fun testAssistantMessageWithReasoning() {
        val content = listOf(
            ReasoningPart("Let me think about this..."),
            TextPart("The answer is 42."),
        )
        val msg = assistantMessage(content)
        assertTrue(msg.hasReasoning)
        assertEquals("Let me think about this...", msg.reasoningContent)
        assertEquals("The answer is 42.", msg.textContent)
    }

    @Test
    fun testAssistantMessageWithToolCalls() {
        val content = listOf(
            TextPart("I'll search for that."),
            ToolCallPart(
                toolCallId = "call_123",
                toolName = "search",
                args = buildJsonObject {
                    put("query", JsonPrimitive("kotlin"))
                },
            ),
        )
        val msg = assistantMessage(content)
        assertTrue(msg.hasToolCalls)
        assertEquals(1, msg.toolCalls.size)
        assertEquals("search", msg.toolCalls.first().toolName)
    }

    // endregion

    // region SystemMessage Tests

    @Test
    fun testSystemMessage() {
        val msg = systemMessage("You are a helpful assistant.")
        assertEquals("You are a helpful assistant.", msg.content)
        assertEquals("You are a helpful assistant.", msg.textContent)
    }

    @Test
    fun testSystemMessageWithMetadata() {
        val msg = systemMessage("System prompt", metadata = mapOf("version" to "1.0"))
        assertEquals(mapOf("version" to "1.0"), msg.metadata)
    }

    // endregion

    // region ToolMessage Tests

    @Test
    fun testToolMessageWithSingleResult() {
        val msg = toolMessage(
            toolCallId = "call_123",
            toolName = "calculator",
            result = JsonPrimitive(42),
        )
        assertEquals("call_123", msg.toolCallId)
        assertEquals("42", msg.textContent)
        assertEquals(1, msg.content.size)
        assertFalse(msg.content.first().isError)
    }

    @Test
    fun testToolMessageWithError() {
        val msg = toolMessage(
            toolCallId = "call_456",
            toolName = "api_call",
            result = JsonPrimitive("Connection timeout"),
            isError = true,
        )
        assertTrue(msg.content.first().isError)
    }

    @Test
    fun testToolMessageWithMultipleResults() {
        val results = listOf(
            ToolResultPart("call_1", "tool1", JsonPrimitive("result1")),
            ToolResultPart("call_2", "tool2", JsonPrimitive("result2")),
        )
        val msg = toolMessage(toolCallId = "call_1", results = results)
        assertEquals(2, msg.content.size)
        assertEquals("\"result1\"\n\"result2\"", msg.textContent)
    }

    // endregion

    // region ImagePart Tests

    @Test
    fun testImagePartFromBase64() {
        val part = ImagePart.fromBase64("base64data", "image/png")
        assertEquals("base64data", part.data)
        assertEquals("image/png", part.mimeType)
        assertFalse(part.isUrl)
    }

    @Test
    fun testImagePartFromUrl() {
        val part = ImagePart.fromUrl("https://example.com/image.jpg")
        assertEquals("https://example.com/image.jpg", part.data)
        assertTrue(part.isUrl)
        assertEquals("", part.mimeType) // URL images don't need mimeType
    }

    // endregion

    // region FinishReason Tests

    @Test
    fun testFinishReasonFromString() {
        assertEquals(FinishReason.Stop, FinishReason.fromString("stop"))
        assertEquals(FinishReason.Stop, FinishReason.fromString("end_turn"))
        assertEquals(FinishReason.Length, FinishReason.fromString("length"))
        assertEquals(FinishReason.Length, FinishReason.fromString("max_tokens"))
        assertEquals(FinishReason.ContentFilter, FinishReason.fromString("content_filter"))
        assertEquals(FinishReason.ToolCalls, FinishReason.fromString("tool_calls"))
        assertEquals(FinishReason.ToolCalls, FinishReason.fromString("tool_use"))
        assertEquals(FinishReason.FunctionCall, FinishReason.fromString("function_call"))
        assertNull(FinishReason.fromString("unknown"))
        assertNull(FinishReason.fromString(null))
    }

    @Test
    fun testFinishReasonCaseInsensitive() {
        assertEquals(FinishReason.Stop, FinishReason.fromString("STOP"))
        assertEquals(FinishReason.Stop, FinishReason.fromString("Stop"))
        assertEquals(FinishReason.Length, FinishReason.fromString("LENGTH"))
    }

    // endregion

    // region ChatUsage Tests

    @Test
    fun testChatUsageDefaults() {
        val usage = ChatUsage()
        assertEquals(0, usage.promptTokens)
        assertEquals(0, usage.completionTokens)
        assertEquals(0, usage.totalTokens)
    }

    @Test
    fun testChatUsageWithValues() {
        val usage = ChatUsage(promptTokens = 100, completionTokens = 50, totalTokens = 150)
        assertEquals(100, usage.promptTokens)
        assertEquals(50, usage.completionTokens)
        assertEquals(150, usage.totalTokens)
    }

    // endregion

    // region Message ID Generation Tests

    @Test
    fun testMessageIdGeneration() {
        val msg1 = userMessage("Test 1")
        val msg2 = userMessage("Test 2")
        // IDs should be unique
        assertTrue(msg1.id != msg2.id)
        // IDs should start with "msg_"
        assertTrue(msg1.id.startsWith("msg_"))
        assertTrue(msg2.id.startsWith("msg_"))
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun testCreatedAtTimestamp() {
        val before = Clock.System.now().toEpochMilliseconds()
        val msg = userMessage("Test")
        val after = Clock.System.now().toEpochMilliseconds()
        assertTrue(msg.createdAt >= before)
        assertTrue(msg.createdAt <= after)
    }

    // endregion
}
