package xyz.junerver.composehooks.example

import android.view.WindowManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.junerver.compose.hooks.useDisableScreenshot
import xyz.junerver.compose.hooks.useFlashlight
import xyz.junerver.compose.hooks.useGetState
import xyz.junerver.compose.hooks.useScreenBrightness
import xyz.junerver.compose.hooks.useWakeLock
import xyz.junerver.compose.hooks.useWindowFlags
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
    val batteryInfo by useBatteryInfo()
    val buildInfo = useBuildInfo()
    val screenInfo = useScreenInfo()
    val (disable, enable, isDisableState) = useDisableScreenshot()
    val isDisable by isDisableState
    val (on, setOn) = useGetState(default = false)
    val (turnOn, turnOff) = useFlashlight()
    val (req, release, isActiveState) = useWakeLock()
    val isActive by isActiveState
    val (addFlags, clearFlags, isFlagsAddedState) = useWindowFlags(
        key = "secure&wakelock",
        flags =
            WindowManager.LayoutParams.FLAG_SECURE or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
    )
    val isFlagsAdded by isFlagsAddedState
    val (setBrightness, initValue) = useScreenBrightness()
    val (sliderValue, setSliderValue) = useGetState(default = initValue)
    Surface {
        Column {
            Text(text = buildInfo.toString(), modifier = Modifier.padding(bottom = 20.dp))
            Text(text = batteryInfo.toString(), modifier = Modifier.padding(bottom = 20.dp))
            Text(text = screenInfo.toString(), modifier = Modifier.padding(bottom = 20.dp))
            // Control flashlight
            Text(text = "Control flashlight")
            Switch(checked = on.value, onCheckedChange = {
                setOn(it)
                if (it) {
                    turnOn()
                } else {
                    turnOff()
                }
            })

            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Control screen brightness")
            Slider(value = sliderValue.value, onValueChange = {
                setSliderValue(it)
                setBrightness(it)
            })

            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Is allow screenshots: ${!isDisable}")
            TButton(text = if (isDisable) "Enable" else "Disable") {
                if (isDisable) {
                    enable()
                    println("Now you can take screenshots")
                } else {
                    disable()
                    println("Now you can't take screenshots")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Is wake lock active: $isActive")
            TButton(text = if (isActive) "release" else "request") {
                if (isActive) {
                    release()
                    println("Release wake lock")
                } else {
                    req()
                    println("Request wake lock")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Is wake lock active & disable screenshots: $isFlagsAdded")
            TButton(text = if (isFlagsAdded) "clearFlags" else "addFlags") {
                if (isFlagsAdded) {
                    clearFlags()
                    println("Window flags clear!")
                } else {
                    addFlags()
                    println("Window flags added!")
                }
            }
        }
    }
}
