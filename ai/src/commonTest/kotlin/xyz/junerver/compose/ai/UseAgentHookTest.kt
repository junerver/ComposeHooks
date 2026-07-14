/*
  Description: useAgent hook behavior tests
  Author: Junerver
  Date: 2026/07/14
  Email: junerver@gmail.com
  Version: v1.0
*/

package xyz.junerver.compose.ai

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import xyz.junerver.compose.ai.http.HttpResult
import xyz.junerver.compose.ai.http.SseEvent
import xyz.junerver.compose.ai.test.FakeChatProvider
import xyz.junerver.compose.ai.test.FakeHttpEngine
import xyz.junerver.compose.ai.useagent.AgentHolder
import xyz.junerver.compose.ai.useagent.tool
import xyz.junerver.compose.ai.useagent.useAgent
import xyz.junerver.compose.ai.usechat.ChatMessage
import xyz.junerver.compose.ai.usechat.ChatUsage
import xyz.junerver.compose.ai.usechat.FinishReason
import xyz.junerver.compose.ai.usechat.Providers
import xyz.junerver.compose.ai.usechat.TextPart
import xyz.junerver.compose.ai.usechat.assistantMessage
import xyz.junerver.compose.ai.usechat.userMessage

@Serializable
private data class EchoParams(
    val value: String,
)

@Suppress("DEPRECATION")
class UseAgentHookTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useAgent_non_streaming_updates_messages_finish_callback_and_token_stats() {
        val usage = ChatUsage(promptTokens = 3, completionTokens = 4, totalTokens = 7)
        val provider = FakeChatProvider().apply { providedUsage = usage }
        val engine = FakeHttpEngine().apply {
            enqueueResponse(HttpResult(200, "text:answer|finish:stop"))
        }
        lateinit var latestHolder: AgentHolder
        var finishedMessage: ChatMessage? = null
        var finishReason: FinishReason? = null

        runComposeUiTest {
            setContent {
                TokenUsageProvider {
                    latestHolder = useAgent {
                        this.provider = provider
                        httpEngine = engine
                        stream = false
                        onFinish = { message, _, reason ->
                            finishedMessage = message
                            finishReason = reason
                        }
                    }
                    latestHolder.messages.value
                    latestHolder.tokenStats
                }
            }

            waitForIdle()
            runOnIdle { latestHolder.send(listOf(TextPart("question"))) }
            mainClock.advanceTimeBy(100)
            waitForIdle()

            assertEquals(listOf("question", "answer"), latestHolder.messages.value.map { it.textContent })
            assertEquals("answer", finishedMessage?.textContent)
            assertEquals(FinishReason.Stop, finishReason)
            assertEquals(7, latestHolder.tokenStats?.totalTokens)
            assertEquals(1, latestHolder.tokenStats?.requestCount)
            assertFalse(latestHolder.isLoading.value)
            assertNull(latestHolder.error.value)
        }

        assertEquals(0, engine.closeCount)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useAgent_streaming_accumulates_partial_messages() = runComposeUiTest {
        val engine = FakeHttpEngine().apply {
            enqueueStream(
                SseEvent.Data("delta:hello"),
                SseEvent.Data("delta:world"),
                SseEvent.Data("finish:stop"),
                SseEvent.Data("done"),
                SseEvent.Complete,
            )
        }
        lateinit var holder: AgentHolder

        setContent {
            holder = useAgent {
                provider = FakeChatProvider()
                httpEngine = engine
                stream = true
            }
            holder.messages.value
        }

        waitForIdle()
        runOnIdle { holder.send(listOf(TextPart("question"))) }
        mainClock.advanceTimeBy(100)
        waitForIdle()

        assertEquals(listOf("question", "helloworld"), holder.messages.value.map { it.textContent })
        assertFalse(holder.isLoading.value)
        assertNull(holder.error.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useAgent_http_error_is_exposed_and_callback_runs_once() = runComposeUiTest {
        val engine = FakeHttpEngine().apply {
            enqueueResponse(HttpResult(503, "agent unavailable"))
        }
        val errors = mutableListOf<Throwable>()
        lateinit var holder: AgentHolder

        setContent {
            holder = useAgent {
                provider = FakeChatProvider()
                httpEngine = engine
                stream = false
                onError = errors::add
            }
        }

        waitForIdle()
        runOnIdle { holder.send(listOf(TextPart("question"))) }
        mainClock.advanceTimeBy(100)
        waitForIdle()

        assertEquals(1, errors.size)
        assertSame(holder.error.value, errors.single())
        assertTrue(holder.error.value?.message.orEmpty().contains("503"))
        assertFalse(holder.isLoading.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useAgent_executes_tool_and_reports_tool_callbacks() = runComposeUiTest {
        val engine = FakeHttpEngine().apply {
            enqueueResponse(
                HttpResult(
                    200,
                    """
                    {
                      "id": "tool-step",
                      "object": "chat.completion",
                      "created": 0,
                      "model": "test-model",
                      "choices": [{
                        "index": 0,
                        "message": {
                          "role": "assistant",
                          "content": null,
                          "tool_calls": [{
                            "id": "call-1",
                            "type": "function",
                            "function": { "name": "echo", "arguments": "{\"value\":\"hello\"}" }
                          }]
                        },
                        "finish_reason": "tool_calls"
                      }]
                    }
                    """.trimIndent(),
                ),
            )
            enqueueResponse(
                HttpResult(
                    200,
                    """
                    {
                      "id": "final-step",
                      "object": "chat.completion",
                      "created": 0,
                      "model": "test-model",
                      "choices": [{
                        "index": 0,
                        "message": { "role": "assistant", "content": "finished" },
                        "finish_reason": "stop"
                      }]
                    }
                    """.trimIndent(),
                ),
            )
        }
        val echoTool = tool<EchoParams>(
            name = "echo",
            description = "Echo a value",
            parameters = buildJsonObject {
                put("type", JsonPrimitive("object"))
                put(
                    "properties",
                    buildJsonObject {
                        put(
                            "value",
                            buildJsonObject { put("type", JsonPrimitive("string")) },
                        )
                    },
                )
            },
        ) { params -> JsonPrimitive(params.value.uppercase()) }
        val toolCalls = mutableListOf<String>()
        val toolResults = mutableListOf<String>()
        lateinit var holder: AgentHolder

        setContent {
            holder = useAgent {
                provider = Providers.OpenAI(apiKey = "test", defaultModel = "test-model")
                httpEngine = engine
                stream = false
                tools = listOf(echoTool)
                parallelToolCalls = false
                onToolCall = { toolCalls += it.toolName }
                onToolResult = { _, toolName, result, isError ->
                    toolResults += "$toolName:$result:$isError"
                }
            }
            holder.messages.value
        }

        waitForIdle()
        runOnIdle { holder.send(listOf(TextPart("run tool"))) }
        mainClock.advanceTimeBy(100)
        waitForIdle()

        assertEquals(listOf("echo"), toolCalls)
        assertEquals(1, toolResults.size)
        assertTrue(toolResults.single().contains("echo"))
        assertTrue(toolResults.single().contains("HELLO"))
        assertTrue(toolResults.single().endsWith("false"))
        assertEquals("finished", holder.messages.value.last().textContent)
        assertEquals(2, engine.recordedRequests.size)
        assertFalse(holder.isLoading.value)
        assertNull(holder.error.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useAgent_empty_send_is_ignored_and_message_mutators_are_observable() = runComposeUiTest {
        val engine = FakeHttpEngine()
        lateinit var holder: AgentHolder
        var initialHolder: AgentHolder? = null

        setContent {
            holder = useAgent {
                provider = FakeChatProvider()
                httpEngine = engine
            }
            holder.messages.value
            if (initialHolder == null) initialHolder = holder
        }

        waitForIdle()
        runOnIdle { holder.send(emptyList()) }
        waitForIdle()
        assertTrue(engine.recordedRequests.isEmpty())

        runOnIdle {
            holder.setMessages(listOf(userMessage("manual")))
            holder.append(assistantMessage("appended"))
        }
        waitForIdle()

        assertEquals(listOf("manual", "appended"), holder.messages.value.map { it.textContent })
        assertSame(initialHolder, holder)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useAgent_stop_cancels_stream_before_later_deltas() = runComposeUiTest {
        val engine = FakeHttpEngine().apply {
            enqueueStream(
                flow {
                    emit(SseEvent.Data("delta:first"))
                    delay(1.seconds)
                    emit(SseEvent.Data("delta:second"))
                    emit(SseEvent.Complete)
                },
            )
        }
        lateinit var holder: AgentHolder

        setContent {
            holder = useAgent {
                provider = FakeChatProvider()
                httpEngine = engine
                stream = true
            }
            holder.messages.value
        }

        waitForIdle()
        runOnIdle { holder.send(listOf(TextPart("question"))) }
        mainClock.advanceTimeBy(20)
        waitForIdle()

        assertTrue(holder.isLoading.value)
        assertEquals("first", holder.messages.value.last().textContent)

        runOnIdle { holder.stop() }
        mainClock.advanceTimeBy(2_000)
        waitForIdle()

        assertFalse(holder.isLoading.value)
        assertEquals(listOf("question", "first"), holder.messages.value.map { it.textContent })
    }
}
