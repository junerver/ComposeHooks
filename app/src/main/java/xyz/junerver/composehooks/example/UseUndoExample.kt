package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import arrow.core.left
import xyz.junerver.compose.hooks.left
import xyz.junerver.compose.hooks.useGetState
import xyz.junerver.compose.hooks.useUndo
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description:
  Author: Junerver
  Date: 2024/3/11-9:33
  Email: junerver@gmail.com
  Version: v1.0
*/
@Composable
fun UseUndoExample() {
    val (state, set, reset, undo, redo, canUndo, canRedo) = useUndo(initialPresent = "")
    val (inputState, setInput) = useGetState("")
    val input by inputState
    Surface {
        Column {
            OutlinedTextField(value = input, onValueChange = setInput.left())
            TButton(text = "submit") {
                set(input)
                setInput("".left())
            }
            Row {
                TButton(text = "reset") {
                    reset("")
                }
                TButton(text = "undo", enabled = canUndo.value) {
                    undo()
                }
                TButton(text = "redo", enabled = canRedo.value) {
                    redo()
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "result:\n${state.value}")
        }
    }
}
