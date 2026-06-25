package xyz.junerver.compose.hooks.usevibrate

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/*
  Description:
  Author: Junerver
  Date: 2024/4/19-16:36
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun useVibrate(short: Long = 100, long: Long = 400): Pair<() -> Unit, () -> Unit> {
    val ctx = LocalContext.current
    return Pair(
        first = { ctx.vibrateShort(short) },
        second = { ctx.vibrateLong(long) },
    )
}
