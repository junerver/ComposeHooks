package xyz.junerver.compose.ai.http

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest

class KtorHttpEngineTest {
    @Test
    fun executeStreamEmitsDataLinesAndComplete() = runTest {
        val mockEngine = MockEngine {
            respond(
                content = ByteReadChannel("data: hello\n\ndata: world\n"),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Text.EventStream.toString()),
            )
        }
        val engine = KtorHttpEngine(HttpClient(mockEngine))

        val events = engine.executeStream(HttpRequest(url = "https://fake.local")).toList()
        assertEquals(4, events.size)
        assertEquals(SseEvent.Data("data: hello"), events[0])
        assertEquals(SseEvent.Data(""), events[1]) // blank line between events
        assertEquals(SseEvent.Data("data: world"), events[2])
        assertEquals(SseEvent.Complete, events[3])
    }

    @Test
    fun executeStreamEmitsErrorForNonSuccessStatus() = runTest {
        val mockEngine = MockEngine {
            respond(
                content = ByteReadChannel("Unauthorized"),
                status = HttpStatusCode.Unauthorized,
            )
        }
        val engine = KtorHttpEngine(HttpClient(mockEngine))

        val events = engine.executeStream(HttpRequest(url = "https://fake.local")).toList()
        val errorEvent = events.single() as SseEvent.Error
        assertTrue(errorEvent.error.message!!.contains("HTTP 401: Unauthorized"))
    }
}
