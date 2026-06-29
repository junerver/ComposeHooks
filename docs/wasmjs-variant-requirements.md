# 需求：为 hooks2 发布 wasmJs（及 js）变体

> 提出日期：2026-06-29
> 提出方：Palette 组件库（`xyz.junerver.compose.palette`）
> 优先级：**P0**（阻塞下游文档站的在线交互预览能力）
> 状态：待评估

---

## 一、背景与动机

### 1.1 谁在依赖这个

`hooks2` 是跨平台 Compose 组件库 **Palette** 的核心状态管理依赖。Palette 的几乎所有组件（Button、Select、Form、Table、Chart、Markdown 编辑器等 88+ 组件）都使用 hooks2 的 API，主要包括：

- `useState` / `useGetState` / `useLatestState`（状态）
- `useBoolean`（布尔状态）
- `useCreation` / `useMemo`（派生计算）
- `useRef`（引用）
- `useMount` / `useUnmount`（生命周期）
- `useContext` / `createContext`（上下文）
- `useMap` / `useSet`（集合状态）

### 1.2 当前的问题

Palette 计划建设**在线文档站点**，目标是在浏览器中提供组件的**真实可交互预览**（可点击、可调参的 Compose 组件）。这需要为 Palette 的 `palette` 模块启用 **wasmJs target**，用 Compose Multiplatform 编译到 WebAssembly 运行在浏览器里。

**阻塞点**：hooks2 当前只发布了 `android` / `ios` / `jvm(desktop)` 三个 target，**没有 wasmJs 变体**。

```
hooks2 已发布变体（2.2.2-beta-2 / 2.2.2-beta-3 / 2.3.0 均如此）：
  ✅ android    ✅ iosArm64 / iosSimulatorArm64    ✅ jvm(desktop)
  ❌ wasmJs     ❌ js
```

Gradle 在解析 `xyz.junerver.compose:hooks2` 时找不到 wasmJs 变体，导致 Palette 无法启用 wasmJs target，整个"在线交互预览"方案无法实施。

### 1.3 期望收益

发布 wasmJs 变体后，不仅是 Palette，**所有使用 hooks2 的 Compose Multiplatform 项目**都能获得 Web 端能力，显著扩大 hooks2 的适用范围与生态价值。

---

## 二、需求范围

### 2.1 核心需求（P0）

| ID | 需求 | 说明 |
|---|---|---|
| **R1** | 发布 **wasmJs** 变体 | 在 `kotlin {}` 中添加 `wasmJs { browser() }` target，随库发布。这是 Compose Multiplatform 官方推荐的 Web 目标（wasm 而非 js）。 |
| **R2** | 核心状态 hooks 全部可用 | 下表"必须在 wasmJs 可用"的 hooks 不能因平台限制降级或编译失败。 |

### 2.2 次要需求（P1）

| ID | 需求 | 说明 |
|---|---|---|
| **R3** | 发布 **js** 变体（可选） | wasmJs 是主目标；js 变体作为兼容补充（部分场景需要纯 JS target）。如成本高可暂缓。 |
| **R4** | 带副作用/平台能力的 hooks 提供 wasmJs 适配或明确的降级策略 | 见 §4 "平台能力风险点"。 |

---

## 三、hook 清单与 wasmJs 可用性评估

基于对 `hooks/src/commonMain`（111 个 .kt 文件）的分析，将 hooks 按 wasmJs 可行性分为三档：

### 3.1 ✅ 预期可直接可用（纯 Compose 状态逻辑，无平台依赖）

这些是 Palette 最核心依赖，**必须在 wasmJs 可用**：

| Hook | 用途 | 备注 |
|---|---|---|
| `useState` | 基础状态 | 纯 Compose runtime |
| `useGetState` | 获取最新状态 | 纯状态 |
| `useLatestState` | 最新状态快照 | 纯状态 |
| `useBoolean` | 布尔状态 | 基于 useState |
| `useCreation` | 派生计算（带依赖） | 纯计算 |
| `useMemo` | 记忆化 | 纯计算 |
| `useRef` | 可变引用 | 纯状态 |
| `useMount` | 挂载回调 | 纯 DisposableEffect |
| `useUnmount` | 卸载回调 | 纯 DisposableEffect |
| `useContext` / `createContext` | 上下文 | 纯 Compose |
| `useMap` / `useSet` | 集合状态 | 纯状态 |
| `useEffect` / `useUpdateEffect` | 副作用 | 纯 Compose Effect |
| `useDebounce` / `useThrottle` | 防抖/节流 | 纯协程/时间 |
| `useEvent` | 事件回调 | 纯状态 |
| `useReactive` | 响应式转换 | 纯状态 |

### 3.2 ⚠️ 需要验证/可能需轻量适配

| Hook | 风险点 | 建议处理 |
|---|---|---|
| `useRequest` / `Fetch` | 使用 `kotlin.reflect.KFunction`（`UseRequest.kt:10-11`）。但实际网络调用是**注入式**（`serviceFunction` 由调用方传入），不绑定 HTTP 客户端。 | 反射用途需确认：若仅用于类型标记/日志，可用 expect/actual 或 `@OptionalExpectation` 降级。 |
| `useForm` | 使用 `kotlin.reflect.KClass`（`Form.kt:13`）做字段类型映射。 | 同上，`KClass` 在 wasmJs 可用（kotlin-reflect 提供），但需验证可用子集。 |
| `useRedux` / `createStore` / `ReduxProvider` | 大量使用 `KClass<*>` 做状态/动作类型注册（`CreateStore.kt:24-25`）。 | `KClass` 在 wasmJs 支持有限；可能需改用字符串键或 `KClass` 简化用法。 |
| `useDateFormat` | 注释提及 `System.currentTimeMillis()`（`UseDateFormat.kt:629`）。 | 应改用 `kotlinx-datetime` 的 `Clock.System.now()`，已是依赖项。 |

### 3.3 ❓ 平台强依赖，需明确 wasmJs 行为

这些 hook 依赖平台特定能力（存储、生物识别等），在 wasmJs 下需要 expect/actual 或降级：

| Hook | 平台依赖 | wasmJs 建议 |
|---|---|---|
| `usePersistent` | 持久化存储。当前架构是**注入式**（`SaveToPersistent<T>` typealias = `(T?) -> Unit`，`Types.kt:106`），通过 `createContext` 注入实际存储实现，**不在 commonMain 绑定具体存储**。 | 优秀设计——wasmJs 下只需提供基于 `localStorage` 的 actual 实现，或注入 no-op。**不阻塞 R1/R2**。 |
| `usePageVisibility` / `useNetwork` 等浏览器环境相关 | 可能依赖平台事件 | 提供 wasmJs（browser）的 actual 实现，或降级。 |

---

## 四、技术评估：wasmJs 移植的已知风险点

### 4.1 ⚠️ `kotlin.reflect`（`KClass` / `KFunction` / `KProperty`）

**这是最大的移植风险点。** 共 11 处 import 分布在 10 个文件（见 §3.2/3.3）。

- **现状**：commonMain 直接 import `kotlin.reflect.KClass` 等，且 `kotlin.reflect` 在 commonMain 是可用的（stdlib 提供）。
- **wasmJs 情况**：kotlin-reflect 对 wasmJs 的支持是**有限的**——`KClass` 基本可用，但 `KFunction`/反射调用（`call()`/`members`）在 wasmJs 下不可用或行为不同。
- **建议**：
  1. 审计每一处 `KClass`/`KFunction` 的**实际用途**：是仅做类型标记/Map 键，还是真的调用反射方法？
  2. 若仅做键：`KClass` 作为 Map 键在 wasmJs 可用，无需改动。
  3. 若需反射调用：抽出为 expect/actual，wasmJs 下用替代实现（字符串键 + 显式注册）。
  4. 考虑对 wasmJs target 标注 `@OptIn(ExperimentalReflectionOnLambdas::class)` 或相关 opt-in。

### 4.2 ✅ `arrow-kt`（`arrow-core` / `arrow-functions`）

- commonMain 通过 `api(libs.arrow.core)` 全平台依赖。
- **arrow-kt 自 2.0+ 支持 wasmJs**（需确认所用 2.1.2 版本已发布 wasmJs 变体）。
- **建议**：确认 `io.arrow-kt:arrow-core:2.1.2` 在 Maven Central 有 wasmJs klib；若没有，升级到已支持的版本。

### 4.3 ✅ `commonJvmAndroid` 中间源集

- 现有架构：`commonMain` → `commonJvmAndroid` → `androidMain` / `desktopMain`。
- wasmJs 应**直接依赖 `commonMain`**（不经过 commonJvmAndroid，因为 commonJvmAndroid 里的 `Types.jvm.kt` 和 `LegalCheck.kt` 是 JVM 专用的）。
- **建议**：检查 `commonJvmAndroid` 的 2 个文件中是否有内容是 wasmJs 也需要的——若有，需提取到 commonMain 或新建 common 层。

### 4.4 ✅ `kotlinx-datetime` / `kotlinx-coroutines` / `kotlinx-collections-immutable`

- 这三个均已发布 wasmJs 变体，无兼容性问题。

### 4.5 ✅ `usePersistent` / `useRequest` 的注入式架构

- 经核查，这两个看似"平台强相关"的 hook 实际采用了**依赖注入式设计**：
  - `usePersistent`：存储实现通过 `SaveToPersistent<T> = (T?) -> Unit` 函数类型注入，不绑定存储后端。
  - `useRequest`：网络请求通过 `serviceFunction` 参数注入，不绑定 HTTP 客户端。
- **结论**：这是良好的架构决策，使 wasmJs 适配成本大幅降低——只需在 wasmJs 源集提供默认的 `localStorage` 存储实现（或不提供默认，让调用方注入）。

---

## 五、验收标准（Definition of Done）

发布版本（如 `2.4.0`）满足以下全部条件即视为需求完成：

### 验收项

- [ ] **V1**：`hooks/build.gradle.kts` 中存在 `wasmJs { browser() }` target。
- [ ] **V2**：`./gradlew :hooks:publishToMavenLocal` 成功，且产物包含 wasmJs klib。
- [ ] **V3**：本地 Maven 中 hooks2 新版本的 `.module` 文件包含 `wasmJs` variant。
- [ ] **V4**：§3.1 列出的核心 hooks 在 wasmJs target 下编译通过（`./gradlew :hooks:compileKotlinWasmJs`）。
- [ ] **V5**：§3.1 核心 hooks 在 wasmJs 下功能正确（新增 `hooks/src/wasmJsTest` 测试，或文档化已验证）。
- [ ] **V6**：§3.2 的 hooks 要么在 wasmJs 可用，要么在 API 文档中**明确标注"wasmJs 不支持/降级"**。
- [ ] **V7**：**下游验证**——Palette 的 `palette` 模块在升级到该版本后，能成功添加 `wasmJs { browser() }` target 并编译（这是本需求的最终目的）。
- [ ] **V8**（可选，对应 R3）：同样验证 js target。

### 验证脚本（下游 Palette 侧）

```kotlin
// palette/build.gradle.kts —— 验证 V7
kotlin {
    // ... 现有 androidTarget / desktop / ios ...
    wasmJs { browser() }  // 添加此行后应能编译
}
```

```bash
# Palette 侧验证命令
./gradlew :palette:compileKotlinWasmJs
```

---

## 六、实施建议（供 hooks2 维护者参考）

### 6.1 推荐分阶段实施

**阶段 1：最小可用（满足 R1+R2）**
1. 在 `hooks/build.gradle.kts` 的 `kotlin {}` 添加：
   ```kotlin
   wasmJs { browser() }
   ```
2. 处理 `kotlin.reflect` 问题（§4.1）——这是唯一可能阻塞编译的点。
3. 编译验证 §3.1 核心 hooks，发布 `2.4.0-beta-1`。
4. Palette 侧做 V7 下游验证。

**阶段 2：完整覆盖（满足 R3+R4）**
5. 适配 §3.2 的 hooks（useRequest/useForm/useRedux 的反射问题）。
6. 为 §3.3 的 hooks 提供 wasmJs actual 实现（usePersistent 的 localStorage 适配）。
7. 发布正式版。

### 6.2 `kotlin.reflect` 处理策略（关键）

如果反射导致 wasmJs 编译失败，按以下优先级处理：

1. **首选**：将 `KClass<*>` 作为 Map 键的用法保留（wasmJs 支持），只移除真正的反射调用。
2. **次选**：对反射依赖的 hook，用 expect/actual 隔离：
   ```kotlin
   // commonMain
   expect fun <T : Any> typeKeyOf(cls: KClass<T>): Any

   // wasmJsMain —— 用 KClass 本身（支持作为键，不支持反射调用）
   actual fun <T : Any> typeKeyOf(cls: KClass<T>): Any = cls

   // jvmAndroidMain —— 可保留完整反射
   actual fun <T : Any> typeKeyOf(cls: KClass<T>): Any = cls
   ```
3. **兜底**：若某 hook（如 useRedux）反射依赖过深，在 wasmJs target 下标记为不可用（`@Deprecated` 或不导出），在文档明确说明。

### 6.3 版本与发布

- 建议发布为 **`2.4.0`**（新增 target 属于 minor 版本 bump）。
- 先发 `2.4.0-beta-1` 供 Palette 做 V7 验证，确认无问题后发正式版。

---

## 七、影响与优先级说明

- **对 hooks2 本身**：增加一个 target 的维护成本，但显著扩大生态（所有 CMP 项目可同时获得 Web 能力）。
- **对 Palette（本需求方）**：**这是 Palette 在线文档站（可交互预览）的唯一硬阻塞**。解决后 Palette 可立即推进 wasm 文档站建设。
- **对 hooks2 生态**：使 hooks2 从"Android/iOS/Desktop 三端"升级为"四端（含 Web）"，与主流 CMP 状态管理库（如其他 React-style hooks 实现）对齐。

---

## 八、联系与协作

- 需求提出方：Palette 组件库（同作者 junerver）
- 如需下游配合验证（V7），Palette 仓库已就绪，可随时在 `palette/build.gradle.kts` 添加 wasmJs target 做联调。
- 若维护者决定推进，建议先在 hooks2 仓库创建对应 issue/分支，双方协同验证。

---

## 附录：核查依据（已实地验证的事实）

| 事实 | 验证方式 | 结论 |
|---|---|---|
| hooks2 现有变体 | 检查 `~/.m2/.../hooks2/{2.2.2-beta-2,beta-3,2.3.0}/.module` | 仅 android/ios/jvm，无 wasmJs/js |
| Kotlin/Compose 版本 | `gradle/libs.versions.toml` | Kotlin 2.3.21 + Compose 1.11.1（均支持 wasmJs） |
| commonMain 文件数 | `find hooks/src/commonMain -name "*.kt" \| wc -l` | 111 个 |
| kotlin.reflect 使用 | `grep "kotlin.reflect" hooks/src/commonMain` | 11 处，10 个文件 |
| commonJvmAndroid 文件 | `find hooks/src/commonJvmAndroid` | 2 个（Types.jvm.kt, LegalCheck.kt） |
| usePersistent 架构 | 读 `UsePersistent.kt` + `Types.kt:106` | 注入式（SaveToPersistent 函数类型），非平台绑定 |
| useRequest 架构 | 读 `UseRequest.kt` | 注入式（serviceFunction 参数），非 HTTP 客户端绑定 |
| arrow-kt 依赖范围 | `hooks/build.gradle.kts:70-71` | commonMain `api` 依赖（全平台必需） |
