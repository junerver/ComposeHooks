package xyz.junerver.composehooks.example.request

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.userequest.useRequest
import xyz.junerver.composehooks.net.WebService
import xyz.junerver.composehooks.net.asRequestFn

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
    useRequest(
        requestFn = WebService::userInfo.asRequestFn(),
        optionsOf = {
            defaultParams = arrayOf("junerver")
            onBefore = {
                state += "onBefore: ${it.joinToString("、")}"
            }
            onSuccess = { data, _ ->
                Log.d("Lifecycle", "Lifecycle: onSuccess")
                state += "\n\nonSuccess:\nData:$data"
            }
            onError = { err, pa ->
                state += "\n\nonError: ${pa.joinToString("、")}\nError: ${err.message}"
            }
            onFinally = { _, _, _ ->
                state += "\n\nonFinally!"
            }
        }
    )
    Surface {
        Column {
            Text(text = state)
        }
    }
}
