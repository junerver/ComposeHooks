package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.useBoolean
import xyz.junerver.compose.hooks.useMount
import xyz.junerver.compose.hooks.useUnmount
import xyz.junerver.compose.hooks.useUnmountedRef
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
            if (visible.value) {
                UnmountableChild(text = "${Random.nextDouble()}")
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
//    val ctx = LocalContext.current
    val unmountedRef = useUnmountedRef()
    useMount {
        println("on mount")
        launch(Dispatchers.Main + SupervisorJob()) {
            delay(4.seconds)
            println("do something after unmount")
            if (!unmountedRef.current) {
                println("on mount delay")
            } else {
                println("component is unmounted!")
            }
        }
    }
    useUnmount {
        println(" on unmount")
    }

    Text(text = text)
}
