package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import xyz.junerver.compose.hooks.usevibrate.useVibrate
import xyz.junerver.composehooks.ui.component.TButton

/**
 * Description:
 * @author Junerver
 * date: 2024/4/19-16:41
 * Email: junerver@gmail.com
 * Version: v1.0
 */
@Composable
fun UseVibrateExample() {
    val (s, l) = useVibrate()
    Surface {
        Column {
            TButton(text = "short") {
                s()
            }

            TButton(text = "long") {
                l()
            }
        }
    }
}
