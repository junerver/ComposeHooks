package xyz.junerver.composehooks.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import xyz.junerver.compose.hooks.getValue
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.setValue
import xyz.junerver.compose.hooks.useBoolean
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
import xyz.junerver.composehooks.ui.component.SimpleContainer

@Composable
fun DeferReads() {
    Surface {
        Column {
            Text("The background color changes indicate that the component recompose")
            TestDeferReads()
        }
    }
}

/**
 * hooks2 不再直接返回*状态的值*，而是返回状态本身，延迟读取状态，缩小重组范围。
 * 下面这段代码如果在hooks1中，所有状态都在函数调用处读取，会使得整个组件变成重组作用域，
 * 可能会造成性能损耗。
 *
 * hooks2 no longer returns the value of the state directly, but the state itself,
 * delaying the read state and narrowing the scope of the recompose
 * If the following code is use hooks1, all the state is read at the function call,
 * which will make the entire component recompose, which may cause performance loss.
 */
@Composable
private fun TestDeferReads() {
    val (ready, toggle) = useBoolean(true)
    var byState by useState("delegate:")
    val (state, setState) = useGetState("getState:")
    val (reducerState, reducerDispatch) = useReducer({ p: String, a: String -> a }, "reducer:")
    var ref by useRef(10)
    val (leftTime, formattedRes) = useCountdown(
        optionsOf = {
            leftTime = 10.seconds
            onEnd = toggle
        }
    )
    useInterval({
        period = 1.seconds
    }, ready.value) {
        reducerDispatch(reducerState.value + "3")
        byState += "1"
        ref -= 1
    }

    ReduxProvider(
        createStore {
            { p: String, a: String -> a } with "redux:"
        }
    ) {
        Column(modifier = Modifier.randomBackground().size(200.dp, 400.dp)) {
            // by委托
            Button(onClick = {
                byState += "1"
            }) { Text(byState) }

            // triple 解构
            Button(onClick = {
                setState { it + "2" }
            }) { Text(state.value) }

            Button(onClick = {
                reducerDispatch(reducerState.value + "3")
            }) { Text(reducerState.value) }

            val dispatch = useDispatch<String>()
            val selector by useSelector<String>()
            Button(onClick = {
                dispatch(selector + "4")
            }) { Text(selector) }
            Text("current ref : $ref")
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
