package xyz.junerver.compose.hooks

import android.hardware.Sensor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember

/**
 * Data class representing illuminance information including current, minimum, maximum, and average values
 *
 * @property now Current illuminance value
 * @property min Minimum illuminance value recorded
 * @property max Maximum illuminance value recorded
 * @property avg Average illuminance value
 */
data class IlluminanceInfo(
    val now: Int = 0,
    val min: Int = 0,
    val max: Int = 0,
    val avg: Int = 0,
)

/**
 * A Hook for monitoring and tracking device illuminance (light sensor) data
 *
 * This Hook uses [useUndo] to maintain history of illuminance values and calculate statistics.
 * It automatically calculates min, max, and average values from the history.
 *
 * @param calibrate Calibration multiplier for sensor values (default = 1.0f)
 * @return [IlluminanceHolder] containing the state of current, min, max, and average illuminance values, along with a reset function
 *
 * Usage example:
 * ```
 * val illuminanceHolder = useIlluminance(calibrate = 1.5f)
 * Text("Current: ${illuminanceHolder.state.value.now} lux")
 * Text("Min: ${illuminanceHolder.state.value.min} lux")
 * Text("Max: ${illuminanceHolder.state.value.max} lux")
 * Text("Average: ${illuminanceHolder.state.value.avg} lux")
 * ```
 */
@Composable
fun useIlluminance(calibrate: Float = 1.0f): IlluminanceHolder {
    // Use useUndo to maintain the history of illuminance values, initial value is -1
    val (undoState, setValue, resetValue) = useUndo(-1)

    // Calculate statistics, filtering out the initial value -1
    val illuminanceInfo = useState(undoState) {
        val validValues = undoState.value.past.filter { it != -1 } + undoState.value.present
        val average = if (validValues.isNotEmpty()) validValues.average() else 0.0
        val max = validValues.maxOrNull() ?: 0
        val min = validValues.minOrNull() ?: 0
        IlluminanceInfo(
            now = if (undoState.value.present < 0) 0 else undoState.value.present,
            min = if (min < 0) 0 else min.toInt(),
            max = if (max < 0) 0 else max.toInt(),
            avg = if (average < 0) 0 else average.toInt()
        )
    }

    // Update the current value using sensor data
    useSensor(
        sensorType = Sensor.TYPE_LIGHT,
        onSensorChangedFn = { event ->
            val currentValue = event.values[0] * calibrate
            setValue(currentValue.toInt())
        }
    )

    // Define reset function to reset the value to -1
    val reset: ResetFn = {
        resetValue(-1)
    }

    // Return IlluminanceHolder containing the state and reset function
    return remember {
        IlluminanceHolder(
            state = illuminanceInfo,
            reset = reset
        )
    }
}

@Stable
/**
 * A holder class for illuminance state and control functions.
 *
 * This class provides access to the illuminance state and a function for resetting
 * the illuminance history.
 *
 * @property state The current illuminance state containing current, min, max, and average values
 * @property reset Function to reset the illuminance history
 */
data class IlluminanceHolder(
    val state: State<IlluminanceInfo>,
    val reset: ResetFn,
)
