package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import kotlin.time.Duration.Companion.seconds
import xyz.junerver.compose.hooks.useCountdown
import xyz.junerver.compose.hooks.useState

/*
  Description:
  Author: Junerver
  Date: 2024/7/8-15:11
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun UseCountdownExample() {
    var show by useState(default = false)
    val (leftTime, formattedRes) = useCountdown(
        optionsOf = {
            leftTime = 10.seconds
//            targetDate = Clock.System.now() + 10.seconds
//            interval = 3.seconds
            onEnd = {
                show = true
            }
        }
    )
    Surface {
        Column {
            Text(text = "LeftTime: ${leftTime.inWholeSeconds}")
            Text(text = formattedRes.toString())
            if (show) {
                Text(text = "countdown on end!!", color = Color.Green)
            }
        }
    }
}
