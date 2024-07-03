package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import java.text.SimpleDateFormat
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import xyz.junerver.kotlin.tuple

/*
  Description:
  Author: Junerver
  Date: 2024/3/14-11:41
  Email: junerver@gmail.com
  Version: v1.0
*/

data class UseNowOptions(
    var interval: Duration = 1.seconds,
    var format: ((Long) -> String)? = null,
) {
    companion object : Options<UseNowOptions>(::UseNowOptions)
}

@Composable
fun useNow(options: UseNowOptions = defaultOption()): String {
    val (interval, format) = with(options) { tuple(interval, format) }
    val sdfRef = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss") }
    val (time) = useTimestamp(
        optionsOf {
            this.interval = interval
        }
    )
    val date by useState(time) {
        format?.run { invoke(time) } ?: sdfRef.format(time)
    }
    return date
}
