package xyz.junerver.compose.hooks

import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.getSystemService
import xyz.junerver.kotlin.Tuple2

/*
  Description:
  Author: Junerver
  Date: 2024/7/2-14:10
  Email: junerver@gmail.com
  Version: v1.0
*/

internal typealias TurnOnFn = () -> Unit
internal typealias TurnOffFn = () -> Unit

@Composable
fun useFlashlight(): Tuple2<TurnOnFn, TurnOffFn> {
    val context = LocalContext.current
    val cameraManager: CameraManager? = context.getSystemService()
    val cameraId = useCreation(cameraManager) {
        cameraManager?.cameraIdList?.find {
            cameraManager.getCameraCharacteristics(it)
                .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }
    }.current
    var isFlashOn by useState(default = false)
    DisposableEffect(isFlashOn) {
        cameraId?.let {
            cameraManager?.setTorchMode(cameraId, isFlashOn)
        }

        onDispose {
            if (isFlashOn && cameraId != null) {
                cameraManager?.setTorchMode(cameraId, false)
            }
        }
    }
    return Tuple2(
        { isFlashOn = true },
        { isFlashOn = false }
    )
}
