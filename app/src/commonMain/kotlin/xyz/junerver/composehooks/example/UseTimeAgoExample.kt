package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import kotlin.math.pow
import kotlin.time.Duration.Companion.milliseconds
import xyz.junerver.compose.hooks.DefaultEnglishTimeAgoMessages
import xyz.junerver.compose.hooks.useControllable
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.useTimeAgo
import xyz.junerver.composehooks.utils.now

/*
  Description:
  Author: Junerver
  Date: 2025/6/24-16:21
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun UseTimeAgoExample() {
    val (sliderPosition, setSliderPosition) = useControllable(0f)
    val time by useState {
        sliderPosition.value.pow(3).toLong()
    }
    val fromInstant by useState {
        now() + time.milliseconds
    }
    val timeAgo by useTimeAgo(fromInstant) {
        messages = DefaultEnglishTimeAgoMessages
    }
    Surface {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = timeAgo)
            Slider(
                value = sliderPosition.value,
                onValueChange = setSliderPosition,
                valueRange = -3800f..3800f,
            )
            Text(text = "$time ms")
        }
    }
}
