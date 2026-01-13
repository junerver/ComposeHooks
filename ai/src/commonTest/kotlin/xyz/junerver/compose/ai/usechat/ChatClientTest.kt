package xyz.junerver.compose.ai.usechat

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import xyz.junerver.compose.ai.http.HttpResult
import xyz.junerver.compose.ai.http.SseEvent
import xyz.junerver.compose.ai.test.FakeChatProvider
import xyz.junerver.compose.ai.test.FakeHttpEngine

private val noMessages: List<ChatMessage> = listOf<ChatMessage>()

class ChatClientTest {
    @Test
    fun streamChatEmitsParsedEvents() = runTest {
        val provider = FakeChatProvider()
        val engine = FakeHttpEngine().apply {
            enqueueStream(
                SseEvent.Data("delta:Hello"),
                SseEvent.Data("delta:World"),
                SseEvent.Data("finish:stop"),
                SseEvent.Complete,
            )
        }
        val client = ChatClient(
            ChatOptions.optionOf {
                this.provider = provider
                httpEngine = engine
            },
        )

        val events: List<StreamEvent> = client.streamChat(noMessages).toList(mutableListOf())
        assertEquals(4, events.size)

        val firstDelta = events[0] as? StreamEvent.Delta ?: fail("First event should be Delta")
        assertEquals("Hello", firstDelta.content)
        val secondDelta = events[1] as? StreamEvent.Delta ?: fail("Second event should be Delta")
        assertEquals("World", secondDelta.content)
        val finishDelta = events[2] as? StreamEvent.Delta ?: fail("Finish event should be Delta")
        assertEquals("", finishDelta.content)
        assertEquals("stop", finishDelta.finishReason)
        assertTrue(events.last() is StreamEvent.Done)
    }

    @Test
    fun streamChatWrapsEngineErrorsIntoOpenAiException() = runTest {
        val provider = FakeChatProvider()
        val engine = FakeHttpEngine().apply {
            enqueueStream(
                SseEvent.Error(
                    Exception(
                        """
                        {"error":{"message":"Invalid request","type":"invalid_request_error","code":"bad_request"}}
                        """.trimIndent(),
                    ),
                ),
            )
        }
        val client = ChatClient(
            ChatOptions.optionOf {
                this.provider = provider
                httpEngine = engine
            },
        )

        val events: List<StreamEvent> = client.streamChat(noMessages).toList(mutableListOf())
        val errorEvent = events.singleOrNull() as? StreamEvent.Error ?: fail("Expected error event")
        val exception = errorEvent.error
        assertIs<OpenAIException>(exception)
        assertEquals("Invalid request", exception.message)
        assertEquals("invalid_request_error", exception.errorType)
        assertEquals("bad_request", exception.errorCode)
    }

    @Test
    fun streamChatWrapsEngineErrorsIntoAnthropicException() = runTest {
        val provider = FakeChatProvider()
        val engine = FakeHttpEngine().apply {
            enqueueStream(
                SseEvent.Error(
                    Exception(
                        """
                        {"type":"error","error":{"type":"authentication_error","message":"Invalid API key"}}
                        """.trimIndent(),
                    ),
                ),
            )
        }
        val client = ChatClient(
            ChatOptions.optionOf {
                this.provider = provider
                httpEngine = engine
            },
        )

        val events: List<StreamEvent> = client.streamChat(noMessages).toList(mutableListOf())
        val errorEvent = events.singleOrNull() as? StreamEvent.Error ?: fail("Expected error event")
        val exception = errorEvent.error
        assertIs<AnthropicException>(exception)
        assertEquals("Invalid API key", exception.message)
        assertEquals("authentication_error", exception.errorType)
    }

    @Test
    fun chatReturnsAssistantMessageFromProvider() = runTest {
        val provider = FakeChatProvider()
        val engine = FakeHttpEngine().apply {
            enqueueResponse(
                HttpResult(
                    statusCode = 200,
                    body = "text:Completed",
                ),
            )
        }
        val client = ChatClient(
            ChatOptions.optionOf {
                this.provider = provider
                httpEngine = engine
                stream = false
            },
        )

        val result = client.chat(noMessages)
        assertEquals("Completed", result.message.textContent)
    }

    @Test
    fun chatThrowsOpenAiExceptionForNonSuccessStatus() = runTest {
        val provider = FakeChatProvider()
        val engine = FakeHttpEngine().apply {
            enqueueResponse(
                HttpResult(
                    statusCode = 429,
                    body =
                        """
                        {
                            "error": {
                                "message": "Too many requests",
                                "type": "rate_limit_error",
                                "code": "rate_limit"
                            }
                        }
                        """.trimIndent(),
                ),
            )
        }
        val client = ChatClient(
            ChatOptions.optionOf {
                this.provider = provider
                httpEngine = engine
                stream = false
            },
        )

        try {
            client.chat(noMessages)
            fail("Expected OpenAIException")
        } catch (e: OpenAIException) {
            assertEquals("Too many requests", e.message)
            assertEquals("rate_limit_error", e.errorType)
            assertEquals("rate_limit", e.errorCode)
        }
    }

    @Test
    fun chatThrowsAnthropicExceptionForNonSuccessStatus() = runTest {
        val engine = FakeHttpEngine().apply {
            enqueueResponse(
                HttpResult(
                    statusCode = 401,
                    body =
                        """
                        {
                            "type": "error",
                            "error": {
                                "type": "authentication_error",
                                "message": "Invalid API key"
                            }
                        }
                        """.trimIndent(),
                ),
            )
        }
        val client = ChatClient(
            ChatOptions.optionOf {
                provider = Providers.Anthropic(apiKey = "test")
                httpEngine = engine
                stream = false
            },
        )

        try {
            client.chat(noMessages)
            fail("Expected AnthropicException")
        } catch (e: AnthropicException) {
            assertEquals("Invalid API key", e.message)
            assertEquals("authentication_error", e.errorType)
        }
    }
}
