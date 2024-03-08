package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.seconds
import xyz.junerver.compose.hooks.optionsOf
import xyz.junerver.compose.hooks.useInterval
import xyz.junerver.compose.hooks.useLatestRef
import xyz.junerver.compose.hooks.useState

/**
 * Description:
 * @author Junerver
 * date: 2024/3/8-13:38
 * Email: junerver@gmail.com
 * Version: v1.0
 */
@Composable
fun UseLatestExample() {
    Surface {
        Column {
            Normal()
            Spacer(modifier = Modifier.height(20.dp))
            UseStateButWithoutUseLatest()
            Spacer(modifier = Modifier.height(20.dp))
            UseStateAndUseLatest()
        }
    }
}

@Composable
fun Normal() {
    var count by remember { mutableIntStateOf(0) }
    useInterval(optionsOf {
        initialDelay = 5.seconds
        period = 1.seconds
    }) {
        count += 1
    }
    Text(text = "Normal : $count")
}

@Composable
fun UseStateButWithoutUseLatest() {
    val (count, setCount) = useState(0)
    useInterval(optionsOf {
        initialDelay = 5.seconds
        period = 1.seconds
    }) {
        // 这里会出现闭包问题，count的值永远是0
        // There will be a closure problem here, the value of count is always 0
        setCount(count + 1)
    }
    Text(text = "closure problem : $count")
}

@Composable
fun UseStateAndUseLatest() {
    val (count, setCount) = useState(0)
    val latestRef = useLatestRef(value = count)
    useInterval(optionsOf {
        initialDelay = 5.seconds
        period = 1.seconds
    }) {
        // 这里会出现闭包问题，count的值永远是0
        // There will be a closure problem here, the value of count is always 0
        setCount(latestRef.current + 1)
    }
    Text(text = "with useLatest : $count")
}
