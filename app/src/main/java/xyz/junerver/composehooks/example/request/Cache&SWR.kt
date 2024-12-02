package xyz.junerver.composehooks.example.request

import android.os.Parcelable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.coroutines.coroutineContext
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.datetime.Clock
import kotlinx.parcelize.Parcelize
import xyz.junerver.compose.hooks.asSuspendNoopFn
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

@Parcelize
data class MockArticle(
    val time: Long,
    val data: String,
) : Parcelable {
    override fun toString(): String {
        return "last request time=$time\ndata=$data"
    }
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
            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
            TestStaleTime()
        }
    }
}

@Composable
private fun TestSWR() {
    val (isVisibleState, toggle) = useBoolean(true)
    val isVisible by isVisibleState
    Column {
        TButton(text = "show/hide") {
            toggle()
        }
        if (isVisible) {
            SWR()
        }
        HorizontalDivider(modifier = Modifier.fillMaxWidth())
        if (isVisible) {
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
    val (data, loading) = useRequest(
        requestFn = ::mockRequestArticle.asSuspendNoopFn(),
        optionsOf = {
            if (useCache) cacheKey = "test-swr-key"
        }
    )
    Column(modifier = Modifier.height(210.dp)) {
        Text(text = "cache: $useCache", color = Color.Red)
        Text(text = "Background loading: $loading")
        if (data.asBoolean()) {
            Text(text = "$data")
        }
    }
}

@Composable
fun TestStaleTime() {
    val (isVisibleState, toggle) = useBoolean(true)
    val isVisible by isVisibleState
    val cacheKey = "test-stale-key"
    Column {
        Row {
            TButton(text = "show/hide") {
                toggle()
            }
            TButton(text = "clearCache") {
                // 通过调用top-level函数 `clearCache` 可以移除指定key的缓存，该函数可以接收多个key
                clearCache(cacheKey)
            }
        }
        if (isVisible) {
            // 相同 cacheKey 的数据全局同步
            StaleTime(cacheKey)
            StaleTime(cacheKey)
        }
    }
}

@Composable
private fun StaleTime(cacheKey: String) {
    val (data, loading) = useRequest(
        requestFn = ::mockRequestArticle.asSuspendNoopFn(),
        optionsOf = {
            this.cacheKey = cacheKey
            staleTime = 5.seconds
        }
    )
    Column(modifier = Modifier.height(210.dp)) {
        Text(text = "statleTime: 5s", color = Color.Red)
        Text(text = "Background loading: $loading")
        if (data.value.asBoolean()) {
            Text(text = "$data")
        }
    }
}
