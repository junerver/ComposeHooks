package xyz.junerver.compose.ai.useasr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import arrow.core.left
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
import xyz.junerver.compose.ai.usechat.AsrOptions
import xyz.junerver.compose.ai.usechat.ChatClient
import xyz.junerver.compose.ai.usechat.ChatMessage
import xyz.junerver.compose.ai.usechat.ChatOptions
import xyz.junerver.compose.ai.usechat.ChatProvider
import xyz.junerver.compose.ai.usechat.InputAudioPart
import xyz.junerver.compose.ai.usechat.StreamEvent
import xyz.junerver.compose.ai.usechat.userMessage
import xyz.junerver.compose.hooks._useGetState
import xyz.junerver.compose.hooks.useCancelableAsync
import xyz.junerver.compose.hooks.useLatestRef
import xyz.junerver.compose.hooks.useRef
import xyz.junerver.compose.hooks.useUnmount

/*
  Description: useAsr hook for speech recognition (ASR) via OpenAI-compatible API
  Author: Junerver
  Date: 2026/06/12
  Email: junerver@gmail.com
  Version: v1.0

  Uses the MiMo-V2.5-ASR model (or any OpenAI-compatible ASR endpoint).
  Endpoint: POST /v1/chat/completions with input_audio content type.
*/

// region Options

/**
 * Configuration options for the useAsr hook.
 *
 * @property provider The chat provider to use (includes apiKey, baseUrl, model)
 * @property model Override the provider's default model (null = use "mimo-v2.5-asr")
 * @property language Recognition language: "auto", "zh", or "en"
 * @property timeout Request timeout duration
 * @property headers Additional HTTP headers
 * @property httpEngine Custom HTTP engine
 * @property onResponse Callback when receiving an HTTP response
 * @property onError Callback when an error occurs
 */
@Stable
data class AsrOptionsConfig(
    var provider: ChatProvider = AIOptionsDefaults.DEFAULT_PROVIDER,
    var model: String? = null,
    var language: String = "auto",
    var timeout: Duration = 60.seconds,
    var headers: Map<String, String> = AIOptionsDefaults.DEFAULT_HEADERS,
    var httpEngine: HttpEngine? = null,
    var onResponse: OnResponseCallback? = null,
    var onError: OnErrorCallback? = null,
) {
    /** The effective model (override or default ASR model). */
    val effectiveModel: String
        get() = model ?: "mimo-v2.5-asr"
}

// endregion

// region Holder

/**
 * Holder class for useAsr hook return values.
 *
 * @property text The recognized text from the last ASR request
 * @property isLoading Whether a recognition request is currently in progress
 * @property error The most recent error, if any
 * @property recognize Function to recognize speech from audio data
 * @property reset Function to clear the recognized text and error state
 */
@Stable
data class AsrHolder(
    val text: State<String>,
    val isLoading: State<Boolean>,
    val error: State<Throwable?>,
    val recognize: RecognizeFn,
    val reset: () -> Unit,
)

/**
 * Function to start speech recognition.
 *
 * @param audioDataUrl Audio data in data URL format: `data:{MIME};base64,{BASE64_AUDIO}`
 *                     Supported formats: mp3 (audio/mpeg), wav (audio/wav)
 */
typealias RecognizeFn = (audioDataUrl: String) -> Unit

// endregion

// region Hook

/**
 * A Composable hook for speech recognition (ASR) using OpenAI-compatible APIs.
 *
 * Sends audio data to the ASR endpoint and returns the recognized text.
 * Supports streaming responses for real-time text output.
 *
 * Example usage:
 * ```kotlin
 * val (text, isLoading, error, recognize, reset) = useAsr {
 *     provider = Providers.MiMo(apiKey = "your-api-key")
 *     language = "zh" // Chinese
 * }
 *
 * // Recognize speech from base64 audio
 * recognize("data:audio/wav;base64,$base64Audio")
 *
 * // Display result
 * Text("Recognized: ${text.value}")
 * ```
 *
 * @param optionsOf Configuration factory function for ASR options
 * @return AsrHolder containing recognized text state and control functions
 */
@Composable
fun useAsr(optionsOf: AsrOptionsConfig.() -> Unit = {}): AsrHolder {
    val options = remember { AsrOptionsConfig() }.apply(optionsOf)
    val optionsRef = useLatestRef(options)

    // State management
    val (textState, setTextInternal, getText) = _useGetState("")
    val (isLoadingState, setIsLoadingInternal, _) = _useGetState(false)
    val (errorState, setErrorInternal, _) = _useGetState<Throwable?>(null)

    // Refs
    val clientRef = useRef<ChatClient?>(null)

    // Cancelable async
    val (asyncRun, cancelAsync, _) = useCancelableAsync()

    // Initialize client
    val chatOptions = remember(options.provider, options.model, options.timeout, options.language) {
        ChatOptions(
            provider = options.provider,
            model = options.effectiveModel,
            stream = true, // ASR supports streaming
            timeout = options.timeout,
            headers = options.headers,
            httpEngine = options.httpEngine,
            onResponse = options.onResponse,
            onError = options.onError,
            asrOptions = AsrOptions(language = options.language),
        )
    }

    // Create/recreate client when options change
    val chatOptionsRef = useLatestRef(chatOptions)
    clientRef.current?.close()
    clientRef.current = ChatClient(chatOptionsRef.current)

    // Cleanup on unmount
    useUnmount {
        clientRef.current?.close()
    }

    // Helper functions
    val setText: (String) -> Unit = { t -> setTextInternal(t.left()) }
    val setIsLoading: (Boolean) -> Unit = { loading -> setIsLoadingInternal(loading.left()) }
    val setError: (Throwable?) -> Unit = { err -> setErrorInternal(err.left()) }

    // Recognize function
    val recognize: RecognizeFn = remember {
        { audioDataUrl: String ->
            if (audioDataUrl.isBlank()) return@remember

            setText("")
            setError(null)
            setIsLoading(true)

            // Build messages with input audio
            val messages = listOf<ChatMessage>(
                userMessage(listOf(InputAudioPart(audioDataUrl))),
            )

            asyncRun {
                try {
                    val client = clientRef.current
                        ?: throw IllegalStateException("ASR client not initialized")

                    var accumulatedText = ""

                    // Use streaming for real-time text output
                    client.streamChat(messages).onEach { event ->
                        when (event) {
                            is StreamEvent.Delta -> {
                                accumulatedText += event.content
                                withContext(Dispatchers.Main) {
                                    setText(accumulatedText)
                                }
                            }
                            is StreamEvent.Done -> {
                                // Final text already accumulated
                            }
                            is StreamEvent.Error -> {
                                withContext(Dispatchers.Main) {
                                    setError(event.error)
                                }
                                optionsRef.current.onError?.invoke(event.error)
                            }
                            else -> Unit // Ignore other event types
                        }
                    }.collect()

                    setIsLoading(false)
                } catch (e: Throwable) {
                    withContext(Dispatchers.Main) {
                        setError(e)
                    }
                    optionsRef.current.onError?.invoke(e)
                    setIsLoading(false)
                }
            }
        }
    }

    // Reset function
    val reset: () -> Unit = remember {
        {
            cancelAsync()
            setText("")
            setError(null)
            setIsLoading(false)
        }
    }

    return AsrHolder(
        text = textState,
        isLoading = isLoadingState,
        error = errorState,
        recognize = recognize,
        reset = reset,
    )
}

// endregion
