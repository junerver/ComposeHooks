package xyz.junerver.compose.hooks

import android.hardware.Sensor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State

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
 * This Hook keeps constant-space running statistics for the valid sensor samples.
 * It automatically calculates min, max, and average values without retaining sample history.
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
    val model = useIlluminanceModel(calibrate)
    useSensor(
        sensorType = Sensor.TYPE_LIGHT,
        onSensorChangedFn = { event ->
            event.values.firstOrNull()?.let(model.onRawValue)
        },
    )
    return model.holder
}

@Stable
internal data class IlluminanceModel(
    val holder: IlluminanceHolder,
    val onRawValue: (Float) -> Unit,
)

@Composable
internal fun useIlluminanceModel(calibrate: Float): IlluminanceModel {
    val illuminanceInfo = useState(IlluminanceInfo())
    val controller = useCreation(illuminanceInfo) { IlluminanceStateController(illuminanceInfo) }.current
    val calibrateRef = useLatestRef(calibrate)
    val onRawValue: (Float) -> Unit = useCreation<(Float) -> Unit>(controller, calibrateRef) {
        { rawValue: Float ->
            controller.onRawValue(rawValue, calibrateRef.current)
        }
    }.current
    val reset: ResetFn = useCreation(controller) {
        { controller.reset() }
    }.current
    val holder = useCreation(illuminanceInfo, reset) {
        IlluminanceHolder(
            state = illuminanceInfo,
            reset = reset,
        )
    }.current
    val model = useCreation(holder, onRawValue) {
        IlluminanceModel(holder, onRawValue)
    }.current

    useEffect(calibrate.toBits()) {
        controller.updateCalibration(calibrate)
    }

    return model
}

internal class IlluminanceStateController(
    private val state: MutableState<IlluminanceInfo>,
    private val accumulator: IlluminanceAccumulator = IlluminanceAccumulator(),
) {
    fun updateCalibration(calibrate: Float) {
        if (accumulator.updateCalibration(calibrate.toBits())) {
            state.value = IlluminanceInfo()
        }
    }

    fun onRawValue(rawValue: Float, calibrate: Float) {
        calibratedIlluminanceOrNull(rawValue, calibrate)?.let { value ->
            state.value = accumulator.add(value, calibrate.toBits())
        }
    }

    fun reset() {
        accumulator.reset()
        state.value = IlluminanceInfo()
    }
}

internal class IlluminanceAccumulator {
    var sampleCount: Long = 0
        private set

    private var calibrationBits: Int? = null
    private var minimum = Int.MAX_VALUE
    private var maximum = Int.MIN_VALUE
    private var mean = 0.0

    @Synchronized
    fun updateCalibration(newCalibrationBits: Int): Boolean {
        if (calibrationBits == newCalibrationBits) return false
        clearStatistics()
        calibrationBits = newCalibrationBits
        return true
    }

    @Synchronized
    fun add(value: Int, calibrationBits: Int): IlluminanceInfo {
        if (this.calibrationBits != calibrationBits) {
            clearStatistics()
            this.calibrationBits = calibrationBits
        }
        if (sampleCount < Long.MAX_VALUE) {
            sampleCount += 1
        }
        minimum = minOf(minimum, value)
        maximum = maxOf(maximum, value)
        mean += (value.toDouble() - mean) / sampleCount.toDouble()
        return IlluminanceInfo(
            now = value,
            min = minimum,
            max = maximum,
            avg = mean.toInt(),
        )
    }

    @Synchronized
    fun reset() {
        calibrationBits = null
        clearStatistics()
    }

    private fun clearStatistics() {
        sampleCount = 0
        minimum = Int.MAX_VALUE
        maximum = Int.MIN_VALUE
        mean = 0.0
    }
}

internal fun calibratedIlluminanceOrNull(value: Float, calibrate: Float): Int? {
    if (!value.isFinite() || value < 0f || !calibrate.isFinite() || calibrate < 0f) return null
    val calibrated = value * calibrate
    if (!calibrated.isFinite() || calibrated < 0f) return null
    return calibrated.toInt().coerceAtLeast(0)
}

/**
 * A holder class for illuminance state and control functions.
 *
 * This class provides access to the illuminance state and a function for resetting
 * the illuminance statistics.
 *
 * @property state The current illuminance state containing current, min, max, and average values
 * @property reset Function to reset the illuminance statistics
 */
@Stable
data class IlluminanceHolder(
    val state: State<IlluminanceInfo>,
    val reset: ResetFn,
)
