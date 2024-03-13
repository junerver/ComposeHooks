package xyz.junerver.composehooks.example.request

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.seconds
import xyz.junerver.compose.hooks.optionsOf
import xyz.junerver.compose.hooks.useRef
import xyz.junerver.compose.hooks.useUpdate
import xyz.junerver.compose.hooks.userequest.useRequest
import xyz.junerver.composehooks.net.WebService
import xyz.junerver.composehooks.net.asRequestFn
import xyz.junerver.kotlin.asBoolean

/**
 * Description:
 * @author Junerver
 * date: 2024/3/13-10:59
 * Email: junerver@gmail.com
 * Version: v1.0
 */
@Composable
fun Polling() {
    // By using `useRef` the count is not lost even when the screen is rotated
    val countRef = useRef(default = 0)
    val update = useUpdate()
    val (userInfo, loading, _, _, _, refresh) = useRequest(
        requestFn = WebService::userInfo.asRequestFn(),
        optionsOf {
            defaultParams = arrayOf("junerver")
            pollingInterval = 3.seconds
            onSuccess = { _, _ ->
                countRef.current += 1
                update()
            }
        }
    )
    Surface {
        Column {
            Text(text = "Polling count: ${countRef.current}")
            Spacer(modifier = Modifier.height(20.dp))
            if (loading) {
                Text(text = "Loading ...")
            } else if (userInfo.asBoolean()) {
                Text(text = "$userInfo".substring(0..100))
            }
        }
    }
}
