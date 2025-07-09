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
import xyz.junerver.compose.hooks.Ref
import xyz.junerver.compose.hooks.observeAsState
import xyz.junerver.compose.hooks.useEffect
import xyz.junerver.compose.hooks.useRef
import xyz.junerver.compose.hooks.useUpdate
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description:
  Author: Junerver
  Date: 2024/3/8-11:16
  Email: junerver@gmail.com
  Version: v1.0
*/
@Composable
fun UseRefExample() {
    val countRef = useRef("0")
    val update = useUpdate()
    // 监听ref执行副作用
    useEffect(countRef) {
        println("ref change: ${countRef.current}")
    }
    Surface {
        Column {
            Text(text = "ref : ${countRef.current}")
            TButton(text = "+1") {
                countRef.current += "1"
            }
            TButton(text = "force update") {
                update()
            }
            TButton(text = "toast ref") {
                println("ref.current: ${countRef.current}")
            }
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            )
            SubRef(ref = countRef)
            SubRef(ref = countRef)
            SubRef(ref = countRef)
        }
    }
}

/**
 * Sub ref
 *
 * @param ref Refs exposed to subcomponents should be read-only [Ref] to avoid modifications by subcomponents [Ref.current]
 */
@Composable
private fun SubRef(ref: Ref<String>) {
    val refState by ref.observeAsState()
    Text(text = "parent's Ref is : $refState")
}
