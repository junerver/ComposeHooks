package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import arrow.core.right
import kotlinx.datetime.Clock
import xyz.junerver.compose.hooks.*
import xyz.junerver.composehooks.net.NetApi
import xyz.junerver.composehooks.ui.component.TButton
import xyz.junerver.composehooks.utils.subStringIf

/*
  Description:
  Author: Junerver
  Date: 2024/3/8-14:13
  Email: junerver@gmail.com
  Version: v1.0
*/
@Composable
fun UseThrottleExample() {
    // for throttle
    val (state, setState) = useGetState(0)
    val throttledState by useThrottle(value = state.value)

    // for useThrottleFn
    val (stateFn, setStateFn) = useGetState(0)
    val throttledFn = useThrottleFn(
        fn = { setStateFn({ it: Int -> it + 1 }.right()) },
        optionsOf = {
            leading = false
            trailing = false
        }
    )
    useEffect(stateFn) {
        println("$stateFn: ${Clock.System.now()}")
    }

    // for throttleEffect
    val (stateEf, setStateEf) = useGetState(0)
    val (result, setResult) = useGetState("")
    useThrottleEffect(stateEf) {
        setResult("loading")
        val result = NetApi.SERVICE.userInfo("junerver")
        setResult(result.toString().subStringIf())
    }
    Surface {
        Column {
            Text(text = "current: ${state.value}")
            Text(text = "throttled: $throttledState")
            TButton(text = "+1") {
                setState({ it: Int -> it + 1 }.right())
            }
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            )
            Text(text = "current: ${stateFn.value}")
            TButton(text = "throttled +1") {
                /** Manual import：`import xyz.junerver.compose.hooks.invoke` */
                throttledFn()
            }
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            )
            Text(text = "deps: ${stateEf.value}")
            TButton(text = "+1 trigger effect execute") {
                setStateEf({ it: Int -> it + 1 }.right())
            }
            Text(text = result.value)
        }
    }
}
