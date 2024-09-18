package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import xyz.junerver.compose.hooks.useBoolean
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description:
  Author: Junerver
  Date: 2024/3/8-10:08
  Email: junerver@gmail.com
  Version: v1.0
*/
@Composable
fun UseBooleanExample() {
    val (state, toggle, _, setTrue, setFalse) = useBoolean(true)
    Surface {
        Column {
            Text(text = "Effect: ${state.value}")
            Row {
                TButton(text = "toggle") {
                    toggle()
                }
                TButton(text = "setTrue") {
                    setTrue()
                }
                TButton(text = "setFalse") {
                    setFalse()
                }
            }
        }
    }
}
