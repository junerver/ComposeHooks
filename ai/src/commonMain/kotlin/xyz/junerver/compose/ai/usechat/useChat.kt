package xyz.junerver.compose.ai.usechat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import arrow.core.left
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import xyz.junerver.compose.ai.AIOptionsDefaults
import xyz.junerver.compose.ai.AppendMessageFn
import xyz.junerver.compose.ai.ReloadFn
import xyz.junerver.compose.ai.SendMessageFn
import xyz.junerver.compose.ai.SetMessagesFn
import xyz.junerver.compose.ai.StopFn
import xyz.junerver.compose.ai.multiprovider.ModelsContext
import xyz.junerver.compose.ai.multiprovider.MultiProviderChatClient
import xyz.junerver.compose.hooks.MutableRef
import xyz.junerver.compose.hooks._useGetState
import xyz.junerver.compose.hooks.useCancelableAsync
import xyz.junerver.compose.hooks.useContext
import xyz.junerver.compose.hooks.useEffect
import xyz.junerver.compose.hooks.useLatestRef
import xyz.junerver.compose.hooks.useRef
import xyz.junerver.compose.hooks.useUnmount

/*
  Description: useChat hook for multi-provider chat completions (multimodal support)
  Author: Junerver
  Date: 2026/01/05-11:06
  Email: junerver@gmail.com
  Version: v3.0

  Inspired by Vercel AI SDK useChat hook.
  See: https://ai-sdk.dev/docs/reference/ai-sdk-ui/use-chat
*/

/**
 * Holder class for useChat hook return values.
 *
 * Following the project's convention for Holder classes:
 * - State values come first
 * - Functions come after
 *
 * @property messages The current list of messages in the conversation
 * @property isLoading Whether a request is currently in progress
 * @property error The most recent error, if any
 * @property sendMessage Function to send a new user message with multimodal content
 * @property setMessages Function to directly set the messages list
 * @property append Function to append a single message without triggering AI response
 * @property reload Function to regenerate the last AI response
 * @property stop Function to cancel the current streaming response
 */
@Stable
data class ChatHolder(
    val messages: State<ImmutableList<ChatMessage>>,
    val isLoading: State<Boolean>,
    val error: State<Throwable?>,
    val sendMessage: SendMessageFn,
    val setMessages: SetMessagesFn,
    val append: AppendMessageFn,
    val reload: ReloadFn,
    val stop: StopFn,
)

/**
 * Extension function to send a text-only message.
 */
fun ChatHolder.sendText(text: String) {
    sendMessage(listOf(TextPart(text)))
}

/**
 * Extension function to send a message with text and image.
 */
fun ChatHolder.sendWithImage(text: String, imageBase64: String, mimeType: String = "image/jpeg") {
    sendMessage(
        listOf(
            TextPart(text),
            ImagePart.fromBase64(imageBase64, mimeType),
        ),
    )
}

/**
 * Extension function to send a message with text and image URL.
 */
fun ChatHolder.sendWithImageUrl(text: String, imageUrl: String) {
    sendMessage(
        listOf(
            TextPart(text),
            ImagePart.fromUrl(imageUrl),
        ),
    )
}

/**
 * Extension function to send a message with text and file.
 */
fun ChatHolder.sendWithFile(
    text: String,
    fileBase64: String,
    mimeType: String,
    fileName: String? = null,
) {
    sendMessage(
        listOf(
            TextPart(text),
            FilePart(fileBase64, mimeType, fileName),
        ),
    )
}

/**
 * A Composable hook for managing chat conversations with multiple AI providers.
 *
 * This hook provides a complete solution for building chat interfaces with:
 * - Multi-provider support (OpenAI, DeepSeek, Anthropic, etc.)
 * - Multimodal support (text, images, files)
 * - Streaming responses support
 * - Message state management
 * - Loading and error states
 * - Control functions (send, stop, reload)
 *
 * Example usage:
 * ```kotlin
 * val (messages, isLoading, error, sendMessage, _, _, reload, stop) = useChat {
 *     provider = Providers.DeepSeek(apiKey = "your-api-key")
 *     model = "deepseek-chat" // optional, uses provider default if null
 *     systemPrompt = "You are a helpful assistant."
 *     onFinish = { message, usage, reason ->
 *         println("Finished: ${message.textContent}")
 *     }
 * }
 *
 * // Send a text message
 * sendMessage(listOf(ContentPart.Text("Hello, how are you?")))
 * // Or use extension function
 * chatHolder.sendText("Hello, how are you?")
 *
 * // Send a message with image
 * chatHolder.sendWithImage("What's in this image?", imageBase64)
 *
 * // Display messages
 * messages.value.forEach { message ->
 *     Text("${message.role}: ${message.textContent}")
 * }
 * ```
 *
 * @param optionsOf Configuration factory function for chat options
 * @return ChatHolder containing messages state and control functions
 */
@Composable
fun useChat(optionsOf: ChatOptions.() -> Unit = {}): ChatHolder {
    val options = remember { ChatOptions.optionOf(optionsOf) }.apply(optionsOf)
    val optionsRef = useLatestRef(options)

    // Check if we're in a ModelsProvider context
    val modelsContext = useContext(ModelsContext)

    // Determine if we should use multi-provider mode
    // Use multi-provider if:
    // 1. We're in a ModelsProvider context
    // 2. The context has providers
    // 3. The user didn't explicitly specify a provider (using default)
    val useMultiProvider = modelsContext.providers.isNotEmpty() &&
        options.provider == AIOptionsDefaults.DEFAULT_PROVIDER

    // Initialize messages with initial messages only (system prompt is handled internally)
    val initialMessages = remember(options.initialMessages) {
        options.initialMessages.toImmutableList()
    }

    // State management using hooks module
    val (messagesState, setMessagesInternal, getMessages) = _useGetState<ImmutableList<ChatMessage>>(initialMessages)
    val (isLoadingState, setIsLoadingInternal, _) = _useGetState(false)
    val (errorState, setErrorInternal, _) = _useGetState<Throwable?>(null)

    // Refs for mutable state that shouldn't trigger recomposition
    val clientRef: MutableRef<ChatClient?> = useRef(null)
    val multiProviderClientRef: MutableRef<MultiProviderChatClient?> = useRef(null)
    val currentAssistantMessageRef: MutableRef<AssistantMessage?> = useRef(null)

    // Cancelable async for streaming
    val (asyncRun, cancelAsync, _) = useCancelableAsync()

    // Initialize client (single or multi-provider)
    useEffect(options.provider, options.model, options.timeout, useMultiProvider, modelsContext) {
        clientRef.current?.close()
        multiProviderClientRef.current?.close()

        if (useMultiProvider) {
            // Use multi-provider client
            multiProviderClientRef.current = MultiProviderChatClient(
                providers = modelsContext.providers,
                strategy = modelsContext.strategy,
                retryConfig = modelsContext.retryConfig,
                baseOptions = optionsRef.current,
            )
        } else {
            // Use single-provider client
            clientRef.current = ChatClient(optionsRef.current)
        }
    }

    // Cleanup on unmount
    useUnmount {
        clientRef.current?.close()
        multiProviderClientRef.current?.close()
    }

    // Helper functions to simplify state updates
    val setMessages: (ImmutableList<ChatMessage>) -> Unit = { msgs -> setMessagesInternal(msgs.left()) }
    val setIsLoading: (Boolean) -> Unit = { loading -> setIsLoadingInternal(loading.left()) }
    val setError: (Throwable?) -> Unit = { error -> setErrorInternal(error.left()) }

    // Send message function (multimodal)
    val sendMessage: SendMessageFn = remember {
        { content: List<UserContentPart> ->
            if (content.isEmpty()) return@remember

            val userMsg = userMessage(content)
            val currentMessages = getMessages().toMutableList()
            currentMessages.add(userMsg)
            setMessages(currentMessages.toImmutableList())

            setError(null)
            setIsLoading(true)

            // Create placeholder for assistant message
            val assistantMsg = assistantMessage(
                text = "",
                model = optionsRef.current.effectiveModel,
            )
            currentAssistantMessageRef.current = assistantMsg
            currentMessages.add(assistantMsg)
            setMessages(currentMessages.toImmutableList())

            asyncRun {
                var accumulatedContent = ""
                var accumulatedReasoning = ""
                var lastUsage: ChatUsage? = null
                var lastFinishReason: FinishReason? = null

                data class ToolCallBuilder(
                    var toolCallId: String? = null,
                    var toolName: String? = null,
                    val args: StringBuilder = StringBuilder(),
                )

                val toolCallBuilders = linkedMapOf<Int, ToolCallBuilder>()

                fun currentContentParts(): List<AssistantContentPart> {
                    val parts = mutableListOf<AssistantContentPart>()

                    if (accumulatedContent.isNotEmpty() || (toolCallBuilders.isEmpty() && accumulatedReasoning.isEmpty())) {
                        parts += TextPart(accumulatedContent)
                    } else {
                        parts += TextPart("")
                    }

                    if (accumulatedReasoning.isNotEmpty()) {
                        parts += ReasoningPart(accumulatedReasoning)
                    }

                    toolCallBuilders.entries.sortedBy { it.key }.forEach { (index, builder) ->
                        val toolCallId = builder.toolCallId ?: "toolcall_$index"
                        val toolName = builder.toolName ?: "tool"
                        val argsJson: JsonObject = try {
                            val raw = builder.args.toString()
                            if (raw.isBlank()) {
                                buildJsonObject { }
                            } else {
                                Providers.json.parseToJsonElement(raw).jsonObject
                            }
                        } catch (_: Exception) {
                            buildJsonObject { }
                        }
                        parts += ToolCallPart(
                            toolCallId = toolCallId,
                            toolName = toolName,
                            args = argsJson,
                        )
                    }

                    return parts
                }

                try {
                    val streamEnabled = optionsRef.current.stream

                    if (streamEnabled) {
                        // Use multi-provider or single-provider client
                        val streamFlow = if (useMultiProvider) {
                            multiProviderClientRef.current?.streamChat(currentMessages.dropLast(1))
                                ?: throw IllegalStateException("MultiProviderChatClient not initialized")
                        } else {
                            clientRef.current?.streamChat(currentMessages.dropLast(1))
                                ?: throw IllegalStateException("ChatClient not initialized")
                        }

                        streamFlow.onEach { event ->
                            when (event) {
                                is StreamEvent.Delta -> {
                                    accumulatedContent += event.content
                                    if (event.content.isNotEmpty()) {
                                        optionsRef.current.onStream?.invoke(event.content)
                                    }

                                    if (event.finishReason != null) {
                                        lastFinishReason = FinishReason.fromString(event.finishReason)
                                    }
                                    if (event.usage != null) {
                                        lastUsage = event.usage
                                    }

                                    val updatedMessage = currentAssistantMessageRef.current?.copy(
                                        content = currentContentParts(),
                                        model = optionsRef.current.effectiveModel,
                                        usage = lastUsage,
                                        finishReason = lastFinishReason,
                                    )
                                    if (updatedMessage != null) {
                                        currentAssistantMessageRef.current = updatedMessage
                                        withContext(Dispatchers.Main) {
                                            val msgs = getMessages().toMutableList()
                                            if (msgs.isNotEmpty()) {
                                                msgs[msgs.lastIndex] = updatedMessage
                                                setMessages(msgs.toImmutableList())
                                            }
                                        }
                                    }
                                }

                                is StreamEvent.ReasoningDelta -> {
                                    accumulatedReasoning += event.text

                                    val updatedMessage = currentAssistantMessageRef.current?.copy(
                                        content = currentContentParts(),
                                        model = optionsRef.current.effectiveModel,
                                        usage = lastUsage,
                                        finishReason = lastFinishReason,
                                    )
                                    if (updatedMessage != null) {
                                        currentAssistantMessageRef.current = updatedMessage
                                        withContext(Dispatchers.Main) {
                                            val msgs = getMessages().toMutableList()
                                            if (msgs.isNotEmpty()) {
                                                msgs[msgs.lastIndex] = updatedMessage
                                                setMessages(msgs.toImmutableList())
                                            }
                                        }
                                    }
                                }

                                is StreamEvent.ToolCallDelta -> {
                                    val builder = toolCallBuilders.getOrPut(event.index) { ToolCallBuilder() }
                                    if (!event.toolCallId.isNullOrBlank()) builder.toolCallId = event.toolCallId
                                    if (!event.toolName.isNullOrBlank()) builder.toolName = event.toolName
                                    if (!event.argumentsDelta.isNullOrEmpty()) builder.args.append(event.argumentsDelta)

                                    val updatedMessage = currentAssistantMessageRef.current?.copy(
                                        content = currentContentParts(),
                                        model = optionsRef.current.effectiveModel,
                                        usage = lastUsage,
                                        finishReason = lastFinishReason,
                                    )
                                    if (updatedMessage != null) {
                                        currentAssistantMessageRef.current = updatedMessage
                                        withContext(Dispatchers.Main) {
                                            val msgs = getMessages().toMutableList()
                                            if (msgs.isNotEmpty()) {
                                                msgs[msgs.lastIndex] = updatedMessage
                                                setMessages(msgs.toImmutableList())
                                            }
                                        }
                                    }
                                }

                                is StreamEvent.Done -> {
                                    val finalMessage = currentAssistantMessageRef.current
                                    if (finalMessage != null) {
                                        optionsRef.current.onFinish?.invoke(
                                            finalMessage,
                                            lastUsage,
                                            lastFinishReason,
                                        )
                                    }
                                }

                                is StreamEvent.Error -> {
                                    withContext(Dispatchers.Main) {
                                        setError(event.error)
                                    }
                                    optionsRef.current.onError?.invoke(event.error)
                                }

                                is StreamEvent.Multi -> Unit
                            }
                        }
                            ?.collect()
                    } else {
                        // Use multi-provider or single-provider client
                        val result = if (useMultiProvider) {
                            multiProviderClientRef.current?.chat(currentMessages.dropLast(1))
                                ?: throw IllegalStateException("MultiProviderChatClient not initialized")
                        } else {
                            clientRef.current?.chat(currentMessages.dropLast(1))
                                ?: throw IllegalStateException("ChatClient not initialized")
                        }
                        val finalMessage = result.message
                        currentAssistantMessageRef.current = finalMessage

                        withContext(Dispatchers.Main) {
                            val msgs = getMessages().toMutableList()
                            if (msgs.isNotEmpty()) {
                                msgs[msgs.lastIndex] = finalMessage
                                setMessages(msgs.toImmutableList())
                            }
                        }
                        optionsRef.current.onFinish?.invoke(
                            finalMessage,
                            result.usage,
                            result.finishReason,
                        )
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        setError(e)
                    }
                    optionsRef.current.onError?.invoke(e)
                } finally {
                    withContext(Dispatchers.Main) {
                        setIsLoading(false)
                    }
                    currentAssistantMessageRef.current = null
                }
            }
        }
    }

    // Set messages function
    val setMessagesFn: SetMessagesFn = remember {
        { messages: List<ChatMessage> ->
            setMessages(messages.toImmutableList())
        }
    }

    // Append message function
    val appendMessage: AppendMessageFn = remember {
        { message: ChatMessage ->
            val current = getMessages().toMutableList()
            current.add(message)
            setMessages(current.toImmutableList())
        }
    }

    // Reload function - regenerate the last assistant response
    val reload: ReloadFn = remember {
        {
            val currentMessages = getMessages().toMutableList()
            // Remove the last assistant message if exists
            if (currentMessages.isNotEmpty() && currentMessages.last() is AssistantMessage) {
                currentMessages.removeAt(currentMessages.lastIndex)
            }
            // Find the last user message
            val lastUserMessage = currentMessages.lastOrNull { it is UserMessage } as? UserMessage
            if (lastUserMessage != null) {
                // Remove it and resend
                currentMessages.removeAt(currentMessages.indexOfLast { it is UserMessage })
                setMessages(currentMessages.toImmutableList())
                sendMessage(lastUserMessage.content)
            }
        }
    }

    // Stop function
    val stop: StopFn = remember {
        {
            cancelAsync()
            setIsLoading(false)
        }
    }

    return remember {
        ChatHolder(
            messages = messagesState,
            isLoading = isLoadingState,
            error = errorState,
            sendMessage = sendMessage,
            setMessages = setMessagesFn,
            append = appendMessage,
            reload = reload,
            stop = stop,
        )
    }
}

/**
 * Alias for useChat following the project's naming convention.
 */
@Composable
fun rememberChat(optionsOf: ChatOptions.() -> Unit = {}): ChatHolder = useChat(optionsOf)
