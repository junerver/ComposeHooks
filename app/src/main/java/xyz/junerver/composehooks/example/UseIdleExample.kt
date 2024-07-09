package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import xyz.junerver.compose.hooks.useidle.useIdle

/*
  Description:
  Author: Junerver
  Date: 2024/7/9-9:31
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun UseIdleExample() {
    val (idle, lastActive) = useIdle()
    Surface {
        Column {
            Text(text = "Idle : $idle")
            Text(text = "lastActive : ${lastActive.toLocalDateTime(TimeZone.of("CTT"))}")
        }
    }
}
