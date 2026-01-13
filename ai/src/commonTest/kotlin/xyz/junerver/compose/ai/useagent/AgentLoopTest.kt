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
import xyz.junerver.compose.ai.usechat.Providers
import xyz.junerver.compose.ai.usechat.ToolMessage
import xyz.junerver.compose.ai.usechat.userMessage

class AgentLoopTest {
    @Test
    fun agentLoopExecutesToolAndContinuesConversation() = runTest {
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
                                  "name": "get_weather",
                                  "arguments": "{\"city\":\"Paris\"}"
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
                              "content": "It is sunny in Paris."
                            },
                            "finish_reason": "stop"
                          }],
                          "usage": { "prompt_tokens": 20, "completion_tokens": 7, "total_tokens": 27 }
                        }
                        """.trimIndent(),
                ),
            )
        }

        val weatherTool = tool<WeatherParams>(
            name = "get_weather",
            description = "Get weather for a city",
            parameters = buildJsonObject {
                put("type", JsonPrimitive("object"))
                put(
                    "properties",
                    buildJsonObject {
                        put(
                            "city",
                            buildJsonObject {
                                put("type", JsonPrimitive("string"))
                            },
                        )
                    },
                )
            },
        ) { params ->
            buildJsonObject {
                put("city", JsonPrimitive(params.city))
                put("forecast", JsonPrimitive("sunny"))
            }
        }

        val options = ChatOptions.optionOf {
            provider = Providers.OpenAI(apiKey = "test")
            httpEngine = engine
            tools = listOf(weatherTool)
            toolChoice = ToolChoice.Auto
        }
        val client = ChatClient(options)

        val toolMessages = mutableListOf<ToolMessage>()

        runAgentLoop(
            client = client,
            messages = mutableListOf(userMessage("Hello")),
            tools = listOf(weatherTool),
            maxSteps = 3,
            parallelToolCalls = false,
            stream = false,
            model = "gpt-4",
            onAssistant = { },
            onToolMessage = { toolMessages += it },
        )

        assertEquals(1, toolMessages.size)
        val firstTool = toolMessages.first()
        val firstResult = firstTool.content.firstOrNull()
        assertNotNull(firstResult)
        val resultJson = firstResult.result.jsonObject
        assertEquals("Paris", resultJson["city"]?.jsonPrimitive?.content)
        assertEquals("sunny", resultJson["forecast"]?.jsonPrimitive?.content)

        assertTrue(engine.recordedRequests.size >= 2)
    }

    @Test
    fun agentLoop_maxStepsExceeded_throwsIllegalStateException() = runTest {
        val engine = FakeHttpEngine().apply {
            // 模拟 AI 持续返回工具调用，超过 maxSteps 限制
            repeat(10) {
                enqueueResponse(
                    HttpResult(
                        statusCode = 200,
                        body =
                            """
                            {
                              "id": "chatcmpl-$it",
                              "object": "chat.completion",
                              "created": 0,
                              "model": "gpt-4",
                              "choices": [{
                                "index": 0,
                                "message": {
                                  "role": "assistant",
                                  "content": null,
                                  "tool_calls": [{
                                    "id": "call_$it",
                                    "type": "function",
                                    "function": {
                                      "name": "get_weather",
                                      "arguments": "{\"city\":\"Paris\"}"
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
            }
        }

        val weatherTool = tool<WeatherParams>(
            name = "get_weather",
            description = "Get weather for a city",
            parameters = buildJsonObject {
                put("type", JsonPrimitive("object"))
                put(
                    "properties",
                    buildJsonObject {
                        put(
                            "city",
                            buildJsonObject {
                                put("type", JsonPrimitive("string"))
                            },
                        )
                    },
                )
            },
        ) { params ->
            buildJsonObject {
                put("city", JsonPrimitive(params.city))
                put("forecast", JsonPrimitive("sunny"))
            }
        }

        val options = ChatOptions.optionOf {
            provider = Providers.OpenAI(apiKey = "test")
            httpEngine = engine
            tools = listOf(weatherTool)
            toolChoice = ToolChoice.Auto
        }
        val client = ChatClient(options)

        val exception = kotlin.test.assertFailsWith<IllegalStateException> {
            runAgentLoop(
                client = client,
                messages = mutableListOf(userMessage("Hello")),
                tools = listOf(weatherTool),
                maxSteps = 3,
                parallelToolCalls = false,
                stream = false,
                model = "gpt-4",
                onAssistant = { },
                onToolMessage = { },
            )
        }

        assertTrue(exception.message!!.contains("maxSteps"))
        assertTrue(exception.message!!.contains("3"))
    }

    @Test
    fun agentLoop_noToolCalls_exitsImmediately() = runTest {
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

        val weatherTool = tool<WeatherParams>(
            name = "get_weather",
            description = "Get weather for a city",
            parameters = buildJsonObject {
                put("type", JsonPrimitive("object"))
                put(
                    "properties",
                    buildJsonObject {
                        put(
                            "city",
                            buildJsonObject {
                                put("type", JsonPrimitive("string"))
                            },
                        )
                    },
                )
            },
        ) { params ->
            buildJsonObject {
                put("city", JsonPrimitive(params.city))
                put("forecast", JsonPrimitive("sunny"))
            }
        }

        val options = ChatOptions.optionOf {
            provider = Providers.OpenAI(apiKey = "test")
            httpEngine = engine
            tools = listOf(weatherTool)
            toolChoice = ToolChoice.Auto
        }
        val client = ChatClient(options)

        var toolMessageCount = 0

        runAgentLoop(
            client = client,
            messages = mutableListOf(userMessage("Hello")),
            tools = listOf(weatherTool),
            maxSteps = 5,
            parallelToolCalls = false,
            stream = false,
            model = "gpt-4",
            onAssistant = { },
            onToolMessage = { toolMessageCount++ },
        )

        assertEquals(0, toolMessageCount)
        assertEquals(1, engine.recordedRequests.size)
    }

    @Test
    fun agentLoop_emptyToolsList_continuesWithoutError() = runTest {
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

        val options = ChatOptions.optionOf {
            provider = Providers.OpenAI(apiKey = "test")
            httpEngine = engine
            tools = emptyList()
        }
        val client = ChatClient(options)

        runAgentLoop(
            client = client,
            messages = mutableListOf(userMessage("Hello")),
            tools = emptyList(),
            maxSteps = 5,
            parallelToolCalls = false,
            stream = false,
            model = "gpt-4",
            onAssistant = { },
            onToolMessage = { },
        )

        assertEquals(1, engine.recordedRequests.size)
    }

    @Test
    fun agentLoop_toolNotFound_returnsErrorMessage() = runTest {
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
                                  "name": "nonexistent_tool",
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
                              "content": "I apologize for the error."
                            },
                            "finish_reason": "stop"
                          }],
                          "usage": { "prompt_tokens": 20, "completion_tokens": 7, "total_tokens": 27 }
                        }
                        """.trimIndent(),
                ),
            )
        }

        val weatherTool = tool<WeatherParams>(
            name = "get_weather",
            description = "Get weather for a city",
            parameters = buildJsonObject {
                put("type", JsonPrimitive("object"))
                put(
                    "properties",
                    buildJsonObject {
                        put(
                            "city",
                            buildJsonObject {
                                put("type", JsonPrimitive("string"))
                            },
                        )
                    },
                )
            },
        ) { params ->
            buildJsonObject {
                put("city", JsonPrimitive(params.city))
                put("forecast", JsonPrimitive("sunny"))
            }
        }

        val options = ChatOptions.optionOf {
            provider = Providers.OpenAI(apiKey = "test")
            httpEngine = engine
            tools = listOf(weatherTool)
            toolChoice = ToolChoice.Auto
        }
        val client = ChatClient(options)

        val toolMessages = mutableListOf<ToolMessage>()

        runAgentLoop(
            client = client,
            messages = mutableListOf(userMessage("Hello")),
            tools = listOf(weatherTool),
            maxSteps = 5,
            parallelToolCalls = false,
            stream = false,
            model = "gpt-4",
            onAssistant = { },
            onToolMessage = { toolMessages += it },
        )

        assertEquals(1, toolMessages.size)
        val errorMessage = toolMessages.first()
        val firstResult = errorMessage.content.firstOrNull()
        assertNotNull(firstResult)
        assertTrue(firstResult.isError)
        assertTrue(firstResult.result.jsonPrimitive.content.contains("Tool not found"))
        assertTrue(firstResult.result.jsonPrimitive.content.contains("nonexistent_tool"))
    }

    @Test
    fun agentLoop_toolExecutionThrows_returnsErrorMessage() = runTest {
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
                              "content": "I'll handle the error."
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
            description = "A tool that always fails",
            parameters = buildJsonObject { },
        ) {
            throw RuntimeException("Tool execution failed")
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
            messages = mutableListOf(userMessage("Hello")),
            tools = listOf(failingTool),
            maxSteps = 5,
            parallelToolCalls = false,
            stream = false,
            model = "gpt-4",
            onAssistant = { },
            onToolMessage = { toolMessages += it },
        )

        assertEquals(1, toolMessages.size)
        val errorMessage = toolMessages.first()
        val firstResult = errorMessage.content.firstOrNull()
        assertNotNull(firstResult)
        assertTrue(firstResult.isError)
        assertTrue(firstResult.result.jsonPrimitive.content.contains("Tool execution failed"))
    }

    @Test
    fun agentLoop_toolExecutionThrows_continuesLoop() = runTest {
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
                              "content": "Error handled, continuing."
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
            description = "A tool that always fails",
            parameters = buildJsonObject { },
        ) {
            throw RuntimeException("Expected error")
        }

        val options = ChatOptions.optionOf {
            provider = Providers.OpenAI(apiKey = "test")
            httpEngine = engine
            tools = listOf(failingTool)
            toolChoice = ToolChoice.Auto
        }
        val client = ChatClient(options)

        var assistantCallCount = 0

        runAgentLoop(
            client = client,
            messages = mutableListOf(userMessage("Hello")),
            tools = listOf(failingTool),
            maxSteps = 5,
            parallelToolCalls = false,
            stream = false,
            model = "gpt-4",
            onAssistant = { assistantCallCount++ },
            onToolMessage = { },
        )

        assertEquals(2, assistantCallCount)
        assertEquals(2, engine.recordedRequests.size)
    }

    @Test
    fun agentLoop_parallelToolCalls_executesSimultaneously() = runTest {
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
                                  "name": "slow_tool",
                                  "arguments": "{\"id\":\"1\"}"
                                }
                              }, {
                                "id": "call_2",
                                "type": "function",
                                "function": {
                                  "name": "slow_tool",
                                  "arguments": "{\"id\":\"2\"}"
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
        data class SlowToolParams(val id: String)

        val slowTool = tool<SlowToolParams>(
            name = "slow_tool",
            description = "A slow tool",
            parameters = buildJsonObject {
                put("type", JsonPrimitive("object"))
                put(
                    "properties",
                    buildJsonObject {
                        put(
                            "id",
                            buildJsonObject {
                                put("type", JsonPrimitive("string"))
                            },
                        )
                    },
                )
            },
        ) { params ->
            kotlinx.coroutines.delay(100)
            buildJsonObject {
                put("id", JsonPrimitive(params.id))
                put("result", JsonPrimitive("ok"))
            }
        }

        val options = ChatOptions.optionOf {
            provider = Providers.OpenAI(apiKey = "test")
            httpEngine = engine
            tools = listOf(slowTool)
            toolChoice = ToolChoice.Auto
        }
        val client = ChatClient(options)

        val toolMessages = mutableListOf<ToolMessage>()

        runAgentLoop(
            client = client,
            messages = mutableListOf(userMessage("Hello")),
            tools = listOf(slowTool),
            maxSteps = 5,
            parallelToolCalls = true,
            stream = false,
            model = "gpt-4",
            onAssistant = { },
            onToolMessage = { toolMessages += it },
        )

        assertEquals(2, toolMessages.size)
        val results = toolMessages.map { it.content.first().result.jsonObject["id"]?.jsonPrimitive?.content }
        assertTrue(results.containsAll(listOf("1", "2")))
    }

    @Test
    fun agentLoop_sequentialToolCalls_executesInOrder() = runTest {
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
                                  "name": "ordered_tool",
                                  "arguments": "{\"order\":1}"
                                }
                              }, {
                                "id": "call_2",
                                "type": "function",
                                "function": {
                                  "name": "ordered_tool",
                                  "arguments": "{\"order\":2}"
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
        data class OrderedToolParams(val order: Int)

        val executionOrder = mutableListOf<Int>()

        val orderedTool = tool<OrderedToolParams>(
            name = "ordered_tool",
            description = "A tool that records execution order",
            parameters = buildJsonObject {
                put("type", JsonPrimitive("object"))
                put(
                    "properties",
                    buildJsonObject {
                        put(
                            "order",
                            buildJsonObject {
                                put("type", JsonPrimitive("integer"))
                            },
                        )
                    },
                )
            },
        ) { params ->
            executionOrder.add(params.order)
            buildJsonObject {
                put("order", JsonPrimitive(params.order))
            }
        }

        val options = ChatOptions.optionOf {
            provider = Providers.OpenAI(apiKey = "test")
            httpEngine = engine
            tools = listOf(orderedTool)
            toolChoice = ToolChoice.Auto
        }
        val client = ChatClient(options)

        runAgentLoop(
            client = client,
            messages = mutableListOf(userMessage("Hello")),
            tools = listOf(orderedTool),
            maxSteps = 5,
            parallelToolCalls = false,
            stream = false,
            model = "gpt-4",
            onAssistant = { },
            onToolMessage = { },
        )

        assertEquals(listOf(1, 2), executionOrder)
    }
}

@Serializable
private data class WeatherParams(val city: String)
