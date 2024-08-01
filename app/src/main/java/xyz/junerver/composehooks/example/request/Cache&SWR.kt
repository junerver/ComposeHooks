package xyz.junerver.composehooks.example.request

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.seconds
import xyz.junerver.compose.hooks.optionsOf
import xyz.junerver.compose.hooks.useBoolean
import xyz.junerver.compose.hooks.userequest.RequestOptions
import xyz.junerver.compose.hooks.userequest.clearCache
import xyz.junerver.compose.hooks.userequest.useRequest
import xyz.junerver.composehooks.net.WebService
import xyz.junerver.composehooks.net.asRequestFn
import xyz.junerver.composehooks.ui.component.TButton
import xyz.junerver.kotlin.asBoolean

/*
  Description:
  Author: Junerver
  Date: 2024/8/1-9:12
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun Cache() {
    Surface {
        Column {
            TestSWR()
            Spacer(modifier = Modifier.height(20.dp))
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
    val (userInfo, userLoading) = useRequest(
        requestFn = WebService::userInfo.asRequestFn(),
        optionsOf {
            defaultParams = arrayOf("junerver")
            if (useCache) cacheKey = "userinfo-junerver"
        }
    )
    Column(modifier = Modifier.height(110.dp)) {
        Text(text = "cache: $useCache", color = Color.Red)
        Text(text = "Background loading: $userLoading")
        if (userInfo.asBoolean()) {
            Text(text = "$userInfo".substring(0..100))
        }
    }
}

@Composable
fun TestStaleTime() {
    val (isVisible, toggle) = useBoolean(true)
    val cacheKey = "userinfo-junerver-stale"
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
            StaleTime(cacheKey)
        }
    }
}

@Composable
private fun StaleTime(cacheKey: String) {
    val (userInfo, userLoading) = useRequest(
        requestFn = WebService::userInfo.asRequestFn(),
        optionsOf {
            defaultParams = arrayOf("junerver")
            this.cacheKey = cacheKey
            staleTime = 5.seconds
        }
    )
    Column(modifier = Modifier.height(110.dp)) {
        Text(text = "statleTime: 5000ms", color = Color.Red)
        Text(text = "Background loading: $userLoading")
        if (userInfo.asBoolean()) {
            Text(text = "$userInfo".substring(0..100))
        }
    }
}
