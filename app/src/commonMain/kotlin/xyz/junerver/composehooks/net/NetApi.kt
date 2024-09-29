package xyz.junerver.composehooks.net

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.headers
import io.ktor.http.withCharset
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.charsets.Charsets
import kotlinx.serialization.json.Json
import xyz.junerver.composehooks.acc_token
import xyz.junerver.composehooks.net.bean.RepoInfo
import xyz.junerver.composehooks.net.bean.UserInfo

/*
  Description:
  Author: Junerver
  Date: 2024/3/12-8:54
  Email: junerver@gmail.com
  Version: v1.0

  Update: 2014/9/11
    迁移请求到 ktor
*/

object NetApi : WebService {
    private const val BASE_URL = "https://api.github.com/"

    private val client = HttpClient(CIO) {
        defaultRequest {
            url(BASE_URL)
            headers.appendAll(
                headers {
                    append(HttpHeaders.ContentType, ContentType.Application.Json.withCharset(Charsets.UTF_8).toString())
                    append(HttpHeaders.Authorization, "token $acc_token")
                }
            )
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 15000L
            connectTimeoutMillis = 15000L
            socketTimeoutMillis = 15000L
        }
        install(ContentNegotiation) {
            json(
                Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    explicitNulls = false
                    coerceInputValues = true
                }
            )
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }
    }

    override suspend fun userInfo(user: String): UserInfo = client.get("users/$user").body()

    override suspend fun repoInfo(user: String, repo: String): RepoInfo = client.get("repos/$user/$repo").body()
}

interface WebService {
    suspend fun userInfo(user: String): UserInfo

    suspend fun repoInfo(user: String, repo: String): RepoInfo
}
