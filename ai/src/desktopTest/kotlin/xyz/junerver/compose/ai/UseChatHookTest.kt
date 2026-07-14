/*
  Description: useChat hook behavior tests
  Author: Junerver
  Date: 2026/07/14
  Email: junerver@gmail.com
  Version: v1.0
*/

package xyz.junerver.compose.ai

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonPrimitive
import xyz.junerver.compose.ai.http.HttpResult
import xyz.junerver.compose.ai.http.SseEvent
import xyz.junerver.compose.ai.multiprovider.LoadBalanceStrategy
import xyz.junerver.compose.ai.multiprovider.ModelsProvider
import xyz.junerver.compose.ai.multiprovider.RetryConfig
import xyz.junerver.compose.ai.test.FakeChatProvider
import xyz.junerver.compose.ai.test.FakeHttpEngine
import xyz.junerver.compose.ai.usechat.ChatHolder
import xyz.junerver.compose.ai.usechat.ChatMessage
import xyz.junerver.compose.ai.usechat.ChatUsage
import xyz.junerver.compose.ai.usechat.FinishReason
import xyz.junerver.compose.ai.usechat.AssistantMessage
import xyz.junerver.compose.ai.usechat.assistantMessage
import xyz.junerver.compose.ai.usechat.sendText
import xyz.junerver.compose.ai.usechat.useChat
import xyz.junerver.compose.ai.usechat.userMessage

@Suppress("DEPRECATION")
class UseChatHookTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useChat_non_streaming_updates_messages_callbacks_and_holder_identity() {
        val engine = FakeHttpEngine().apply {
            enqueueResponse(HttpResult(200, "text:answer|finish:stop"))
        }
        val provider = FakeChatProvider()
        var initialHolder: ChatHolder? = null
        lateinit var latestHolder: ChatHolder
        var finishedMessage: ChatMessage? = null
        var finishReason: FinishReason? = null

        runComposeUiTest {
            setContent {
                latestHolder = useChat {
                    this.provider = provider
                    httpEngine = engine
                    stream = false
                    onFinish = { message, _, reason ->
                        finishedMessage = message
                        finishReason = reason
                    }
                }
                latestHolder.messages.value
                if (initialHolder == null) initialHolder = latestHolder
            }

            waitForIdle()
            runOnIdle { latestHolder.sendText("question") }
            mainClock.advanceTimeBy(100)
            waitForIdle()

            assertEquals(2, latestHolder.messages.value.size)
            assertEquals("question", latestHolder.messages.value.first().textContent)
            assertEquals("answer", latestHolder.messages.value.last().textContent)
            assertEquals("answer", finishedMessage?.textContent)
            assertEquals(FinishReason.Stop, finishReason)
            assertFalse(latestHolder.isLoading.value)
            assertNull(latestHolder.error.value)
            assertSame(initialHolder, latestHolder)
        }

        assertEquals(0, engine.closeCount)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useChat_streaming_accumulates_content_and_records_usage_once() = runComposeUiTest {
        val usage = ChatUsage(promptTokens = 4, completionTokens = 6, totalTokens = 10)
        val provider = FakeChatProvider().apply { providedUsage = usage }
        val engine = FakeHttpEngine().apply {
            enqueueStream(
                SseEvent.Data("delta:hello"),
                SseEvent.Data("delta:world"),
                SseEvent.Data("finish:stop"),
                SseEvent.Data("done"),
                SseEvent.Complete,
            )
        }
        val chunks = mutableListOf<String>()
        var finishCalls = 0
        lateinit var holder: ChatHolder

        setContent {
            TokenUsageProvider {
                holder = useChat {
                    this.provider = provider
                    httpEngine = engine
                    stream = true
                    onStream = chunks::add
                    onFinish = { _, _, _ -> finishCalls++ }
                }
                holder.messages.value
                holder.tokenStats
            }
        }

        waitForIdle()
        runOnIdle { holder.sendText("question") }
        mainClock.advanceTimeBy(100)
        waitForIdle()

        assertEquals("helloworld", holder.messages.value.last().textContent)
        assertEquals(listOf("hello", "world"), chunks)
        assertEquals(1, finishCalls)
        assertEquals(10, holder.tokenStats?.totalTokens)
        assertEquals(1, holder.tokenStats?.requestCount)
        assertFalse(holder.isLoading.value)
        assertNull(holder.error.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useChat_latest_stream_remains_active_after_previous_request_cleans_up() = runComposeUiTest {
        val secondStarted = CompletableDeferred<Unit>()
        val allowSecondToFinish = CompletableDeferred<Unit>()
        val engine = FakeHttpEngine().apply {
            enqueueStream(
                flow {
                    try {
                        awaitCancellation()
                    } finally {
                        withContext(NonCancellable) {
                            secondStarted.await()
                        }
                    }
                },
            )
            enqueueStream(
                flow {
                    secondStarted.complete(Unit)
                    allowSecondToFinish.await()
                    emit(SseEvent.Data("delta:second"))
                    emit(SseEvent.Data("done"))
                    emit(SseEvent.Complete)
                },
            )
        }
        lateinit var holder: ChatHolder

        setContent {
            holder = useChat {
                provider = FakeChatProvider()
                httpEngine = engine
                stream = true
            }
            holder.messages.value
            holder.isLoading.value
        }

        waitForIdle()
        runOnIdle { holder.sendText("first") }
        mainClock.advanceTimeBy(1)
        waitForIdle()
        assertTrue(holder.isLoading.value)

        runOnIdle { holder.sendText("second") }
        mainClock.advanceTimeBy(1)
        waitForIdle()

        assertTrue(holder.isLoading.value)
        assertEquals(listOf("first", "", "second", ""), holder.messages.value.map { it.textContent })

        allowSecondToFinish.complete(Unit)
        mainClock.advanceTimeBy(1)
        waitForIdle()

        assertFalse(holder.isLoading.value)
        assertEquals(listOf("first", "", "second", "second"), holder.messages.value.map { it.textContent })
        assertNull(holder.error.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useChat_streaming_accumulates_reasoning_and_tool_call_arguments() = runComposeUiTest {
        val engine = FakeHttpEngine().apply {
            enqueueStream(
                SseEvent.Data("reasoning:check "),
                SseEvent.Data("reasoning:weather"),
                SseEvent.Data("tool:0|call_1|weather|{\"city\":"),
                SseEvent.Data("tool:0|||\"Paris\"}"),
                SseEvent.Data("finish:tool_calls"),
                SseEvent.Data("done"),
                SseEvent.Complete,
            )
        }
        lateinit var holder: ChatHolder

        setContent {
            holder = useChat {
                provider = FakeChatProvider()
                httpEngine = engine
                stream = true
            }
            holder.messages.value
        }

        waitForIdle()
        runOnIdle { holder.sendText("weather") }
        mainClock.advanceTimeBy(100)
        waitForIdle()

        val assistant = holder.messages.value.last() as AssistantMessage
        assertEquals("check weather", assistant.reasoningContent)
        assertEquals(FinishReason.ToolCalls, assistant.finishReason)
        assertEquals(1, assistant.toolCalls.size)
        assertEquals("call_1", assistant.toolCalls.single().toolCallId)
        assertEquals("weather", assistant.toolCalls.single().toolName)
        assertEquals(JsonPrimitive("Paris"), assistant.toolCalls.single().args["city"])
        assertFalse(holder.isLoading.value)
        assertNull(holder.error.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useChat_stop_cancels_stream_and_preserves_received_content() = runComposeUiTest {
        val streamStarted = CompletableDeferred<Unit>()
        val engine = FakeHttpEngine().apply {
            enqueueStream(
                flow {
                    emit(SseEvent.Data("delta:partial"))
                    streamStarted.complete(Unit)
                    delay(1_000)
                    emit(SseEvent.Data("delta:late"))
                    emit(SseEvent.Data("done"))
                    emit(SseEvent.Complete)
                },
            )
        }
        var finishCalls = 0
        lateinit var holder: ChatHolder

        setContent {
            holder = useChat {
                provider = FakeChatProvider()
                httpEngine = engine
                stream = true
                onFinish = { _, _, _ -> finishCalls++ }
            }
            holder.messages.value
            holder.isLoading.value
        }

        waitForIdle()
        runOnIdle { holder.sendText("question") }
        mainClock.advanceTimeBy(1)
        waitForIdle()
        streamStarted.await()
        assertEquals("partial", holder.messages.value.last().textContent)
        assertTrue(holder.isLoading.value)

        runOnIdle { holder.stop() }
        mainClock.advanceTimeBy(2_000)
        waitForIdle()

        assertEquals("partial", holder.messages.value.last().textContent)
        assertFalse(holder.isLoading.value)
        assertEquals(0, finishCalls)
        assertNull(holder.error.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useChat_in_flight_request_keeps_callbacks_from_send_time() = runComposeUiTest {
        val streamStarted = CompletableDeferred<Unit>()
        val releaseStream = CompletableDeferred<Unit>()
        val callbackVersion = mutableStateOf(0)
        val firstChunks = mutableListOf<String>()
        val secondChunks = mutableListOf<String>()
        var firstFinishCalls = 0
        var secondFinishCalls = 0
        val engine = FakeHttpEngine().apply {
            enqueueStream(
                flow {
                    streamStarted.complete(Unit)
                    releaseStream.await()
                    emit(SseEvent.Data("delta:answer"))
                    emit(SseEvent.Data("done"))
                    emit(SseEvent.Complete)
                },
            )
        }
        lateinit var holder: ChatHolder

        setContent {
            val version = callbackVersion.value
            holder = useChat {
                provider = FakeChatProvider()
                httpEngine = engine
                stream = true
                if (version == 0) {
                    onStream = firstChunks::add
                    onFinish = { _, _, _ -> firstFinishCalls++ }
                } else {
                    onStream = secondChunks::add
                    onFinish = { _, _, _ -> secondFinishCalls++ }
                }
            }
            holder.messages.value
        }

        waitForIdle()
        runOnIdle { holder.sendText("question") }
        mainClock.advanceTimeBy(1)
        waitForIdle()
        streamStarted.await()

        runOnIdle { callbackVersion.value = 1 }
        waitForIdle()
        releaseStream.complete(Unit)
        mainClock.advanceTimeBy(100)
        waitForIdle()

        assertEquals(listOf("answer"), firstChunks)
        assertTrue(secondChunks.isEmpty())
        assertEquals(1, firstFinishCalls)
        assertEquals(0, secondFinishCalls)
        assertEquals("answer", holder.messages.value.last().textContent)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useChat_models_provider_round_robin_persists_across_requests() = runComposeUiTest {
        val firstProvider = FakeChatProvider(name = "First", defaultModel = "first-model")
        val secondProvider = FakeChatProvider(name = "Second", defaultModel = "second-model")
        val engine = FakeHttpEngine().apply {
            enqueueResponse(HttpResult(200, "text:first-answer"))
            enqueueResponse(HttpResult(200, "text:second-answer"))
        }
        lateinit var holder: ChatHolder

        setContent {
            ModelsProvider(
                providers = listOf(firstProvider, secondProvider),
                strategy = LoadBalanceStrategy.RoundRobin,
                retryConfig = RetryConfig.noRetry(),
            ) {
                holder = useChat {
                    httpEngine = engine
                    stream = false
                }
                holder.messages.value
            }
        }

        waitForIdle()
        runOnIdle { holder.sendText("first-question") }
        mainClock.advanceTimeBy(100)
        waitForIdle()
        runOnIdle { holder.sendText("second-question") }
        mainClock.advanceTimeBy(100)
        waitForIdle()

        assertEquals(
            listOf("first-question", "first-answer", "second-question", "second-answer"),
            holder.messages.value.map { it.textContent },
        )
        assertEquals("first-question", firstProvider.lastRequestMessages?.last()?.textContent)
        assertEquals("second-question", secondProvider.lastRequestMessages?.last()?.textContent)
        assertEquals(
            listOf("first-model", "second-model"),
            holder.messages.value.filterIsInstance<AssistantMessage>().map { it.model },
        )
        assertEquals(2, engine.recordedRequests.size)
        assertEquals(0, engine.closeCount)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useChat_models_provider_stream_fails_over_before_first_delta() = runComposeUiTest {
        val firstProvider = FakeChatProvider(name = "First")
        val usage = ChatUsage(promptTokens = 2, completionTokens = 3, totalTokens = 5)
        val secondProvider = FakeChatProvider(name = "Second").apply { providedUsage = usage }
        val engine = FakeHttpEngine().apply {
            enqueueStream(
                SseEvent.Error(IllegalStateException("HTTP 429: retry")),
            )
            enqueueStream(
                SseEvent.Data("delta:recovered"),
                SseEvent.Data("finish:stop"),
                SseEvent.Data("done"),
                SseEvent.Complete,
            )
        }
        lateinit var holder: ChatHolder

        setContent {
            TokenUsageProvider {
                ModelsProvider(
                    providers = listOf(firstProvider, secondProvider),
                    strategy = LoadBalanceStrategy.RoundRobin,
                    retryConfig = RetryConfig(
                        maxRetries = 2,
                        retryDelay = 0,
                        exponentialBackoff = false,
                    ),
                ) {
                    holder = useChat {
                        httpEngine = engine
                        stream = true
                    }
                    holder.messages.value
                    holder.tokenStats
                }
            }
        }

        waitForIdle()
        runOnIdle { holder.sendText("question") }
        mainClock.advanceTimeBy(100)
        waitForIdle()

        assertEquals("recovered", holder.messages.value.last().textContent)
        assertEquals(2, engine.recordedRequests.size)
        assertTrue(firstProvider.lastRequestMessages != null)
        assertTrue(secondProvider.lastRequestMessages != null)
        assertNull(holder.error.value)
        assertFalse(holder.isLoading.value)
        val assistant = holder.messages.value.last() as AssistantMessage
        assertEquals(secondProvider.defaultModel, assistant.model)
        assertEquals("Second", holder.tokenStats?.records?.single()?.provider)
        assertEquals(secondProvider.defaultModel, holder.tokenStats?.records?.single()?.model)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useChat_recomposition_switches_from_models_provider_to_explicit_provider() = runComposeUiTest {
        val useExplicitProvider = mutableStateOf(false)
        val contextProvider = FakeChatProvider(name = "Context", defaultModel = "context-model")
        val explicitProvider = FakeChatProvider(name = "Explicit", defaultModel = "explicit-model")
        val engine = FakeHttpEngine().apply {
            enqueueResponse(HttpResult(200, "text:context-answer"))
            enqueueResponse(HttpResult(200, "text:explicit-answer"))
        }
        lateinit var holder: ChatHolder

        setContent {
            ModelsProvider(
                providers = listOf(contextProvider),
                retryConfig = RetryConfig.noRetry(),
            ) {
                val explicit = useExplicitProvider.value
                holder = useChat {
                    httpEngine = engine
                    stream = false
                    if (explicit) provider = explicitProvider
                }
                holder.messages.value
            }
        }

        waitForIdle()
        runOnIdle { holder.sendText("context-question") }
        mainClock.advanceTimeBy(100)
        waitForIdle()
        assertEquals("context-answer", holder.messages.value.last().textContent)

        runOnIdle { useExplicitProvider.value = true }
        waitForIdle()
        runOnIdle { holder.sendText("explicit-question") }
        mainClock.advanceTimeBy(100)
        waitForIdle()

        assertEquals("explicit-answer", holder.messages.value.last().textContent)
        assertEquals("context-question", contextProvider.lastRequestMessages?.last()?.textContent)
        assertEquals("explicit-question", explicitProvider.lastRequestMessages?.last()?.textContent)
        assertEquals("explicit-model", (holder.messages.value.last() as AssistantMessage).model)
        assertEquals(2, engine.recordedRequests.size)
        assertEquals(0, engine.closeCount)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useChat_http_error_is_exposed_and_callback_runs_once() = runComposeUiTest {
        val engine = FakeHttpEngine().apply {
            enqueueResponse(HttpResult(500, "server failed"))
        }
        val errors = mutableListOf<Throwable>()
        lateinit var holder: ChatHolder

        setContent {
            holder = useChat {
                provider = FakeChatProvider()
                httpEngine = engine
                stream = false
                onError = errors::add
            }
        }

        waitForIdle()
        runOnIdle { holder.sendText("question") }
        mainClock.advanceTimeBy(100)
        waitForIdle()

        assertEquals(1, errors.size)
        assertSame(holder.error.value, errors.single())
        assertTrue(holder.error.value?.message.orEmpty().contains("500"))
        assertFalse(holder.isLoading.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useChat_reload_setMessages_and_append_preserve_observable_state() = runComposeUiTest {
        val engine = FakeHttpEngine().apply {
            enqueueResponse(HttpResult(200, "text:first"))
            enqueueResponse(HttpResult(200, "text:second"))
        }
        lateinit var holder: ChatHolder

        setContent {
            holder = useChat {
                provider = FakeChatProvider()
                httpEngine = engine
                stream = false
            }
            holder.messages.value
        }

        waitForIdle()
        runOnIdle { holder.sendText("question") }
        mainClock.advanceTimeBy(100)
        waitForIdle()
        assertEquals("first", holder.messages.value.last().textContent)

        runOnIdle { holder.reload() }
        mainClock.advanceTimeBy(100)
        waitForIdle()
        assertEquals(listOf("question", "second"), holder.messages.value.map { it.textContent })
        assertEquals(2, engine.recordedRequests.size)

        runOnIdle {
            holder.setMessages(listOf(userMessage("manual")))
            holder.append(assistantMessage("appended"))
        }
        waitForIdle()

        assertEquals(listOf("manual", "appended"), holder.messages.value.map { it.textContent })
    }
}
