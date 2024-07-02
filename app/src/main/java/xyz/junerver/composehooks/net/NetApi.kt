package xyz.junerver.composehooks.net

import java.util.concurrent.TimeUnit
import kotlin.reflect.KFunction
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
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
*/

object NetApi {

    private val mRetrofit: Retrofit

    private const val BASE_URL = "https://api.github.com/"
    private const val DEFAULT_TIMEOUT = 60L
    var SERVICE: WebService

    init {
        val builder = OkHttpClient.Builder()
        builder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            .pingInterval(30, TimeUnit.SECONDS)
        mRetrofit = Retrofit.Builder()
            .client(builder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
        SERVICE = mRetrofit.create(WebService::class.java)
    }
}

interface WebService {
    @Headers(
        "Content-Type:application/json;charset=UTF-8",
        "Authorization: token $acc_token"
    )
    @GET("users/{user}")
    suspend fun userInfo(@Path("user") user: String): UserInfo

    @Headers(
        "Content-Type:application/json;charset=UTF-8",
        "Authorization: token $acc_token"
    )
    @GET("repos/{user}/{repo}")
    suspend fun repoInfo(@Path("user") user: String, @Path("repo") repo: String): RepoInfo
}

/**
 * 自定义一个传递Retrofit接口实例的扩展函数，省去调用 [asSuspendNoopFn] 每次都要传递实例的步骤
 */
fun <T : Any> KFunction<T>.asRequestFn() = this.asSuspendNoopFn(NetApi.SERVICE)
