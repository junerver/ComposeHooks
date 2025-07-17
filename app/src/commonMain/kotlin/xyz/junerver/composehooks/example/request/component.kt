package xyz.junerver.composehooks.example.request

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.seconds
import xyz.junerver.compose.hooks.DebounceOptions
import xyz.junerver.compose.hooks.ThrottleOptions
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.useEventPublish
import xyz.junerver.compose.hooks.useEventSubscribe
import xyz.junerver.compose.hooks.userequest.RequestOptions
import xyz.junerver.compose.hooks.userequest.useRequest
import xyz.junerver.compose.hooks.utils.asBoolean
import xyz.junerver.composehooks.net.NetApi
import xyz.junerver.composehooks.ui.component.DividerSpacer
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description:
  Author: Junerver
  Date: 2024/3/13-15:04
  Email: junerver@gmail.com
  Version: v1.0
*/

enum class OptionFunc {
    LoadingDelay,
    Debounce,
    Throttle,
}

@Composable
fun Container(label: String, optionFunc: OptionFunc) {
    val post = useEventPublish<Unit>()
    Column {
        TButton(text = "refresh") {
            post(Unit)
        }
        SubComponent(label, false, optionFunc)
        DividerSpacer()
        SubComponent(label, true, optionFunc)
    }
}

@Composable
fun SubComponent(label: String, isUsed: Boolean = false, optionFunc: OptionFunc) {
    val (userInfoState, loadingState, _, request) = useRequest(
        requestFn = { NetApi.userInfo(it[0] as String) },
        optionsOf = {
            defaultParams = arrayOf("junerver")
            when (optionFunc) {
                OptionFunc.LoadingDelay -> run {
                    /**
                     * 当你配置了[RequestOptions.loadingDelay]，如果请求在这个时间之内返回就不会引起loading的变化，
                     * 这可以避免闪烁，适用于接口较为快速返回的场景。
                     * When you configure [RequestOptions.loadingDelay], if the request response within this duration,
                     * it will not cause loading state changes.
                     * This can avoid flickering and is suitable for scenarios
                     * where the interface returns quickly.
                     */
                    if (isUsed) loadingDelay = 1.seconds
                }

                OptionFunc.Debounce -> run {
                    /**
                     * 当你配置了[DebounceOptions.wait]，会按照设置值进行防抖处理
                     *
                     * When you configure [DebounceOptions.wait], anti-shake processing will be performed according to the set value.
                     */
                    if (isUsed) debounceOptionsOf = { wait = 3.seconds }
                }

                OptionFunc.Throttle -> run {
                    /**
                     * 当你配置了[ThrottleOptions.wait]，会按照设置值进行节流处理
                     *
                     * When you configure [ThrottleOptions.wait], throttling will be performed according to the set value.
                     */
                    if (isUsed) throttleOptionsOf = { wait = 3.seconds }
                }
            }
        },
    )
    useEventSubscribe { _: Unit ->
        request()
    }
    val userInfo by userInfoState
    val loading by loadingState
    Column(modifier = Modifier.height(100.dp)) {
        Text(text = "$label:$isUsed${if (isUsed) " $label wait 3seconds" else ""}")
        if (loading) {
            Text(text = "Loading ...")
        } else if (userInfo.asBoolean()) {
            Text(text = "$userInfo".substring(0..100))
        }
    }
}
