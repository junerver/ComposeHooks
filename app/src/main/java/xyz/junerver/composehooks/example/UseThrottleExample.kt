package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.useThrottle
import xyz.junerver.compose.hooks.useThrottleFn
import xyz.junerver.composehooks.ui.component.TButton

/**
 * Description:
 *
 * @author Junerver date: 2024/3/8-14:13 Email: junerver@gmail.com Version:
 *     v1.0
 */
@Composable
fun UseThrottleExample() {
    // for throttle
    val (state, setState) = useState(0)
    val throttledState = useThrottle(value = state)

    // for useThrottleFn
    val (stateFn, setStateFn) = useState(0)
    val throttledFn = useThrottleFn(fn = { setStateFn(stateFn + 1) })
    Surface {
        Column {
            Text(text = "current: $state")
            Text(text = "throttled: $throttledState")
            TButton(text = "+1") {
                setState(state + 1)
            }

            Text(text = "current: $stateFn")
            TButton(text = "throttled +1") {
                /** Manual import：`import xyz.junerver.compose.hooks.invoke` */
                throttledFn()
            }
        }
    }
}
