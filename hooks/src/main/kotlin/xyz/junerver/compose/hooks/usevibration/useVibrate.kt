package xyz.junerver.compose.hooks.usevibration

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import xyz.junerver.kotlin.Tuple2

/**
 * Description:
 * @author Junerver
 * date: 2024/4/19-16:36
 * Email: junerver@gmail.com
 * Version: v1.0
 */
@SuppressLint("ComposableNaming")
@Composable
fun useVibrate(): Tuple2<() -> Unit, () -> Unit> {
    val ctx = LocalContext.current
    return Tuple2(
        first = { ctx.vibrateShort() },
        second = { ctx.vibrateLong() },
    )
}
