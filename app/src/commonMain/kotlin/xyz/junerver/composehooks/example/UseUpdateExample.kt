package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import xyz.junerver.compose.hooks.useUpdate
import xyz.junerver.composehooks.ui.component.TButton
import xyz.junerver.composehooks.utils.Timestamp

/*
  Description:
  Author: Junerver
  Date: 2024/3/8-12:03
  Email: junerver@gmail.com
  Version: v1.0
*/
@Composable
fun UseUpdateExample() {
    val update = useUpdate()
    Surface {
        Column {
            Text(text = "${Timestamp.now()}")
            TButton(text = "update") {
                update()
            }
        }
    }
}
