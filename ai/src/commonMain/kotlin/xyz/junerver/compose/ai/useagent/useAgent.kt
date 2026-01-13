package xyz.junerver.compose.ai.useagent

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import arrow.core.left
import kotlin.time.Duration
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonElement
import xyz.junerver.compose.ai.AIOptionsDefaults
import xyz.junerver.compose.ai.AppendMessageFn
import xyz.junerver.compose.ai.BaseAIOptions
import xyz.junerver.compose.ai.OnErrorCallback
import xyz.junerver.compose.ai.OnResponseCallback
import xyz.junerver.compose.ai.SendMessageFn
import xyz.junerver.compose.ai.SetMessagesFn
import xyz.junerver.compose.ai.StopFn
import xyz.junerver.compose.ai.http.HttpEngine
import xyz.junerver.compose.ai.usechat.ChatClient
import xyz.junerver.compose.ai.usechat.ChatMessage
import xyz.junerver.compose.ai.usechat.ChatProvider
import xyz.junerver.compose.ai.usechat.ChatUsage
import xyz.junerver.compose.ai.usechat.FinishReason
import xyz.junerver.compose.ai.usechat.ToolCallPart
import xyz.junerver.compose.ai.usechat.UserContentPart
import xyz.junerver.compose.ai.usechat.userMessage
import xyz.junerver.compose.hooks.MutableRef
import xyz.junerver.compose.hooks.Options
import xyz.junerver.compose.hooks._useGetState
import xyz.junerver.compose.hooks.useCancelableAsync
import xyz.junerver.compose.hooks.useLatestRef
import xyz.junerver.compose.hooks.useRef
import xyz.junerver.compose.hooks.useUnmount

/*
  Description: useAgent hook for tool calling + multi-turn chat
  Author: Junerver
  Date: 2026/01/07
  Email: junerver@gmail.com
  Version: v1.0
*/

typealias OnToolCallCallback = (toolCall: ToolCallPart) -> Unit
typealias OnToolResultCallback = (toolCallId: String, toolName: String, result: JsonElement, isError: Boolean) -> Unit
typealias OnAgentFinishCallback = (message: ChatMessage, usage: ChatUsage?, finishReason: FinishReason?) -> Unit

@Stable
data class AgentOptions internal constructor(
    override var provider: ChatProvider = AIOptionsDefaults.DEFAULT_PROVIDER,
    override var model: String? = null,
    override var systemPrompt: String? = null,
    var initialMessages: List<ChatMessage> = emptyList(),
    override var temperature: Float? = null,
    override var maxTokens: Int? = null,
    override var timeout: Duration = AIOptionsDefaults.DEFAULT_TIMEOUT,
    var stream: Boolean = false,
    override var headers: Map<String, String> = AIOptionsDefaults.DEFAULT_HEADERS,
    override var httpEngine: HttpEngine? = null,
    var tools: List<Tool<*>> = emptyList(),
    var toolChoice: ToolChoice = ToolChoice.Auto,
    var maxSteps: Int = 8,
    var parallelToolCalls: Boolean = true,
    override var onResponse: OnResponseCallback? = null,
    var onToolCall: OnToolCallCallback? = null,
    var onToolResult: OnToolResultCallback? = null,
    var onFinish: OnAgentFinishCallback? = null,
    override var onError: OnErrorCallback? = null,
) : BaseAIOptions {
    companion object : Options<AgentOptions>(::AgentOptions)

    internal fun toChatOptions(): xyz.junerver.compose.ai.usechat.ChatOptions = xyz.junerver.compose.ai.usechat.ChatOptions.optionOf {
        provider = this@AgentOptions.provider
        model = this@AgentOptions.model
        systemPrompt = this@AgentOptions.systemPrompt
        temperature = this@AgentOptions.temperature
        maxTokens = this@AgentOptions.maxTokens
        timeout = this@AgentOptions.timeout
        stream = this@AgentOptions.stream
        headers = this@AgentOptions.headers
        httpEngine = this@AgentOptions.httpEngine
        tools = this@AgentOptions.tools
        toolChoice = this@AgentOptions.toolChoice
        onResponse = this@AgentOptions.onResponse
        onError = this@AgentOptions.onError
    }
}

@Stable
data class AgentHolder(
    val messages: State<ImmutableList<ChatMessage>>,
    val isLoading: State<Boolean>,
    val error: State<Throwable?>,
    val send: SendMessageFn,
    val setMessages: SetMessagesFn,
    val append: AppendMessageFn,
    val stop: StopFn,
)

@Composable
fun useAgent(optionsOf: AgentOptions.() -> Unit = {}): AgentHolder {
    val options = remember { AgentOptions.optionOf(optionsOf) }.apply(optionsOf)
    val optionsRef = useLatestRef(options)

    val initialMessages = remember(options.initialMessages) {
        options.initialMessages.toImmutableList()
    }

    val (messagesState, setMessagesInternal, getMessages) = _useGetState<ImmutableList<ChatMessage>>(initialMessages)
    val (isLoadingState, setIsLoadingInternal, _) = _useGetState(false)
    val (errorState, setErrorInternal, _) = _useGetState<Throwable?>(null)

    val clientRef: MutableRef<ChatClient?> = useRef(null)
    val (asyncRun, cancelAsync, _) = useCancelableAsync()

    useUnmount {
        clientRef.current?.close()
    }

    val setMessages: (ImmutableList<ChatMessage>) -> Unit = { msgs -> setMessagesInternal(msgs.left()) }
    val setIsLoading: (Boolean) -> Unit = { loading -> setIsLoadingInternal(loading.left()) }
    val setError: (Throwable?) -> Unit = { error -> setErrorInternal(error.left()) }

    val setMessagesFn: SetMessagesFn = remember {
        { messages: List<ChatMessage> ->
            setMessages(messages.toImmutableList())
        }
    }

    val appendMessage: AppendMessageFn = remember {
        { message: ChatMessage ->
            val current = getMessages().toMutableList()
            current.add(message)
            setMessages(current.toImmutableList())
        }
    }

    val stop: StopFn = remember {
        {
            cancelAsync()
            setIsLoading(false)
        }
    }

    val send: SendMessageFn = remember {
        { content: List<UserContentPart> ->
            if (content.isEmpty()) return@remember

            setError(null)
            setIsLoading(true)

            asyncRun {
                val chatOptions = optionsRef.current.toChatOptions()
                clientRef.current?.close()
                val client = ChatClient(chatOptions).also { clientRef.current = it }
                try {
                    val localMessages = getMessages().toMutableList()
                    localMessages.add(userMessage(content))
                    withContext(Dispatchers.Main) {
                        setMessages(localMessages.toImmutableList())
                    }

                    runAgentLoop(
                        client = client,
                        messages = localMessages,
                        tools = optionsRef.current.tools,
                        maxSteps = optionsRef.current.maxSteps,
                        parallelToolCalls = optionsRef.current.parallelToolCalls,
                        stream = optionsRef.current.stream,
                        model = optionsRef.current.effectiveModel,
                        onAssistant = { response ->
                            response.message.toolCalls.forEach { optionsRef.current.onToolCall?.invoke(it) }
                            withContext(Dispatchers.Main) {
                                setMessages(localMessages.toImmutableList())
                            }

                            if (response.message.toolCalls.isEmpty()) {
                                optionsRef.current.onFinish?.invoke(
                                    response.message,
                                    response.usage,
                                    response.finishReason,
                                )
                            }
                        },
                        onAssistantPartial = {
                            withContext(Dispatchers.Main) {
                                setMessages(localMessages.toImmutableList())
                            }
                        },
                        onToolMessage = { toolMsg ->
                            withContext(Dispatchers.Main) {
                                setMessages(localMessages.toImmutableList())
                            }

                            val first = toolMsg.content.firstOrNull()
                            if (first != null) {
                                optionsRef.current.onToolResult?.invoke(
                                    first.toolCallId,
                                    first.toolName,
                                    first.result,
                                    first.isError,
                                )
                            }
                        },
                    )
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        setError(e)
                    }
                    optionsRef.current.onError?.invoke(e)
                } finally {
                    client.close()
                    withContext(Dispatchers.Main) {
                        setIsLoading(false)
                    }
                }
            }
        }
    }

    return remember {
        AgentHolder(
            messages = messagesState,
            isLoading = isLoadingState,
            error = errorState,
            send = send,
            setMessages = setMessagesFn,
            append = appendMessage,
            stop = stop,
        )
    }
}

@Composable
fun rememberAgent(optionsOf: AgentOptions.() -> Unit = {}): AgentHolder = useAgent(optionsOf)
