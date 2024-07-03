package xyz.junerver.compose.hooks

import android.view.WindowManager
import androidx.compose.runtime.Composable
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
fun useDisableScreenshot(): Tuple3<DisableFn, EnableFn, IsDisabled> = useWindowFlags(
    key = "HOOK_INTERNAL_SCREENSHOT",
    flags = WindowManager.LayoutParams.FLAG_SECURE
)
