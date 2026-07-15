package xyz.junerver.compose.hooks

import android.content.Context
import android.provider.Settings
import android.view.Window
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
    val localContext = LocalContext.current
    val context = localContext.applicationContext ?: localContext
    val window = LocalActivity.current?.window
    val target = useCreation(window) { window?.let(::AndroidScreenBrightnessTarget) }.current
    val controller = useCreation(target) { target?.let(::createScreenBrightnessController) }.current
    val systemBrightness = useCreation(context) { readSystemBrightness(context) }.current
    val initialBrightness = resolveInitialScreenBrightness(
        windowBrightness = controller?.originalBrightness ?: -1f,
        systemBrightness = systemBrightness,
    )

    DisposableEffect(controller) {
        onDispose {
            controller?.restore()
        }
    }

    val setBrightness = useCreation(controller) {
        { brightness: Float ->
            controller?.set(brightness)
            Unit
        }
    }.current
    return useCreation(setBrightness, initialBrightness) {
        Pair(setBrightness, initialBrightness)
    }.current
}

internal fun normalizeScreenBrightness(brightness: Float): Float =
    brightness.takeIf { it.isFinite() && it in 0f..1f } ?: -1f

internal fun normalizeSystemBrightness(brightness: Int): Float = brightness.coerceIn(0, 255) / 255f

internal fun resolveInitialScreenBrightness(windowBrightness: Float, systemBrightness: Float): Float =
    windowBrightness.takeIf { it.isFinite() && it in 0f..1f } ?: normalizeScreenBrightness(systemBrightness)

internal interface ScreenBrightnessTarget {
    var brightness: Float
}

internal class ScreenBrightnessController private constructor(
    private val target: ScreenBrightnessTarget,
    val originalBrightness: Float,
) {
    fun set(brightness: Float): Boolean = try {
        target.brightness = normalizeScreenBrightness(brightness)
        true
    } catch (_: Exception) {
        false
    }

    fun restore(): Boolean = try {
        target.brightness = originalBrightness
        true
    } catch (_: Exception) {
        false
    }

    companion object {
        fun create(target: ScreenBrightnessTarget): ScreenBrightnessController? = try {
            ScreenBrightnessController(target, target.brightness)
        } catch (_: Exception) {
            null
        }
    }
}

internal fun createScreenBrightnessController(target: ScreenBrightnessTarget): ScreenBrightnessController? =
    ScreenBrightnessController.create(target)

private class AndroidScreenBrightnessTarget(private val window: Window) : ScreenBrightnessTarget {
    override var brightness: Float
        get() = window.attributes.screenBrightness
        set(value) {
            window.attributes = window.attributes.apply { screenBrightness = value }
        }
}

private fun readSystemBrightness(context: Context): Float = try {
    normalizeSystemBrightness(
        Settings.System.getInt(
            context.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS,
            255,
        ),
    )
} catch (_: SecurityException) {
    1f
}
