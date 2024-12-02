package xyz.junerver.composehooks.example.request

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.userequest.useRequest
import xyz.junerver.composehooks.net.WebService
import xyz.junerver.composehooks.net.asRequestFn
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description:
  Author: Junerver
  Date: 2024/3/12-13:49
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun Refresh() {
    var params by useState("")
    val (userInfoState, loadingState, _, request, _, refresh) = useRequest(
        requestFn = WebService::userInfo.asRequestFn(),
        optionsOf = {
            defaultParams = arrayOf("junerver")
            onBefore = {
                params = it.joinToString("、")
            }
        }
    )
    val userInfo by userInfoState
    val loading by loadingState
    Surface {
        Column {
            Text(text = "Refresh: ")
            Spacer(modifier = Modifier.height(10.dp))
            Row {
                TButton(text = "request Other") {
                    request("jeremymailen")
                }
                TButton(text = "refresh") {
                    refresh()
                }
            }
            Spacer(modifier = Modifier.height(15.dp))
            Text(text = "current user: $params")
            Spacer(modifier = Modifier.height(15.dp))
            if (loading) {
                Text(text = "Loading ...")
            } else {
                Text(text = "$userInfo")
            }
        }
    }
}
