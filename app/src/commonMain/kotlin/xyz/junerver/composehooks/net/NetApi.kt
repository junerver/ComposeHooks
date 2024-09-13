package xyz.junerver.composehooks.net

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.http.headers
import io.ktor.serialization.kotlinx.json.*
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
            headers {
                append("Content-Type", "application/json;charset=UTF-8")
                append("Authorization", "token $acc_token")
            }
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
                }
            )
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }
    }

    override suspend fun userInfo(user: String): UserInfo = client.get("users/$user").body()

    override suspend fun repoInfo(user: String, repo: String): RepoInfo = client.get("repos/$user/$repo").body()
}

interface WebService {
    suspend fun userInfo(user: String): UserInfo

    suspend fun repoInfo(user: String, repo: String): RepoInfo
}
