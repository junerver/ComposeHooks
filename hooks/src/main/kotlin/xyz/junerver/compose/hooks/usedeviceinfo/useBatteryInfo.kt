package xyz.junerver.compose.hooks.usedeviceinfo

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import xyz.junerver.compose.hooks.useState

/*
  Description:
  Author: Junerver
  Date: 2024/7/1-9:35
  Email: junerver@gmail.com
  Version: v1.0
*/
@Composable
fun useBatteryInfo(): BatteryInfo {
    val batteryStatus = LocalContext.current.registerReceiver(
        null,
        IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    )
    val level by useState {
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) ?: 0
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, 0) ?: 0
        (level / scale.toFloat() * 100).toInt()
    }
    val isCharging by useState {
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
    }
    return BatteryInfo(level, isCharging)
}

data class BatteryInfo(
    val level: Int,
    val isCharging: Boolean,
)
