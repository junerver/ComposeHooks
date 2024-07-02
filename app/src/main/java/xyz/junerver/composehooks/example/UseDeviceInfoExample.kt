package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.junerver.compose.hooks.useDisableScreenshot
import xyz.junerver.compose.hooks.useFlashlight
import xyz.junerver.compose.hooks.useGetState
import xyz.junerver.compose.hooks.usedeviceinfo.useBatteryInfo
import xyz.junerver.compose.hooks.usedeviceinfo.useBuildInfo
import xyz.junerver.compose.hooks.usedeviceinfo.useScreenInfo
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description:
  Author: Junerver
  Date: 2024/7/1-9:39
  Email: junerver@gmail.com
  Version: v1.0
*/
@Composable
fun UseDeviceInfoExample() {
    val batteryInfo = useBatteryInfo()
    val buildInfo = useBuildInfo()
    val screenInfo = useScreenInfo()
    val (disable, enable, isDisable) = useDisableScreenshot()
    val (on, setOn) = useGetState(default = false)
    val (turnOn, turnOff) = useFlashlight()
    Surface {
        Column {
            Text(text = buildInfo.toString(), modifier = Modifier.padding(bottom = 20.dp))
            Text(text = batteryInfo.toString(), modifier = Modifier.padding(bottom = 20.dp))
            Text(text = screenInfo.toString(), modifier = Modifier.padding(bottom = 20.dp))
            Text(text = "Is allow screenshots: ${!isDisable}")
            TButton(text = if (isDisable) "Enable" else "Disable") {
                if (isDisable) {
                    enable()
                    toast("Now you can take screenshots")
                } else {
                    disable()
                    toast("Now you can't take screenshots")
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Control flashlight")
            Switch(checked = on, onCheckedChange = {
                setOn(it)
                if (it) {
                    turnOn()
                } else {
                    turnOff()
                }
            })
        }
    }
}
