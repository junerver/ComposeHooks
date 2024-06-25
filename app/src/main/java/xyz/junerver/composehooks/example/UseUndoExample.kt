package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.junerver.compose.hooks.useGetState
import xyz.junerver.compose.hooks.useUndo
import xyz.junerver.composehooks.ui.component.TButton

/**
 * Description:
 * @author Junerver
 * date: 2024/3/11-9:33
 * Email: junerver@gmail.com
 * Version: v1.0
 */
@Composable
fun UseUndoExample() {
    val (state, set, reset, undo, redo, canUndo, canRedo) = useUndo(initialPresent = "")
    val (input, setInput) = useGetState("")
    Surface {
        Column {
            OutlinedTextField(value = input, onValueChange = setInput)
            TButton(text = "submit") {
                set(input)
                setInput("")
            }
            Row {
                TButton(text = "reset") {
                    reset("")
                }
                TButton(text = "undo", enabled = canUndo) {
                    undo()
                }
                TButton(text = "redo", enabled = canRedo) {
                    redo()
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "result:\n$state")
        }
    }
}
