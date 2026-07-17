package xyz.junerver.compose.hooks

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.getSystemService
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import xyz.junerver.compose.hooks.useref.Ref

/**
 * A Hook for monitoring device sensor data in Jetpack Compose
 *
 * This Hook provides a simple way to listen to various sensor data on Android devices.
 * It automatically registers the sensor listener while the lifecycle is resumed and
 * unregisters it when paused or unmounted to prevent background sampling and memory leaks.
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
    val localContext = LocalContext.current
    val context = localContext.applicationContext ?: localContext
    val registrar = useCreation(context) { context.sensorRegistrarOrNull() }.current
    useSensorSubscription(
        registrar = registrar,
        sensorType = sensorType,
        onAccuracyChangedFn = onAccuracyChangedFn,
        onSensorChangedFn = onSensorChangedFn,
    )
}

@Composable
internal fun useSensorSubscription(
    registrar: SensorRegistrar?,
    sensorType: Int,
    onAccuracyChangedFn: (Sensor, Int) -> Unit,
    onSensorChangedFn: (SensorEvent) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val onAccuracyChangedRef = useLatestRef(onAccuracyChangedFn)
    val onSensorChangedRef = useLatestRef(onSensorChangedFn)
    val subscription = useCreation(registrar, sensorType) {
        SensorSubscriptionController(
            registrar = registrar,
            sensorType = sensorType,
            onAccuracyChangedRef = onAccuracyChangedRef,
            onSensorChangedRef = onSensorChangedRef,
        )
    }.current

    LifecycleResumeEffect(subscription, lifecycleOwner = lifecycleOwner) {
        subscription.start()
        onPauseOrDispose {
            subscription.stop()
        }
    }
}

internal interface SensorRegistrar {
    fun getDefaultSensor(sensorType: Int): SensorHandle?

    fun registerListener(listener: SensorEventListener, sensor: SensorHandle, delay: Int): Boolean

    fun unregisterListener(listener: SensorEventListener)
}

internal interface SensorHandle

internal class SensorCallbackDispatcher<S : Any, E : Any>(
    private val onAccuracyChangedRef: Ref<(S, Int) -> Unit>,
    private val onSensorChangedRef: Ref<(E) -> Unit>,
) {
    fun onSensorChanged(event: E?) {
        if (event != null) {
            onSensorChangedRef.current(event)
        }
    }

    fun onAccuracyChanged(sensor: S?, accuracy: Int) {
        if (sensor != null) {
            onAccuracyChangedRef.current(sensor, accuracy)
        }
    }
}

internal class SensorSubscriptionController(
    private val registrar: SensorRegistrar?,
    private val sensorType: Int,
    private val onAccuracyChangedRef: Ref<(Sensor, Int) -> Unit>,
    private val onSensorChangedRef: Ref<(SensorEvent) -> Unit>,
) {
    private val callbacks = SensorCallbackDispatcher(onAccuracyChangedRef, onSensorChangedRef)
    private var listener: SensorEventListener? = null
    private var registered = false

    fun start(): Boolean {
        if (registered) return true
        val target = registrar ?: return false
        return try {
            val sensor = target.getDefaultSensor(sensorType) ?: return false
            val newListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    callbacks.onSensorChanged(event)
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    callbacks.onAccuracyChanged(sensor, accuracy)
                }
            }
            if (target.registerListener(newListener, sensor, SensorManager.SENSOR_DELAY_UI)) {
                listener = newListener
                registered = true
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    fun stop(): Boolean {
        val currentListener = listener ?: return true
        return try {
            registrar?.unregisterListener(currentListener)
            listener = null
            registered = false
            true
        } catch (_: Exception) {
            false
        }
    }
}

private class AndroidSensorRegistrar(private val sensorManager: SensorManager) : SensorRegistrar {
    override fun getDefaultSensor(sensorType: Int): SensorHandle? = sensorManager.getDefaultSensor(sensorType)?.let(::AndroidSensorHandle)

    override fun registerListener(listener: SensorEventListener, sensor: SensorHandle, delay: Int): Boolean =
        sensorManager.registerListener(listener, (sensor as AndroidSensorHandle).sensor, delay)

    override fun unregisterListener(listener: SensorEventListener) {
        sensorManager.unregisterListener(listener)
    }
}

private class AndroidSensorHandle(val sensor: Sensor) : SensorHandle

private fun Context.sensorRegistrarOrNull(): SensorRegistrar? = try {
    getSystemService<SensorManager>()?.let(::AndroidSensorRegistrar)
} catch (_: Exception) {
    null
}
