package xyz.junerver.compose.hooks.usevibrate

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.getSystemService

/**
 * @author 海盐芝士不加糖
 */

/**
 * 触发短震动
 */
fun Context.vibrateShort() {
    vibrate(50)
}

/**
 * 触发长震动
 */
fun Context.vibrateLong() {
    vibrate(400)
}

private fun Context.vibrate(milliseconds: Long) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibrator: VibratorManager? = getSystemService()
        vibrator?.defaultVibrator?.vibrate(
            VibrationEffect.createOneShot(
                milliseconds,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
        )
    } else {
        val vibrator: Vibrator? = getSystemService()
        @Suppress("DEPRECATION")
        vibrator?.vibrate(milliseconds)
    }
}
