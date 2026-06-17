# 平台专属 Hooks

## 目录

- [Android Hooks](#android-hooks)
- [Desktop Hooks](#desktop-hooks)

---

## Android Hooks

Android Hook 位于 `hooks/src/androidMain/kotlin/xyz/junerver/compose/hooks/`。所有公开 `useXxx` 均有 `rememberXxx` 别名，返回类型以源码为准：部分 Hook 返回 `State`，部分返回 `Pair`/`Triple` 控制函数。

### useBiometric / rememberBiometric

生物识别认证。返回 `Pair<() -> Unit, State<Boolean>>`：第一个函数打开认证 Activity，第二个状态表示是否认证成功。

```kotlin
val (openBiometric, isAuthed) = useBiometric {
    title = "身份验证"
    negativeButtonText = "取消"
    onAuthenticationSucceeded = { result ->
        println("认证成功: $result")
    }
    onAuthenticationError = { code, message ->
        println("认证失败: $code $message")
    }
}

Button(onClick = openBiometric) {
    Text("开始认证")
}

if (isAuthed.value) {
    Text("已认证")
}
```

---

### useNetwork / rememberNetwork

`useNetwork()` 直接创建网络监听并返回 `State<NetworkState>`。更推荐在根部使用 `NetworkProvider`，子组件通过 `rememberNetwork()` 读取同一个 `NetworkContext` 状态，避免重复创建监听。

```kotlin
NetworkProvider {
    val network by rememberNetwork()

    Text("是否连接: ${network.isConnect}")
    Text("网络类型: ${network.connectType}")
}
```

只在局部需要独立监听时直接使用：

```kotlin
val network by useNetwork()
Text("是否连接: ${network.isConnect}")
```

---

### useBatteryInfo / rememberBatteryInfo

电池信息，返回 `State<BatteryInfo>`。

```kotlin
val battery by useBatteryInfo()

Text("电量: ${battery.level}%")
Text("正在充电: ${battery.isCharging}")
```

---

### useBuildInfo / rememberBuildInfo

设备构建信息，返回普通 `BuildInfo`。

```kotlin
val build = useBuildInfo()

Text("品牌: ${build.brand}")
Text("型号: ${build.model}")
Text("Android 版本: ${build.release}")
```

---

### useScreenInfo / rememberScreenInfo

屏幕信息，返回普通 `ScreenInfo`。

```kotlin
val screen = useScreenInfo()

Text("宽高 dp: ${screen.screenDp.width} x ${screen.screenDp.height}")
Text("宽高 px: ${screen.screenPx.width} x ${screen.screenPx.height}")
Text("密度: ${screen.density}")
Text("横屏: ${screen.isLandscape}")
```

---

### useVibrate / rememberVibrate

振动控制，返回 `Pair<shortVibrate, longVibrate>`。

```kotlin
val (shortVibrate, longVibrate) = useVibrate(short = 100, long = 400)

Button(onClick = shortVibrate) { Text("短振动") }
Button(onClick = longVibrate) { Text("长振动") }
```

---

### useFlashlight / rememberFlashlight

手电筒控制，返回 `Pair<TurnOnFn, TurnOffFn>`。

```kotlin
val (turnOn, turnOff) = useFlashlight()

Button(onClick = turnOn) { Text("打开") }
Button(onClick = turnOff) { Text("关闭") }
```

---

### useWakeLock / rememberWakeLock

屏幕唤醒锁，基于 `useWindowFlags`，返回 `Triple<request, release, isActive>`。

```kotlin
val (requestWakeLock, releaseWakeLock, isActive) = useWakeLock()

Button(onClick = requestWakeLock) { Text("保持唤醒") }
Button(onClick = releaseWakeLock) { Text("释放") }
Text("是否启用: ${isActive.value}")
```

---

### useSensor / rememberSensor

传感器监听。必须传入 Android `Sensor` 类型常量，通过回调处理 `SensorEvent`。

```kotlin
useSensor(
    sensorType = Sensor.TYPE_ACCELEROMETER,
    onSensorChangedFn = { event ->
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        println("x=$x y=$y z=$z")
    },
)
```

---

### useIlluminance / rememberIlluminance

环境光照强度统计，内部监听 `Sensor.TYPE_LIGHT`，返回 `IlluminanceHolder`。

```kotlin
val illuminance = useIlluminance(calibrate = 1.0f)
val info by illuminance.state

Text("当前: ${info.now} lux")
Text("最小: ${info.min} lux")
Text("最大: ${info.max} lux")
Text("平均: ${info.avg} lux")

Button(onClick = illuminance.reset) {
    Text("重置统计")
}
```

---

### useIdle / rememberIdle

用户空闲检测，返回 `State<IdleInfo>`。

```kotlin
val idleInfo by useIdle(timeout = 30.seconds)

if (idleInfo.idle) {
    Text("用户空闲，最后活动: ${idleInfo.lastActive}")
}
```

---

### useScreenBrightness / rememberScreenBrightness

屏幕亮度控制，返回 `Pair<setBrightness, initialBrightness>`。`setBrightness` 接受 `0f..1f`，超出范围时恢复系统默认；组件卸载时会恢复初始亮度。

```kotlin
val (setBrightness, initialBrightness) = useScreenBrightness()
var brightness by useFloat(initialBrightness)

Slider(
    value = brightness,
    onValueChange = {
        brightness = it
        setBrightness(it)
    },
)
```

---

### useDisableScreenshot / rememberDisableScreenshot

禁用截图，基于 `WindowManager.LayoutParams.FLAG_SECURE`，返回 `Triple<disable, enable, isDisabled>`。

```kotlin
val (disableScreenshot, enableScreenshot, isDisabled) = useDisableScreenshot()

useMount {
    disableScreenshot()
}

useUnmount {
    enableScreenshot()
}

Text("截图禁用: ${isDisabled.value}")
```

---

### useWindowFlags / rememberWindowFlags

窗口标志控制，返回 `Triple<addFlags, clearFlags, isFlagsAdded>`。`key` 用于 `usePersistent(..., forceUseMemory = true)` 记录当前标志状态。

```kotlin
val (addFlags, clearFlags, isAdded) = useWindowFlags(
    key = "keep_screen_on",
    flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
)

Button(onClick = addFlags) { Text("添加标志") }
Button(onClick = clearFlags) { Text("清除标志") }
Text("已添加: ${isAdded.value}")
```

---

## Desktop Hooks

### useKeyPress / rememberKeyPress

Desktop 键盘组合键检测。`useKeyPress` 监听 `KeyPressDelegate.keyPress`，需要在可接收键盘事件的节点上接入 `KeyPressDelegate.onKeyEvent`。

```kotlin
Box(
    modifier = Modifier
        .focusable()
        .onKeyEvent(KeyPressDelegate::onKeyEvent),
) {
    useKeyPress(Key.Enter) {
        println("按下 Enter")
    }

    useKeyPress(Key.CtrlLeft, Key.S) {
        println("按下 Ctrl+S")
    }
}
```

支持 1 到 4 个 `Key` 参数的精确组合匹配，当前按下键集合必须与参数数量一致才会触发。

---

## 依据

- Android hooks: `hooks/src/androidMain/kotlin/xyz/junerver/compose/hooks/`
- Desktop hooks: `hooks/src/desktopMain/kotlin/xyz/junerver/compose/hooks/`
