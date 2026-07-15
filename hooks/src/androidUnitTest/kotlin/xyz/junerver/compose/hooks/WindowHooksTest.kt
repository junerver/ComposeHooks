package xyz.junerver.compose.hooks

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/*
  Description: Android window ownership hook unit tests.
  Author: Junerver
  Date: 2026/7/15
  Email: junerver@gmail.com
  Version: v1.0
*/

class WindowHooksTest {
    @Test
    fun windowFlagReleaseRestoresPreexistingBits() {
        val coordinator = WindowFlagCoordinator()
        val target = FakeWindowFlagTarget(initialFlags = 0b1010)

        assertTrue(coordinator.acquire(target, 0b0011))
        assertEquals(0b1011, target.flags)

        assertTrue(coordinator.release(target, 0b0011))
        assertEquals(0b1010, target.flags)
    }

    @Test
    fun overlappingWindowFlagOwnersReleaseOnlyTheirBits() {
        val coordinator = WindowFlagCoordinator()
        val target = FakeWindowFlagTarget()

        assertTrue(coordinator.acquire(target, 0b0011))
        assertTrue(coordinator.acquire(target, 0b0010))
        assertEquals(0b0011, target.flags)

        assertTrue(coordinator.release(target, 0b0011))
        assertEquals(0b0010, target.flags)

        assertTrue(coordinator.release(target, 0b0010))
        assertEquals(0, target.flags)
    }

    @Test
    fun failedWindowFlagAcquireDoesNotClaimOwnership() {
        val coordinator = WindowFlagCoordinator()
        val target = FakeWindowFlagTarget(failOnAdd = true)

        assertFalse(coordinator.acquire(target, 0b0001))
        assertEquals(0, target.flags)

        target.failOnAdd = false
        assertTrue(coordinator.acquire(target, 0b0001))
        assertEquals(0b0001, target.flags)
        assertTrue(coordinator.release(target, 0b0001))
        assertEquals(0, target.flags)
    }

    @Test
    fun zeroWindowFlagsNeverClaimOwnership() {
        val coordinator = WindowFlagCoordinator()
        val target = FakeWindowFlagTarget()

        assertFalse(coordinator.acquire(target, 0))
        assertFalse(coordinator.release(target, 0))
        assertEquals(0, target.flags)
    }

    @Test
    fun screenBrightnessControllerRestoresExactWindowValue() {
        val target = FakeScreenBrightnessTarget(initialBrightness = -1f)
        val controller = createScreenBrightnessController(target)!!

        assertTrue(controller.set(0.65f))
        assertEquals(0.65f, target.brightness)
        assertTrue(controller.set(Float.NaN))
        assertEquals(-1f, target.brightness)

        assertTrue(controller.restore())
        assertEquals(-1f, target.brightness)
    }

    @Test
    fun effectiveBrightnessPrefersValidWindowOverride() {
        assertEquals(0.8f, resolveInitialScreenBrightness(windowBrightness = 0.8f, systemBrightness = 0.4f))
        assertEquals(0.4f, resolveInitialScreenBrightness(windowBrightness = -1f, systemBrightness = 0.4f))
        assertEquals(1f, normalizeSystemBrightness(400))
        assertEquals(0f, normalizeSystemBrightness(-20))
    }

    private class FakeWindowFlagTarget(
        initialFlags: Int = 0,
        var failOnAdd: Boolean = false,
    ) : WindowFlagTarget {
        var flags: Int = initialFlags

        override fun currentFlags(): Int = flags

        override fun addFlags(flags: Int) {
            if (failOnAdd) error("add failed")
            this.flags = this.flags or flags
        }

        override fun clearFlags(flags: Int) {
            this.flags = this.flags and flags.inv()
        }
    }

    private class FakeScreenBrightnessTarget(initialBrightness: Float) : ScreenBrightnessTarget {
        override var brightness: Float = initialBrightness
    }
}
