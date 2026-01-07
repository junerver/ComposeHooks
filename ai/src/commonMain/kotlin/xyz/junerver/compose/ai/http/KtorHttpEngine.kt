package xyz.junerver.compose.ai.http

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.plugins.timeout
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.http.withCharset
import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/*
  Description: Ktor-based default HTTP engine implementation
  Author: Junerver
  Date: 2026/01/06
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Default HTTP engine implementation using Ktor.
 *
 * This is the default engine used when no custom engine is provided.
 * Uses Ktor's CIO engine on JVM/Android and Darwin engine on iOS.
 *
 * @param client Optional custom HttpClient instance. If not provided,
 *               a default client with timeout and logging will be created.
 */
class KtorHttpEngine(
    private val client: HttpClient = createDefaultClient(),
) : HttpEngine {
    companion object {
        /**
         * Creates a default HttpClient with basic configuration.
         */
        fun createDefaultClient(): HttpClient = HttpClient {
            install(HttpTimeout)
            install(Logging) {
                logger = Logger.SIMPLE
                level = LogLevel.ALL
            }
        }
    }

    override suspend fun execute(request: HttpRequest): HttpResult {
        val response = client.post(request.url) {
            timeout {
                requestTimeoutMillis = request.timeout
                connectTimeoutMillis = request.timeout
                socketTimeoutMillis = request.timeout
            }
            contentType(ContentType.Application.Json.withCharset(Charsets.UTF_8))
            request.headers.forEach { (key, value) ->
                header(key, value)
            }
            request.body?.let { setBody(it) }
        }

        return HttpResult(
            statusCode = response.status.value,
            body = response.bodyAsText(),
        )
    }

    override suspend fun executeStream(request: HttpRequest): Flow<SseEvent> = flow {
        try {
            client.preparePost(request.url) {
                // SSE streams need longer/no timeout
                timeout {
                    requestTimeoutMillis = Long.MAX_VALUE
                    socketTimeoutMillis = Long.MAX_VALUE
                }
                contentType(ContentType.Application.Json.withCharset(Charsets.UTF_8))
                header(HttpHeaders.Accept, "text/event-stream")
                header(HttpHeaders.CacheControl, "no-cache")
                header(HttpHeaders.Connection, "keep-alive")
                request.headers.forEach { (key, value) ->
                    header(key, value)
                }
                request.body?.let { setBody(it) }
            }.execute { response ->
                if (!response.status.isSuccess()) {
                    val errorBody = response.bodyAsText()
                    emit(SseEvent.Error(Exception("HTTP ${response.status.value}: $errorBody")))
                    return@execute
                }

                val channel = response.bodyAsChannel()
                while (!channel.isClosedForRead) {
                    val line = channel.readUTF8Line() ?: continue
                    emit(SseEvent.Data(line))
                }
                emit(SseEvent.Complete)
            }
        } catch (e: Exception) {
            emit(SseEvent.Error(e))
        }
    }

    override fun close() {
        client.close()
    }
}
