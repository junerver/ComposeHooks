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
import xyz.junerver.compose.hooks.MutableRef
import xyz.junerver.compose.hooks._useGetState
import xyz.junerver.compose.hooks.useCancelableAsync
import xyz.junerver.compose.hooks.useEffect
import xyz.junerver.compose.hooks.useLatestRef
import xyz.junerver.compose.hooks.useRef
import xyz.junerver.compose.hooks.useUnmount

/*
  Description: useChat hook for OpenAI-compatible chat completions
  Author: Junerver
  Date: 2024
  Email: junerver@gmail.com
  Version: v1.0

  Inspired by Vercel AI SDK useChat hook.
  See: https://ai-sdk.dev/docs/reference/ai-sdk-ui/use-chat
*/

/**
 * Function type definitions for chat operations.
 */
typealias SendMessageFn = (content: String) -> Unit
typealias SetMessagesFn = (messages: List<Message>) -> Unit
typealias AppendMessageFn = (message: Message) -> Unit
typealias ReloadFn = () -> Unit
typealias StopFn = () -> Unit

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
 * @property sendMessage Function to send a new user message and trigger AI response
 * @property setMessages Function to directly set the messages list
 * @property append Function to append a single message without triggering AI response
 * @property reload Function to regenerate the last AI response
 * @property stop Function to cancel the current streaming response
 */
@Stable
data class ChatHolder(
    val messages: State<ImmutableList<Message>>,
    val isLoading: State<Boolean>,
    val error: State<Throwable?>,
    val sendMessage: SendMessageFn,
    val setMessages: SetMessagesFn,
    val append: AppendMessageFn,
    val reload: ReloadFn,
    val stop: StopFn,
)

/**
 * A Composable hook for managing chat conversations with OpenAI-compatible APIs.
 *
 * This hook provides a complete solution for building chat interfaces with:
 * - Streaming responses support
 * - Message state management
 * - Loading and error states
 * - Control functions (send, stop, reload)
 *
 * Example usage:
 * ```kotlin
 * val (messages, isLoading, error, sendMessage, _, _, reload, stop) = useChat {
 *     baseUrl = "https://api.openai.com/v1"
 *     apiKey = "your-api-key"
 *     model = "gpt-3.5-turbo"
 *     systemPrompt = "You are a helpful assistant."
 *     onFinish = { message, usage, reason ->
 *         println("Finished: ${message.content}")
 *     }
 * }
 *
 * // Send a message
 * sendMessage("Hello, how are you?")
 *
 * // Display messages
 * messages.value.forEach { message ->
 *     Text("${message.role}: ${message.content}")
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

    // Initialize messages with system prompt and initial messages
    val initialMessages = remember(options.systemPrompt, options.initialMessages) {
        buildList {
            options.systemPrompt?.let { add(Message.system(it)) }
            addAll(options.initialMessages)
        }.toImmutableList()
    }

    // State management using hooks module
    val (messagesState, setMessagesInternal, getMessages) = _useGetState<ImmutableList<Message>>(initialMessages)
    val (isLoadingState, setIsLoadingInternal, _) = _useGetState(false)
    val (errorState, setErrorInternal, _) = _useGetState<Throwable?>(null)

    // Refs for mutable state that shouldn't trigger recomposition
    val clientRef: MutableRef<ChatClient?> = useRef(null)
    val currentAssistantMessageRef: MutableRef<Message?> = useRef(null)

    // Cancelable async for streaming
    val (asyncRun, cancelAsync, _) = useCancelableAsync()

    // Initialize client
    useEffect(options.baseUrl, options.apiKey, options.model, options.timeout) {
        clientRef.current?.close()
        clientRef.current = ChatClient(optionsRef.current)
    }

    // Cleanup on unmount
    useUnmount {
        clientRef.current?.close()
    }

    // Helper functions to simplify state updates
    val setMessages: (ImmutableList<Message>) -> Unit = { msgs -> setMessagesInternal(msgs.left()) }
    val setIsLoading: (Boolean) -> Unit = { loading -> setIsLoadingInternal(loading.left()) }
    val setError: (Throwable?) -> Unit = { error -> setErrorInternal(error.left()) }

    // Send message function
    val sendMessage: SendMessageFn = remember {
        { content: String ->
            if (content.isBlank()) return@remember

            val userMessage = Message.user(content)
            val currentMessages = getMessages().toMutableList()
            currentMessages.add(userMessage)
            setMessages(currentMessages.toImmutableList())

            setError(null)
            setIsLoading(true)

            // Create placeholder for assistant message
            val assistantMessage = Message.assistant("")
            currentAssistantMessageRef.current = assistantMessage
            currentMessages.add(assistantMessage)
            setMessages(currentMessages.toImmutableList())

            asyncRun {
                var accumulatedContent = ""
                var lastUsage: ChatUsage? = null
                var lastFinishReason: FinishReason? = null

                try {
                    clientRef.current?.streamChat(currentMessages.dropLast(1))
                        ?.onEach { event ->
                            when (event) {
                                is StreamEvent.Delta -> {
                                    accumulatedContent += event.content
                                    optionsRef.current.onStream?.invoke(event.content)

                                    if (event.finishReason != null) {
                                        lastFinishReason = FinishReason.fromString(event.finishReason)
                                    }
                                    if (event.usage != null) {
                                        lastUsage = event.usage
                                    }

                                    // Update assistant message with accumulated content on Main thread
                                    val updatedMessage = currentAssistantMessageRef.current?.copy(
                                        content = accumulatedContent
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
                                            lastFinishReason
                                        )
                                    }
                                }

                                is StreamEvent.Error -> {
                                    withContext(Dispatchers.Main) {
                                        setError(event.error)
                                    }
                                    optionsRef.current.onError?.invoke(event.error)
                                }
                            }
                        }
                        ?.collect()
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
        { messages: List<Message> ->
            setMessages(messages.toImmutableList())
        }
    }

    // Append message function
    val appendMessage: AppendMessageFn = remember {
        { message: Message ->
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
            if (currentMessages.isNotEmpty() && currentMessages.last().role == Role.Assistant) {
                currentMessages.removeAt(currentMessages.lastIndex)
            }
            // Find the last user message
            val lastUserMessage = currentMessages.lastOrNull { it.role == Role.User }
            if (lastUserMessage != null) {
                // Remove it and resend
                currentMessages.removeAt(currentMessages.indexOfLast { it.role == Role.User })
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
