# 设计：为 hooks2 发布 wasmJs 变体

> 日期：2026-06-29
> 关联需求：`docs/wasmjs-variant-requirements.md`
> 范围：**R1（wasmJs target）+ R2（核心 hooks 可用）+ R4（平台能力 wasmJs 适配/降级）**；不含 R3（js target，暂缓）
> 状态：设计中

---

## 一、探查结论（对需求文档的修正）

需求文档 §4.1 把 `kotlin.reflect` 列为"最大移植风险点（11 处 import）"。实地核查代码后，结论**显著更乐观**：

### 1.1 commonMain 没有任何真正的反射调用

`hooks/src/commonMain` 中所有 `KClass` / `KFunction` / `KProperty` 引用都只属于以下三类用途，全部来自 `kotlin-stdlib`（wasmJs 可用），**不是** `kotlin-reflect` artifact：

| 用途 | 示例位置 | wasmJs 可用性 |
|---|---|---|
| Map 的键 | `HooksEventManager.kt:13`（`KClass<*>`）、`ReduxProvider.kt:28-29`、`Form.kt:227` | ✅ KClass 可作 Map 键 |
| 委托签名 | `Types.kt:248`、`UseRef.kt:60`、`UsePersistent.kt:65`（`KProperty<*>` 的 `getValue`/`setValue`） | ✅ stdlib 提供 |
| 仅存函数引用、不调用 | `Fetch.kt:92,94`（`originRunAsync: KFunction<Any> = ::_runAsync`） | ✅ stdlib 提供 |

全文 grep 验证：commonMain 中**零处**出现 `.call(` / `.callSuspend(` / `.members` / `.functions` / `.createType(` / `.isSubtypeOf(` 等反射 API 调用。

### 1.2 真正的反射调用已经隔离在 commonJvmAndroid

`.call()` / `.callSuspend()` / `.createType()` / `.isSubtypeOf()` 仅出现在两个文件，且都在 `commonJvmAndroid` 源集（wasmJs **不继承**它）：

- `Types.jvm.kt`：`asNoopFn` / `asSuspendNoopFn` / `synthesisParametersAndCheck`
- `utils/LegalCheck.kt`：`checkIsLegalParameters`

→ 因此 wasmJs target 天然不会触及这些反射调用，**无需 expect/actual 隔离**。

### 1.3 真正的构建阻塞点

`hooks/build.gradle.kts:65`：

```kotlin
commonMain.dependencies {
    ...
    implementation(libs.kotlin.reflect)   // ← org.jetbrains.kotlin:kotlin-reflect
```

`org.jetbrains.kotlin:kotlin-reflect` 这个 artifact **没有 wasmJs klib**，放在 `commonMain` 会阻塞 wasmJs target 的依赖解析。

但 commonMain **根本不需要它**——stdlib 已经提供了 commonMain 用到的全部 `KClass`/`KFunction`/`KProperty`。真正的反射调用都在 commonJvmAndroid 里。所以这个依赖应当**下沉到 `commonJvmAndroid` 依赖块**。这是本次唯一必须的代码层修复。

### 1.4 其余依赖均已发布 wasmJs 变体

| 依赖 | 版本 | wasmJs 支持 |
|---|---|---|
| compose-runtime / compose-ui | 1.11.1 | ✅ |
| kotlinx-coroutines-core | 1.11.0 | ✅（`Dispatchers.Default` 在 wasmJs 可用） |
| kotlinx-datetime | 0.8.0 | ✅ |
| kotlinx-collections-immutable | 0.4.0 | ✅ |
| arrow-core / arrow-functions | 2.1.2 | ✅（arrow-kt 2.0+ 支持 wasmJs） |
| compose.lifecycle.runtime.compose | 2.11.0-beta01 | ✅ |

### 1.5 平台/浏览器相关 hook 不在 commonMain

`useNetwork` / `usePageVisibility` / `useCopy` / `useScroll` 等浏览器环境 hook 经 grep 确认**不在 commonMain**（它们在 `androidMain` / `desktopMain` 等平台源集）。commonMain 内的平台相关项只有 `usePersistent`（注入式架构，见下）。

---

## 二、目标与非目标

### 目标（DoD）

- **V1**：`hooks/build.gradle.kts` 存在 `wasmJs { browser() }` target。
- **V2**：`publishToMavenLocal` 成功，产物含 wasmJs klib。
- **V3**：本地 Maven 的 `.module` 文件含 `wasmJs` variant。
- **V4**：§3.1 核心 hooks 在 wasmJs 编译通过（`compileKotlinWasmJs`）。
- **V5**：新增 `hooks/src/wasmJsTest`，核心 hooks 逻辑在 wasmJs 测试通过。
- **V6**：§3.2 的 hooks 要么 wasmJs 可用、要么文档明确标注 wasmJs 不支持/降级。
- **V7**：下游 Palette 升级到该版本后能加 `wasmJs { browser() }` 并编译（最终目的，需下游配合）。

### 非目标（本次明确排除）

- ❌ **R3 / V8**：js target。js 比 wasmJs 坑多（typed arrays、Compose 对 js 支持弱），Palette 主目标是 wasmJs。本次只做 wasmJs。
- ❌ `asNoopFn` / `asSuspendNoopFn` / `checkIsLegalParameters` 的 wasmJs 实现——这些函数继续只存在于 commonJvmAndroid，wasmJs 不提供。用户在 wasmJs 下需自行传 `SuspendNormalFunction` 闭包给 `useRequest`。
- ❌ `usePersistent` 的 localStorage / 浏览器存储后端——wasmJs 仅用现有内存默认（`memoryGetPersistent`），用户要持久化可自行注入。

---

## 三、架构

### 3.1 源集层次（改动后）

```
commonMain ──────────────────┬─► wasmJsMain (新增，标准模板)
                              ├─► commonJvmAndroid ─► androidMain
                              │                     └─► desktopMain
                              ├─► iosArm64
                              └─► iosSimulatorArm64

commonTest ─► wasmJsTest (新增)
                  desktopTest
                  ...
```

- `applyDefaultHierarchyTemplate()` 已存在（`build.gradle.kts:52`），新建 `hooks/src/wasmJsMain` 与 `hooks/src/wasmJsTest` 会被模板自动正确接线，**无需手写 `dependsOn`**。
- `wasmJsMain` 依赖 `commonMain`，**不经过 commonJvmAndroid**（与需求文档 §4.3 一致：commonJvmAndroid 的 `Types.jvm.kt` / `LegalCheck.kt` 是 JVM 专用反射，wasmJs 不需要）。
- 初始 `wasmJsMain` 可为空目录或仅放占位文件——所有逻辑都在 commonMain。仅当未来需要 wasmJs 专属 actual 时才填充。

### 3.2 依赖改动

**`hooks/build.gradle.kts`**：

```diff
 commonMain.dependencies {
     api(compose.runtime)
     api(compose.ui)
     implementation(libs.compose.lifecycle.runtime.compose)

     implementation(project.dependencies.platform(libs.kotlin.bom))
     api(libs.kotlin.stdlib)
-    implementation(libs.kotlin.reflect)   // 下沉到 commonJvmAndroid
     api(libs.kotlinx.coroutines)
     ...
     api(libs.arrow.core)
     api(libs.arrow.functions)
 }

 val commonJvmAndroid by creating {
     dependsOn(commonMain.get())
+    dependencies {
+        implementation(libs.kotlin.reflect)   // JVM/Android 才需要真正的反射 artifact
+    }
 }
```

理由：reflect artifact 无 wasmJs klib；commonMain 用到的 `KClass`/`KFunction`/`KProperty` 由 stdlib 提供；真正的反射调用（`asNoopFn` 等）都在 commonJvmAndroid。

### 3.3 target 声明

```kotlin
kotlin {
    ...
    wasmJs { browser() }   // 新增
    ...
    applyDefaultHierarchyTemplate()
}
```

发布配置：`maven-publish` 插件（`com.vanniktech.maven.publish`，已应用）会自动把 wasmJs klib 纳入发布变体，无需额外 `publishLibraryVariants`（那是 Android 专属）。

---

## 四、组件改动清单

| 文件 | 改动 | 风险 |
|---|---|---|
| `hooks/build.gradle.kts` | (1) `kotlin {}` 加 `wasmJs { browser() }`；(2) reflect 依赖从 commonMain 下沉到 commonJvmAndroid；(3) 触发 wasmJs target 后确认 `runCoverageChecks`/`runQualityChecks` 等自定义 task 的依赖不破裂 | 低 |
| `hooks/src/wasmJsMain/` | 新建空源集（放一个占位 `.kt` 或留空，取决于 KMP 是否要求至少有目录） | 极低 |
| `hooks/src/wasmJsTest/` | 新建测试源集，从 commonTest 挑选纯逻辑核心 hooks 的测试镜像/复用（见 §六） | 低 |
| 无源码改动 | commonMain / commonJvmAndroid 的 `.kt` 文件**均不需要修改**——反射函数已在正确源集，wasmJs 自然不继承 | — |

**关键**：方案 A 的核心优势正是"零业务代码改动"。唯一的代码层修复在 build 脚本的依赖位置。

---

## 五、数据/依赖流（关键路径）

```
Palette (palette 模块)
   │ enable wasmJs target
   ▼
hooks2 (wasmJs variant)
   │ 解析依赖：
   ├─ compose-runtime/ui 1.11.1 ─────► ✅ wasmJs klib
   ├─ kotlinx-* ─────────────────────► ✅ wasmJs klib
   ├─ arrow-core/functions 2.1.2 ────► ✅ wasmJs klib
   ├─ kotlin-stdlib ─────────────────► ✅ (KClass/KFunction/KProperty 来源)
   └─ kotlin-reflect ─────────────────► ⛔ 下沉到 commonJvmAndroid，wasmJs 不解析

commonMain 编译 (wasmJs)
   │ KClass/KFunction/KProperty ─► 全部来自 stdlib ✅
   │ 无 .call()/.callSuspend()    ─► 无反射调用 ✅
   ▼
wasmJsMain (空) ─► 编译通过
wasmJsTest ─► 核心逻辑测试通过
```

---

## 六、测试策略

### 6.1 新增 `hooks/src/wasmJsTest`

覆盖 §3.1 核心 hooks 的**纯逻辑行为**（用 `kotlin.test`，不依赖 UI）：

- `useState` / `useGetState` / `useLatestState`
- `useBoolean`
- `useCreation` / `useMemo`
- `useRef`
- `useMap` / `useSet`
- `useDebounce` / `useThrottle`（`kotlinx-coroutines-test`）
- `useEvent`
- `useReactive`

UI 行为测试（需 Compose runtime）放 wasmJsTest 的 `compose.uiTest` 变体（如 KMP 支持）；若 wasmJs 的 uiTest 链路复杂，V5 先以纯逻辑测试满足，UI 行为由下游 Palette 的 V7 间接验证。

### 6.2 验证命令

```bash
# Windows
.\gradlew.bat :hooks:compileKotlinWasmJs
.\gradlew.bat :hooks:wasmJsTest
.\gradlew.bat :hooks:publishToMavenLocal
# 检查产物
# ~/.m2/.../hooks2/<version>/ 应含 *.klib 与 wasmJs variant 的 .module
```

### 6.3 §3.2 hooks 的 wasmJs 状态标注（V6）

| Hook | wasmJs 状态 | 说明 |
|---|---|---|
| `useRequest` | ✅ 可用 | 注入式架构，commonMain 不绑 HTTP 客户端 |
| `useRequest` 的 `asNoopFn`/`asSuspendNoopFn` | ❌ wasmJs 不可用 | 反射函数仅 commonJvmAndroid；wasmJs 用户需自传 `SuspendNormalFunction` 闭包。文档标注 |
| `useForm` | ✅ 可用 | `KClass<*>` 仅作 Map 键，stdlib 提供 |
| `useRedux` / `createStore` | ✅ 可用 | `KClass<*>` 仅作 Map 键，stdlib 提供 |
| `useDateFormat` | ✅ 可用 | 注释提到 `System.currentTimeMillis()` 但代码用 `kotlinx-datetime`，wasmJs 可用 |

---

## 七、风险与回退

| 风险 | 概率 | 影响 | 缓解/回退 |
|---|---|---|---|
| reflect 下沉后 commonJvmAndroid 内某处仍引用不到 | 低 | JVM/Android 编译失败 | 回退：把 reflect 同时放回 commonJvmAndroid 即可（它本就该在那） |
| commonMain 有未被发现的反射调用 | 低（已全量 grep） | wasmJs 编译失败 | 实现阶段编译报错时，针对该点用 expect/actual 隔离或下沉到 commonJvmAndroid |
| arrow 2.1.2 实际无 wasmJs klib | 低（文档说 2.0+ 支持） | 依赖解析失败 | 升级 arrow 到确认发布 wasmJs 的版本 |
| wasmJs 的 `Dispatchers.Default` 行为与预期不同 | 低 | useRequest/CacheManager 协程行为差异 | 若出现问题，改为 `EmptyCoroutineContext` 或注入 dispatcher |
| `runCoverageChecks` 等自定义 task 在 wasmJs target 下破裂 | 中 | CI/quality 检查失败 | 实现阶段运行 `runQualityChecks` 验证；必要时把 wasmJsTest 纳入或排除 |
| 回退 | — | — | 整体回退 = 移除 `wasmJs{}` target + 把 reflect 放回 commonMain，回到现状 |

---

## 八、版本与发布

- 版本号：**`2.4.0-beta-1`**（新增 target 属于 minor bump，按需求文档 §6.3）。
- 流程：
  1. 实现改动 → `compileKotlinWasmJs` 通过。
  2. 加 wasmJsTest → `wasmJsTest` 通过。
  3. `publishToMavenLocal` → 验证产物含 wasmJs。
  4. 发 `2.4.0-beta-1`。
  5. Palette 侧做 V7 下游验证。
  6. 确认后发 `2.4.0` 正式版。

---

## 九、未决事项（实现阶段验证）

1. **确认 commonMain 不依赖 `asNoopFn` 系列**：初步 grep 显示仅 `UseRequest.kt:78` 文档注释引用，无实际调用。实现阶段编译验证。
2. **wasmJsMain 是否需要至少一个文件**：KMP 默认模板下空目录通常可用，实现阶段确认；不行则放占位文件。
3. **`runCoverageChecks` / `verifyCoverageBaseline`** 是否需调整以纳入 wasmJsTest（当前依赖 `allTests`，应自动包含新 target，需验证）。
4. **arrow 2.1.2 wasmJs klib 实际可用性**：依赖解析阶段确认。
