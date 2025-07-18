package xyz.junerver.composehooks.example.request

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.userequest.useRequest
import xyz.junerver.composehooks.net.NetApi
import xyz.junerver.composehooks.net.bean.UserInfo

/*
  Description:
  Author: Junerver
  Date: 2024/3/12-10:43
  Email: junerver@gmail.com
  Version: v1.0
*/
@Composable
fun Lifecycle() {
    var state by useState(default = "")
    useRequest<String, UserInfo>(
        requestFn = { NetApi.userInfo(it) },
        optionsOf = {
            defaultParams = "junerver"
            onBefore = {
                state += "onBefore: $it"
            }
            onSuccess = { data, _ ->
                println("Lifecycle Lifecycle: onSuccess")
                state += "\n\nonSuccess:\nData:$data"
            }
            onError = { err, pa ->
                state += "\n\nonError: $pa\nError: ${err.message}"
            }
            onFinally = { _, _, _ ->
                state += "\n\nonFinally!"
            }
        },
    )
    Surface {
        Column {
            Text(text = state)
        }
    }
}
