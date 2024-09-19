package xyz.junerver.compose.hooks

import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    val context = LocalContext.current
    val cameraManager: CameraManager? = context.getSystemService()
    val cameraId = remember {
        cameraManager?.cameraIdList?.find {
            cameraManager.getCameraCharacteristics(it)
                .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }
    }
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
    return remember {
        Pair(
            { isFlashOn = true },
            { isFlashOn = false }
        )
    }
}
