package xyz.junerver.composehooks.example.request

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.userequest.RequestOptions
import xyz.junerver.compose.hooks.userequest.useRequest
import xyz.junerver.compose.hooks.utils.asBoolean
import xyz.junerver.composehooks.net.NetApi
import xyz.junerver.composehooks.ui.component.DividerSpacer
import xyz.junerver.composehooks.ui.component.TButton

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
                .verticalScroll(rememberScrollState()),
        ) {
            Auto()
            Spacer(modifier = Modifier.height(10.dp))
            DividerSpacer()
            Manual()
        }
    }
}

@Composable
fun Auto() {
    val (userInfoState, loadingState, errorState) = useRequest(
//        requestFn = NetApi::userInfo.asSuspendNoopFn(), // Make a request directly through the WebService instance
        requestFn = { NetApi.userInfo(it[0] as String) }, // Make a request WebService interface
        optionsOf = {
            defaultParams =
                arrayOf("junerver") // Automatically requests must set default parameters
        },
    )
    val userInfo by userInfoState
    val loading by loadingState
    val error by errorState
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
            Text(text = "error: ${error!!.message}")
        }
    }
}

@Composable
fun Manual() {
    val (repoInfoState, loadingState, errorState, request) = useRequest(
        requestFn = { NetApi.repoInfo(it[0] as String, it[1] as String) },
        // 使用 `options = optionsOf {}`这种传参会带来性能问题，请尽快更新使用性能优化版本，你可以简单的在`optionsOf`后面加`=`来进行替换
        optionsOf = {
            println("Configure closure execution!")
            manual = true
            defaultParams = arrayOf("junerver", "ComposeHooks") // Automatically requests must set default parameters
        },
    )
    val repoInfo by repoInfoState
    val loading by loadingState
    val error by errorState
    Surface {
        Column {
            Row {
                Text(text = "Manual:")
                TButton(text = "request with default") {
                    /**
                     * 一般来说，手动请求的时候需要设置参数，但是如果你已经设置了默认参数[RequestOptions.defaultParams]，你可以不传递,
                     * 但是你可能需要手动导入[invoke].
                     *
                     * Generally , parameters need to be set when making a manual request, but
                     * if you have set [RequestOptions.defaultParams], you do not need to pass them, but you
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
                Text(text = "error: ${error!!.message}")
            }
            if (repoInfo.asBoolean()) {
                Text(text = repoInfo.toString())
            }
        }
    }
}
