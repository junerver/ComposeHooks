package xyz.junerver.composehooks.example.request

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import xyz.junerver.compose.hooks.optionsOf
import xyz.junerver.compose.hooks.useGetState
import xyz.junerver.compose.hooks.userequest.useRequest
import xyz.junerver.composehooks.net.WebService
import xyz.junerver.composehooks.net.asRequestFn
import xyz.junerver.composehooks.ui.component.TButton
import xyz.junerver.kotlin.asBoolean

/**
 * Description:
 * 当你某个请求发起后需要刷新另一个请求时这会很有用
 *
 * This is useful when you need to refresh a request after another request has been initiated.
 *
 * @author Junerver
 * date: 2024/3/13-14:27
 * Email: junerver@gmail.com
 * Version: v1.0
 */
@Composable
fun DepsRefresh() {
    val (state, setState) = useGetState(0)
    val (userInfo, loading, error) = useRequest(
        requestFn = WebService::userInfo.asRequestFn(),
        optionsOf {
            defaultParams =
                arrayOf("junerver")
            refreshDeps = arrayOf(state)
        }
    )
    Surface {
        Column {
            TButton(text = "+1") {
                setState(state + 1)
            }
            if (loading) {
                Text(text = "Loading ...")
            } else if (userInfo.asBoolean()) {
                Text(text = "$userInfo".substring(0..100))
            }
        }
    }
}
