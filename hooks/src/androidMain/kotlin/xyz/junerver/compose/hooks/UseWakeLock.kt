package xyz.junerver.compose.hooks

import android.view.WindowManager
import androidx.compose.runtime.Composable
import xyz.junerver.compose.hooks.internal.KEY_PREFIX

/*
  Description: Provides a way to prevent devices from dimming or locking the screen
  when an application needs to keep running.

  Author: Junerver
  Date: 2024/7/3-11:13
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun useWakeLock(): Triple<RequestFn, ReleaseFn, IsActive> = useWindowFlags(
    key = "${KEY_PREFIX}WAKE_LOCK",
    flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
)
