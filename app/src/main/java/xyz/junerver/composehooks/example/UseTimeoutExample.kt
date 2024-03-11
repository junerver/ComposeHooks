package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import xyz.junerver.compose.hooks.useLatestRef
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.useTimeout
import xyz.junerver.compose.hooks.useUpdate
import xyz.junerver.composehooks.ui.component.TButton

/**
 * Description:
 * @author Junerver
 * date: 2024/3/11-9:09
 * Email: junerver@gmail.com
 * Version: v1.0
 */
@Composable
fun UseTimeoutExample() {
    val (state, setState) = useState(10)
    val stateRef = useLatestRef(state)
    val update = useUpdate()
    /**
     * When the component is mounted, the closure block function is executed with a delay of 1s(default).
     */
    useTimeout {
        setState(stateRef.current - 1)
    }

    Surface {
        Column {
            Text(text = "current: $state  ${Math.random()}")

            TButton(text = "update") {
                /**
                 * Even if the component is refreshed, the closure in use Timeout will not be executed again
                 */
                update()
            }
        }
    }
}
