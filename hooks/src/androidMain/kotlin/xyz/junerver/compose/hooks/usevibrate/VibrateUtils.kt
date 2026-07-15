package xyz.junerver.compose.hooks.usevibrate

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.getSystemService

/*
 * @author 海盐芝士不加糖
 */

/**
 * 触发短震动
 */
fun Context.vibrateShort(milliseconds: Long = 100) {
    createVibrationController(applicationContext ?: this).vibrate(milliseconds)
}

/**
 * 触发长震动
 */
fun Context.vibrateLong(milliseconds: Long = 400) {
    createVibrationController(applicationContext ?: this).vibrate(milliseconds)
}

internal interface VibrationDevice {
    fun vibrate(milliseconds: Long)

    fun cancel()
}

internal class VibrationController(private val device: VibrationDevice?) {
    private val lock = Any()
    private var ownsVibration = false

    fun vibrate(milliseconds: Long): Boolean {
        if (milliseconds <= 0) return false
        return synchronized(lock) {
            val target = device ?: return@synchronized false
            try {
                target.vibrate(milliseconds)
                ownsVibration = true
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    fun close(): Boolean = synchronized(lock) {
        if (!ownsVibration) return@synchronized true
        val target = device ?: return@synchronized true
        try {
            target.cancel()
            ownsVibration = false
            true
        } catch (_: Exception) {
            false
        }
    }
}

internal fun createVibrationController(context: Context): VibrationController = VibrationController(context.vibrationDeviceOrNull())

private class AndroidVibrationDevice(private val vibrator: Vibrator) : VibrationDevice {
    override fun vibrate(milliseconds: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    milliseconds,
                    VibrationEffect.DEFAULT_AMPLITUDE,
                ),
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(milliseconds)
        }
    }

    override fun cancel() {
        vibrator.cancel()
    }
}

private fun Context.vibrationDeviceOrNull(): VibrationDevice? = try {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        getSystemService<VibratorManager>()?.defaultVibrator
    } else {
        getSystemService<Vibrator>()
    }
    vibrator?.takeIf { it.hasVibrator() }?.let(::AndroidVibrationDevice)
} catch (_: Exception) {
    null
}
