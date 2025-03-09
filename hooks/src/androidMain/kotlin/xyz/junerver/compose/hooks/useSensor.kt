package xyz.junerver.compose.hooks

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.getSystemService

/**
 * A Hook for monitoring device sensor data in Jetpack Compose
 *
 * This Hook provides a simple way to listen to various sensor data on Android devices.
 * It automatically registers the sensor listener when the component mounts and
 * unregisters it when the component unmounts to prevent memory leaks.
 *
 * @param sensorType The type of sensor to monitor, using constants defined in [Sensor] class,
 *                   e.g., [Sensor.TYPE_ACCELEROMETER]
 * @param onAccuracyChangedFn Callback function triggered when sensor accuracy changes,
 *                            with parameters for the sensor object and new accuracy value
 * @param onSensorChangedFn Callback function triggered when sensor data changes,
 *                         with parameter containing sensor data in [SensorEvent] object
 *
 * Usage example:
 * ```
 * useSensor(
 *     sensorType = Sensor.TYPE_ACCELEROMETER,
 *     onSensorChangedFn = { event ->
 *         val (x, y, z) = event.values
 *         // Process accelerometer data
 *     }
 * )
 * ```
 */
@Composable
fun useSensor(
    sensorType: Int,
    onAccuracyChangedFn: (Sensor, Int) -> Unit = { _, _ -> },
    onSensorChangedFn: (SensorEvent) -> Unit = { _ -> },
) {
    val ctx = LocalContext.current
    val sensorManager = remember {
        ctx.getSystemService<SensorManager>()
    }
    DisposableEffect(Unit) {
        val sensor = sensorManager?.getDefaultSensor(sensorType)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(p0: SensorEvent?) {
                p0?.let { onSensorChangedFn(it) }
            }

            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
                if (p0 != null) {
                    onAccuracyChangedFn(p0, p1)
                }
            }
        }
        sensorManager?.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }
}
