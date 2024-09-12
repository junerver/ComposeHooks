package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.seconds
import xyz.junerver.compose.hooks.optionsOf
import xyz.junerver.compose.hooks.useInterval
import xyz.junerver.compose.hooks.useLatestRef
import xyz.junerver.compose.hooks.useState

/*
  Description:
  Author: Junerver
  Date: 2024/3/8-13:38
  Email: junerver@gmail.com
  Version: v1.0
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
    useInterval(
        optionsOf {
            initialDelay = 2.seconds
            period = 1.seconds
        },
        true
    ) {
        count += 1
    }
    Text(text = "Normal : $count")
}

@Composable
fun UseStateButWithoutUseLatest() {
    val (count, setCount) = useState(0)
    useInterval(
        optionsOf {
            initialDelay = 2.seconds
            period = 1.seconds
        },
        true
    ) {
        setCount(count + 1)
    }
    Text(text = "closure problem : $count")
}

@Composable
fun UseStateAndUseLatest() {
    val (count, setCount) = useState(0)
    val latestRef = useLatestRef(value = count)
    useInterval(
        optionsOf {
            initialDelay = 2.seconds
            period = 1.seconds
        },
        true
    ) {
        setCount(latestRef.current + 1)
    }
    Text(text = "with useLatest : $count")
}
