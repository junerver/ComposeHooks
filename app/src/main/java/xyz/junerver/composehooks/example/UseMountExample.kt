package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.*
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
            if (visible.value) {
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
    val unmountedRef = useUnmountedRef()
    useMount {
        ctx.toast("on mount")
        launch(Dispatchers.Main + SupervisorJob()) {
            delay(4.seconds)
            println("do something after unmount")
            if (!unmountedRef.current) {
                ctx.toast("on mount delay")
            } else {
                println("component is unmounted!")
            }
        }
    }
    useUnmount {
        ctx.toast(" on unmount")
    }

    Text(text = text)
}
