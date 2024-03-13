package xyz.junerver.composehooks.example.request

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.seconds
import xyz.junerver.compose.hooks.optionsOf
import xyz.junerver.compose.hooks.useMount
import xyz.junerver.compose.hooks.useRef
import xyz.junerver.compose.hooks.useUnmount
import xyz.junerver.compose.hooks.userequest.useRequest
import xyz.junerver.composehooks.net.WebService
import xyz.junerver.composehooks.net.asRequestFn
import xyz.junerver.composehooks.ui.component.TButton
import xyz.junerver.kotlin.asBoolean

/**
 * Description:
 * @author Junerver
 * date: 2024/3/12-15:28
 * Email: junerver@gmail.com
 * Version: v1.0
 */
@Composable
fun LoadingDelay() {

    Surface {
        Column {
            TButton(text = "refresh") {
                trigger()
            }
            SubComponent()
            Divider(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(20.dp))
            SubComponent(true)
        }
    }
}

@Composable
fun SubComponent(isLoadingDelay: Boolean = false) {
    val (userInfo, loading, _, _, _, refresh) = useRequest(
        requestFn = WebService::userInfo.asRequestFn(),
        optionsOf {
            defaultParams = arrayOf("junerver")
            if (isLoadingDelay) {
                /**
                 * 当你配置了[loadingDelay]，如果请求在这个时间之内返回就不会引起loading的变化，
                 * 这可以避免闪烁，适用于接口较为快速返回的场景。
                 * When you configure [loadingDelay], if the request response within this duration,
                 * it will not cause loading state changes.
                 * This can avoid flickering and is suitable for scenarios
                 * where the interface returns quickly.
                 */
                loadingDelay = 1.seconds
            }
        }
    )
    val unsubscribeRef = useRef<(() -> Unit)?>(null)
    useMount {
        unsubscribeRef.current = subscribe {
            refresh()
        }
    }
    useUnmount {
        unsubscribeRef.current?.invoke()
    }
    Column(modifier = Modifier.height(100.dp)) {
        Text(text = "isLoadingDelay:$isLoadingDelay")
        if (loading) {
            Text(text = "Loading ...")
        } else if (userInfo.asBoolean()) {
            Text(text = "$userInfo".substring(0..100))
        }
    }
}

val listeners = mutableListOf<() -> Unit>()

fun subscribe(listener: () -> Unit): () -> Unit {
    listeners.add(listener)
    return fun() {
        listeners.remove(listener)
    }
}

fun trigger() {
    listeners.forEach {
        it.invoke()
    }
}
