# 测试覆盖率提升 + CI 质量检测 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use compose:subagent (recommended) or compose:execute to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 hooks 模块的行覆盖率从 53.5% 提升到 80%+，并添加 CI 质量检测流水线

**Architecture:** 分两阶段：1) 补充测试用例提升覆盖率；2) 添加 GitHub Actions CI 配置

**Tech Stack:** Kotlin, Kover, GitHub Actions, Gradle

## 当前状态

| 指标 | 当前覆盖率 | 目标 | 差距 |
|------|-----------|------|------|
| Line | 53.5% (2540/4745) | 80% | +1256 行 |
| Method | 41.1% (685/1665) | - | - |
| Branch | 27% (711/2631) | - | - |

## 全局约束

- 测试文件放在 `hooks/src/commonTest/kotlin/xyz/junerver/compose/hooks/test/`
- 使用 `kotlin.test` 和 `kotlinx.coroutines.test`
- 遵循 TDD 原则：先写测试，再验证
- 每个任务完成后运行 `.\gradlew.bat :hooks:jvmTest` 验证

---

## Phase 1: CI 配置（先建立质量门禁）

### Task 1: 添加 runQualityChecks 任务到 build.gradle.kts

**Files:**
- Modify: `hooks/build.gradle.kts:191-195`

**Interfaces:**
- Produces: `runQualityChecks` Gradle task

- [ ] **Step 1: 添加 runQualityChecks 任务**

在 `hooks/build.gradle.kts` 的 `runCoverageChecks` 任务后面添加：

```kotlin
tasks.register("runQualityChecks") {
    group = "verification"
    description = "Run scoped static checks for baseline test/quality tasks."
    dependsOn(
        "ktlintCommonTestSourceSetCheck",
        "ktlintAndroidUnitTestSourceSetCheck",
        "ktlintAndroidInstrumentedTestSourceSetCheck",
    )
}

tasks.register("verifyReleaseReadiness") {
    group = "verification"
    description = "Run quality checks and all tests before release."
    dependsOn("runQualityChecks", "runCoverageChecks")
}
```

- [ ] **Step 2: 验证任务可执行**

Run: `.\gradlew.bat :hooks:tasks --group=verification`
Expected: 看到 `runQualityChecks` 和 `verifyReleaseReadiness` 任务

- [ ] **Step 3: Commit**

```bash
git add hooks/build.gradle.kts
git commit -m "✨ [CI]: Add runQualityChecks and verifyReleaseReadiness tasks"
```

---

### Task 2: 创建 GitHub Actions CI workflow

**Files:**
- Create: `.github/workflows/ci.yml`

**Interfaces:**
- Consumes: `runQualityChecks`, `runCoverageChecks`, `build` Gradle tasks

- [ ] **Step 1: 创建 ci.yml**

创建文件 `.github/workflows/ci.yml`：

```yaml
name: CI

on:
  push:
    branches: [ master, main, develop ]
  pull_request:
    branches: [ master, main ]

jobs:
  quality:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'
          cache: gradle

      - name: Grant gradlew execute permission
        run: chmod +x ./gradlew

      - name: Run static checks
        run: ./gradlew :hooks:runQualityChecks --no-daemon

  tests:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'
          cache: gradle

      - name: Grant gradlew execute permission
        run: chmod +x ./gradlew

      - name: Run tests with coverage gate
        run: ./gradlew :hooks:runCoverageChecks --no-daemon

  build:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'
          cache: gradle

      - name: Grant gradlew execute permission
        run: chmod +x ./gradlew

      - name: Build library
        run: ./gradlew :hooks:build --no-daemon
```

- [ ] **Step 2: Commit**

```bash
git add .github/workflows/ci.yml
git commit -m "✨ [CI]: Add GitHub Actions CI workflow with quality gates"
```

---

## Phase 2: 补充测试用例（按优先级排序）

### Task 3: usemath 测试（0% → 80%+）

**Covers:** 最大体量的未覆盖包（4213 指令）

**Files:**
- Create: `hooks/src/commonTest/kotlin/xyz/junerver/compose/hooks/test/UseMathTest.kt`

- [ ] **Step 1: 探索 usemath 包结构**

查看 `hooks/src/commonMain/kotlin/xyz/junerver/compose/hooks/usemath/` 目录，了解有哪些函数需要测试

- [ ] **Step 2: 创建 UseMathTest.kt**

```kotlin
package xyz.junerver.compose.hooks.test

import kotlin.test.Test
import kotlin.test.assertEquals
import xyz.junerver.compose.hooks.usemath.*

class UseMathTest {
    // 根据实际 API 填充测试用例
    // 重点测试：
    // 1. 基础数学运算
    // 2. 边界条件（溢出、除零等）
    // 3. 各种数值类型转换
}
```

- [ ] **Step 3: 运行测试验证**

Run: `.\gradlew.bat :hooks:jvmTest --tests "*.UseMathTest"`
Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add hooks/src/commonTest/kotlin/xyz/junerver/compose/hooks/test/UseMathTest.kt
git commit -m "🧪 [Test]: Add usemath tests"
```

---

### Task 4: usetimestamp 测试（0% → 80%+）

**Files:**
- Create: `hooks/src/commonTest/kotlin/xyz/junerver/compose/hooks/test/UseTimestampTest.kt`

- [ ] **Step 1: 探索 usetimestamp 包结构**

查看 `hooks/src/commonMain/kotlin/xyz/junerver/compose/hooks/usetimestamp/`

- [ ] **Step 2: 创建 UseTimestampTest.kt**

```kotlin
package xyz.junerver.compose.hooks.test

import kotlin.test.Test
import kotlin.test.assertTrue
import xyz.junerver.compose.hooks.usetimestamp.*

class UseTimestampTest {
    // 测试重点：
    // 1. 时间戳获取和转换
    // 2. 不同时间单位（毫秒、秒等）
    // 3. 边界条件
}
```

- [ ] **Step 3: 运行测试验证**

Run: `.\gradlew.bat :hooks:jvmTest --tests "*.UseTimestampTest"`
Expected: PASS

- [ ] **Step 4: Commit**

---

### Task 5: usenetwork 测试（0% → 80%+）

**Files:**
- Create: `hooks/src/commonTest/kotlin/xyz/junerver/compose/hooks/test/UseNetworkTest.kt`

- [ ] **Step 1: 探索 usenetwork 包结构**

查看 `hooks/src/commonMain/kotlin/xyz/junerver/compose/hooks/usenetwork/`

- [ ] **Step 2: 创建 UseNetworkTest.kt**

```kotlin
package xyz.junerver.compose.hooks.test

import kotlin.test.Test
import xyz.junerver.compose.hooks.usenetwork.*

class UseNetworkTest {
    // 测试重点：
    // 1. 网络状态监听
    // 2. 连接类型判断
    // 3. 状态变化回调
}
```

- [ ] **Step 3: 运行测试验证**

Run: `.\gradlew.bat :hooks:jvmTest --tests "*.UseNetworkTest"`
Expected: PASS

- [ ] **Step 4: Commit**

---

### Task 6: usebackfront 测试（0% → 80%+）

**Files:**
- Create: `hooks/src/commonTest/kotlin/xyz/junerver/compose/hooks/test/UseBackFrontTest.kt`

- [ ] **Step 1: 探索 usebackfront 包结构**

- [ ] **Step 2: 创建测试文件**

- [ ] **Step 3: 运行测试验证**

Run: `.\gradlew.bat :hooks:jvmTest --tests "*.UseBackFrontTest"`
Expected: PASS

- [ ] **Step 4: Commit**

---

### Task 7: useimmutablelist 测试（0% → 80%+）

**Files:**
- Create: `hooks/src/commonTest/kotlin/xyz/junerver/compose/hooks/test/UseImmutableListTest.kt`

- [ ] **Step 1: 探索包结构**

- [ ] **Step 2: 创建测试文件**

- [ ] **Step 3: 运行测试验证**

Run: `.\gradlew.bat :hooks:jvmTest --tests "*.UseImmutableListTest"`
Expected: PASS

- [ ] **Step 4: Commit**

---

### Task 8: usesorted 测试（0% → 80%+）

**Files:**
- Create: `hooks/src/commonTest/kotlin/xyz/junerver/compose/hooks/test/UseSortedTest.kt`

- [ ] **Step 1-4: 同上模式**

---

### Task 9: usedeviceinfo 测试（0% → 80%+）

**Files:**
- Create: `hooks/src/commonTest/kotlin/xyz/junerver/compose/hooks/test/UseDeviceInfoTest.kt`

- [ ] **Step 1-4: 同上模式**

---

### Task 10: usenow 测试（0% → 80%+）

**Files:**
- Create: `hooks/src/commonTest/kotlin/xyz/junerver/compose/hooks/test/UseNowTest.kt`

- [ ] **Step 1-4: 同上模式**

---

### Task 11: usepausableeffect 测试（0% → 80%+）

**Files:**
- Create: `hooks/src/commonTest/kotlin/xyz/junerver/compose/hooks/test/UsePausableEffectTest.kt`

- [ ] **Step 1-4: 同上模式**

---

### Task 12: usevibrate 测试（0% → 80%+）

**Files:**
- Create: `hooks/src/commonTest/kotlin/xyz/junerver/compose/hooks/test/UseVibrateTest.kt`

- [ ] **Step 1-4: 同上模式**

---

### Task 13: usetimeout 测试（0% → 80%+）

**Files:**
- Create: `hooks/src/commonTest/kotlin/xyz/junerver/compose/hooks/test/UseTimeoutTest.kt`

- [ ] **Step 1-4: 同上模式**

---

### Task 14: useidle 测试（0% → 80%+）

**Files:**
- Create: `hooks/src/commonTest/kotlin/xyz/junerver/compose/hooks/test/UseIdleTest.kt`

- [ ] **Step 1-4: 同上模式**

---

### Task 15: 小包批量补充（usekeyboard, useclipboard, useunmountedref, uselastchanged, usememoizedfn）

**Files:**
- Create: `hooks/src/commonTest/kotlin/xyz/junerver/compose/hooks/test/SmallHooksTest.kt`

- [ ] **Step 1: 创建合并测试文件**

将 5 个小包的测试放在一个文件中

- [ ] **Step 2: 运行测试验证**

Run: `.\gradlew.bat :hooks:jvmTest --tests "*.SmallHooksTest"`
Expected: PASS

- [ ] **Step 3: Commit**

---

### Task 16: useselectable 测试（0% → 80%+）

**Files:**
- Create: `hooks/src/commonTest/kotlin/xyz/junerver/compose/hooks/test/UseSelectableTest.kt`

- [ ] **Step 1-4: 同上模式**

---

### Task 17: usestate 分支覆盖提升（29.4% → 80%+）

**Files:**
- Modify: `hooks/src/commonTest/kotlin/xyz/junerver/compose/hooks/test/` (可能需要新建)

- [ ] **Step 1: 分析未覆盖的分支**

查看 Kover HTML 报告中 usestate 的详细覆盖情况

- [ ] **Step 2: 补充测试用例**

重点测试各种状态更新场景和边界条件

- [ ] **Step 3: 运行测试验证**

- [ ] **Step 4: Commit**

---

### Task 18: useevent 测试补充（35% → 80%+）

**Files:**
- Create/Modify: `hooks/src/commonTest/kotlin/xyz/junerver/compose/hooks/test/UseEventTest.kt`

- [ ] **Step 1-4: 同上模式**

---

### Task 19: useform 分支覆盖提升（61.2% → 80%+）

**Files:**
- Modify: `hooks/src/commonTest/kotlin/xyz/junerver/compose/hooks/test/UseForm*Test.kt`

- [ ] **Step 1: 分析未覆盖的分支**

- [ ] **Step 2: 补充验证器和边界条件测试**

- [ ] **Step 3: 运行测试验证**

- [ ] **Step 4: Commit**

---

### Task 20: useredux 测试补充（32.1% → 80%+）

**Files:**
- Modify: `hooks/src/commonTest/kotlin/xyz/junerver/compose/hooks/test/UseReduxTest.kt`

- [ ] **Step 1: 分析现有测试**

- [ ] **Step 2: 补充 reducer 和 middleware 测试**

- [ ] **Step 3: 运行测试验证**

- [ ] **Step 4: Commit**

---

### Task 21: usethrottle 测试补充（46.8% → 80%+）

**Files:**
- Modify: `hooks/src/commonTest/kotlin/xyz/junerver/compose/hooks/test/UseThrottleComprehensiveTest.kt`

- [ ] **Step 1-4: 同上模式**

---

### Task 22: usedebounce 测试补充（57.5% → 80%+）

**Files:**
- Modify: `hooks/src/commonTest/kotlin/xyz/junerver/compose/hooks/test/UseDebounceComprehensiveTest.kt`

- [ ] **Step 1-4: 同上模式**

---

### Task 23: usetimeago 测试补充（23.7% → 80%+）

**Files:**
- Create/Modify: `hooks/src/commonTest/kotlin/xyz/junerver/compose/hooks/test/UseTimeAgoTest.kt`

- [ ] **Step 1-4: 同上模式**

---

### Task 24: usepersistent 测试补充（14.6% → 80%+）

**Files:**
- Create: `hooks/src/commonTest/kotlin/xyz/junerver/compose/hooks/test/UsePersistentTest.kt`

- [ ] **Step 1-4: 同上模式**

---

## Phase 3: 验证与收尾

### Task 25: 运行完整覆盖率检查

- [ ] **Step 1: 生成覆盖率报告**

Run: `.\gradlew.bat :hooks:koverHtmlReport`

- [ ] **Step 2: 验证覆盖率达标**

检查报告，确认 Line 覆盖率 ≥ 80%

- [ ] **Step 3: 运行覆盖率门禁**

Run: `.\gradlew.bat :hooks:runCoverageChecks`
Expected: PASS（如果覆盖率达标）

- [ ] **Step 4: Commit**

```bash
git add .
git commit -m "✅ [Test]: Achieve 80%+ line coverage"
```

---

### Task 26: 最终验证

- [ ] **Step 1: 运行完整 CI 检查**

Run: `.\gradlew.bat :hooks:verifyReleaseReadiness`
Expected: PASS

- [ ] **Step 2: 推送到远程**

```bash
git push origin main
```

---

## 覆盖率提升预估

| 包名 | 当前覆盖率 | 指令数 | 预计新增覆盖行 |
|------|-----------|--------|---------------|
| usemath | 0% | 4213 | ~200 |
| usetimestamp | 0% | 710 | ~50 |
| usenetwork | 0% | 729 | ~100 |
| usebackfront | 0% | 391 | ~15 |
| useimmutablelist | 0% | 379 | ~13 |
| usesorted | 0% | 421 | ~31 |
| usedeviceinfo | 0% | 360 | ~35 |
| usenow | 0% | 284 | ~18 |
| usepausableeffect | 0% | 245 | ~20 |
| usevibrate | 0% | 197 | ~17 |
| usetimeout | 0% | 165 | ~4 |
| useidle | 0% | 421 | ~52 |
| 其他小包 | 0% | ~370 | ~30 |
| usestate 提升 | 29.4% | 547 | +24 |
| useevent 提升 | 35% | 379 | +13 |
| useform 提升 | 61.2% | 2310 | +80 |
| useredux 提升 | 32.1% | 965 | +55 |
| usethrottle 提升 | 46.8% | 1041 | +39 |
| usedebounce 提升 | 57.5% | 1120 | +45 |
| usetimeago 提升 | 23.7% | 877 | +69 |
| usepersistent 提升 | 14.6% | 377 | +33 |
| **总计** | | | **~945** |

**预估最终覆盖率:** (2540 + 945) / 4745 ≈ **73.4%**

> 注意：要达到 80% 需要更深入的测试，特别是 usemath 和 useform 这种体量大的包。

---

## 执行顺序建议

1. **先完成 Task 1-2**（CI 配置），建立质量门禁
2. **然后按指令数从大到小补充测试**：usemath → usetimestamp → usenetwork → ...
3. **最后提升现有测试的分支覆盖**
4. **每完成 5 个 Task 运行一次覆盖率检查**，确保进度
