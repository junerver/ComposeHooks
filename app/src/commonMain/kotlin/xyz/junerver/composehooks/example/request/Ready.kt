package xyz.junerver.composehooks.example.request

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.junerver.compose.hooks.optionsOf
import xyz.junerver.compose.hooks.userequest.useRequest
import xyz.junerver.composehooks.net.WebService
import xyz.junerver.composehooks.net.asRequestFn
import xyz.junerver.kotlin.asBoolean

/*
  Description: 通过使用 Ready 你可以轻松的创建链式请求
  By using Ready you can easily create chained requests

  Author: Junerver
  Date: 2024/3/13-14:11
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun Ready() {
    val (userInfo, userLoading) = useRequest(
        requestFn = WebService::userInfo.asRequestFn(),
        optionsOf {
            defaultParams =
                arrayOf("junerver")
        }
    )
    val (repoInfo, repoLoading) = useRequest(
        requestFn = WebService::repoInfo.asRequestFn(),
        optionsOf {
            defaultParams = arrayOf(
                "junerver",
                "ComposeHooks"
            )
            ready = userInfo.asBoolean()
        }
    )

    Surface {
        Column {
            if (userLoading) {
                Text(text = "user loading ...")
            } else if (userInfo.asBoolean()) {
                Text(text = "$userInfo".substring(0..100))
            }
            Spacer(modifier = Modifier.height(30.dp))
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
            if (repoLoading) {
                Text(text = "repo loading ...")
            } else if (repoInfo.asBoolean()) {
                Text(text = "$repoInfo".substring(0..100))
            }
        }
    }
}
