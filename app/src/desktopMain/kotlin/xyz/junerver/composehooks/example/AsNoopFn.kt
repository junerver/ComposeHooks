package xyz.junerver.composehooks.example

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
import xyz.junerver.compose.hooks.ArrayParams
import xyz.junerver.compose.hooks.asSuspendNoopFn
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.tuple
import xyz.junerver.compose.hooks.userequest.useRequest
import xyz.junerver.compose.hooks.utils.asBoolean
import xyz.junerver.composehooks.net.NetApi
import xyz.junerver.composehooks.net.bean.RepoInfo
import xyz.junerver.composehooks.ui.component.DividerSpacer
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description:
  Author: Junerver
  Date: 2025/7/21-14:13
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun AsNoopFn() {
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
    val (userInfoState, loading, error) = useRequest(
        requestFn = NetApi::userInfo.asSuspendNoopFn(), // Make a request directly through the WebService instance
        optionsOf = {
            defaultParams = tuple("junerver") // Automatically requests must set default parameters
        },
    )
    val userInfo by userInfoState
    Column {
        Text(text = "Auto:")
        Spacer(modifier = Modifier.height(10.dp))
        if (loading.asBoolean()) {
            Text(text = "user info loading ...")
        }
        if (userInfo.asBoolean()) {
            Text(text = userInfo.toString())
        }
        if (error.asBoolean()) {
            Text(text = "error: ${error.value?.message}")
        }
    }
}

@Composable
fun Manual() {
    val (repoInfoState, loadingState, errorState, request) = useRequest<ArrayParams, RepoInfo>(
        requestFn = NetApi::repoInfo.asSuspendNoopFn(),
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
