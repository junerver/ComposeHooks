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

@Composable
fun useDisableScreenshot(): Tuple3<DisableFn, EnableFn, IsDisabled> = useWindowFlags(
    key = "${KEY_PREFIX}SCREENSHOT",
    flags = WindowManager.LayoutParams.FLAG_SECURE
)
