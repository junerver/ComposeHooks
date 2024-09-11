package xyz.junerver.composehooks.example

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kotlin.time.Duration.Companion.seconds
import xyz.junerver.compose.hooks.optionsOf
import xyz.junerver.compose.hooks.useTimestamp
import xyz.junerver.compose.hooks.useTimestampRef
import xyz.junerver.composehooks.example.request.DividerSpacer
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description:
  Author: Junerver
  Date: 2024/3/14-10:24
  Email: junerver@gmail.com
  Version: v1.0
*/
@Composable
fun UseTimestampExample() {
    Surface {
        Column {
            SubState()
            DividerSpacer()
            SubRef()
        }
    }
}

@Composable
fun SubState() {
    val (timestamp, pause, resume, isActive) = useTimestamp(
        optionsOf {
            interval = 1.seconds
            callback = {
                Log.d("UseTimestampExample", "UseTimestampExample: $it")
            }
        }
    )
    Column {
        Text(text = "TimestampState: $timestamp")
        Row {
            TButton(text = "pause", enabled = isActive) {
                pause()
            }
            TButton(text = "resume", enabled = !isActive) {
                resume()
            }
        }
    }
}

@Composable
fun SubRef() {
    val (timestamp, _, resume) = useTimestampRef(
        optionsOf {
            interval = 1.seconds
        }
    )
    resume()
    Column {
        Text(text = "TimestampRef: ${timestamp.current}")
        Row {
            TButton(text = "alert time") {
                println("${timestamp.current}")
            }
        }
    }
}
