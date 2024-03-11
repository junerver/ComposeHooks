package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import xyz.junerver.compose.hooks.useState

/**
 * Description: [useState]can make controlled components easier to create
 * @author Junerver
 * date: 2024/3/8-14:29
 * Email: junerver@gmail.com
 * Version: v1.0
 */
@Composable
fun UseStateExample() {
    val (state, setState) = useState("")
    Surface {
        Column {
            Text(text = "this is a simple controlled component:")
            OutlinedTextField(value = state, onValueChange = setState)
            Text(text = "input：$state")
        }
    }
}
