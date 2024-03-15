package xyz.junerver.compose.hooks

import android.annotation.SuppressLint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.getSystemService

/**
 * Description:
 *
 * @author Junerver date: 2024/3/15-13:06 Email: junerver@gmail.com
 *     Version: v1.0
 */
@SuppressLint("ComposableNaming")
@Composable
fun useSensor(
    sensorType: Int,
    onAccuracyChangedFn: (Sensor, Int) -> Unit = { _, _ -> },
    onSensorChangedFn: (SensorEvent) -> Unit = { _ -> },
) {
    val ctx = LocalContext.current
    val sensorManagerRef = useCreation {
        ctx.getSystemService<SensorManager>()
    }
    DisposableEffect(Unit) {
        val sensor = sensorManagerRef.current?.getDefaultSensor(sensorType)
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
        sensorManagerRef.current?.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        onDispose {
            sensorManagerRef.current?.unregisterListener(listener)
        }
    }
}
