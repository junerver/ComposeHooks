# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

ComposeHooks 是一个 Kotlin Multiplatform 库，在 Jetpack Compose 中提供 React 风格的 Hooks。灵感来自 [alibaba/hooks](https://github.com/alibaba/hooks)。

**支持平台**: Android, Desktop (JVM), iOS (arm64, x64, simulator-arm64)

**Artifact**: `xyz.junerver.compose:hooks2:<version>`

## Build Commands

```bash
# 构建
./gradlew build

# 格式化代码 (提交前必须运行)
./gradlew formatKotlin

# 检查格式
./gradlew lintKotlin

# 测试
./gradlew test                          # 单元测试
./gradlew desktopTest                   # Desktop 测试
./gradlew androidInstrumentedTest       # Android 插桩测试

# 运行示例应用
./gradlew :app:run                      # Desktop
./gradlew :app:installDebug             # Android

# 发布
./gradlew publishToMavenCentral
```

## Architecture

```
hooks/src/
├── commonMain/kotlin/xyz/junerver/compose/hooks/
│   ├── userequest/          # 网络请求管理 (插件架构)
│   ├── useform/             # 表单验证框架
│   ├── useref/              # Ref 相关 hooks
│   └── *.kt                 # 各种 hooks (useState, useEffect, useReducer 等)
├── commonJvmAndroid/        # JVM+Android 共享代码
├── androidMain/             # Android 专属 hooks (useBiometric, useNetwork 等)
├── desktopMain/             # Desktop 专属代码
└── iosMain/                 # iOS 专属代码

app/src/commonMain/          # 示例代码，展示各 hook 用法
```

## Hook Development Standards

### 命名和返回值
- Hook 函数名以 `use` 开头，如 `useNetwork`
- 返回值类型命名为 `XxxHolder`
- 所有 `use` 函数都有对应的 `remember` 签名别名

### 实现规范
- 不直接返回状态值，包装在 `State` 中
- Holder 中 `State` 放在前面，函数放在后面
- 优先使用现有 hooks 而非原生 Compose 函数：
  - `useState` 代替 `derivedStateOf`
  - `useCreation` 或 `useRef` 代替 `remember`
  - `useEffect` 代替 `LaunchedEffect`
- 函数成员声明类型别名

### useRequest 插件系统
位于 `userequest/` 目录，核心功能通过插件实现：缓存、防抖、节流、重试、轮询等。

## Commit Message Format

使用 Gitmoji 格式：
```
[Gitmoji] [Module]: Short description

✨ - 新功能    🐛 - Bug修复    📝 - 文档    ⚡️ - 优化
🩹 - 小修复    ⬆️ - 依赖更新   🔖 - 版本    🧪 - 测试
```

示例：`✨ [Network]: Add network state hook`
