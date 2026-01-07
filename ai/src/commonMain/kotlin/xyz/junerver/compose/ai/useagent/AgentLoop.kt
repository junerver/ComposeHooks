package xyz.junerver.compose.ai.useagent

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import xyz.junerver.compose.ai.usechat.AssistantMessage
import xyz.junerver.compose.ai.usechat.ChatClient
import xyz.junerver.compose.ai.usechat.ChatMessage
import xyz.junerver.compose.ai.usechat.ChatResponseResult
import xyz.junerver.compose.ai.usechat.Providers
import xyz.junerver.compose.ai.usechat.ReasoningPart
import xyz.junerver.compose.ai.usechat.TextPart
import xyz.junerver.compose.ai.usechat.ToolCallPart
import xyz.junerver.compose.ai.usechat.ToolMessage
import xyz.junerver.compose.ai.usechat.assistantMessage
import xyz.junerver.compose.ai.usechat.toolMessage
import xyz.junerver.compose.ai.usechat.StreamEvent

/*
  Description: Agent loop for tool calling + multi-turn chat
  Author: Junerver
  Date: 2026/01/07
  Email: junerver@gmail.com
  Version: v1.0
*/

internal data class AgentStepResult(
    val assistant: AssistantMessage,
    val toolMessages: List<ToolMessage>,
)

internal suspend fun runAgentLoop(
    client: ChatClient,
    messages: MutableList<ChatMessage>,
    tools: List<Tool<*>>,
    maxSteps: Int,
    parallelToolCalls: Boolean,
    stream: Boolean,
    model: String,
    onAssistant: suspend (ChatResponseResult) -> Unit,
    onAssistantPartial: suspend (AssistantMessage) -> Unit = { },
    onToolMessage: suspend (ToolMessage) -> Unit,
): Unit {
    require(maxSteps > 0) { "maxSteps must be > 0" }

    var steps = 0
    while (true) {
        if (steps++ >= maxSteps) throw IllegalStateException("Agent exceeded maxSteps=$maxSteps")

        val response = if (stream) {
            streamChatToResult(
                client = client,
                messages = messages.toList(),
                model = model,
                onAssistantPartial = { msg ->
                    if (messages.isNotEmpty() && messages.last() is AssistantMessage) {
                        messages[messages.lastIndex] = msg
                    } else {
                        messages += msg
                    }
                    onAssistantPartial(msg)
                },
            )
        } else {
            client.chat(messages).also { messages += it.message }
        }
        onAssistant(response)

        val toolCalls = response.message.toolCalls
        if (toolCalls.isEmpty()) return

        val toolMessages = executeToolCalls(
            toolCalls = toolCalls,
            tools = tools,
            parallel = parallelToolCalls,
        )
        messages += toolMessages
        toolMessages.forEach { onToolMessage(it) }
    }
}

private suspend fun streamChatToResult(
    client: ChatClient,
    messages: List<ChatMessage>,
    model: String,
    onAssistantPartial: suspend (AssistantMessage) -> Unit,
): ChatResponseResult {
    var accumulatedText = ""
    var accumulatedReasoning = ""
    var lastUsage: xyz.junerver.compose.ai.usechat.ChatUsage? = null
    var lastFinishReason: xyz.junerver.compose.ai.usechat.FinishReason? = null

    data class ToolCallBuilder(
        var toolCallId: String? = null,
        var toolName: String? = null,
        val args: StringBuilder = StringBuilder(),
    )

    val toolCallBuilders = linkedMapOf<Int, ToolCallBuilder>()

    fun buildContentParts(): List<xyz.junerver.compose.ai.usechat.AssistantContentPart> {
        val parts = mutableListOf<xyz.junerver.compose.ai.usechat.AssistantContentPart>()

        if (accumulatedText.isNotEmpty() || (toolCallBuilders.isEmpty() && accumulatedReasoning.isEmpty())) {
            parts += TextPart(accumulatedText)
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

    var assistant = assistantMessage(
        text = "",
        model = model,
    )
    onAssistantPartial(assistant)

    client.streamChat(messages).collect { event ->
        when (event) {
            is StreamEvent.Delta -> {
                accumulatedText += event.content
                event.finishReason?.let { lastFinishReason = xyz.junerver.compose.ai.usechat.FinishReason.fromString(it) }
                event.usage?.let { lastUsage = it }
            }

            is StreamEvent.ReasoningDelta -> {
                accumulatedReasoning += event.text
            }

            is StreamEvent.ToolCallDelta -> {
                val builder = toolCallBuilders.getOrPut(event.index) { ToolCallBuilder() }
                if (!event.toolCallId.isNullOrBlank()) builder.toolCallId = event.toolCallId
                if (!event.toolName.isNullOrBlank()) builder.toolName = event.toolName
                if (!event.argumentsDelta.isNullOrEmpty()) builder.args.append(event.argumentsDelta)
            }

            is StreamEvent.Done -> return@collect
            is StreamEvent.Error -> throw event.error
            is StreamEvent.Multi -> Unit
        }

        assistant = assistant.copy(
            content = buildContentParts(),
            model = model,
            usage = lastUsage,
            finishReason = lastFinishReason,
        )
        onAssistantPartial(assistant)
    }

    return ChatResponseResult(
        message = assistant,
        usage = lastUsage,
        finishReason = lastFinishReason,
    )
}

private suspend fun executeToolCalls(
    toolCalls: List<ToolCallPart>,
    tools: List<Tool<*>>,
    parallel: Boolean,
): List<ToolMessage> = if (parallel && toolCalls.size > 1) {
    coroutineScope {
        toolCalls.map { call ->
            async {
                executeSingleToolCall(call, tools)
            }
        }.awaitAll()
    }
} else {
    toolCalls.map { call -> executeSingleToolCall(call, tools) }
}

private suspend fun executeSingleToolCall(call: ToolCallPart, tools: List<Tool<*>>): ToolMessage {
    val match = tools.firstOrNull { it.name == call.toolName }
    if (match == null) {
        return toolMessage(
            toolCallId = call.toolCallId,
            toolName = call.toolName,
            result = JsonPrimitive("Tool not found: ${call.toolName}"),
            isError = true,
        )
    }

    return try {
        val resultJson = executeTool(match, call.args)
        toolMessage(
            toolCallId = call.toolCallId,
            toolName = call.toolName,
            result = resultJson,
            isError = false,
        )
    } catch (e: Exception) {
        toolMessage(
            toolCallId = call.toolCallId,
            toolName = call.toolName,
            result = JsonPrimitive(e.message ?: "Tool execution failed"),
            isError = true,
        )
    }
}

@Suppress("UNCHECKED_CAST")
private suspend fun executeTool(tool: Tool<*>, args: JsonObject): JsonElement = (tool as Tool<Any?>).executeWithJson(args)
