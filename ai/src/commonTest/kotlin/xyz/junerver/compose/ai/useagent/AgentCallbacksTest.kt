package xyz.junerver.compose.ai.useagent

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import xyz.junerver.compose.ai.http.HttpResult
import xyz.junerver.compose.ai.test.FakeHttpEngine
import xyz.junerver.compose.ai.usechat.ChatClient
import xyz.junerver.compose.ai.usechat.ChatOptions
import xyz.junerver.compose.ai.usechat.ChatResponseResult
import xyz.junerver.compose.ai.usechat.ChatUsage
import xyz.junerver.compose.ai.usechat.FinishReason
import xyz.junerver.compose.ai.usechat.Providers
import xyz.junerver.compose.ai.usechat.ToolCallPart
import xyz.junerver.compose.ai.usechat.ToolMessage
import xyz.junerver.compose.ai.usechat.userMessage

class AgentCallbacksTest {

    @Test
    fun callbacks_onToolCall_invokedForEachTool() = runTest {
        val engine = FakeHttpEngine().apply {
            enqueueResponse(
                HttpResult(
                    statusCode = 200,
                    body =
                        """
                        {
                          "id": "chatcmpl-1",
                          "object": "chat.completion",
                          "created": 0,
                          "model": "gpt-4",
                          "choices": [{
                            "index": 0,
                            "message": {
                              "role": "assistant",
                              "content": null,
                              "tool_calls": [{
                                "id": "call_1",
                                "type": "function",
                                "function": {
                                  "name": "tool1",
                                  "arguments": "{\"value\":\"a\"}"
                                }
                              }, {
                                "id": "call_2",
                                "type": "function",
                                "function": {
                                  "name": "tool2",
                                  "arguments": "{\"value\":\"b\"}"
                                }
                              }]
                            },
                            "finish_reason": "tool_calls"
                          }],
                          "usage": { "prompt_tokens": 10, "completion_tokens": 3, "total_tokens": 13 }
                        }
                        """.trimIndent(),
                ),
            )
            enqueueResponse(
                HttpResult(
                    statusCode = 200,
                    body =
                        """
                        {
                          "id": "chatcmpl-2",
                          "object": "chat.completion",
                          "created": 0,
                          "model": "gpt-4",
                          "choices": [{
                            "index": 0,
                            "message": {
                              "role": "assistant",
                              "content": "Done."
                            },
                            "finish_reason": "stop"
                          }],
                          "usage": { "prompt_tokens": 20, "completion_tokens": 7, "total_tokens": 27 }
                        }
                        """.trimIndent(),
                ),
            )
        }

        @Serializable
        data class ToolParams(val value: String)

        val tool1 = tool<ToolParams>(
            name = "tool1",
            description = "Tool 1",
            parameters = buildJsonObject {
                put("type", JsonPrimitive("object"))
                put(
                    "properties",
                    buildJsonObject {
                        put(
                            "value",
                            buildJsonObject {
                                put("type", JsonPrimitive("string"))
                            },
                        )
                    },
                )
            },
        ) { params ->
            buildJsonObject {
                put("result", JsonPrimitive(params.value))
            }
        }

        val tool2 = tool<ToolParams>(
            name = "tool2",
            description = "Tool 2",
            parameters = buildJsonObject {
                put("type", JsonPrimitive("object"))
                put(
                    "properties",
                    buildJsonObject {
                        put(
                            "value",
                            buildJsonObject {
                                put("type", JsonPrimitive("string"))
                            },
                        )
                    },
                )
            },
        ) { params ->
            buildJsonObject {
                put("result", JsonPrimitive(params.value))
            }
        }

        val options = ChatOptions.optionOf {
            provider = Providers.OpenAI(apiKey = "test")
            httpEngine = engine
            tools = listOf(tool1, tool2)
            toolChoice = ToolChoice.Auto
        }
        val client = ChatClient(options)

        val toolCalls = mutableListOf<ToolCallPart>()

        runAgentLoop(
            client = client,
            messages = mutableListOf(userMessage("test")),
            tools = listOf(tool1, tool2),
            maxSteps = 5,
            parallelToolCalls = false,
            stream = false,
            model = "gpt-4",
            onAssistant = { response ->
                toolCalls.addAll(response.message.toolCalls)
            },
            onToolMessage = { },
        )

        assertEquals(2, toolCalls.size)
        assertEquals("call_1", toolCalls[0].toolCallId)
        assertEquals("tool1", toolCalls[0].toolName)
        assertEquals("call_2", toolCalls[1].toolCallId)
        assertEquals("tool2", toolCalls[1].toolName)
    }

    @Test
    fun callbacks_onToolCall_receivesCorrectArguments() = runTest {
        val engine = FakeHttpEngine().apply {
            enqueueResponse(
                HttpResult(
                    statusCode = 200,
                    body =
                        """
                        {
                          "id": "chatcmpl-1",
                          "object": "chat.completion",
                          "created": 0,
                          "model": "gpt-4",
                          "choices": [{
                            "index": 0,
                            "message": {
                              "role": "assistant",
                              "content": null,
                              "tool_calls": [{
                                "id": "call_1",
                                "type": "function",
                                "function": {
                                  "name": "test_tool",
                                  "arguments": "{\"city\":\"Paris\",\"units\":\"celsius\"}"
                                }
                              }]
                            },
                            "finish_reason": "tool_calls"
                          }],
                          "usage": { "prompt_tokens": 10, "completion_tokens": 3, "total_tokens": 13 }
                        }
                        """.trimIndent(),
                ),
            )
            enqueueResponse(
                HttpResult(
                    statusCode = 200,
                    body =
                        """
                        {
                          "id": "chatcmpl-2",
                          "object": "chat.completion",
                          "created": 0,
                          "model": "gpt-4",
                          "choices": [{
                            "index": 0,
                            "message": {
                              "role": "assistant",
                              "content": "Done."
                            },
                            "finish_reason": "stop"
                          }],
                          "usage": { "prompt_tokens": 20, "completion_tokens": 7, "total_tokens": 27 }
                        }
                        """.trimIndent(),
                ),
            )
        }

        @Serializable
        data class WeatherParams(val city: String, val units: String)

        val testTool = tool<WeatherParams>(
            name = "test_tool",
            description = "Test tool",
            parameters = buildJsonObject { },
        ) { params ->
            buildJsonObject {
                put("city", JsonPrimitive(params.city))
                put("units", JsonPrimitive(params.units))
            }
        }

        val options = ChatOptions.optionOf {
            provider = Providers.OpenAI(apiKey = "test")
            httpEngine = engine
            tools = listOf(testTool)
            toolChoice = ToolChoice.Auto
        }
        val client = ChatClient(options)

        var capturedToolCall: ToolCallPart? = null

        runAgentLoop(
            client = client,
            messages = mutableListOf(userMessage("test")),
            tools = listOf(testTool),
            maxSteps = 5,
            parallelToolCalls = false,
            stream = false,
            model = "gpt-4",
            onAssistant = { response ->
                if (capturedToolCall == null && response.message.toolCalls.isNotEmpty()) {
                    capturedToolCall = response.message.toolCalls.firstOrNull()
                }
            },
            onToolMessage = { },
        )

        assertNotNull(capturedToolCall)
        assertEquals("Paris", capturedToolCall!!.args.jsonObject["city"]?.jsonPrimitive?.content)
        assertEquals("celsius", capturedToolCall!!.args.jsonObject["units"]?.jsonPrimitive?.content)
    }

    @Test
    fun callbacks_onToolResult_invokedAfterExecution() = runTest {
        val engine = FakeHttpEngine().apply {
            enqueueResponse(
                HttpResult(
                    statusCode = 200,
                    body =
                        """
                        {
                          "id": "chatcmpl-1",
                          "object": "chat.completion",
                          "created": 0,
                          "model": "gpt-4",
                          "choices": [{
                            "index": 0,
                            "message": {
                              "role": "assistant",
                              "content": null,
                              "tool_calls": [{
                                "id": "call_1",
                                "type": "function",
                                "function": {
                                  "name": "test_tool",
                                  "arguments": "{}"
                                }
                              }]
                            },
                            "finish_reason": "tool_calls"
                          }],
                          "usage": { "prompt_tokens": 10, "completion_tokens": 3, "total_tokens": 13 }
                        }
                        """.trimIndent(),
                ),
            )
            enqueueResponse(
                HttpResult(
                    statusCode = 200,
                    body =
                        """
                        {
                          "id": "chatcmpl-2",
                          "object": "chat.completion",
                          "created": 0,
                          "model": "gpt-4",
                          "choices": [{
                            "index": 0,
                            "message": {
                              "role": "assistant",
                              "content": "Done."
                            },
                            "finish_reason": "stop"
                          }],
                          "usage": { "prompt_tokens": 20, "completion_tokens": 7, "total_tokens": 27 }
                        }
                        """.trimIndent(),
                ),
            )
        }

        val testTool = tool<Unit>(
            name = "test_tool",
            description = "Test tool",
            parameters = buildJsonObject { },
        ) {
            buildJsonObject {
                put("success", JsonPrimitive(true))
            }
        }

        val options = ChatOptions.optionOf {
            provider = Providers.OpenAI(apiKey = "test")
            httpEngine = engine
            tools = listOf(testTool)
            toolChoice = ToolChoice.Auto
        }
        val client = ChatClient(options)

        val toolMessages = mutableListOf<ToolMessage>()

        runAgentLoop(
            client = client,
            messages = mutableListOf(userMessage("test")),
            tools = listOf(testTool),
            maxSteps = 5,
            parallelToolCalls = false,
            stream = false,
            model = "gpt-4",
            onAssistant = { },
            onToolMessage = { toolMessages += it },
        )

        assertEquals(1, toolMessages.size)
        val result = toolMessages.first().content.first()
        assertEquals(false, result.isError)
        assertEquals(true, result.result.jsonObject["success"]?.jsonPrimitive?.content?.toBoolean())
    }

    @Test
    fun callbacks_onToolResult_receivesErrorFlag() = runTest {
        val engine = FakeHttpEngine().apply {
            enqueueResponse(
                HttpResult(
                    statusCode = 200,
                    body =
                        """
                        {
                          "id": "chatcmpl-1",
                          "object": "chat.completion",
                          "created": 0,
                          "model": "gpt-4",
                          "choices": [{
                            "index": 0,
                            "message": {
                              "role": "assistant",
                              "content": null,
                              "tool_calls": [{
                                "id": "call_1",
                                "type": "function",
                                "function": {
                                  "name": "failing_tool",
                                  "arguments": "{}"
                                }
                              }]
                            },
                            "finish_reason": "tool_calls"
                          }],
                          "usage": { "prompt_tokens": 10, "completion_tokens": 3, "total_tokens": 13 }
                        }
                        """.trimIndent(),
                ),
            )
            enqueueResponse(
                HttpResult(
                    statusCode = 200,
                    body =
                        """
                        {
                          "id": "chatcmpl-2",
                          "object": "chat.completion",
                          "created": 0,
                          "model": "gpt-4",
                          "choices": [{
                            "index": 0,
                            "message": {
                              "role": "assistant",
                              "content": "Error handled."
                            },
                            "finish_reason": "stop"
                          }],
                          "usage": { "prompt_tokens": 20, "completion_tokens": 7, "total_tokens": 27 }
                        }
                        """.trimIndent(),
                ),
            )
        }

        val failingTool = tool<Unit>(
            name = "failing_tool",
            description = "Failing tool",
            parameters = buildJsonObject { },
        ) {
            throw RuntimeException("Tool failed")
        }

        val options = ChatOptions.optionOf {
            provider = Providers.OpenAI(apiKey = "test")
            httpEngine = engine
            tools = listOf(failingTool)
            toolChoice = ToolChoice.Auto
        }
        val client = ChatClient(options)

        val toolMessages = mutableListOf<ToolMessage>()

        runAgentLoop(
            client = client,
            messages = mutableListOf(userMessage("test")),
            tools = listOf(failingTool),
            maxSteps = 5,
            parallelToolCalls = false,
            stream = false,
            model = "gpt-4",
            onAssistant = { },
            onToolMessage = { toolMessages += it },
        )

        assertEquals(1, toolMessages.size)
        val result = toolMessages.first().content.first()
        assertTrue(result.isError)
        assertTrue(result.result.jsonPrimitive.content.contains("Tool failed"))
    }

    @Test
    fun callbacks_onFinish_invokedWhenNoToolCalls() = runTest {
        val engine = FakeHttpEngine().apply {
            enqueueResponse(
                HttpResult(
                    statusCode = 200,
                    body =
                        """
                        {
                          "id": "chatcmpl-1",
                          "object": "chat.completion",
                          "created": 0,
                          "model": "gpt-4",
                          "choices": [{
                            "index": 0,
                            "message": {
                              "role": "assistant",
                              "content": "Hello! How can I help you?"
                            },
                            "finish_reason": "stop"
                          }],
                          "usage": { "prompt_tokens": 10, "completion_tokens": 7, "total_tokens": 17 }
                        }
                        """.trimIndent(),
                ),
            )
        }

        val testTool = tool<Unit>(
            name = "test_tool",
            description = "Test tool",
            parameters = buildJsonObject { },
        ) {
            buildJsonObject { }
        }

        val options = ChatOptions.optionOf {
            provider = Providers.OpenAI(apiKey = "test")
            httpEngine = engine
            tools = listOf(testTool)
            toolChoice = ToolChoice.Auto
        }
        val client = ChatClient(options)

        var finishCalled = false
        var assistantCallCount = 0

        runAgentLoop(
            client = client,
            messages = mutableListOf(userMessage("test")),
            tools = listOf(testTool),
            maxSteps = 5,
            parallelToolCalls = false,
            stream = false,
            model = "gpt-4",
            onAssistant = { response ->
                assistantCallCount++
                if (response.message.toolCalls.isEmpty()) {
                    finishCalled = true
                }
            },
            onToolMessage = { },
        )

        assertEquals(1, assistantCallCount)
        assertTrue(finishCalled)
    }

    @Test
    fun callbacks_onFinish_notInvokedWhenToolCallsPresent() = runTest {
        val engine = FakeHttpEngine().apply {
            enqueueResponse(
                HttpResult(
                    statusCode = 200,
                    body =
                        """
                        {
                          "id": "chatcmpl-1",
                          "object": "chat.completion",
                          "created": 0,
                          "model": "gpt-4",
                          "choices": [{
                            "index": 0,
                            "message": {
                              "role": "assistant",
                              "content": null,
                              "tool_calls": [{
                                "id": "call_1",
                                "type": "function",
                                "function": {
                                  "name": "test_tool",
                                  "arguments": "{}"
                                }
                              }]
                            },
                            "finish_reason": "tool_calls"
                          }],
                          "usage": { "prompt_tokens": 10, "completion_tokens": 3, "total_tokens": 13 }
                        }
                        """.trimIndent(),
                ),
            )
            enqueueResponse(
                HttpResult(
                    statusCode = 200,
                    body =
                        """
                        {
                          "id": "chatcmpl-2",
                          "object": "chat.completion",
                          "created": 0,
                          "model": "gpt-4",
                          "choices": [{
                            "index": 0,
                            "message": {
                              "role": "assistant",
                              "content": "Done."
                            },
                            "finish_reason": "stop"
                          }],
                          "usage": { "prompt_tokens": 20, "completion_tokens": 7, "total_tokens": 27 }
                        }
                        """.trimIndent(),
                ),
            )
        }

        val testTool = tool<Unit>(
            name = "test_tool",
            description = "Test tool",
            parameters = buildJsonObject { },
        ) {
            buildJsonObject { }
        }

        val options = ChatOptions.optionOf {
            provider = Providers.OpenAI(apiKey = "test")
            httpEngine = engine
            tools = listOf(testTool)
            toolChoice = ToolChoice.Auto
        }
        val client = ChatClient(options)

        var finishCalledWithToolCalls = false
        var finishCalledWithoutToolCalls = false

        runAgentLoop(
            client = client,
            messages = mutableListOf(userMessage("test")),
            tools = listOf(testTool),
            maxSteps = 5,
            parallelToolCalls = false,
            stream = false,
            model = "gpt-4",
            onAssistant = { response ->
                if (response.message.toolCalls.isNotEmpty()) {
                    finishCalledWithToolCalls = true
                } else {
                    finishCalledWithoutToolCalls = true
                }
            },
            onToolMessage = { },
        )

        assertTrue(finishCalledWithToolCalls)
        assertTrue(finishCalledWithoutToolCalls)
    }

    @Test
    fun callbacks_onFinish_receivesUsageAndFinishReason() = runTest {
        val engine = FakeHttpEngine().apply {
            enqueueResponse(
                HttpResult(
                    statusCode = 200,
                    body =
                        """
                        {
                          "id": "chatcmpl-1",
                          "object": "chat.completion",
                          "created": 0,
                          "model": "gpt-4",
                          "choices": [{
                            "index": 0,
                            "message": {
                              "role": "assistant",
                              "content": "Hello!"
                            },
                            "finish_reason": "stop"
                          }],
                          "usage": { "prompt_tokens": 10, "completion_tokens": 7, "total_tokens": 17 }
                        }
                        """.trimIndent(),
                ),
            )
        }

        val testTool = tool<Unit>(
            name = "test_tool",
            description = "Test tool",
            parameters = buildJsonObject { },
        ) {
            buildJsonObject { }
        }

        val options = ChatOptions.optionOf {
            provider = Providers.OpenAI(apiKey = "test")
            httpEngine = engine
            tools = listOf(testTool)
            toolChoice = ToolChoice.Auto
        }
        val client = ChatClient(options)

        var capturedUsage: ChatUsage? = null
        var capturedFinishReason: FinishReason? = null

        runAgentLoop(
            client = client,
            messages = mutableListOf(userMessage("test")),
            tools = listOf(testTool),
            maxSteps = 5,
            parallelToolCalls = false,
            stream = false,
            model = "gpt-4",
            onAssistant = { response ->
                if (response.message.toolCalls.isEmpty()) {
                    capturedUsage = response.usage
                    capturedFinishReason = response.finishReason
                }
            },
            onToolMessage = { },
        )

        assertNotNull(capturedUsage)
        assertEquals(10, capturedUsage!!.promptTokens)
        assertEquals(7, capturedUsage!!.completionTokens)
        assertEquals(17, capturedUsage!!.totalTokens)
        assertEquals(FinishReason.Stop, capturedFinishReason)
    }

    @Test
    fun callbacks_executionOrder_isCorrect() = runTest {
        val engine = FakeHttpEngine().apply {
            enqueueResponse(
                HttpResult(
                    statusCode = 200,
                    body =
                        """
                        {
                          "id": "chatcmpl-1",
                          "object": "chat.completion",
                          "created": 0,
                          "model": "gpt-4",
                          "choices": [{
                            "index": 0,
                            "message": {
                              "role": "assistant",
                              "content": null,
                              "tool_calls": [{
                                "id": "call_1",
                                "type": "function",
                                "function": {
                                  "name": "test_tool",
                                  "arguments": "{}"
                                }
                              }]
                            },
                            "finish_reason": "tool_calls"
                          }],
                          "usage": { "prompt_tokens": 10, "completion_tokens": 3, "total_tokens": 13 }
                        }
                        """.trimIndent(),
                ),
            )
            enqueueResponse(
                HttpResult(
                    statusCode = 200,
                    body =
                        """
                        {
                          "id": "chatcmpl-2",
                          "object": "chat.completion",
                          "created": 0,
                          "model": "gpt-4",
                          "choices": [{
                            "index": 0,
                            "message": {
                              "role": "assistant",
                              "content": "Done."
                            },
                            "finish_reason": "stop"
                          }],
                          "usage": { "prompt_tokens": 20, "completion_tokens": 7, "total_tokens": 27 }
                        }
                        """.trimIndent(),
                ),
            )
        }

        val testTool = tool<Unit>(
            name = "test_tool",
            description = "Test tool",
            parameters = buildJsonObject { },
        ) {
            buildJsonObject { }
        }

        val options = ChatOptions.optionOf {
            provider = Providers.OpenAI(apiKey = "test")
            httpEngine = engine
            tools = listOf(testTool)
            toolChoice = ToolChoice.Auto
        }
        val client = ChatClient(options)

        val executionOrder = mutableListOf<String>()

        runAgentLoop(
            client = client,
            messages = mutableListOf(userMessage("test")),
            tools = listOf(testTool),
            maxSteps = 5,
            parallelToolCalls = false,
            stream = false,
            model = "gpt-4",
            onAssistant = { response ->
                if (response.message.toolCalls.isNotEmpty()) {
                    executionOrder.add("onAssistant_with_tools")
                } else {
                    executionOrder.add("onAssistant_finish")
                }
            },
            onToolMessage = {
                executionOrder.add("onToolMessage")
            },
        )

        assertEquals(
            listOf("onAssistant_with_tools", "onToolMessage", "onAssistant_finish"),
            executionOrder,
        )
    }
}
