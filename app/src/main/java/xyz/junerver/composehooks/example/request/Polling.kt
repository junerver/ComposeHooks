package xyz.junerver.composehooks.example.request

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.seconds
import xyz.junerver.compose.hooks.*
import xyz.junerver.compose.hooks.userequest.useRequest
import xyz.junerver.compose.hooks.utils.asBoolean
import xyz.junerver.composehooks.net.WebService
import xyz.junerver.composehooks.net.asRequestFn

/*
  Description:
  Author: Junerver
  Date: 2024/3/13-10:59
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun Polling() {
    val (showTipsState, _, set) = useBoolean(false)
    val showTips by showTipsState
    useEventSubscribe { count: Int ->
        if (!showTips && count >= 1) {
            set(true)
        }
    }
    Surface {
        Column {
            Sub()
            DividerSpacer()
            Sub(true)
            DividerSpacer()
            if (showTips) {
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
    val (userInfoState, loadingState) = useRequest(
        requestFn = WebService::userInfo.asRequestFn(),
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
    val userInfo by userInfoState
    val loading by loadingState
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
