@file:Suppress("unused", "ComposableNaming")

package xyz.junerver.compose.hooks

import android.hardware.Sensor
import android.hardware.SensorEvent
import androidx.compose.runtime.Composable
import xyz.junerver.compose.hooks.usedeviceinfo.*
import xyz.junerver.compose.hooks.usevibrate.useVibrate

/** 更符合 Compose 的函数命名方式 */

//region useDeviceInfo
@Composable
fun rememberBatteryInfo(): BatteryInfo = useBatteryInfo()

@Composable
fun rememberBuildInfo(): BuildInfo = useBuildInfo()

@Composable
fun rememberScreenInfo(): ScreenInfo = useScreenInfo()
//endregion

@Composable
fun rememberDisableScreenshot(): Triple<DisableFn, EnableFn, IsDisabled> = useDisableScreenshot()

@Composable
fun rememberFlashlight(): Pair<TurnOnFn, TurnOffFn> = useFlashlight()

@Composable
fun rememberScreenBrightness(): Pair<SetValueFn<Float>, Float> = useScreenBrightness()

@Composable
fun rememberSensor(
    sensorType: Int,
    onAccuracyChangedFn: (Sensor, Int) -> Unit = { _, _ -> },
    onSensorChangedFn: (SensorEvent) -> Unit = { _ -> },
) = useSensor(sensorType, onAccuracyChangedFn, onSensorChangedFn)

@Composable
fun rememberVibrate() = useVibrate()

@Composable
fun rememberWakeLock(): Triple<RequestFn, ReleaseFn, IsActive> = useWakeLock()

@Composable
fun rememberWindowFlags(key: String, flags: Int): Triple<AddFlagsFn, ClearFlagsFn, IsFlagsAdded> = useWindowFlags(key, flags)
