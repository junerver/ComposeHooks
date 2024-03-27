package xyz.junerver.composehooks.example

import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import xyz.junerver.compose.hooks.useSensor
import xyz.junerver.compose.hooks.useState

/**
 * Description:
 *
 * @author Junerver date: 2024/3/15-13:17 Email: junerver@gmail.com
 *     Version: v1.0
 */
@Composable
fun UseSensorExample() {
    var sensorData by useState(default = arrayOf(0f, 0f, 0f))
    useSensor(sensorType = Sensor.TYPE_ROTATION_VECTOR, onSensorChangedFn = {
        val rotationMatrix = FloatArray(9)
        val rotationVector = FloatArray(3)
        System.arraycopy(it.values, 0, rotationVector, 0, 3)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector)
        val orientationAngles = FloatArray(3)
        SensorManager.getOrientation(rotationMatrix, orientationAngles)
        sensorData = orientationAngles.map { r -> Math.toDegrees(r.toDouble()).toFloat() }.toTypedArray()
    })
    Surface {
        Column {
            Text(text = "azimuth：${normalizeAzimuth(sensorData[0])}")
            Text(text = "pitch：${sensorData[1]}")
            Text(text = "roll：${sensorData[2]}")
        }
    }
}

fun normalizeAzimuth(azimuth: Float): Float {
    var normalizedAzimuth = azimuth
    if (normalizedAzimuth < 0) {
        normalizedAzimuth += 360f
    }
    return normalizedAzimuth % 360f
}
