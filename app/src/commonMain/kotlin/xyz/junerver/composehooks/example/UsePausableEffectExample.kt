package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.junerver.compose.hooks.usePausableEffect
import xyz.junerver.compose.hooks.useState
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description:
  Author: Junerver
  Date: 2025/7/3-16:22
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * usePausableEffect Example
 */
@Composable
fun UsePausableEffectExample() {
    val (source, setSource) = useState("")
    val (log, setLog) = useState("")
    val stop = usePausableEffect(source) {
        setLog(log + "Changed to \"${source}\"\n")
    }
    val (_, pause, resume) = stop

    fun clear() {
        setLog("")
    }

    fun pauseEffect() {
        setLog(log + "Paused\n")
        pause()
    }

    fun resumeEffect() {
        setLog(log + "Resumed\n")
        resume()
    }

    fun stopEffect() {
        setLog(log + "Stopped\n")
        stop()
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Type something below to trigger the effect")

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = source,
            onValueChange = setSource,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            TButton("Pause", onClick = ::pauseEffect)
            TButton("Resume", onClick = ::resumeEffect)
            TButton("Stop", onClick = ::stopEffect)
            TButton("Clear Log", onClick = ::clear)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Log：")
        Text(log)
    }
}
