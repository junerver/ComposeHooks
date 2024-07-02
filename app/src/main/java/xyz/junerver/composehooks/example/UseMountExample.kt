package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import xyz.junerver.compose.hooks.useBoolean
import xyz.junerver.compose.hooks.useMount
import xyz.junerver.compose.hooks.useUnmount
import xyz.junerver.compose.hooks.useUpdate
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description:
  Author: Junerver
  Date: 2024/3/8-11:41
  Email: junerver@gmail.com
  Version: v1.0
*/
@Composable
fun UseMountExample() {
    val update = useUpdate()
    val (visible, toggle) = useBoolean()
    Surface {
        Column {
            if (visible) {
                UnmountableChild(text = "${Math.random()}")
            }
            TButton(text = "Update") {
                update()
            }
            TButton(text = "toggle Visible") {
                toggle()
            }
        }
    }
}

@Composable
fun UnmountableChild(text: String) {
    val ctx = LocalContext.current
    useMount {
        ctx.toast("on mount")
    }
    useUnmount {
        ctx.toast(" on unmount")
    }
    Text(text = text)
}
