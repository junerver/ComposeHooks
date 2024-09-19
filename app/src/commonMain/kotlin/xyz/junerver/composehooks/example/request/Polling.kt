package xyz.junerver.composehooks.example.request

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.seconds
import xyz.junerver.compose.hooks.useBoolean
import xyz.junerver.compose.hooks.useEventPublish
import xyz.junerver.compose.hooks.useEventSubscribe
import xyz.junerver.compose.hooks.useRef
import xyz.junerver.compose.hooks.useUpdate
import xyz.junerver.compose.hooks.userequest.useRequest
import xyz.junerver.composehooks.net.NetApi
import xyz.junerver.kotlin.asBoolean

/*
  Description:
  Author: Junerver
  Date: 2024/3/13-10:59
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun Polling() {
    val (showTips, _, set) = useBoolean(false)
    useEventSubscribe { count: Int ->
        if (!showTips.value && count >= 1) {
            set(true)
        }
    }
    Surface {
        Column {
            Sub()
            DividerSpacer()
            Sub(true)
            DividerSpacer()
            if (showTips.value) {
                Text(
                    text = "!!! now push 'home' and wait a moment then back to app",
                    color = Color.Magenta
                )
            }
        }
    }
}

@Composable
fun Sub(isPollingWhenHidden: Boolean = false) {
    // By using `useRef` the count is not lost even when the screen is rotated
    val countRef = useRef(default = 0)
    val update = useUpdate()
    val post = useEventPublish<Int>()
    val (userInfo, loading) = useRequest(
        requestFn = { NetApi.userInfo(it[0] as String) },
        optionsOf = {
            defaultParams = arrayOf("junerver")
            pollingInterval = 3.seconds
            pollingWhenHidden = isPollingWhenHidden
            onSuccess = { _, _ ->
                countRef.current += 1
                post(countRef.current)
                update()
            }
        }
    )
    Column(modifier = Modifier.height(100.dp)) {
        Text(text = "Polling when hidden: $isPollingWhenHidden count: ${countRef.current}")
        Spacer(modifier = Modifier.height(20.dp))
        if (loading) {
            Text(text = "Loading ...")
        } else if (userInfo.asBoolean()) {
            Text(text = "$userInfo".substring(0..100))
        }
    }
}
