package xyz.junerver.compose.hooks

import android.app.Activity
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import xyz.junerver.kotlin.Tuple3

/*
  Description: Disable screenshots for privacy page
  Author: Junerver
  Date: 2024/7/1-16:43
  Email: junerver@gmail.com
  Version: v1.0
*/
internal typealias DisableFn = () -> Unit
internal typealias EnableFn = () -> Unit
internal typealias IsDisabled = Boolean

@Composable
fun useDisableScreenshot(): Tuple3<DisableFn, EnableFn, IsDisabled> {
    val (isDisabled, set) = usePersistent(
        key = "HOOK_INTERNAL_SCREENSHOT",
        defaultValue = false,
        forceUseMemory = true
    )
    val context = LocalContext.current
    val window = (context as Activity).window
    fun disable() {
        set(true)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    fun enable() {
        set(false)
        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    return Tuple3(
        ::disable,
        ::enable,
        isDisabled
    )
}
