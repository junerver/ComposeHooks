package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.junerver.compose.hooks.useGetState
import xyz.junerver.compose.hooks.usePrevious
import xyz.junerver.compose.hooks.useState

/**
 * Description:
 * @author Junerver
 * date: 2024/3/11-9:50
 * Email: junerver@gmail.com
 * Version: v1.0
 */
@Composable
fun UsePreviousExample() {
    val (input, setInput) = useGetState("")
    val previous = usePrevious(present = input)
    Surface {
        Column {
            OutlinedTextField(value = input, onValueChange = setInput)

            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "Previous: $previous")
        }
    }
}
