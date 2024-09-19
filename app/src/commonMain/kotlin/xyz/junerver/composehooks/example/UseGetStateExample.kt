package xyz.junerver.composehooks.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import xyz.junerver.compose.hooks.component1
import xyz.junerver.compose.hooks.component2
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.useController
import xyz.junerver.compose.hooks.useCountdown
import xyz.junerver.compose.hooks.useGetState
import xyz.junerver.compose.hooks.useInterval
import xyz.junerver.compose.hooks.useReducer
import xyz.junerver.compose.hooks.useRef
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.useredux.ReduxProvider
import xyz.junerver.compose.hooks.useredux.createStore
import xyz.junerver.compose.hooks.useredux.useDispatch
import xyz.junerver.compose.hooks.useredux.useSelector
import xyz.junerver.compose.hooks.userequest.useRequest
import xyz.junerver.composehooks.net.NetApi
import xyz.junerver.composehooks.ui.component.SimpleContainer

/*
  Description:
  Author: Junerver
  Date: 2024/5/10-10:10
  Email: junerver@gmail.com
  Version: v1.0
*/
@Composable
fun UseGetStateExample() {
    Surface {
        Column {
            Text(text = "Resolve two issues that arise when using [useState] via destructuring declarations:")
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Question1. Closure problems")
            // in pass, you need to use [useLatestRef] to get latest value of state
            val (state, setState, getState) = useGetState("getState")
            LaunchedEffect(key1 = Unit) {
                repeat(10) {
                    delay(1.seconds)
                    // Now there is no need to use [useLatestRef] to get the latest value directly through [getter]
                    setState("${getState()}.")
                }
            }
            Text(text = state.value)

            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Question2. modify state very quickly")
            val (state2, setState2, getState2) = useGetState("getState2")
            LaunchedEffect(key1 = Unit) {
                repeat(20) {
                    setState2("${getState2()}.")
                }
            }
            Text(text = state2.value)
            HorizontalDivider()
            TestDeferReads()
        }
    }
}

@Composable
private fun TestDeferReads() {
    var byState by useState("default1")
    val controller = useController("default")
    val (setState, getState) = controller
    val state by useGetState(controller)

//    val (state, setState, getState) = useGetState("getState")
    val (stater, dispatch) = useReducer({ p: String, a: String -> a }, "reducer")
    val ref = useRef(10)
    val (leftTime, formattedRes) = useCountdown(
        optionsOf = {
            leftTime = 10.seconds
        }
    )
    useInterval({
        period = 1.seconds
    }, true) {
        dispatch(stater.value + "in")
        byState += "1"
        ref.current -= 1
    }
    val (userInfo, loading, error, req) = useRequest(
//        requestFn = NetApi::userInfo.asSuspendNoopFn(), // Make a request directly through the WebService instance
        requestFn = { NetApi.userInfo(it[0] as String) }, // Make a request WebService interface
        optionsOf = {
            defaultParams =
                arrayOf("junerver") // Automatically requests must set default parameters
        }
    )
    ReduxProvider(
        createStore {
            { p: String, a: String -> a } with "redux"
        }
    ) {
        Column(modifier = Modifier.randomBackground().size(200.dp, 400.dp)) {
            // by委托
            Button(onClick = {
                byState += "1"
            }) { Text(byState) }

            // triple 解构
            Button(onClick = {
                setState(getState() + "2")
            }) { Text(state) }

            Button(onClick = {
                dispatch(stater.value + "rx")
            }) { Text(stater.value) }

            Button(onClick = {
                req()
            }) { Text("request") }

            val dispatch = useDispatch<String>()
            val selector by useSelector<String>()
            Button(onClick = {
                dispatch(selector + "dx")
            }) { Text(selector) }
            Text("current ref : ${ref.current}")
            SimpleContainer { Text(text = "LeftTime: ${leftTime.value.inWholeSeconds}") }
            SimpleContainer { Text(text = formattedRes.value.toString()) }
        }
    }
}

val colors = arrayOf(
    0x5D000000,
    0x5Dfef200,
    0x5Db5e51d,
    0x5D9ad9ea,
    0x5Dc3c3c3,
    0x5Dff7f26,
    0x5D23b14d,
    0x5D00a3e8,
    0x5D7f7f7f,
    0x5Dfeaec9,
    0x5Dc7bfe8,
    0x5Da349a3,
    0x5Dffffff,
    0x5Ded1b24,
    0x5D7092bf,
    0x5D3f47cc
)

@Composable
fun Modifier.randomBackground(): Modifier = composed {
    this.background(Color(colors[Random.nextInt(colors.size)]))
}
