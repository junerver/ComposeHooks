/*
  Description: useGenerateObject behavior and extension tests
  Author: Junerver
  Date: 2026/07/14
  Email: junerver@gmail.com
  Version: v1.0
*/

package xyz.junerver.compose.ai

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlinx.serialization.Serializable
import xyz.junerver.compose.ai.http.HttpResult
import xyz.junerver.compose.ai.test.FakeChatProvider
import xyz.junerver.compose.ai.test.FakeHttpEngine
import xyz.junerver.compose.ai.usechat.FilePart
import xyz.junerver.compose.ai.usechat.ImagePart
import xyz.junerver.compose.ai.usechat.TextPart
import xyz.junerver.compose.ai.usechat.UserContentPart
import xyz.junerver.compose.ai.usegenerateobject.GenerateObjectHolder
import xyz.junerver.compose.ai.usegenerateobject.submitText
import xyz.junerver.compose.ai.usegenerateobject.submitWithFile
import xyz.junerver.compose.ai.usegenerateobject.submitWithImage
import xyz.junerver.compose.ai.usegenerateobject.submitWithImageUrl
import xyz.junerver.compose.ai.usegenerateobject.useGenerateObject

@Serializable
private data class GeneratedPayload(
    val name: String,
    val count: Int,
)

@Suppress("DEPRECATION")
class UseGenerateObjectTest {
    private val schema =
        """
        {
          "type": "object",
          "properties": {
            "name": { "type": "string" },
            "count": { "type": "integer" }
          },
          "required": ["name", "count"]
        }
        """.trimIndent()

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useGenerateObject_parses_response_and_keeps_holder_identity_stable() = runComposeUiTest {
        val engine = FakeHttpEngine().apply {
            enqueueResponse(HttpResult(200, "text:{\"name\":\"Ada\",\"count\":2}"))
        }
        val provider = FakeChatProvider()
        var initialHolder: GenerateObjectHolder<GeneratedPayload>? = null
        lateinit var latestHolder: GenerateObjectHolder<GeneratedPayload>
        var callbackPayload: GeneratedPayload? = null

        setContent {
            latestHolder = useGenerateObject(schema) {
                this.provider = provider
                httpEngine = engine
                stream = false
                systemPrompt = "Return a compact object."
                onFinish = { payload, _ -> callbackPayload = payload }
            }
            latestHolder.object_.value
            if (initialHolder == null) initialHolder = latestHolder
        }

        waitForIdle()
        runOnIdle { latestHolder.submitText("Create an object") }
        mainClock.advanceTimeBy(100)
        waitForIdle()

        val expected = GeneratedPayload("Ada", 2)
        assertEquals(expected, latestHolder.object_.value)
        assertEquals(expected, callbackPayload)
        assertEquals("{\"name\":\"Ada\",\"count\":2}", latestHolder.rawJson.value)
        assertNull(latestHolder.error.value)
        assertSame(initialHolder, latestHolder)
        assertTrue(engine.recordedRequests.single().body.orEmpty().contains(schema))
        assertTrue(engine.recordedRequests.single().body.orEmpty().contains("Return a compact object."))
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useGenerateObject_heals_markdown_and_trailing_comma() = runComposeUiTest {
        val engine = FakeHttpEngine().apply {
            enqueueResponse(HttpResult(200, "text:```json\n{\"name\":\"Ada\",\"count\":2,}\n```"))
        }
        lateinit var holder: GenerateObjectHolder<GeneratedPayload>

        setContent {
            holder = useGenerateObject(schema, GeneratedPayload.serializer()) {
                provider = FakeChatProvider()
                httpEngine = engine
                stream = false
            }
        }

        waitForIdle()
        runOnIdle { holder.submitText("Create an object") }
        mainClock.advanceTimeBy(100)
        waitForIdle()

        assertEquals(GeneratedPayload("Ada", 2), holder.object_.value)
        assertNull(holder.error.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useGenerateObject_exposes_parse_error_and_invokes_callback() = runComposeUiTest {
        val engine = FakeHttpEngine().apply {
            enqueueResponse(HttpResult(200, "text:not-json"))
        }
        val errors = mutableListOf<Throwable>()
        lateinit var holder: GenerateObjectHolder<GeneratedPayload>

        setContent {
            holder = useGenerateObject(schema) {
                provider = FakeChatProvider()
                httpEngine = engine
                stream = false
                enableJsonHealing = false
                onError = errors::add
            }
        }

        waitForIdle()
        runOnIdle { holder.submitText("Create an object") }
        mainClock.advanceTimeBy(100)
        waitForIdle()

        assertNull(holder.object_.value)
        assertTrue(holder.error.value != null)
        assertEquals(1, errors.size)
        assertSame(holder.error.value, errors.single())
    }

    @Test
    fun submit_extensions_build_expected_multimodal_parts() {
        val submissions = mutableListOf<List<UserContentPart>>()
        val holder = GenerateObjectHolder<GeneratedPayload>(
            object_ = mutableStateOf(null),
            rawJson = mutableStateOf(""),
            isLoading = mutableStateOf(false),
            error = mutableStateOf(null),
            submit = submissions::add,
            stop = {},
        )

        holder.submitText("text")
        holder.submitWithImage("image", "BASE64", "image/png")
        holder.submitWithImageUrl("url", "https://example.test/image.png")
        holder.submitWithFile("file", "FILE", "application/pdf", "doc.pdf")

        assertEquals("text", assertIs<TextPart>(submissions[0].single()).text)
        assertEquals("image", assertIs<TextPart>(submissions[1][0]).text)
        assertIs<ImagePart>(submissions[1][1])
        assertEquals("url", assertIs<TextPart>(submissions[2][0]).text)
        assertIs<ImagePart>(submissions[2][1])
        assertEquals("file", assertIs<TextPart>(submissions[3][0]).text)
        val file = assertIs<FilePart>(submissions[3][1])
        assertEquals("application/pdf", file.mimeType)
        assertEquals("doc.pdf", file.fileName)
    }
}
