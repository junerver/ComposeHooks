package xyz.junerver.composehooks.example.request

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable

/**
 * Description:
 * @author Junerver
 * date: 2024/3/13-14:28
 * Email: junerver@gmail.com
 * Version: v1.0
 */
@Composable
fun Throttle() {
    Surface {
        Container("throttled", OptionFunc.Throttle)
    }
}
