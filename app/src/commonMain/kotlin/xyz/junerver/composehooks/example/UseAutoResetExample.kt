package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlin.time.Duration.Companion.seconds
import xyz.junerver.compose.hooks.useAutoReset
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description:
  Author: Junerver
  Date: 2024/7/8-15:58
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun UseAutoResetExample() {
    var state by useAutoReset(default = "default value", interval = 2.seconds)
    Surface {
        Column {
            Text(text = state)
            TButton(text = "set value") {
                state = "has set new value"
            }
        }
    }
}
