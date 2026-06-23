package xyz.junerver.compose.ai.usetts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import arrow.core.left
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import xyz.junerver.compose.ai.AIOptionsDefaults
import xyz.junerver.compose.ai.OnErrorCallback
import xyz.junerver.compose.ai.OnResponseCallback
import xyz.junerver.compose.ai.http.HttpEngine
import xyz.junerver.compose.ai.usechat.AudioOptions
import xyz.junerver.compose.ai.usechat.ChatClient
import xyz.junerver.compose.ai.usechat.ChatMessage
import xyz.junerver.compose.ai.usechat.ChatOptions
import xyz.junerver.compose.ai.usechat.ChatProvider
import xyz.junerver.compose.ai.usechat.StreamEvent
import xyz.junerver.compose.ai.usechat.assistantMessage
import xyz.junerver.compose.ai.usechat.userMessage
import xyz.junerver.compose.hooks._useGetState
import xyz.junerver.compose.hooks.useCancelableAsync
import xyz.junerver.compose.hooks.useCreation
import xyz.junerver.compose.hooks.useLatestRef
import xyz.junerver.compose.hooks.useRef
import xyz.junerver.compose.hooks.useUnmount

/*
  Description: useTts hook for text-to-speech (TTS) via OpenAI-compatible API
  Author: Junerver
  Date: 2026/06/12
  Email: junerver@gmail.com
  Version: v1.0

  Uses the MiMo-V2.5-TTS model (or any OpenAI-compatible TTS endpoint).
  Endpoint: POST /v1/chat/completions with audio field.

  TTS message format:
  - role: user → style instruction (optional)
  - role: assistant → text to synthesize
  - audio: {format, voice} in request body
*/

// region Options

/**
 * Configuration options for the useTts hook.
 *
 * @property provider The chat provider to use (includes apiKey, baseUrl, model)
 * @property model Override the provider's default model (null = use "mimo-v2.5-tts")
 * @property voice Voice ID or name (e.g., "Chloe", "mimo_default", "冰糖")
 * @property format Audio output format: "wav" for non-streaming, "pcm16" for streaming
 * @property stream Whether to use streaming responses (default: true for lower latency)
 * @property timeout Request timeout duration
 * @property headers Additional HTTP headers
 * @property httpEngine Custom HTTP engine
 * @property onResponse Callback when receiving an HTTP response
 * @property onError Callback when an error occurs
 * @property onAudioChunk Callback for each streaming audio chunk (base64 PCM16)
 */
@Stable
data class TtsOptionsConfig(
    var provider: ChatProvider = AIOptionsDefaults.DEFAULT_PROVIDER,
    var model: String? = null,
    var voice: String = "mimo_default",
    var format: String? = null, // null = auto: "pcm16" for stream, "wav" for non-stream
    var stream: Boolean = true,
    var timeout: Duration = 120.seconds,
    var headers: Map<String, String> = AIOptionsDefaults.DEFAULT_HEADERS,
    var httpEngine: HttpEngine? = null,
    var onResponse: OnResponseCallback? = null,
    var onError: OnErrorCallback? = null,
    var onAudioChunk: ((base64Chunk: String) -> Unit)? = null,
) {
    /** The effective model (override or default TTS model). */
    val effectiveModel: String
        get() = model ?: "mimo-v2.5-tts"

    /** The effective audio format. */
    val effectiveFormat: String
        get() = format ?: if (stream) "pcm16" else "wav"
}

// endregion

// region Holder

/**
 * Holder class for useTts hook return values.
 *
 * @property audioDataBase64 The accumulated audio data as base64 string (WAV or PCM16)
 * @property isLoading Whether a synthesis request is currently in progress
 * @property error The most recent error, if any
 * @property synthesize Function to synthesize speech from text
 * @property stop Function to stop the current synthesis
 * @property reset Function to clear audio data and error state
 */
@Stable
data class TtsHolder(
    val audioDataBase64: State<String>,
    val isLoading: State<Boolean>,
    val error: State<Throwable?>,
    val synthesize: SynthesizeFn,
    val stop: () -> Unit,
    val reset: () -> Unit,
)

/**
 * Function to start text-to-speech synthesis.
 *
 * @param text The text to synthesize into speech
 * @param styleInstruction Optional style instruction (e.g., "用欢快的语调说").
 *                         Passed as the user message to control TTS style/emotion.
 */
typealias SynthesizeFn = (text: String, styleInstruction: String?) -> Unit

// endregion

// region Hook

/**
 * A Composable hook for text-to-speech (TTS) synthesis using OpenAI-compatible APIs.
 *
 * Converts text to speech audio. Supports streaming for lower latency (PCM16 chunks)
 * and non-streaming for complete audio files (WAV).
 *
 * Example usage:
 * ```kotlin
 * val (audioBase64, isLoading, error, synthesize, stop, reset) = useTts {
 *     provider = Providers.MiMo(apiKey = "your-api-key")
 *     voice = "Chloe"
 *     stream = true
 * }
 *
 * // Synthesize speech
 * synthesize("Hello, world!", "用欢快的语调说")
 *
 * // The audioDataBase64.value contains the base64-encoded audio
 * // For streaming: raw PCM16 data (needs WAV header to play)
 * // For non-streaming: complete WAV file
 * ```
 *
 * @param optionsOf Configuration factory function for TTS options
 * @return TtsHolder containing audio data state and control functions
 */
@Composable
fun useTts(optionsOf: TtsOptionsConfig.() -> Unit = {}): TtsHolder {
    val options = useCreation { TtsOptionsConfig() }.current.apply(optionsOf)
    val optionsRef = useLatestRef(options)

    // State management
    val (audioState, setAudioInternal, getAudio) = _useGetState("")
    val (isLoadingState, setIsLoadingInternal, _) = _useGetState(false)
    val (errorState, setErrorInternal, _) = _useGetState<Throwable?>(null)

    // Refs
    val clientRef = useRef<ChatClient?>(null)

    // Cancelable async
    val (asyncRun, cancelAsync, _) = useCancelableAsync()

    // Build ChatOptions for TTS
    val chatOptions = useCreation(
        options.provider,
        options.model,
        options.voice,
        options.effectiveFormat,
        options.stream,
        options.timeout,
    ) {
        ChatOptions(
            provider = options.provider,
            model = options.effectiveModel,
            stream = options.stream,
            timeout = options.timeout,
            headers = options.headers,
            httpEngine = options.httpEngine,
            onResponse = options.onResponse,
            onError = options.onError,
            audioOptions = AudioOptions(
                format = options.effectiveFormat,
                voice = options.voice,
            ),
        )
    }.current

    // Create/recreate client when options change
    val chatOptionsRef = useLatestRef(chatOptions)
    clientRef.current?.close()
    clientRef.current = ChatClient(chatOptionsRef.current)

    // Cleanup on unmount
    useUnmount {
        clientRef.current?.close()
    }

    // Helper functions
    val setAudio: (String) -> Unit = { a -> setAudioInternal(a.left()) }
    val setIsLoading: (Boolean) -> Unit = { loading -> setIsLoadingInternal(loading.left()) }
    val setError: (Throwable?) -> Unit = { err -> setErrorInternal(err.left()) }

    // Synthesize function
    val synthesize: SynthesizeFn = useCreation {
        { text: String, styleInstruction: String? ->
            if (text.isBlank()) return@useCreation

            setAudio("")
            setError(null)
            setIsLoading(true)

            // Build TTS messages:
            // - user message: style instruction (optional)
            // - assistant message: text to synthesize
            val messages = buildList<ChatMessage> {
                if (!styleInstruction.isNullOrBlank()) {
                    add(userMessage(styleInstruction))
                }
                add(assistantMessage(text))
            }

            asyncRun {
                try {
                    val client = clientRef.current
                        ?: throw IllegalStateException("TTS client not initialized")
                    val opts = optionsRef.current

                    if (opts.stream) {
                        // Streaming mode: accumulate PCM16 base64 chunks
                        var accumulatedAudio = ""

                        client.streamChat(messages).onEach { event ->
                            when (event) {
                                is StreamEvent.AudioDelta -> {
                                    accumulatedAudio += event.audioDataBase64
                                    withContext(Dispatchers.Main) {
                                        setAudio(accumulatedAudio)
                                    }
                                    // Notify chunk callback
                                    opts.onAudioChunk?.invoke(event.audioDataBase64)
                                }
                                is StreamEvent.Done -> {
                                    // Audio already accumulated
                                }
                                is StreamEvent.Error -> {
                                    if (event.error is CancellationException) throw event.error
                                    withContext(Dispatchers.Main) {
                                        setError(event.error)
                                    }
                                    opts.onError?.invoke(event.error)
                                }
                                else -> Unit // Ignore text deltas etc.
                            }
                        }.collect()
                    } else {
                        // Non-streaming mode: get complete WAV in one response
                        val result = client.chat(messages)
                        val audioData = result.audioDataBase64
                        if (audioData != null) {
                            withContext(Dispatchers.Main) {
                                setAudio(audioData)
                            }
                        } else {
                            throw Exception("No audio data in TTS response")
                        }
                    }

                    setIsLoading(false)
                } catch (e: Throwable) {
                    if (e is CancellationException) throw e
                    withContext(Dispatchers.Main) {
                        setError(e)
                    }
                    optionsRef.current.onError?.invoke(e)
                    setIsLoading(false)
                }
            }
        }
    }.current

    // Stop function
    val stop: () -> Unit = useCreation {
        {
            cancelAsync()
            setIsLoading(false)
        }
    }.current

    // Reset function
    val reset: () -> Unit = useCreation {
        {
            cancelAsync()
            setAudio("")
            setError(null)
            setIsLoading(false)
        }
    }.current

    return TtsHolder(
        audioDataBase64 = audioState,
        isLoading = isLoadingState,
        error = errorState,
        synthesize = synthesize,
        stop = stop,
        reset = reset,
    )
}

// endregion
