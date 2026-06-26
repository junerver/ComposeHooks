# ComposeHooks 架构文档

> 本文档记录 ComposeHooks 库在完成子包化重构后的架构设计、公共 API 约束和扩展指南。

## 1. 整体结构

```
hooks/src/commonMain/kotlin/xyz/junerver/compose/hooks/
├── Hooks.kt              # 门面：所有 useXxx/rememberXxx 包装 + typealias + createContext
├── Types.kt              # 基础类型（Reducer/Dispatch/Fn 别名 + Ref 委托转发 + SelectionMode）
├── Options.kt            # Options 框架（基类 + createOptions/useDynamicOptions）
├── Tuple.kt              # Tuple 解构支持
├── useContext.kt          # ReactContext/createContext 基础设施接口
│
├── useasync/             # useAsyncImpl, useCancelableAsyncImpl
├── useautoreset/         # useAutoResetImpl
├── usebackfront/         # useBackToFrontEffectImpl, useFrontToBackEffectImpl
├── useboolean/           # useBooleanImpl, BooleanHolder
├── useclipboard/         # useClipboardImpl, CopyPasteHolder
├── usecontext/           # useContextImpl, ReactContext, createContextImpl
├── usecontrollable/      # useControllableImpl, _useControllableImpl, ControllableHolder
├── usecountdown/         # useCountdownImpl, CountdownHolder, FormattedRes
├── usecounter/           # useCounterImpl, CounterHolder, UseCounterOptions
├── usecreation/          # useCreationImpl
├── usecyclelist/         # useCycleListImpl, CycleListHolder, UseCycleListOptions
├── usedateformat/        # useDateFormatImpl, UseDateFormatOptions, DateFormatMessages, etc.
├── usedebounce/          # useDebounceImpl/useDebounceFnImpl/useDebounceEffectImpl, UseDebounceOptions
├── useeffect/            # useEffectImpl
├── useevent/             # useEventSubscribeImpl/useEventPublishImpl (reified), HooksEventManager
├── usegetstate/          # useGetStateImpl/_useGetStateImpl, GetStateHolder
├── useimmutablelist/     # useImmutableListImpl, ImmutableListHolder
├── useinterval/          # useIntervalImpl, IntervalHolder, UseIntervalOptions
├── usekeyboard/          # useKeyboardImpl, KeyboardHolder
├── uselastchanged/       # useLastChangedImpl
├── uselatest/            # useLatestRefImpl, useLatestStateImpl
├── uselist/              # useListImpl, useListReduceImpl
├── usemap/               # useMapImpl
├── usemath/              # useAbsImpl/useCeilImpl/useFloorImpl/... (所有数学 hook)
├── usememoizedfn/        # useMemoizedFnImpl
├── usemount/             # useMountImpl
├── usenow/               # useNowImpl, UseNowOptions
├── usenumber/            # useDoubleImpl/useFloatImpl/useIntImpl/useLongImpl
├── usepausableeffect/    # usePausableEffectImpl, PausableEffectHolder
├── usepersistent/        # usePersistentImpl, PersistentHolder
├── useprevious/          # usePreviousImpl
├── usereducer/           # useReducerImpl, ReducerHolder
├── useredux/             # ReduxProvider, Store, createStore, useSelector, useDispatch
├── useref/               # Ref/MutableRef, getValue/setValue, useRefImpl, observeAsStateImpl
├── userequest/           # useRequest, Fetch, Plugin, useTableRequest, etc.
├── useresetstate/        # useResetStateImpl, ResetStateHolder
├── useselectable/        # useSelectableImpl, SelectableHolder, IsSelected, etc.
├── usesorted/            # useSortedImpl, UseSortedOptions
├── usestate/             # useStateImpl/_useStateImpl/useStateAsyncImpl, UseStateAsyncOptions
├── usestatemachine/      # useStateMachineImpl, StateMachineHolder, MachineGraph, etc.
├── usethrottle/          # useThrottleImpl/useThrottleFnImpl/useThrottleEffectImpl, UseThrottleOptions
├── usetimeago/           # useTimeAgoImpl, UseTimeAgoOptions, TimeAgoMessages, formatTimeAgo
├── usetimeout/           # useTimeoutImpl (deprecated)
├── usetimeoutfn/         # useTimeoutFnImpl, TimeoutFnHolder, UseTimeoutFnOptions
├── usetimeoutpoll/       # useTimeoutPollImpl, TimeoutPollHolder, UseTimeoutPollOptions
├── usetimestamp/         # useTimestampImpl/useTimestampRefImpl, TimestampHolder, UseTimestampOptions
├── usetoggle/            # useToggleImpl/useToggleEitherImpl/useToggleVisibleImpl
├── useundo/              # useUndoImpl, UndoHolder
├── useunmount/           # useUnmountImpl
├── useunmountedref/      # useUnmountedRefImpl
├── useupdate/            # useUpdateImpl
├── useupdateeffect/      # useUpdateEffectImpl
├── usses/                # useSse, SseHolder, UseSseOptions
├── useform/              # Form, FormInstance, FormScope, Validator, useForm, etc.
├── useable/              # useTable, TableHolder, TableInstance, ColumnDef, etc.
├── utils/                # HooksEventManager, CacheManager, ext, etc.
└── internal/             # internal utilities (genFormFieldKey, persistentKey, etc.)
```

**ai 模块**遵循相同模式，每个 hook 位于独立子包中，通过 `Ai.kt` 集中导出。

## 2. 集中导出模式

### 2.1 三类导出手段

`Hooks.kt` 和 `Ai.kt` 使用三种方式将子包公开 API 重新导出到根包：

#### (1) typealias — 类型重导出
```kotlin
import xyz.junerver.compose.hooks.useboolean.BooleanHolder as BooleanHolderImpl
typealias BooleanHolder = BooleanHolderImpl
```
适用于：Holder、Options、接口、sealed class（部分）、data class 等。

#### (2) val — object/常量重导出
```kotlin
import xyz.junerver.compose.hooks.usetimeago.DefaultEnglishTimeAgoMessages as DefaultEnglishTimeAgoMessagesImpl
val DefaultEnglishTimeAgoMessages: TimeAgoMessages = DefaultEnglishTimeAgoMessagesImpl
```
适用于：`object`（单例）、`val` 常量（如上下文对象）。

#### (3) 包装函数 — useXxx/rememberXxx 重导出
```kotlin
import xyz.junerver.compose.hooks.useboolean.useBooleanImpl

// 根包 useXxx 包装
@Composable
fun useBoolean(default: Boolean = false): BooleanHolder = useBooleanImpl(default)

// 根包 rememberXxx 包装
@Composable
fun rememberBoolean(default: Boolean = false): BooleanHolder = useBooleanImpl(default)
```
两者都直接调用 `Impl`，互不绕路。**必须保留 `@Composable fun` 完整包装**，因为：
- Kotlin 函数引用（`val x = ::useXxx`）会**丢失默认参数**
- `inline fun <reified T>` 函数无法通过 `::` 捕获

### 2.2 无法 typealias 的情况

| 情况 | 处理方式 | 示例 |
|---|---|---|
| 扩展运算符（`getValue`/`setValue`） | 根包直接声明 forwarding wrapper | `Types.kt` 中的 `operator fun Ref<T>.getValue` |
| `sealed class` 嵌套类访问 | sealed class 留在根包 | `SelectionMode` (sealed class 留在 `Types.kt`) |
| `@Composable` 跨模块匿名对象 | 根包直接实现匿名对象 | `createContext` (在 `Hooks.kt` 中直接实现) |
| `inline reified` 函数 | 保留 `inline` 包装器 | `useEventSubscribe`、`useDispatch` |

## 3. 根包保留的基础设施

| 文件 | 内容 | 原因 |
|---|---|---|
| `Types.kt` | `Reducer`/`Dispatch`/各种 `Fn` typealias；`Ref` 委托运算符；`SelectionMode` sealed class | 所有子包依赖；委托运算符无法跨包别名；sealed class 嵌套类无法 typealias |
| `Options.kt` | `Options<T>` 基类 + `createOptions` 分发 + `useDynamicOptions` | 14 个 hook 的 Options 框架核心 |
| `Tuple.kt` | `Tuple1`~`Tuple9`、`tuple()` | 解构赋值基础 |
| `useContext.kt` | `ReactContext<T>` 接口 + `ComposeComponent` typealias | 跨子包共享的基础设施 |

## 4. 子包迁移模式

每个 hook 从根包迁移至子包的步骤：

```
根包 useXxx.kt           子包 usexxx/UseXxx.kt
─────────────────        ─────────────────────────
fun useXxx(              fun useXxxImpl(
  default: Boolean = false  default: Boolean = false
): BooleanHolder {        ): BooleanHolder {
  val (state, setState,     val (state, setState,
    getState) = useGetState   getState) = useGetStateImpl(default)
    (default)               ↑ 调用也改为 Impl
  return remember { ... }   return remember { ... }
}                         }

                          需要额外导入：
                          - useGetStateImpl（从 usegetstate 子包）
                          - 根包类型（BooleanHolder、SetValueFn 等）
                          - 委托运算符（getValue/setValue）
                          - invoke 扩展（Either setter 使用时）
```

### 4.1 需要特殊处理的迁移陷阱

| 陷阱 | 原因 | 修复 |
|---|---|---|
| `setState(value)` Either 不匹配 | `_useGetState` 返回 `SetValueFn<SetterEither<T>>`；同包的 `invoke` 扩展无法自动解析 | 添加 `import xyz.junerver.compose.hooks.invoke` |
| `var x by useRef(...)` 委托失败 | `Ref` 的 `getValue`/`setValue` 是根包扩展 | 添加 `import xyz.junerver.compose.hooks.useref.getValue/setValue` |
| `var x by useState(...)` 委托失败 | `androidx.compose.runtime.getValue/setValue` 需要显式导入 | 添加 `import androidx.compose.runtime.getValue/setValue` |
| `var x by useCreation { }` 中的 `by` | `Ref` 的 `getValue` 同上 | 同上 |
| `useRef<Job?>(...)` 泛型调用未重命名 | sed 的 `\buseRef(` 无法匹配 `useRef<` | 单独处理 `useRef<` 和 `useRef {` 形式 |
| `internal` 函数跨包子包访问 | `Debounce`/`Throttle` 类在 `usedebounce`/`usethrottle` 中是 `internal` 的 | 测试文件需更新 import 路径 |
| `createContext` 跨模块 `@Composable` 错误 | Compose 编译器对 `typealias` 匿名对象的跨模块分析有限制 | `createContext` 在根包直接实现（不转发子包） |

## 5. 测试覆盖率

### 5.1 Kover 配置

- **版本**: Kover 0.9.2
- **模块**: `hooks` 和 `ai`
- **报告格式**: XML + HTML
- **门槛**: 80% 行覆盖率（`verifyCoverageBaseline`）

### 5.2 Gradle Task

| Task | 功能 |
|---|---|
| `./gradlew :hooks:koverHtmlReport` | 生成 HTML 覆盖率报告（浏览器查看） |
| `./gradlew :hooks:koverXmlReport` | 生成 XML 覆盖率报告 |
| `./gradlew :hooks:verifyCoverageBaseline` | 验证行覆盖率 >= 80% |
| `./gradlew :hooks:runCoverageChecks` | 一键：跑测试 + 生成报告 + 验证门槛 |

### 5.3 已知限制

部分协程测试（`FetchComprehensiveTest`、`FetchEdgeCaseTest` 等）在 Windows 环境下因 `UncompletedCoroutinesError` 而超时挂起。这是既有的测试基础设施问题，在 CI 环境（Linux/macOS）下应能正常运行。

## 6. 公共 API 约定

### 6.1 零破坏变更原则

**消费者从根包导入的所有符号必须始终可用**。验证方法：

```bash
# app 模块代码必须与重构前基线（6c9d611）完全一致（忽略空白）
diff --ignore-all-space <(git show 6c9d611:app/.../Xxx.kt | tr -d '\r') <(cat app/.../Xxx.kt | tr -d '\r')
```

消费者导入的根包符号包括：
- `useState`、`_useState`、`useRef`、`useEffect`、`useMount`、`useUnmount` 等所有 `useXxx` 函数
- `Ref`、`MutableRef`、`ReactContext` 等类型
- `getValue`、`setValue`、`observeAsState` 等委托扩展
- `createContext`、`useContext`、`useToggle` 等函数
- `Reducer`、`Dispatch`、`Middleware`、`ReducerHolder` 等类型别名

### 6.2 外部消费者导入路径

消费者应始终从根包导入：
```kotlin
import xyz.junerver.compose.hooks.useState        // ✅ 根包包装
import xyz.junerver.compose.hooks.usestate.useState // ❌ 不推荐（内部路径）
```

## 7. 构建命令

```bash
# 构建
./gradlew build
./gradlew :hooks:build

# 格式化（提交前必须）
./gradlew formatKotlin
./gradlew lintKotlin

# 测试
./gradlew test
./gradlew desktopTest
./gradlew :hooks:check

# 覆盖率
./gradlew :hooks:koverHtmlReport
./gradlew :hooks:runCoverageChecks

# 运行示例
./gradlew :app:run              # Desktop
./gradlew :app:installDebug     # Android

# 发布
./gradlew :hooks:publishToMavenLocal
```

## 8. 提交规范

使用 Gitmoji 格式：

```
[Gitmoji] [Module]: Short description
```

| Emoji | 用途 | 示例 |
|---|---|---|
| ✨ | 新功能 | `✨ [Hooks]: Add useToggleHolder` |
| 🐛 | Bug 修复 | `🐛 [Hooks]: Fix null in useCounter` |
| ♻️ | 重构 | `♻️ [Hooks]: Move useBoolean to subpackage` |
| 🩹 | 小修复 | `🩹 [Hooks]: Fix test imports` |
| 🎨 | 代码风格 | `🎨 [Hooks]: Unify filenames to PascalCase` |
| 🧪 | 测试 | `🧪 [Hooks]: Add Kover coverage` |
| 🔖 | 版本 | `🔖 [Release]: Prepare 2.3.0` |

保持 subject 50 字符以内，使用祈使句。
