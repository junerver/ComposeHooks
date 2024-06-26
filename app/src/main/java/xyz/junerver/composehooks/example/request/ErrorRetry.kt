package xyz.junerver.composehooks.example.request

import android.os.Parcelable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlin.coroutines.coroutineContext
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.parcelize.Parcelize
import xyz.junerver.compose.hooks.asSuspendNoopFn
import xyz.junerver.compose.hooks.optionsOf
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.userequest.useRequest
import xyz.junerver.kotlin.asBoolean

/*
  Description:
  Author: Junerver
  Date: 2024/3/13-14:28
  Email: junerver@gmail.com
  Version: v1.0
*/

@Parcelize
data class MockInfo(
    val name: String,
    val age: Int,
    val sex: String,
) : Parcelable

// 伪装的一个
var count = 0
suspend fun mockRequest(s1: String, s2: String): MockInfo {
    delay(200)
    count++
    if (count <= 3) {
        error("MockError")
    }
    if (count >= 7) {
        count = 0
    }
    if (coroutineContext.isActive) {
        return MockInfo("MockName+$s1", Random.nextInt(20), "s2:$s2")
    } else {
        error("coroutine cancelled")
    }
}

@Composable
fun ErrorRetry() {
    var count by useState("")

    val (mockInfo, stuLoading, err) = useRequest(
        requestFn = ::mockRequest.asSuspendNoopFn(),
        optionsOf {
            defaultParams = arrayOf("1", "2")
            retryCount = 5
            retryInterval = 2.seconds
            onError = { _, _ ->
                count += "${System.currentTimeMillis() / 1000}\n"
            }
        }
    )
    Surface {
        Column {
            Text("error time：\n$count")
            if (stuLoading) {
                Text("Loading ...")
            } else if (mockInfo.asBoolean()) {
                Text("MockSucc：${(mockInfo)}")
            } else if (err.asBoolean()) {
                Text(text = "Error msg: ${err.message}")
            }
        }
    }
}
