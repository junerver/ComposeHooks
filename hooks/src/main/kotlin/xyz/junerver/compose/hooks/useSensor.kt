package xyz.junerver.compose.hooks

import android.annotation.SuppressLint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.getSystemService

@SuppressLint("ComposableNaming")
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
