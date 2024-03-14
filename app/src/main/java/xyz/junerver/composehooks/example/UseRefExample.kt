package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import xyz.junerver.compose.hooks.useRef
import xyz.junerver.compose.hooks.useUpdate
import xyz.junerver.composehooks.ui.component.TButton

/**
 * Description:
 * @author Junerver
 * date: 2024/3/8-11:16
 * Email: junerver@gmail.com
 * Version: v1.0
 */
@Composable
fun UseRefExample() {
    val countRef = useRef(0)
    val update = useUpdate()
    Surface {
        Column {
            Text(text = "ref : ${countRef.current}")
            TButton(text = "+1") {
                countRef.current += 1
            }
            TButton(text = "force update") {
                update()
            }
            TButton(text = "toast ref") {
                toast("ref.current: ${countRef.current}")
            }
        }
    }
}
