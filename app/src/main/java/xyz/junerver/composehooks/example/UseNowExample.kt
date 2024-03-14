package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import xyz.junerver.compose.hooks.useNow

/**
 * Description:
 * @author Junerver
 * date: 2024/3/14-12:08
 * Email: junerver@gmail.com
 * Version: v1.0
 */
@Composable
fun UseNowExample() {
    val now = useNow()
    Surface {
        Column {
            Text(text = now)
        }
    }
}
