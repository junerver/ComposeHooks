# Repository Guidelines

## Project Overview

ComposeHooks 是一个 Kotlin Multiplatform 库，在 Jetpack Compose 中提供 React 风格的 Hooks。灵感来自 [alibaba/hooks](https://github.com/alibaba/hooks)。

**支持平台**: Android, Desktop (JVM), iOS (arm64, x64, simulator-arm64)

**Artifact**: `xyz.junerver.compose:hooks2:<version>`

## Project Structure & Module Organization

ComposeHooks is split into the `hooks` Kotlin Multiplatform library and the `app` multiplatform showcase client. Library sources live in `hooks/src/*` with platform folders like `commonMain`, `commonTest`, `androidMain`, `desktopMain`, and the `ios*` targets. Example screens and demo scaffolding reside under `app/src/...`, mirroring the platform folders, while shared assets and docs sit in `art/` and `docs/`. Gradle wiring remains at the root (`build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`) alongside contributor resources.

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

## Build, Test, and Development Commands

```bash
# 构建
./gradlew build
./gradlew :hooks:build       # 仅构建库

# 格式化代码 (提交前必须运行)
./gradlew formatKotlin

# 检查格式
./gradlew lintKotlin

# 测试
./gradlew test                          # 单元测试
./gradlew desktopTest                   # Desktop 测试
./gradlew :hooks:check                  # 完整检查
./gradlew androidInstrumentedTest       # Android 插桩测试

# 运行示例应用
./gradlew :app:run                      # Desktop
./gradlew :app:installDebug             # Android

# 发布
./gradlew :hooks:publishToMavenLocal    # 本地验证
./gradlew publishToMavenCentral         # 正式发布
```

## Coding Style & Naming Conventions

Indent with four spaces and keep Kotlin lines under 140 characters as enforced by `.editorconfig`. Use Kotlin Official formatting with trailing commas enabled and never introduce wildcard imports. Include the standard header comment block in every new Kotlin file and favor descriptive, imperative function names.

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

## Testing Guidelines

Add shared coverage in `hooks/src/commonTest/kotlin` with `kotlin.test` and `kotlinx.coroutines.test`; desktop-only verifications belong in `hooks/src/desktopTest`. UI behavior should use Compose testing APIs in the relevant platform source set. Run `./gradlew :hooks:check` before opening a PR, and complement feature work with focused tests. Android instrumentation lives in `hooks/src/androidInstrumentedTest` and can be executed on a device or emulator via `./gradlew :hooks:connectedDebugAndroidTest`.

### TDD Practice Requirements

Tests exist to discover implementation defects, not merely to pass. Follow these principles:

1. **Write tests that challenge the implementation**: Design test cases based on expected behavior and edge cases, not by mimicking the implementation logic. Avoid "copy-paste" tests that simply mirror what the code does.

2. **When a test fails, fix the implementation first**: If a well-designed test case fails, the default action is to fix the implementation, not the test. Only modify a test when it genuinely contains a logical error or misunderstands the requirement.

3. **Test boundary conditions and error paths**: Include tests for null inputs, empty collections, concurrent access, timeout scenarios, and other edge cases that reveal hidden bugs.

4. **Verify behavior, not implementation details**: Assert on observable outcomes (return values, state changes, side effects) rather than internal method calls or private state.

5. **Red-Green-Refactor cycle**: When adding new functionality, write a failing test first, implement the minimum code to pass, then refactor while keeping tests green.

## Troubleshooting: WSL 混合构建导致增量编译缓存损坏

**症状**: `./gradlew :app:hotRunDesktop` 或其他编译任务失败，报错类似：
```
e: file:///E:/GitHub/ComposeHooks/hooks/src/commonMain/.../declare.kt:5:8 Unresolved reference 'androidx'
```
或：
```
Incremental compilation failed: Expected absolute path but found relative path: \mnt\e\github\composehooks\...
```

**根因**: 在 WSL (Windows Subsystem for Linux) 和 Windows 之间交替构建同一项目时，Kotlin 增量编译缓存（`.gradle/` 目录）会混入两种路径格式：
- Windows: `E:\GitHub\ComposeHooks\...`
- WSL: `/mnt/e/github/composehooks/...`

缓存中的路径不一致导致编译器无法正确定位源文件，产生 `Unresolved reference` 或路径解析错误。

**修复方法**:
```bash
# 1. 停止所有 Gradle 守护进程
./gradlew --stop

# 2. 删除项目级 .gradle 缓存
Remove-Item -Recurse -Force .gradle   # PowerShell
# rm -rf .gradle                       # WSL/Linux

# 3. 删除所有模块的 build 目录
Remove-Item -Recurse -Force hooks\build, ai\build, app\build

# 4. 重新构建
./gradlew :app:hotRunDesktop
```

**预防措施**:
- 避免在 WSL 和 Windows 之间交替构建同一项目
- 如果必须切换环境，先执行上述清理步骤
- 考虑在 `.gitignore` 中确保 `.gradle/` 不被提交

## Commit & Pull Request Guidelines

使用 Gitmoji 格式：
```
[Gitmoji] [Module]: Short description

✨ - 新功能    🐛 - Bug修复    📝 - 文档    ⚡️ - 优化
🩹 - 小修复    ⬆️ - 依赖更新   🔖 - 版本    🧪 - 测试
```

示例：`✨ [Hooks]: Add useToggleHolder`

Keep the subject under 50 characters and in present tense. Expand on context in the body when behavior is non-trivial and reference issues after the first line. Pull requests should summarize the change, link related issues, note verification commands, and include screenshots or recordings for UI-facing updates. Keep PR scope tight, ensure CI passes, and request review only when the branch is green.
