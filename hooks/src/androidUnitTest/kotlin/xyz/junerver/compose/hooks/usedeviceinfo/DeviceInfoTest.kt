package xyz.junerver.compose.hooks.usedeviceinfo

import androidx.compose.ui.unit.IntSize
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/*
  Description: Android device information hook unit tests.
  Author: Junerver
  Date: 2026/7/15
  Email: junerver@gmail.com
  Version: v1.0
*/

class DeviceInfoTest {
    @Test
    fun batteryPercentageUsesValidScale() {
        assertEquals(
            BatteryInfo(level = 57, isCharging = true),
            createBatteryInfo(level = 573, scale = 1_000, isCharging = true),
        )
    }

    @Test
    fun invalidBatteryScaleKeepsLevelUnknown() {
        assertEquals(
            BatteryInfo(level = -1, isCharging = true),
            createBatteryInfo(level = 42, scale = 0, isCharging = true),
        )
        assertEquals(
            BatteryInfo(level = -1, isCharging = false),
            createBatteryInfo(level = -1, scale = 100, isCharging = false),
        )
    }

    @Test
    fun batteryPercentageIsClampedToPublicRange() {
        assertEquals(100, createBatteryInfo(level = 150, scale = 100, isCharging = false).level)
        assertEquals(0, createBatteryInfo(level = 0, scale = 100, isCharging = false).level)
    }

    @Test
    fun missingBuildFieldsBecomeEmptyStrings() {
        assertEquals(
            BuildInfo(brand = "", model = "", release = ""),
            createBuildInfo(brand = null, model = null, release = null),
        )
    }

    @Test
    fun screenInfoUsesCurrentWindowPixels() {
        val info = createScreenInfo(
            screenWidthDp = 400,
            screenHeightDp = 700,
            density = 2.5f,
            isLandscape = true,
            containerSize = IntSize(1_080, 1_800),
        )

        assertEquals(IntSize(400, 700), info.screenDp)
        assertEquals(IntSize(1_080, 1_800), info.screenPx)
        assertEquals(2.5f, info.density)
        assertTrue(info.isLandscape)
    }

    @Test
    fun screenInfoFallsBackWhileWindowSizeIsUnavailable() {
        val info = createScreenInfo(
            screenWidthDp = 360,
            screenHeightDp = 640,
            density = 2.5f,
            isLandscape = false,
            containerSize = IntSize.Zero,
        )

        assertEquals(IntSize(900, 1_600), info.screenPx)
        assertFalse(info.isLandscape)
    }
}
