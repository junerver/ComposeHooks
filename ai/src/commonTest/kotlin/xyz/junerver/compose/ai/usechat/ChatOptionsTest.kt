package xyz.junerver.compose.ai.usechat

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

/**
 * Tests for ChatOptions configuration.
 *
 * TDD approach: Test endpoint building and configuration logic.
 */
class ChatOptionsTest {
    // region Endpoint Building Tests

    @Test
    fun testBuildEndpointOpenAI() {
        val options = ChatOptions.optionOf {
            provider = Providers.OpenAI(apiKey = "test")
        }
        assertEquals("https://api.openai.com/v1/chat/completions", options.buildEndpoint())
    }

    @Test
    fun testBuildEndpointAnthropic() {
        val options = ChatOptions.optionOf {
            provider = Providers.Anthropic(apiKey = "test")
        }
        assertEquals("https://api.anthropic.com/v1/messages", options.buildEndpoint())
    }

    @Test
    fun testBuildEndpointCustomBaseUrl() {
        val options = ChatOptions.optionOf {
            provider = Providers.Custom(
                name = "Custom",
                baseUrl = "http://localhost:8080/api/",
                defaultModel = "test",
            )
        }
        // Should trim trailing slash and append endpoint
        assertEquals("http://localhost:8080/api/chat/completions", options.buildEndpoint())
    }

    @Test
    fun testBuildEndpointCustomEndpoint() {
        val options = ChatOptions.optionOf {
            provider = Providers.Custom(
                name = "Custom",
                baseUrl = "http://localhost:8080",
                defaultModel = "test",
                chatEndpoint = "/v2/generate",
            )
        }
        assertEquals("http://localhost:8080/v2/generate", options.buildEndpoint())
    }

    // endregion

    // region Auth Headers Tests

    @Test
    fun testBuildAuthHeadersOpenAI() {
        val options = ChatOptions.optionOf {
            provider = Providers.OpenAI(apiKey = "sk-test-123")
        }
        val headers = options.buildAuthHeaders()
        assertEquals("Bearer sk-test-123", headers["Authorization"])
    }

    @Test
    fun testBuildAuthHeadersAnthropic() {
        val options = ChatOptions.optionOf {
            provider = Providers.Anthropic(apiKey = "sk-ant-test")
        }
        val headers = options.buildAuthHeaders()
        assertEquals("sk-ant-test", headers["x-api-key"])
        assertEquals("2023-06-01", headers["anthropic-version"])
    }

    // endregion

    // region Request Body Building Tests

    @Test
    fun testBuildRequestBodyWithSystemPrompt() {
        val options = ChatOptions.optionOf {
            provider = Providers.OpenAI(apiKey = "test")
            systemPrompt = "You are a helpful assistant"
        }
        val messages = listOf(userMessage("Hello"))
        val body = options.buildRequestBody(messages, stream = true)
        // System prompt should be included
        assertTrue(body.contains("You are a helpful assistant"))
    }

    @Test
    fun testBuildRequestBodyWithTemperature() {
        val options = ChatOptions.optionOf {
            provider = Providers.OpenAI(apiKey = "test")
            temperature = 0.8f
        }
        val messages = listOf(userMessage("Hello"))
        val body = options.buildRequestBody(messages, stream = false)
        assertTrue(body.contains("0.8"))
    }

    @Test
    fun testBuildRequestBodyWithMaxTokens() {
        val options = ChatOptions.optionOf {
            provider = Providers.OpenAI(apiKey = "test")
            maxTokens = 500
        }
        val messages = listOf(userMessage("Hello"))
        val body = options.buildRequestBody(messages, stream = false)
        assertTrue(body.contains("500"))
    }

    // endregion

    // region Effective Model Tests

    @Test
    fun testEffectiveModelUsesProviderDefault() {
        val options = ChatOptions.optionOf {
            provider = Providers.OpenAI(apiKey = "test")
        }
        assertEquals("gpt-4o-mini", options.effectiveModel)
    }

    @Test
    fun testEffectiveModelOverride() {
        val options = ChatOptions.optionOf {
            provider = Providers.OpenAI(apiKey = "test")
            model = "gpt-4-turbo"
        }
        assertEquals("gpt-4-turbo", options.effectiveModel)
    }

    // endregion

    // region Configuration Tests

    @Test
    fun testDefaultTimeout() {
        val options = ChatOptions.optionOf {}
        assertEquals(60.seconds, options.timeout)
    }

    @Test
    fun testCustomTimeout() {
        val options = ChatOptions.optionOf {
            timeout = 120.seconds
        }
        assertEquals(120.seconds, options.timeout)
    }

    @Test
    fun testDefaultStreamEnabled() {
        val options = ChatOptions.optionOf {}
        assertTrue(options.stream)
    }

    @Test
    fun testCustomHeaders() {
        val options = ChatOptions.optionOf {
            headers = mapOf("X-Custom-Header" to "value")
        }
        assertEquals("value", options.headers["X-Custom-Header"])
    }

    @Test
    fun testInitialMessages() {
        val initial = listOf(
            userMessage("Previous question"),
            assistantMessage("Previous answer"),
        )
        val options = ChatOptions.optionOf {
            initialMessages = initial
        }
        assertEquals(2, options.initialMessages.size)
    }

    // endregion
}
