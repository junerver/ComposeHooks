package xyz.junerver.compose.hooks.usedeviceinfo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext

/*
  Description:
  Author: Junerver
  Date: 2024/7/1-9:35
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun useBatteryInfo(): State<BatteryInfo> {
    val context = LocalContext.current
    return produceState(initialValue = BatteryInfo()) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
                val originLevel = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) ?: 0
                val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 0) ?: 0
                val level = (originLevel / scale.toFloat() * 100).toInt()
                value = BatteryInfo(level, isCharging)
            }
        }
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        awaitDispose {
            context.unregisterReceiver(receiver)
        }
    }
}

@Stable
data class BatteryInfo(
    val level: Int = -1,
    val isCharging: Boolean = false,
)
