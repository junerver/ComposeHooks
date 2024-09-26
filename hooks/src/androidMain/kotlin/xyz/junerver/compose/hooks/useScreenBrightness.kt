package xyz.junerver.compose.hooks

import android.app.Activity
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/*
  Description: Hook used to adjust screen brightness, returns a setting function,
  and passes a float value to control screen brightness.

  Author: Junerver
  Date: 2024/7/3-13:38
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun useScreenBrightness(): Pair<SetValueFn<Float>, Float> {
    val context = LocalContext.current
    val window = (context as Activity).window
    val initBrightness = useCreation {
        Settings.System.getInt(
            context.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS
        ).run { this / 255f }
    }.current

    fun setBrightness(brightness: Float) {
        window.attributes = window.attributes.apply {
            screenBrightness = if (brightness > 1 || brightness < 0) -1f else brightness
        }
    }
    useUnmount {
        setBrightness(initBrightness)
    }
    return Pair(
        ::setBrightness,
        initBrightness
    )
}
