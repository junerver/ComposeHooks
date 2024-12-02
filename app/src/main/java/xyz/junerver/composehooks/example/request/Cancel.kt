package xyz.junerver.composehooks.example.request

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.userequest.useRequest
import xyz.junerver.compose.hooks.utils.asBoolean
import xyz.junerver.composehooks.net.WebService
import xyz.junerver.composehooks.net.asRequestFn
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
    val (userInfo, loading, _, request, _, _, cancel) = useRequest(
        requestFn = WebService::userInfo.asRequestFn(),
        optionsOf = {
            manual = true
            defaultParams = arrayOf("junerver")
        }
    )

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
            HorizontalDivider(modifier = Modifier.fillMaxWidth())

            if (loading.value) {
                Text(text = "Loading ...")
            } else if (userInfo.asBoolean()) {
                Text(text = "$userInfo")
            }
        }
    }
}
