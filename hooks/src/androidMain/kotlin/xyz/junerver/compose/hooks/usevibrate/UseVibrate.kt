package xyz.junerver.compose.hooks.usevibrate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import xyz.junerver.compose.hooks.useCreation

/*
  Description:
  Author: Junerver
  Date: 2024/4/19-16:36
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun useVibrate(short: Long = 100, long: Long = 400): Pair<() -> Unit, () -> Unit> {
    val localContext = LocalContext.current
    val context = localContext.applicationContext ?: localContext
    val controller = useCreation(context) { createVibrationController(context) }.current

    DisposableEffect(controller) {
        onDispose {
            controller.close()
        }
    }

    return useCreation(controller, short, long) {
        Pair(
            first = {
                controller.vibrate(short)
                Unit
            },
            second = {
                controller.vibrate(long)
                Unit
            },
        )
    }.current
}
