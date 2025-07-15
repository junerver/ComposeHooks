package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import xyz.junerver.compose.hooks.DefaultEnglishTimeAgoMessages
import xyz.junerver.compose.hooks.useControllable
import xyz.junerver.compose.hooks.useLastChanged
import xyz.junerver.compose.hooks.useTimeAgo

/*
  Description:
  Author: Junerver
  Date: 2025/6/24-16:21
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun UseLastChangedExample() {
    val (text, setText) = useControllable("")
    val lastChange by useLastChanged(text)
    val timeAgo by useTimeAgo(lastChange) {
        messages = DefaultEnglishTimeAgoMessages
    }
    Surface {
        Column {
            TextField(value = text.value, onValueChange = setText, label = { Text("Type anything...") })
            Text("Last changed: $timeAgo (${lastChange.toEpochMilliseconds()})")
        }
    }
}
