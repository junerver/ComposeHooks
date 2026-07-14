package xyz.junerver.compose.ai.useasr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import arrow.core.left
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
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
import xyz.junerver.compose.hooks.useCreation
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
    val options = useCreation { AsrOptionsConfig() }.current.apply(optionsOf)
    val optionsRef = useLatestRef(options)

    // State management
    val (textState, setTextInternal, _) = _useGetState("")
    val (isLoadingState, setIsLoadingInternal, _) = _useGetState(false)
    val (errorState, setErrorInternal, _) = _useGetState<Throwable?>(null)

    // Refs
    val clientRef = useRef<ChatClient?>(null)

    // Cancelable async
    val (asyncRun, cancelAsync, _) = useCancelableAsync()

    // Cleanup on unmount
    useUnmount {
        clientRef.current?.close()
    }

    // Helper functions
    val setText: (String) -> Unit = { t -> setTextInternal(t.left()) }
    val setIsLoading: (Boolean) -> Unit = { loading -> setIsLoadingInternal(loading.left()) }
    val setError: (Throwable?) -> Unit = { err -> setErrorInternal(err.left()) }

    // Recognize function
    val recognize: RecognizeFn = useCreation {
        { audioDataUrl: String ->
            if (audioDataUrl.isBlank()) return@useCreation

            setText("")
            setError(null)
            setIsLoading(true)

            // Build messages with input audio
            val messages = listOf<ChatMessage>(
                userMessage(listOf(InputAudioPart(audioDataUrl))),
            )

            asyncRun {
                val requestOptions = optionsRef.current.copy()
                val client = ChatClient(
                    ChatOptions(
                        provider = requestOptions.provider,
                        model = requestOptions.effectiveModel,
                        stream = true,
                        timeout = requestOptions.timeout,
                        headers = requestOptions.headers,
                        httpEngine = requestOptions.httpEngine,
                        onResponse = requestOptions.onResponse,
                        onError = requestOptions.onError,
                        asrOptions = AsrOptions(language = requestOptions.language),
                    ),
                )
                clientRef.current = client
                try {
                    var accumulatedText = ""

                    // Use streaming for real-time text output
                    client.streamChat(messages).onEach { event ->
                        when (event) {
                            is StreamEvent.Delta -> {
                                accumulatedText += event.content
                                setText(accumulatedText)
                            }
                            is StreamEvent.Done -> {
                                // Final text already accumulated
                            }
                            is StreamEvent.Error -> {
                                if (event.error is CancellationException) throw event.error
                                setError(event.error)
                                requestOptions.onError?.invoke(event.error)
                            }
                            else -> Unit // Ignore other event types
                        }
                    }.collect()
                } catch (e: Throwable) {
                    if (e is CancellationException) throw e
                    setError(e)
                    requestOptions.onError?.invoke(e)
                } finally {
                    client.close()
                    if (clientRef.current === client) clientRef.current = null
                    setIsLoading(false)
                }
            }
        }
    }.current

    // Reset function
    val reset: () -> Unit = useCreation {
        {
            cancelAsync()
            setText("")
            setError(null)
            setIsLoading(false)
        }
    }.current

    return useCreation(textState, isLoadingState, errorState, recognize, reset) {
        AsrHolder(
            text = textState,
            isLoading = isLoadingState,
            error = errorState,
            recognize = recognize,
            reset = reset,
        )
    }.current
}

// endregion
