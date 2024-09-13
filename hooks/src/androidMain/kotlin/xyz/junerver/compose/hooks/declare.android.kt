@file:Suppress("unused", "ComposableNaming")

package xyz.junerver.compose.hooks

import android.hardware.Sensor
import android.hardware.SensorEvent
import androidx.compose.runtime.Composable
import xyz.junerver.compose.hooks.usedeviceinfo.*
import xyz.junerver.compose.hooks.usevibrate.useVibrate
import xyz.junerver.kotlin.Tuple2
import xyz.junerver.kotlin.Tuple3

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
fun rememberDisableScreenshot(): Tuple3<DisableFn, EnableFn, IsDisabled> = useDisableScreenshot()

@Composable
fun rememberFlashlight(): Tuple2<TurnOnFn, TurnOffFn> = useFlashlight()

@Composable
fun rememberScreenBrightness(): Tuple2<SetValueFn<Float>, Float> = useScreenBrightness()

@Composable
fun rememberSensor(
    sensorType: Int,
    onAccuracyChangedFn: (Sensor, Int) -> Unit = { _, _ -> },
    onSensorChangedFn: (SensorEvent) -> Unit = { _ -> },
) = useSensor(sensorType, onAccuracyChangedFn, onSensorChangedFn)

@Composable
fun rememberVibrate() = useVibrate()

@Composable
fun rememberWakeLock(): Tuple3<RequestFn, ReleaseFn, IsActive> = useWakeLock()

@Composable
fun rememberWindowFlags(key: String, flags: Int): Tuple3<AddFlagsFn, ClearFlagsFn, IsFlagsAdded> = useWindowFlags(key, flags)
