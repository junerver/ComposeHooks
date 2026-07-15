package xyz.junerver.compose.hooks

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.getSystemService

/*
  Description:
  Author: Junerver
  Date: 2024/7/2-14:10
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun useFlashlight(): Pair<TurnOnFn, TurnOffFn> {
    val localContext = LocalContext.current
    val context = localContext.applicationContext ?: localContext
    val cameraManager = useCreation(context) { context.cameraManagerOrNull() }.current
    val controller = useCreation(cameraManager) {
        TorchController(cameraManager?.findTorchDevice())
    }.current

    DisposableEffect(controller) {
        onDispose {
            controller.close()
        }
    }

    return useCreation(controller) {
        Pair<TurnOnFn, TurnOffFn>(
            first = {
                controller.turnOn()
                Unit
            },
            second = {
                controller.turnOff()
                Unit
            },
        )
    }.current
}

internal interface TorchDevice {
    fun setEnabled(enabled: Boolean)
}

internal class TorchController(private val device: TorchDevice?) {
    private val lock = Any()
    private var isOwned = false

    fun turnOn(): Boolean = synchronized(lock) {
        if (isOwned) return@synchronized true
        val target = device ?: return@synchronized false
        try {
            target.setEnabled(true)
            isOwned = true
            true
        } catch (_: Exception) {
            false
        }
    }

    fun turnOff(): Boolean = synchronized(lock) {
        if (!isOwned) return@synchronized true
        val target = device ?: return@synchronized true
        try {
            target.setEnabled(false)
            isOwned = false
            true
        } catch (_: Exception) {
            false
        }
    }

    fun close(): Boolean = turnOff()
}

private class AndroidTorchDevice(
    private val cameraManager: CameraManager,
    private val cameraId: String,
) : TorchDevice {
    override fun setEnabled(enabled: Boolean) {
        cameraManager.setTorchMode(cameraId, enabled)
    }
}

private fun Context.cameraManagerOrNull(): CameraManager? = try {
    getSystemService()
} catch (_: Exception) {
    null
}

private fun CameraManager.findTorchDevice(): TorchDevice? = try {
    cameraIdList.firstOrNull { cameraId ->
        getCameraCharacteristics(cameraId).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
    }?.let { cameraId -> AndroidTorchDevice(this, cameraId) }
} catch (_: Exception) {
    null
}
