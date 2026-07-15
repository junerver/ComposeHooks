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
    val localContext = LocalContext.current
    val context = localContext.applicationContext ?: localContext
    return produceState(initialValue = BatteryInfo(), context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                value = intent?.toBatteryInfo() ?: BatteryInfo()
            }
        }
        val isRegistered = try {
            val stickyIntent = context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            stickyIntent?.let { value = it.toBatteryInfo() }
            true
        } catch (_: SecurityException) {
            false
        }

        awaitDispose {
            if (isRegistered) {
                try {
                    context.unregisterReceiver(receiver)
                } catch (_: IllegalArgumentException) {
                    // The platform may already have detached the receiver while its context was shutting down.
                }
            }
        }
    }
}

private fun Intent.toBatteryInfo(): BatteryInfo {
    val status = getIntExtra(BatteryManager.EXTRA_STATUS, -1)
    return createBatteryInfo(
        level = getIntExtra(BatteryManager.EXTRA_LEVEL, -1),
        scale = getIntExtra(BatteryManager.EXTRA_SCALE, -1),
        isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL,
    )
}

internal fun createBatteryInfo(level: Int, scale: Int, isCharging: Boolean): BatteryInfo {
    val percentage = if (level < 0 || scale <= 0) {
        -1
    } else {
        (level.toDouble() / scale * 100).toInt().coerceIn(0, 100)
    }
    return BatteryInfo(level = percentage, isCharging = isCharging)
}

@Stable
data class BatteryInfo(
    val level: Int = -1,
    val isCharging: Boolean = false,
)
