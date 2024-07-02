package xyz.junerver.compose.hooks.usevibrate

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import xyz.junerver.kotlin.Tuple2

/*
  Description:
  Author: Junerver
  Date: 2024/4/19-16:36
  Email: junerver@gmail.com
  Version: v1.0
*/

@SuppressLint("ComposableNaming")
@Composable
fun useVibrate(): Tuple2<() -> Unit, () -> Unit> {
    val ctx = LocalContext.current
    return Tuple2(
        first = { ctx.vibrateShort() },
        second = { ctx.vibrateLong() }
    )
}
