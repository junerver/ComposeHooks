package xyz.junerver.compose.hooks

import android.hardware.SensorEventListener
import androidx.compose.runtime.mutableStateOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue
import xyz.junerver.compose.hooks.useref.MutableRef

/*
  Description: Android sensor and illuminance hook behavior tests.
  Author: Junerver
  Date: 2026/7/17
  Email: junerver@gmail.com
  Version: v1.0
*/

class SensorIlluminanceHooksTest {
    @Test
    fun sensorCallbackDispatcherUsesLatestCallbacksAndIgnoresNulls() {
        val calls = mutableListOf<String>()
        val accuracyRef = MutableRef<(String, Int) -> Unit> { sensor, accuracy ->
            calls += "first-accuracy:$sensor:$accuracy"
        }
        val eventRef = MutableRef<(String) -> Unit> { event ->
            calls += "first-event:$event"
        }
        val dispatcher = SensorCallbackDispatcher(accuracyRef, eventRef)

        dispatcher.onAccuracyChanged(null, 1)
        dispatcher.onSensorChanged(null)
        dispatcher.onAccuracyChanged("sensor", 2)
        dispatcher.onSensorChanged("event")

        accuracyRef.current = { sensor, accuracy -> calls += "latest-accuracy:$sensor:$accuracy" }
        eventRef.current = { event -> calls += "latest-event:$event" }
        dispatcher.onAccuracyChanged("sensor", 3)
        dispatcher.onSensorChanged("event")

        assertEquals(
            listOf(
                "first-accuracy:sensor:2",
                "first-event:event",
                "latest-accuracy:sensor:3",
                "latest-event:event",
            ),
            calls,
        )
    }

    @Test
    fun sensorControllerContainsLookupFailuresAndRetriesFailedUnregister() {
        val registrar = FakeSensorRegistrar(throwOnGet = true)
        val controller = sensorController(registrar, sensorType = 7)

        assertFalse(controller.start())

        registrar.throwOnGet = false
        assertTrue(controller.start())
        assertEquals(1, registrar.activeListeners.size)

        registrar.throwOnUnregister = true
        assertFalse(controller.stop())
        assertEquals(1, registrar.activeListeners.size)

        registrar.throwOnUnregister = false
        assertTrue(controller.stop())
        assertTrue(registrar.activeListeners.isEmpty())
        assertEquals(2, registrar.unregisterAttempts.size)
    }

    @Test
    fun sensorControllerIsSafeForMissingHardwareAndRegistrationFailures() {
        val missing = FakeSensorRegistrar(handle = null)
        assertFalse(sensorController(missing, sensorType = 1).start())
        assertTrue(missing.registerAttempts.isEmpty())

        val rejected = FakeSensorRegistrar(registerResult = false)
        val rejectedController = sensorController(rejected, sensorType = 2)
        assertFalse(rejectedController.start())
        assertTrue(rejectedController.stop())
        assertTrue(rejected.activeListeners.isEmpty())

        val throwing = FakeSensorRegistrar(throwOnRegister = true)
        assertFalse(sensorController(throwing, sensorType = 3).start())
        assertTrue(throwing.activeListeners.isEmpty())
    }

    @Test
    fun sensorControllersReleaseOldTypeBeforeRegisteringNewType() {
        val registrar = FakeSensorRegistrar()
        val first = sensorController(registrar, sensorType = 1)
        val second = sensorController(registrar, sensorType = 2)

        assertTrue(first.start())
        assertTrue(first.start())
        assertTrue(first.stop())
        assertTrue(first.stop())
        assertTrue(second.start())

        assertEquals(listOf(1, 2), registrar.sensorRequests)
        assertEquals(2, registrar.registerAttempts.size)
        assertEquals(1, registrar.unregisterAttempts.size)
        assertEquals(1, registrar.activeListeners.size)

        assertTrue(second.stop())
        assertTrue(registrar.activeListeners.isEmpty())
    }

    @Test
    fun illuminanceAccumulatorCountsDuplicateSamplesAndResetsCalibration() {
        val accumulator = IlluminanceAccumulator()
        val calibration = 1f.toBits()

        accumulator.add(value = 10, calibrationBits = calibration)
        accumulator.add(value = 10, calibrationBits = calibration)
        val info = accumulator.add(value = 20, calibrationBits = calibration)

        assertEquals(IlluminanceInfo(now = 20, min = 10, max = 20, avg = 13), info)
        assertEquals(3L, accumulator.sampleCount)
        assertFalse(accumulator.updateCalibration(calibration))
        assertTrue(accumulator.updateCalibration(2f.toBits()))
        assertEquals(0L, accumulator.sampleCount)
    }

    @Test
    fun calibratedIlluminanceIgnoresInvalidReadingsInsteadOfWritingZero() {
        assertEquals(24, calibratedIlluminanceOrNull(value = 12.4f, calibrate = 2f))
        assertEquals(0, calibratedIlluminanceOrNull(value = 0f, calibrate = 2f))
        assertEquals(null, calibratedIlluminanceOrNull(value = -1f, calibrate = 1f))
        assertEquals(null, calibratedIlluminanceOrNull(value = Float.NaN, calibrate = 1f))
        assertEquals(null, calibratedIlluminanceOrNull(value = Float.POSITIVE_INFINITY, calibrate = 1f))
        assertEquals(null, calibratedIlluminanceOrNull(value = 1f, calibrate = -1f))
        assertEquals(null, calibratedIlluminanceOrNull(value = 1f, calibrate = Float.NaN))
    }

    @Test
    fun illuminanceStateControllerResetsWhenCalibrationChanges() {
        val state = mutableStateOf(IlluminanceInfo())
        val controller = IlluminanceStateController(state)

        controller.updateCalibration(1f)
        controller.onRawValue(10f, 1f)
        controller.onRawValue(10f, 1f)
        controller.onRawValue(20f, 1f)
        assertEquals(IlluminanceInfo(now = 20, min = 10, max = 20, avg = 13), state.value)

        controller.onRawValue(Float.NaN, 1f)
        assertEquals(IlluminanceInfo(now = 20, min = 10, max = 20, avg = 13), state.value)

        controller.updateCalibration(2f)
        assertEquals(IlluminanceInfo(), state.value)

        controller.onRawValue(5f, 2f)
        assertEquals(IlluminanceInfo(now = 10, min = 10, max = 10, avg = 10), state.value)

        controller.reset()
        assertEquals(IlluminanceInfo(), state.value)
    }

    private fun sensorController(registrar: SensorRegistrar, sensorType: Int): SensorSubscriptionController =
        SensorSubscriptionController(
            registrar = registrar,
            sensorType = sensorType,
            onAccuracyChangedRef = MutableRef({ _, _ -> }),
            onSensorChangedRef = MutableRef({ _ -> }),
        )

    private class FakeSensorRegistrar(
        var registerResult: Boolean = true,
        var throwOnGet: Boolean = false,
        var throwOnRegister: Boolean = false,
        var throwOnUnregister: Boolean = false,
        var handle: SensorHandle? = FakeSensorHandle,
    ) : SensorRegistrar {
        val sensorRequests = mutableListOf<Int>()
        val registerAttempts = mutableListOf<SensorEventListener>()
        val unregisterAttempts = mutableListOf<SensorEventListener>()
        val activeListeners = mutableSetOf<SensorEventListener>()

        override fun getDefaultSensor(sensorType: Int): SensorHandle? {
            if (throwOnGet) error("getDefaultSensor failed")
            sensorRequests += sensorType
            return handle
        }

        override fun registerListener(listener: SensorEventListener, sensor: SensorHandle, delay: Int): Boolean {
            if (throwOnRegister) error("registerListener failed")
            assertSame(FakeSensorHandle, sensor)
            assertEquals(android.hardware.SensorManager.SENSOR_DELAY_UI, delay)
            registerAttempts += listener
            if (registerResult) activeListeners += listener
            return registerResult
        }

        override fun unregisterListener(listener: SensorEventListener) {
            unregisterAttempts += listener
            if (throwOnUnregister) error("unregisterListener failed")
            activeListeners -= listener
        }
    }

    private data object FakeSensorHandle : SensorHandle
}
