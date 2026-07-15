package xyz.junerver.compose.hooks

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import xyz.junerver.compose.hooks.usevibrate.VibrationController
import xyz.junerver.compose.hooks.usevibrate.VibrationDevice

/*
  Description: Android hardware control hook unit tests.
  Author: Junerver
  Date: 2026/7/15
  Email: junerver@gmail.com
  Version: v1.0
*/

class HardwareControlHooksTest {
    @Test
    fun missingHardwareIsSafeNoOp() {
        val torchController = TorchController(device = null)
        val vibrationController = VibrationController(device = null)

        assertFalse(torchController.turnOn())
        assertTrue(torchController.close())
        assertFalse(vibrationController.vibrate(100))
        assertTrue(vibrationController.close())
    }

    @Test
    fun torchControllerOwnsOnlySuccessfulEnable() {
        val device = FakeTorchDevice()
        val controller = TorchController(device)

        assertTrue(controller.turnOn())
        assertTrue(controller.turnOn())
        assertEquals(listOf(true), device.calls)

        assertTrue(controller.close())
        assertTrue(controller.close())
        assertEquals(listOf(true, false), device.calls)
    }

    @Test
    fun failedTorchEnableDoesNotTurnOffExternalTorch() {
        val device = FakeTorchDevice(failOnEnable = true)
        val controller = TorchController(device)

        assertFalse(controller.turnOn())
        assertTrue(controller.close())
        assertEquals(listOf(true), device.calls)
    }

    @Test
    fun failedTorchDisableCanBeRetried() {
        val device = FakeTorchDevice()
        val controller = TorchController(device)
        assertTrue(controller.turnOn())
        device.failOnDisable = true

        assertFalse(controller.turnOff())
        device.failOnDisable = false
        assertTrue(controller.turnOff())
        assertEquals(listOf(true, false, false), device.calls)
    }

    @Test
    fun vibrationControllerRejectsInvalidDurations() {
        val device = FakeVibrationDevice()
        val controller = VibrationController(device)

        assertFalse(controller.vibrate(0))
        assertFalse(controller.vibrate(-1))
        assertEquals(emptyList(), device.durations)
        assertTrue(controller.close())
        assertEquals(0, device.cancelCount)
    }

    @Test
    fun vibrationControllerCancelsOwnedVibrationOnce() {
        val device = FakeVibrationDevice()
        val controller = VibrationController(device)

        assertTrue(controller.vibrate(250))
        assertEquals(listOf(250L), device.durations)
        assertTrue(controller.close())
        assertTrue(controller.close())
        assertEquals(1, device.cancelCount)
    }

    @Test
    fun failedVibrationDoesNotClaimCancellation() {
        val device = FakeVibrationDevice(failOnVibrate = true)
        val controller = VibrationController(device)

        assertFalse(controller.vibrate(100))
        assertTrue(controller.close())
        assertEquals(0, device.cancelCount)
    }

    private class FakeTorchDevice(
        var failOnEnable: Boolean = false,
        var failOnDisable: Boolean = false,
    ) : TorchDevice {
        val calls = mutableListOf<Boolean>()

        override fun setEnabled(enabled: Boolean) {
            calls += enabled
            if (enabled && failOnEnable) error("enable failed")
            if (!enabled && failOnDisable) error("disable failed")
        }
    }

    private class FakeVibrationDevice(var failOnVibrate: Boolean = false) : VibrationDevice {
        val durations = mutableListOf<Long>()
        var cancelCount = 0

        override fun vibrate(milliseconds: Long) {
            if (failOnVibrate) error("vibrate failed")
            durations += milliseconds
        }

        override fun cancel() {
            cancelCount += 1
        }
    }
}
