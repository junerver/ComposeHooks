/*
  Description: useAsr and useTts behavior tests
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
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import xyz.junerver.compose.ai.http.HttpResult
import xyz.junerver.compose.ai.http.SseEvent
import xyz.junerver.compose.ai.test.FakeChatProvider
import xyz.junerver.compose.ai.test.FakeHttpEngine
import xyz.junerver.compose.ai.useasr.AsrHolder
import xyz.junerver.compose.ai.useasr.useAsr
import xyz.junerver.compose.ai.usechat.AssistantMessage
import xyz.junerver.compose.ai.usechat.ChatResponseResult
import xyz.junerver.compose.ai.usechat.InputAudioPart
import xyz.junerver.compose.ai.usechat.UserMessage
import xyz.junerver.compose.ai.usechat.assistantMessage
import xyz.junerver.compose.ai.usetts.TtsHolder
import xyz.junerver.compose.ai.usetts.useTts

@Suppress("DEPRECATION")
class UseAudioHooksTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useAsr_ignores_blank_input_without_starting_request() = runComposeUiTest {
        val engine = FakeHttpEngine()
        lateinit var holder: AsrHolder

        setContent {
            holder = useAsr {
                provider = FakeChatProvider()
                httpEngine = engine
            }
        }

        waitForIdle()
        runOnIdle { holder.recognize("   ") }
        waitForIdle()

        assertTrue(engine.recordedRequests.isEmpty())
        assertEquals("", holder.text.value)
        assertFalse(holder.isLoading.value)
        assertNull(holder.error.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useAsr_accumulates_streamed_text_and_forwards_options() = runComposeUiTest {
        val engine = FakeHttpEngine().apply {
            enqueueStream(
                SseEvent.Data("delta:hello"),
                SseEvent.Data("delta:world"),
                SseEvent.Complete,
            )
        }
        val provider = FakeChatProvider()
        lateinit var holder: AsrHolder

        setContent {
            holder = useAsr {
                this.provider = provider
                httpEngine = engine
                language = "zh"
            }
        }

        waitForIdle()
        runOnIdle { holder.recognize("data:audio/wav;base64,AAAA") }
        mainClock.advanceTimeBy(100)
        waitForIdle()

        assertEquals("helloworld", holder.text.value)
        assertFalse(holder.isLoading.value)
        assertNull(holder.error.value)
        assertEquals("zh", provider.lastAsrOptions?.language)
        val message = assertIs<UserMessage>(provider.lastRequestMessages?.single())
        assertEquals("data:audio/wav;base64,AAAA", assertIs<InputAudioPart>(message.content.single()).data)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useTts_accumulates_audio_chunks_and_invokes_callback() = runComposeUiTest {
        val engine = FakeHttpEngine().apply {
            enqueueStream(
                SseEvent.Data("audio:AAA"),
                SseEvent.Data("audio:BBB"),
                SseEvent.Complete,
            )
        }
        val provider = FakeChatProvider()
        val chunks = mutableListOf<String>()
        lateinit var holder: TtsHolder

        setContent {
            holder = useTts {
                this.provider = provider
                httpEngine = engine
                voice = "test-voice"
                onAudioChunk = chunks::add
            }
        }

        waitForIdle()
        runOnIdle { holder.synthesize("hello", "cheerful") }
        mainClock.advanceTimeBy(100)
        waitForIdle()

        assertEquals("AAABBB", holder.audioDataBase64.value)
        assertEquals(listOf("AAA", "BBB"), chunks)
        assertEquals("test-voice", provider.lastAudioOptions?.voice)
        assertEquals("pcm16", provider.lastAudioOptions?.format)
        assertFalse(holder.isLoading.value)
        assertNull(holder.error.value)
        assertIs<UserMessage>(provider.lastRequestMessages?.first())
        assertIs<AssistantMessage>(provider.lastRequestMessages?.last())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useTts_non_streaming_returns_complete_audio() = runComposeUiTest {
        val engine = FakeHttpEngine().apply {
            enqueueResponse(HttpResult(200, "unused"))
        }
        val provider = FakeChatProvider().apply {
            nextResponse = ChatResponseResult(
                message = assistantMessage("done"),
                audioDataBase64 = "WAV-DATA",
            )
        }
        lateinit var holder: TtsHolder

        setContent {
            holder = useTts {
                this.provider = provider
                httpEngine = engine
                stream = false
            }
        }

        waitForIdle()
        runOnIdle { holder.synthesize("hello", null) }
        mainClock.advanceTimeBy(100)
        waitForIdle()

        assertEquals("WAV-DATA", holder.audioDataBase64.value)
        assertEquals("wav", provider.lastAudioOptions?.format)
        assertFalse(holder.isLoading.value)
        assertNull(holder.error.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useAsr_exposes_stream_error_and_invokes_callback_once() = runComposeUiTest {
        val expected = IllegalStateException("asr failed")
        val engine = FakeHttpEngine().apply {
            enqueueStream(SseEvent.Error(expected))
        }
        val errors = mutableListOf<Throwable>()
        lateinit var holder: AsrHolder

        setContent {
            holder = useAsr {
                provider = FakeChatProvider()
                httpEngine = engine
                onError = errors::add
            }
        }

        waitForIdle()
        runOnIdle { holder.recognize("data:audio/wav;base64,AAAA") }
        mainClock.advanceTimeBy(100)
        waitForIdle()

        assertSame(expected, holder.error.value)
        assertEquals(1, errors.size)
        assertSame(expected, errors.single())
        assertFalse(holder.isLoading.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useTts_reports_missing_audio_and_finishes_loading() = runComposeUiTest {
        val engine = FakeHttpEngine().apply {
            enqueueResponse(HttpResult(200, "unused"))
        }
        val provider = FakeChatProvider().apply {
            nextResponse = ChatResponseResult(message = assistantMessage("no audio"))
        }
        val errors = mutableListOf<Throwable>()
        lateinit var holder: TtsHolder

        setContent {
            holder = useTts {
                this.provider = provider
                httpEngine = engine
                stream = false
                onError = errors::add
            }
        }

        waitForIdle()
        runOnIdle { holder.synthesize("hello", null) }
        mainClock.advanceTimeBy(100)
        waitForIdle()

        assertEquals("No audio data in TTS response", holder.error.value?.message)
        assertEquals(1, errors.size)
        assertSame(holder.error.value, errors.single())
        assertFalse(holder.isLoading.value)
        assertEquals("", holder.audioDataBase64.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun audio_hooks_do_not_close_caller_owned_engine_on_recomposition_or_disposal() {
        val asrEngine = FakeHttpEngine()
        val ttsEngine = FakeHttpEngine()
        val recomposition = mutableStateOf(0)

        runComposeUiTest {
            setContent {
                recomposition.value
                useAsr {
                    provider = FakeChatProvider()
                    httpEngine = asrEngine
                }
                useTts {
                    provider = FakeChatProvider()
                    httpEngine = ttsEngine
                }
            }

            waitForIdle()
            runOnIdle { recomposition.value++ }
            waitForIdle()

            assertEquals(0, asrEngine.closeCount)
            assertEquals(0, ttsEngine.closeCount)
        }

        assertEquals(0, asrEngine.closeCount)
        assertEquals(0, ttsEngine.closeCount)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useAsr_reset_clears_result_and_error_without_replacing_holder() = runComposeUiTest {
        val engine = FakeHttpEngine().apply {
            enqueueStream(SseEvent.Data("delta:text"), SseEvent.Complete)
        }
        var initialHolder: AsrHolder? = null
        lateinit var latestHolder: AsrHolder

        setContent {
            latestHolder = useAsr {
                provider = FakeChatProvider()
                httpEngine = engine
            }
            if (initialHolder == null) initialHolder = latestHolder
        }

        waitForIdle()
        runOnIdle { latestHolder.recognize("data:audio/wav;base64,AAAA") }
        mainClock.advanceTimeBy(100)
        waitForIdle()
        runOnIdle { latestHolder.reset() }
        waitForIdle()

        assertSame(initialHolder, latestHolder)
        assertEquals("", latestHolder.text.value)
        assertNull(latestHolder.error.value)
        assertFalse(latestHolder.isLoading.value)
    }
}
