# 平台专属 Hooks

## 目录

- [Android Hooks](#android-hooks)
- [Desktop Hooks](#desktop-hooks)

---

## Android Hooks

### useBiometric

生物识别认证（指纹、面部识别等）。

```kotlin
val biometric = useBiometric()

// 发起认证
biometric.authenticate(
    title = "身份验证",
    subtitle = "请验证指纹",
    onSuccess = { println("认证成功") },
    onError = { code, message -> println("认证失败: $message") }
)

// 检查是否支持
if (biometric.isAvailable.value) {
    Text("设备支持生物识别")
}
```

---

### useNetwork

网络状态监测。

```kotlin
val network = useNetwork()

// 当前网络状态
Text("网络类型: ${network.networkType.value}")
Text("是否连接: ${network.isConnected.value}")

// 监听网络变化
useEffect(network.isConnected.value) {
    if (network.isConnected.value) {
        refreshData()
    }
}
```

---

### useBatteryInfo

电池信息。

```kotlin
val battery = useBatteryInfo()

Text("电量: ${battery.level.value}%")
Text("正在充电: ${battery.isCharging.value}")
Text("充电方式: ${battery.pluggedType.value}")
```

---

### useBuildInfo

设备构建信息。

```kotlin
val build = useBuildInfo()

Text("设备: ${build.device.value}")
Text("型号: ${build.model.value}")
Text("品牌: ${build.brand.value}")
Text("Android 版本: ${build.sdkVersion.value}")
```

---

### useScreenInfo

屏幕信息。

```kotlin
val screen = useScreenInfo()

Text("宽度: ${screen.widthPixels.value}px")
Text("高度: ${screen.heightPixels.value}px")
Text("密度: ${screen.density.value}")
Text("DPI: ${screen.dpi.value}")
```

---

### useVibrate

振动控制。

```kotlin
val vibrate = useVibrate()

// 短振动
Button(onClick = { vibrate.vibrate(100) }) {
    Text("振动")
}

// 模式振动（振动-暂停-振动...）
Button(onClick = { vibrate.vibratePattern(longArrayOf(0, 100, 50, 100)) }) {
    Text("模式振动")
}

// 取消振动
Button(onClick = { vibrate.cancel() }) {
    Text("停止")
}
```

---

### useFlashlight

手电筒控制。

```kotlin
val flashlight = useFlashlight()

Switch(
    checked = flashlight.isAvailable.value,
    onCheckedChange = { flashlight.toggle() }
)
```

---

### useWakeLock

屏幕唤醒锁。

```kotlin
val wakeLock = useWakeLock()

// 保持屏幕常亮
Button(onClick = { wakeLock.acquire() }) {
    Text("保持唤醒")
}

// 释放
Button(onClick = { wakeLock.release() }) {
    Text("释放")
}
```

---

### useSensor

传感器数据。

```kotlin
val sensor = useSensor()

// 加速度传感器
Text("X: ${sensor.accelerometer.x.value}")
Text("Y: ${sensor.accelerometer.y.value}")
Text("Z: ${sensor.accelerometer.z.value}")
```

---

### useIlluminance

环境光照强度。

```kotlin
val lux = useIlluminance()
Text("光照强度: ${lux.value} lux")
```

---

### useIdle

用户空闲检测。

```kotlin
val idle = useIdle(timeout = 30.seconds)

useEffect(idle.isIdle.value) {
    if (idle.isIdle.value) {
        println("用户空闲超过30秒")
    }
}
```

---

### useScreenBrightness

屏幕亮度控制。

```kotlin
val brightness = useScreenBrightness()

Slider(
    value = brightness.brightness.value,
    onValueChange = { brightness.setBrightness(it) }
)
```

---

### useDisableScreenshot

禁用应用截图（安全场景）。

```kotlin
// 在需要安全的页面中使用
useDisableScreenshot()
```

---

### useWindowFlags

窗口标志控制（如全屏、保持屏幕常亮等）。

```kotlin
useWindowFlags(
    flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
)
```

---

## Desktop Hooks

### useKeyPress

键盘按键检测。

```kotlin
// 检测单个按键
useKeyPress("Enter") {
    println("按下了回车键")
}

// 检测组合键
useKeyPress("Ctrl+S") {
    println("按下了 Ctrl+S")
    saveDocument()
}

// 检测多个按键
useKeyPress("Escape") {
    println("按下了 Escape")
    closeModal()
}
```

---

## 依据

- Android hooks: hooks/src/androidMain/kotlin/xyz/junerver/compose/hooks/
- Desktop hooks: hooks/src/desktopMain/kotlin/xyz/junerver/compose/hooks/
