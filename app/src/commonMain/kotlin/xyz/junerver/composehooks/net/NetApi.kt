package xyz.junerver.composehooks.net

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.http.headers
import io.ktor.serialization.kotlinx.json.json
import kotlin.reflect.KFunction
import kotlinx.serialization.json.Json
import xyz.junerver.compose.hooks.asSuspendNoopFn
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
                set("Content-Type", "application/json;charset=UTF-8")
                set("Authorization", "token $acc_token")
            }
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 15000L
            connectTimeoutMillis = 15000L
            socketTimeoutMillis = 15000L
        }
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }
    }

    override suspend fun userInfo(user: String): UserInfo {
        return client.get("users/${user}").body()
    }

    override suspend fun repoInfo(user: String, repo: String): RepoInfo {
        return client.get("repos/${user}/${repo}").body()
    }
}

interface WebService {

    suspend fun userInfo(user: String): UserInfo

    suspend fun repoInfo(user: String, repo: String): RepoInfo
}

/** 自定义一个传递Retrofit接口实例的扩展函数，省去调用 [asSuspendNoopFn] 每次都要传递实例的步骤 */
fun <T : Any> KFunction<T>.asRequestFn() = this.asSuspendNoopFn(NetApi)
