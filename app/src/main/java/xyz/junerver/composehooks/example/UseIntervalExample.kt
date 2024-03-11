package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import kotlin.time.Duration.Companion.seconds
import xyz.junerver.compose.hooks.optionsOf
import xyz.junerver.compose.hooks.useEffect
import xyz.junerver.compose.hooks.useInterval
import xyz.junerver.compose.hooks.useLatestState
import xyz.junerver.compose.hooks.useState

/**
 * Description:
 * @author Junerver
 * date: 2024/3/8-13:05
 * Email: junerver@gmail.com
 * Version: v1.0
 */
@Composable
fun UseIntervalExample() {
    val (countDown, setCountDown) = useState(60)
    val currentCount by useLatestState(value = countDown)
    val (_, cancel) = useInterval(
        optionsOf {
            initialDelay = 5.seconds
            period = 2.seconds
        }
    ) {
        setCountDown(currentCount - 1)
    }
    useEffect(currentCount) {
        if (currentCount == 0) cancel()
    }
    Surface {
        Column {
            Text(text = "current: $countDown")
        }
    }
}
