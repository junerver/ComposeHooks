package xyz.junerver.composehooks.example.request

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.optionsOf
import xyz.junerver.compose.hooks.userequest.RequestOptions
import xyz.junerver.compose.hooks.userequest.useRequest
import xyz.junerver.composehooks.net.NetApi
import xyz.junerver.composehooks.net.WebService
import xyz.junerver.composehooks.net.asRequestFn
import xyz.junerver.composehooks.ui.component.TButton
import xyz.junerver.kotlin.asBoolean

/*
  Description:
  Author: Junerver
  Date: 2024/3/12-8:36
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun AutoManual() {
    Surface {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
        ) {
            Auto()
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
            Manual()
        }
    }
}

@Composable
fun Auto() {
    val (userInfo, loading, error) = useRequest(
//        requestFn = NetApi.SERVICE::userInfo.asSuspendNoopFn(), // Make a request directly through the WebService instance
        requestFn = WebService::userInfo.asRequestFn(), // Make a request WebService interface
        optionsOf {
            defaultParams =
                arrayOf("junerver") // Automatically requests must set default parameters
        }
    )
    Column {
        Text(text = "Auto:")
        Spacer(modifier = Modifier.height(10.dp))
        if (loading) {
            Text(text = "user info loading ...")
        }
        if (userInfo.asBoolean()) {
            Text(text = userInfo.toString())
        }
        if (error.asBoolean()) {
            Text(text = "error: ${error.message}")
        }
    }
}

@Preview
@Composable
fun Manual() {
    val (repoInfo, loading, error, request) = useRequest(
        requestFn = {
            NetApi.SERVICE.repoInfo(it[0] as String, it[1] as String)
        },
        RequestOptions.optionOf {
            manual = true
            defaultParams =
                arrayOf(
                    "junerver",
                    "ComposeHooks"
                ) // Automatically requests must set default parameters
        }
    )
    Surface {
        Column {
            Row {
                Text(text = "Manual:")
                TButton(text = "request with default") {
                    /**
                     * 一般来说，手动请求的时候需要设置参数，但是如果你已经设置了默认参数[defaultParams]，你可以不传递,
                     * 但是你可能需要手动导入[invoke].
                     *
                     * Generally , parameters need to be set when making a manual request, but
                     * if you have set [defaultParams], you do not need to pass them, but you
                     * may need to manually import [invoke]
                     */
                    request()
                }
                TButton(text = "request with error params") {
                    request("unknow", "unknow")
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            if (loading) {
                Text(text = "user info loading ...")
            }
            if (error.asBoolean()) {
                Text(text = "error: ${error.message}")
            }
            if (repoInfo.asBoolean()) {
                Text(text = repoInfo.toString())
            }
        }
    }
}
