package xyz.junerver.composehooks.example.request

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import xyz.junerver.compose.hooks.ArrayParams
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.userequest.useRequest
import xyz.junerver.compose.hooks.utils.asBoolean
import xyz.junerver.composehooks.net.NetApi
import xyz.junerver.composehooks.net.bean.UserInfo
import xyz.junerver.composehooks.ui.component.DividerSpacer
import xyz.junerver.composehooks.ui.component.TButton

/**
 * Description:
 * Author: Junerver
 * Date: 2024/3/12-15:05
 * Email: junerver@gmail.com
 * Version: v1.0
 */
@Composable
fun Cancel() {
    val (userInfoState, loadingState, _, request, _, _, cancel) = useRequest<ArrayParams,UserInfo>(
        requestFn = { NetApi.userInfo(it[0] as String) },
        optionsOf = {
            manual = true
            defaultParams = arrayOf("junerver")
        },
    )
    val userInfo by userInfoState
    val loading by loadingState
    Surface {
        Column {
            Text(text = "loading : $loading")

            Row {
                TButton(text = "request") {
                    request()
                }
                TButton(text = "cancel") {
                    cancel()
                }
            }
            DividerSpacer()

            if (loading) {
                Text(text = "Loading ...")
            } else if (userInfo.asBoolean()) {
                Text(text = "$userInfo")
            }
        }
    }
}
