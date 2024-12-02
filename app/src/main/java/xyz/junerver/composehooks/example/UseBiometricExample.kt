package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import xyz.junerver.compose.hooks.useBiometric
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description:
  Author: Junerver
  Date: 2024/7/22-8:42
  Email: junerver@gmail.com
  Version: v1.0
*/
@Composable
fun UseBiometricExample() {
    val (open, isAuthed) = useBiometric()
    Surface {
        Column {
            TButton(text = "open fingerprint auth") {
                open()
            }
            Text(text = "IsAuthed: ${isAuthed.value}")
        }
    }
}
