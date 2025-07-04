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
import kotlin.time.Clock
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.useEffect
import xyz.junerver.compose.hooks.useGetState
import xyz.junerver.compose.hooks.useThrottle
import xyz.junerver.compose.hooks.useThrottleEffect
import xyz.junerver.compose.hooks.useThrottleFn
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
        fn = { setStateFn(stateFn.value + 1) },
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
        val resp = NetApi.userInfo("junerver")
        setResult(resp.toString().subStringIf())
    }
    Surface {
        Column {
            Text(text = "current: ${state.value}")
            Text(text = "throttled: $throttledState")
            TButton(text = "+1") {
                setState(state.value + 1)
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
                setStateEf(stateEf.value + 1)
            }
            Text(text = result.value)
        }
    }
}
