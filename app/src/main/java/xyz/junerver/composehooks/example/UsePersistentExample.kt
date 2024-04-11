package xyz.junerver.composehooks.example

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import xyz.junerver.compose.hooks.usePersistent
import xyz.junerver.compose.hooks.useState
import xyz.junerver.composehooks.ui.component.TButton
import xyz.junerver.kotlin.hideKeyboard

/**
 * Description:
 * @author Junerver
 * date: 2024/4/10-14:44
 * Email: junerver@gmail.com
 * Version: v1.0
 */

@Composable
fun UsePersistentExample() {
    val (count, saveCount) = usePersistent(key = "count", -1)
    val (token) = usePersistent(key = "token", "")
    Surface {
        Column {
            TButton(text = "+1") {
                saveCount(count + 1)
            }
            Text(text = "persistent: $count")
            Text(text = "token: $token")
            Divider(modifier = Modifier.fillMaxWidth())
            SubCount()
        }
    }
}

@Composable
private fun SubCount() {
    val (count) = usePersistent(key = "count", -1)
    val (_, saveToken) = usePersistent(key = "token", "")
    val (state, setState) = useState("")
    Column {
        Text(text = "other component persistent: $count")
        OutlinedTextField(value = state, onValueChange = {
            setState(it)
        })
        TButton(text = "saveToken") {
            (this as ComponentActivity).hideKeyboard()
            saveToken(state)
            setState("")
            toast("now you can exit app,and reopen")
        }
    }
}
