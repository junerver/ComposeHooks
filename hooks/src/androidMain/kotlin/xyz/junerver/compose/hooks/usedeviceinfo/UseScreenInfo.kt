package xyz.junerver.compose.hooks.usedeviceinfo

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt
import xyz.junerver.compose.hooks.useCreation

/*
  Description:
  Author: Junerver
  Date: 2024/7/1-9:51
  Email: junerver@gmail.com
  Version: v1.0
*/
@Composable
fun useScreenInfo(): ScreenInfo {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val containerSize = LocalWindowInfo.current.containerSize
    val screenWidthDp = configuration.screenWidthDp
    val screenHeightDp = configuration.screenHeightDp
    val densityValue = density.density
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    return useCreation(screenWidthDp, screenHeightDp, densityValue, isLandscape, containerSize) {
        createScreenInfo(
            screenWidthDp = screenWidthDp,
            screenHeightDp = screenHeightDp,
            density = densityValue,
            isLandscape = isLandscape,
            containerSize = containerSize,
        )
    }.current
}

internal fun createScreenInfo(
    screenWidthDp: Int,
    screenHeightDp: Int,
    density: Float,
    isLandscape: Boolean,
    containerSize: IntSize,
): ScreenInfo {
    val screenDp = IntSize(screenWidthDp, screenHeightDp)
    val screenPx = if (containerSize.width > 0 && containerSize.height > 0) {
        containerSize
    } else {
        val safeDensity = density.takeIf { it.isFinite() && it > 0f } ?: 1f
        IntSize(
            (screenWidthDp.coerceAtLeast(0) * safeDensity).roundToInt(),
            (screenHeightDp.coerceAtLeast(0) * safeDensity).roundToInt(),
        )
    }
    return ScreenInfo(
        screenDp = screenDp,
        screenPx = screenPx,
        density = density,
        isLandscape = isLandscape,
    )
}

@Stable
data class ScreenInfo(
    val screenDp: IntSize,
    val screenPx: IntSize,
    val density: Float,
    val isLandscape: Boolean,
)
