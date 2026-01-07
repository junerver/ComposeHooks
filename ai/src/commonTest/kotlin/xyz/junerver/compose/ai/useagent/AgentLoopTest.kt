package xyz.junerver.compose.ai.useagent

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
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
import xyz.junerver.compose.ai.usechat.ToolMessage
import xyz.junerver.compose.ai.usechat.Providers
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
}

@Serializable
private data class WeatherParams(val city: String)
