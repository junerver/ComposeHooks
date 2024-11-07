package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.seconds
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
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
    Surface {
        Column {
            SimpleOne()
            HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 20.dp))
            SimpleTwo()
        }
    }
}

@Composable
private fun SimpleOne() {
    var show by useState(default = false)
    val (leftTime, formattedRes) = useCountdown(
        optionsOf = {
            leftTime = 10.seconds
            onEnd = {
                show = true
            }
        }
    )
    Column {
        Text(text = "LeftTime: ${leftTime.value.inWholeSeconds}")
        Text(text = formattedRes.value.toString())
        if (show) {
            Text(text = "countdown on end!!", color = Color.Green)
        }
    }
}

@Composable
private fun SimpleTwo() {
    val (leftTime, formattedRes) = useCountdown(
        optionsOf = {
            targetDate = LocalDateTime.parse("2025-01-29T00:00:00").toInstant(TimeZone.of("UTC+8"))
            interval = 1.seconds
        }
    )
    val formated by formattedRes
    Column {
        Text(text = "There are ${leftTime.value.inWholeDays} days left until Chinese New Year")
        Text(
            text =
                """
                There are ${formated.days} days ${formated.hours} hours ${formated.minutes} minutes ${formated.seconds} seconds ${formated.milliseconds} milliseconds until Chinese New Year
                """.trimIndent()
        )
    }
}
