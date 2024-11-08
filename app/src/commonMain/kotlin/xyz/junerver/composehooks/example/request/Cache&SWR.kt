package xyz.junerver.composehooks.example.request

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import kotlin.coroutines.coroutineContext
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.datetime.Clock
import xyz.junerver.compose.hooks.useBoolean
import xyz.junerver.compose.hooks.userequest.RequestOptions
import xyz.junerver.compose.hooks.userequest.useRequest
import xyz.junerver.compose.hooks.userequest.utils.clearCache
import xyz.junerver.compose.hooks.utils.asBoolean
import xyz.junerver.composehooks.ui.component.TButton
import xyz.junerver.composehooks.utils.NanoId

/*
  Description:
  Author: Junerver
  Date: 2024/8/1-9:12
  Email: junerver@gmail.com
  Version: v1.0
*/

data class MockArticle(
    val time: Long,
    val data: String,
) {
    override fun toString(): String = "last request time=$time\ndata=$data"
}

suspend fun mockRequestArticle(): MockArticle {
    delay(1000 + Random.nextLong(500, 1000))
    if (coroutineContext.isActive) {
        return MockArticle(Clock.System.now().toEpochMilliseconds(), NanoId.generate(200))
    } else {
        error("coroutine cancelled")
    }
}

@Composable
fun Cache() {
    Surface {
        Column {
            TestSWR()
            DividerSpacer()
            TestStaleTime()
        }
    }
}

@Composable
private fun TestSWR() {
    val (isVisible, toggle) = useBoolean(true)
    Column {
        TButton(text = "show/hide") {
            toggle()
        }
        if (isVisible.value) {
            SWR()
        }
        DividerSpacer()
        if (isVisible.value) {
            SWR(true)
        }
    }
}

/**
 * 当配置了[RequestOptions.cacheKey]时，发起请求时会先查询是否存在缓存，
 * 如果缓存存在则优先使用缓存返回，同时发起请求，在请求成功后更新状态
 *
 * @param useCache
 */
@Composable
private fun SWR(useCache: Boolean = false) {
    val (dataState, loadingState) = useRequest(
        requestFn = {
            mockRequestArticle()
        },
        optionsOf = {
            if (useCache) cacheKey = "test-swr-key"
        }
    )
    val data by dataState
    val loading by loadingState
    Column {
        Text(text = "cache: $useCache", color = Color.Red)
        Text(text = "Background loading: $loading")
        if (data.asBoolean()) {
            Text(text = "$data")
        }
    }
}

@Composable
fun TestStaleTime() {
    val (isVisible, toggle) = useBoolean(true)
    val cacheKey = "test-stale-key"
    Column {
        Text("↓ The following two components use the same 'cacheKey' and they will share the data")
        Row {
            TButton(text = "show/hide") {
                toggle()
            }
            TButton(text = "clearCache") {
                // 通过调用top-level函数 `clearCache` 可以移除指定key的缓存，该函数可以接收多个key
                clearCache(cacheKey)
            }
        }
        if (isVisible.value) {
            // 相同 cacheKey 的数据全局同步
            StaleTime(cacheKey)
            StaleTime(cacheKey)
        }
    }
}

@Composable
private fun StaleTime(cacheKey: String) {
    val (dataState, loadingState) = useRequest(
        requestFn = {
            mockRequestArticle()
        },
        optionsOf = {
            this.cacheKey = cacheKey
            staleTime = 5.seconds
        }
    )
    val data by dataState
    val loading by loadingState
    Column {
        Text(text = "statleTime: 5s", color = Color.Red)
        Text(text = "Background loading: $loading")
        if (data.asBoolean()) {
            Text(text = "$data")
        }
    }
}
