package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import xyz.junerver.compose.hooks.usePersistent
import xyz.junerver.composehooks.ui.component.TButton

/**
 * Description:
 * @author Junerver
 * date: 2024/4/10-14:44
 * Email: junerver@gmail.com
 * Version: v1.0
 */


@Composable
fun UsePersistentExample() {
    val (count, saveCount) = usePersistent(key = "count",-1)
    Surface {
        Column {
            TButton(text = "+1") {
                saveCount(count + 1)
            }
            Text(text = "persistent: $count")
            Divider(modifier = Modifier.fillMaxWidth())
            SubCount()
        }
    }
}

@Composable
private fun SubCount() {
    val (count) = usePersistent(key = "count",-1)
    Text(text = "other component persistent: $count")
}
