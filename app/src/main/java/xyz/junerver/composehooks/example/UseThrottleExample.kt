package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.useLatestRef
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.useThrottle
import xyz.junerver.compose.hooks.useThrottleFn
import xyz.junerver.composehooks.ui.component.TButton

/**
 * Description:
 * @author Junerver
 * date: 2024/3/8-14:13
 * Email: junerver@gmail.com
 * Version: v1.0
 */
@Composable
fun UseThrottleExample() {
    val (state, setState) = useState(0)
    val latest = useLatestRef(value = state)
    fun simpleFn() {
        /**
         * Because [useThrottleFn] needs to pass in a function reference,
         * it will cause closure problems and needs to be avoided by using [useLatestRef];
         */
        setState(latest.current + 1)
    }

    val throttledState = useThrottle(value = state)
    val throttledFn = useThrottleFn(fn = { simpleFn() })
    Surface {
        Column {
            Text(text = "current: $state")
            Text(text = "throttled: $throttledState")

            TButton(text = "+1") {
                simpleFn()
            }
            TButton(text = "throttled +1") {
                /**
                 *  Manual import：`import xyz.junerver.compose.hooks.invoke`
                 */
                throttledFn()
            }
        }
    }
}
