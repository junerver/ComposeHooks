package xyz.junerver.compose.hooks.usedeviceinfo

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize

/*
  Description:
  Author: Junerver
  Date: 2024/7/1-9:51
  Email: junerver@gmail.com
  Version: v1.0
*/
@Composable
fun useScreenInfo(): ScreenInfo {
    val context = LocalContext.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val displayMetrics = context.resources.displayMetrics
    return ScreenInfo(
        IntSize(configuration.screenWidthDp, configuration.screenHeightDp),
        IntSize(displayMetrics.widthPixels, displayMetrics.heightPixels),
        density.density,
        configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    )
}

data class ScreenInfo(
    val screenDp: IntSize,
    val screenPx: IntSize,
    val density: Float,
    val isLandscape: Boolean,
)
