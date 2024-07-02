package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import xyz.junerver.compose.hooks.useClipboard
import xyz.junerver.compose.hooks.useGetState

/*
  Description:
  Author: Junerver
  Date: 2024/4/2-11:15
  Email: junerver@gmail.com
  Version: v1.0
*/
@Composable
fun UseClipboardExample() {
    Surface {
        Column {
            Copy()
            Paste()
        }
    }
}

@Composable
private fun Copy() {
    val (state, setState) = useGetState("")
    val (copy, _) = useClipboard()
    Column {
        TextField(
            value = state,
            onValueChange = setState,
            label = { Text("Text to copy") }
        )
        Button(onClick = { copy(state) }) {
            Text("Copy to clipboard")
        }
    }
}

@Composable
private fun Paste() {
    val (state, setState) = useGetState("")
    val (_, paste) = useClipboard()
    Column {
        Text("$state")
        Button(onClick = { setState(paste()) }) {
            Text("Paste from clipboard")
        }
    }
}
