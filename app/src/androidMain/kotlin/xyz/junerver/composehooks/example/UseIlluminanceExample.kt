package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import xyz.junerver.compose.hooks.useIlluminance
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description:
  Author: Junerver
  Date: 2024/4/19-16:41
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Example demonstrating the useIlluminance hook
 *
 * This composable shows how to use the useIlluminance hook to monitor
 * ambient light levels from the device's light sensor and display
 * current, minimum, maximum, and average illuminance values.
 */
@Composable
fun UseIlluminanceExample() {
    // Initialize the illuminance hook to track light sensor data
    val (state, reset) = useIlluminance()

    Surface {
        Column {
            // Display the current illuminance value
            Text("now: ${state.value.now}")
            // Display the minimum recorded illuminance
            Text("min: ${state.value.min}")
            // Display the maximum recorded illuminance
            Text("max: ${state.value.max}")
            // Display the average illuminance
            Text("avg: ${state.value.avg}")

            // Button to reset the illuminance history
            TButton(text = "reset") {
                reset()
            }
        }
    }
}
