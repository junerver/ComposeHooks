package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.random.Random
import xyz.junerver.compose.hooks.useGetState
import xyz.junerver.compose.hooks.useTimeout
import xyz.junerver.compose.hooks.useUpdate
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description:
  Author: Junerver
  Date: 2024/3/11-9:09
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun UseTimeoutExample() {
    val (state, setState, getState) = useGetState(10)
    val update = useUpdate()
    /**
     * When the component is mounted, the closure block function is executed with a delay of 1s(default).
     */
    useTimeout {
        setState(getState() - 1)
    }

    Surface {
        Column {
            Text(text = "current: $state  flag: ${Random.nextDouble()}")

            TButton(text = "update") {
                /**
                 * Even if the component is refreshed, the closure in [useTimeout] will not be executed again
                 */
                update()
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
