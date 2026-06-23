# Jetpack Compose Audit Report

Target: E:\GitHub\ComposeHooks
Date: 2026-06-23
Scope: `hooks`, `ai`, `app` Compose/KMP source sets; Android release compiler diagnostics for `hooks` and `ai`.
Excluded from scoring: tests, generated/build outputs, and demo-only issues where they do not affect the reusable library surface.
Confidence: High
Overall Score: 86/100

## Scorecard

| Category | Score | Weight | Status | Notes |
|----------|-------|--------|--------|-------|
| Performance | 9/10 | 35% | excellent | Strong Skipping is on, measured named skippability is 100%, and the remaining mutable/demo lazy lists now use stable keys. |
| State management | 9/10 | 25% | excellent | Hooks mostly expose clear holder/state APIs; `FormRef` now uses private backing maps and explicit operations, app examples use `FormItemWithState`, and the deprecated legacy `FormItem` Triple API remains only for compatibility. |
| Side effects | 8/10 | 20% | solid | `useSse`, `useStateAsync`, `Fetch`, and AI streaming paths now rethrow cancellation; polling no longer forces `Dispatchers.Default`. |
| Composable API quality | 8/10 | 20% | solid | The audited extracted components now expose/root-apply `modifier`; a few demo-local private UI helpers remain lighter-weight than a full shared UI kit. |

## Critical Findings

No unresolved critical findings remain from the prioritized remediation pass. The remaining item is a compatibility cleanup opportunity for the deprecated form API.

## Remediation Status

1. **Fixed: cancellation is propagated from SSE and async-state hooks**
   - Evidence: `hooks/src/commonMain/kotlin/xyz/junerver/compose/hooks/usses/useSse.kt:107`, `hooks/src/commonMain/kotlin/xyz/junerver/compose/hooks/usses/useSse.kt:113`, `hooks/src/commonMain/kotlin/xyz/junerver/compose/hooks/useState.kt:146`.
   - References: <https://developer.android.com/kotlin/coroutines/coroutines-best-practices>, <https://developer.android.com/develop/ui/compose/side-effects>

2. **Fixed: polling no longer overrides the caller/hook dispatcher with `Dispatchers.Default`**
   - Evidence: `hooks/src/commonMain/kotlin/xyz/junerver/compose/hooks/userequest/plugins/usePollingPlugin.kt:89`.
   - References: <https://developer.android.com/develop/ui/compose/side-effects>, <https://developer.android.com/kotlin/coroutines/coroutines-best-practices>

3. **Fixed: audited extracted components now expose/root-apply `modifier`**
   - Evidence: `app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseChatExample.kt:432`, `app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseTtsExample.kt:428`, `app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseGenerateObjectExample.kt:445`, `app/src/commonMain/kotlin/xyz/junerver/composehooks/ui/component/DividerSpacer.kt:21`.
   - References: <https://developer.android.com/develop/ui/compose/api-guidelines>, <https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-component-api-guidelines.md>

4. **Fixed: remaining mutable/demo lazy lists now use stable keys**
   - Evidence: `app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseImmutableListExample.kt:224`, `app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseSelectableExample.kt:75`.
   - References: <https://developer.android.com/develop/ui/compose/lists>

5. **Fixed: `FormRef` internal mutation surfaces are now explicit operations**
   - Evidence: `hooks/src/commonMain/kotlin/xyz/junerver/compose/hooks/useform/FormRef.kt:23`, `hooks/src/commonMain/kotlin/xyz/junerver/compose/hooks/useform/FormRef.kt:57`, `hooks/src/commonMain/kotlin/xyz/junerver/compose/hooks/useform/FormRef.kt:84`, `hooks/src/commonMain/kotlin/xyz/junerver/compose/hooks/useform/FormInstance.kt:62`, `hooks/src/commonMain/kotlin/xyz/junerver/compose/hooks/useform/FormInstance.kt:100`.
   - References: <https://developer.android.com/develop/ui/compose/state>, <https://developer.android.com/develop/ui/compose/state-hoisting>

6. **Fixed: app form examples were migrated off the deprecated `FormItem` overload**
   - Evidence: `app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseFormExample.kt:100`, `app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseFormExample.kt:120`, `app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseFormExample.kt:339`, `app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseFormExample.kt:450`; source search shows no actual `FormItem` calls under `app/src` or `ai/src`.
   - References: <https://developer.android.com/develop/ui/compose/state>, <https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-component-api-guidelines.md>

## Adjacent Findings

### Android Launch UX

- Android 12+ splash icon status: not configured.
- Evidence: no `windowSplashScreenAnimatedIcon` item was found under `app/src`, `hooks/src`, or `ai/src`; launcher icons are adaptive/static launcher resources only.
- Finding: no Android 12+ splash-icon blur finding.
- References: <https://developer.android.com/develop/ui/views/launch/splash-screen>, <https://developer.android.com/reference/androidx/core/splashscreen/SplashScreen>, <https://issuetracker.google.com/issues/520672537>

## Category Details

### Performance - 9/10

**Ceiling check**

- Strong Skipping: on for measured modules (`gradle/libs.versions.toml:3` uses Kotlin `2.3.21`; module reports show `featureFlags.StrongSkipping = true`).
- Ceiling table applied: SSM-on.
- Module-wide `skippable%`: `ai` 4/4 = 100.0%; `hooks` 51/51 = 100.0%.
- Named-only `skippable%`: `ai` 3/3 = 100.0%; `hooks` 46/46 = 100.0%.
- Unstable shared types from compiler: `ai` 82 inferred unstable classes; `hooks` 38 inferred unstable classes. Under SSM-on this count is not a cap by itself.
- SSM-on binding evidence: no widespread source churn in measured `hooks` / `ai` composable bodies; previously flagged mutable/demo lazy-list keys have been fixed.
- Qualitative score: 9/10.
- Ceiling: none. Remaining demo-level small allocations do not bind the SSM-on ceiling.
- Applied score: 9/10.

**What is working**

- The Android release compiler reports for `hooks` and `ai` are clean on named skippability.
- Previously risky app examples now use stable keys in the chat/agent/table message lists, table rows, immutable-list demo, and selectable demo.
- `useTable` now keeps a stable `TableInstance` and returns stable state wrappers, reducing holder churn in the reusable library surface.

**What is hurting the score**

- A few composable bodies still allocate small option lists or perform string aggregation for display. They are sample-level, not systemic.

**Animation performance signals**

- Status: clean in audited hotspots. No `Animatable`-without-`remember` or composition-phase animated modifier reads were found in the library surface. `animateContentSize()` usage in cards/messages is ordinary declarative animation.

**Evidence**

- `hooks/build/compose_audit/hooks_release-composables.csv` - 46/46 named restartable composables are skippable under SSM-on. References: <https://developer.android.com/develop/ui/compose/performance/tooling>, <https://developer.android.com/develop/ui/compose/performance/stability/strongskipping>
- `ai/build/compose_audit/ai_release-composables.csv` - 3/3 named restartable composables are skippable under SSM-on. References: <https://developer.android.com/develop/ui/compose/performance/tooling>, <https://developer.android.com/develop/ui/compose/performance/stability/strongskipping>
- `app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseImmutableListExample.kt:224` - duplicate-prone random values are wrapped in `DemoImmutableItem(id, value)` and rendered with `key = { item.id }`. References: <https://developer.android.com/develop/ui/compose/lists>
- `app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseSelectableExample.kt:75` - selectable demo renders with `key = { demo.userId }`. References: <https://developer.android.com/develop/ui/compose/lists>

### State Management - 9/10

**What is working**

- Hook holders consistently expose observable `State` fields first and operations after them, matching the repository's own hook API standard.
- `useTable` and `useRequest` keep table/request state in holder abstractions rather than scattering ownership through UI examples.
- AI hooks use immutable message lists (`ImmutableList` / persistent collections) for chat-like state, which is a strong signal for observable state boundaries.
- `FormRef` now keeps its mutable maps as private backing state and exposes named internal operations for registration, validation, errors, touched/dirty state, pending validation, and submit callbacks.
- The app showcase now uses `FormItemWithState` in its form examples instead of demonstrating the deprecated Triple overload.

**What is hurting the score**

- The legacy `FormItem` Triple API remains for external compatibility, although it now has a deprecation warning pointing callers to `FormItemWithState`. Removing it would be a breaking API change and should be handled as a planned migration.

**Evidence**

- `hooks/src/commonMain/kotlin/xyz/junerver/compose/hooks/useform/Form.kt:177` - legacy `FormItem` is deprecated and directs callers to `FormItemWithState`. References: <https://developer.android.com/develop/ui/compose/state>, <https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-component-api-guidelines.md>
- `hooks/src/commonMain/kotlin/xyz/junerver/compose/hooks/useform/Form.kt:216` - newer `FormItemWithState` narrows usage through `FormItemState`, which is the better direction. References: <https://developer.android.com/develop/ui/compose/state-hoisting>
- `hooks/src/commonMain/kotlin/xyz/junerver/compose/hooks/useform/FormRef.kt:23` - field state is now held in a private backing map rather than a mutable map property exposed to the rest of the module.
- `hooks/src/commonMain/kotlin/xyz/junerver/compose/hooks/useform/FormRef.kt:57` - read access is exposed as a read-only `Map<String, State<Any?>>`.
- `hooks/src/commonMain/kotlin/xyz/junerver/compose/hooks/useform/FormRef.kt:84` - field registration is explicit through `registerField`.
- `hooks/src/commonMain/kotlin/xyz/junerver/compose/hooks/useform/FormRef.kt:110` - validation state is updated through `setValidation`.
- `hooks/src/commonMain/kotlin/xyz/junerver/compose/hooks/useform/FormRef.kt:120` - error state is updated through `setErrorMessages`.
- `hooks/src/commonMain/kotlin/xyz/junerver/compose/hooks/useform/FormRef.kt:183` - submit callback registration is explicit through `setOnSubmitCallback`. References: <https://developer.android.com/develop/ui/compose/state>, <https://developer.android.com/develop/ui/compose/state-hoisting>
- `app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseFormExample.kt:100` - app examples now use `FormItemWithState` for ordinary fields.
- `app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseFormExample.kt:450` - watched-field examples also use `FormItemWithState`; there are no remaining actual `FormItem` calls under `app/src` or `ai/src`.
- `ai/src/commonMain/kotlin/xyz/junerver/compose/ai/usechat/useChat.kt:69` - chat state uses `State<ImmutableList<ChatMessage>>`, which is a positive observable/immutable state signal. References: <https://developer.android.com/develop/ui/compose/state>, <https://developer.android.com/develop/ui/compose/performance/stability/fix>

### Side Effects - 8/10

**What is working**

- `Fetch`, `useSse`, and `useStateAsync` now rethrow `CancellationException` from broad exception paths.
- AI streaming hooks reviewed here (`useAsr`, `useTts`, `useChat`, `AgentLoop`) mostly rethrow cancellation before converting errors to UI state.
- Long-lived async work generally starts from event callbacks or Compose-owned scopes rather than directly from composition bodies.
- Polling delayed refresh now stays in the selected hook/plugin scope instead of unconditionally switching to `Dispatchers.Default`.

**What is hurting the score**

- Some plugin and callback surfaces still use broad exception isolation by design, so future async additions should keep the `CancellationException` rethrow rule as a regression guard.

**Animation side-effect signals**

- Status: clean. No target-driven `Animatable.animateTo()` launched directly from composition was found in audited library paths.

**Evidence**

- `hooks/src/commonMain/kotlin/xyz/junerver/compose/hooks/usses/useSse.kt:113` - cancellation is rethrown before converting stream failures to hook error state. References: <https://developer.android.com/kotlin/coroutines/coroutines-best-practices>, <https://developer.android.com/develop/ui/compose/side-effects>
- `hooks/src/commonMain/kotlin/xyz/junerver/compose/hooks/userequest/plugins/usePollingPlugin.kt:89` - delayed polling uses `usedScope.launch` without an unconditional dispatcher override. References: <https://developer.android.com/develop/ui/compose/side-effects>, <https://developer.android.com/kotlin/coroutines/coroutines-best-practices>
- `hooks/src/commonMain/kotlin/xyz/junerver/compose/hooks/useState.kt:146` - `useStateAsync` rethrows `CancellationException` before handling normal exceptions. References: <https://developer.android.com/kotlin/coroutines/coroutines-best-practices>
- `hooks/src/commonMain/kotlin/xyz/junerver/compose/hooks/userequest/Fetch.kt:196` - broad catch now rethrows cancellation, a positive fix pattern to reuse. References: <https://developer.android.com/kotlin/coroutines/coroutines-best-practices>
- `ai/src/commonMain/kotlin/xyz/junerver/compose/ai/usechat/useChat.kt:466` - broad catch in the main chat path rethrows `CancellationException`, showing the desired pattern is already present in adjacent code. References: <https://developer.android.com/kotlin/coroutines/coroutines-best-practices>

### Composable API Quality - 8/10

**What is working**

- Core shared app components such as `TButton`, `SimpleContainer`, `ExampleCard`, `LogCard`, and `ScrollColumn` expose `modifier: Modifier = Modifier`.
- Several selector/card/message components now pass the modifier to their root node.
- The hook library intentionally returns non-UI holders from composable hook functions; that is a domain-specific API shape and was not treated as a generic UI-emitting composable violation.

**What is hurting the score**

- Some private demo-only UI helpers remain intentionally compact and do not yet follow full shared-component API rigor.

**Evidence**

- `app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseChatExample.kt:432` - `ChatMessageBubble` now accepts `modifier: Modifier = Modifier` and applies it to the root `Row`. References: <https://developer.android.com/develop/ui/compose/api-guidelines>, <https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-component-api-guidelines.md>
- `app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseTtsExample.kt:428` - `VoiceSelector` now accepts and root-applies `modifier`. References: <https://developer.android.com/develop/ui/compose/api-guidelines>, <https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-component-api-guidelines.md>
- `app/src/commonMain/kotlin/xyz/junerver/composehooks/example/UseGenerateObjectExample.kt:445` - `RecipeCard` now accepts and root-applies `modifier`. References: <https://developer.android.com/develop/ui/compose/api-guidelines>, <https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-component-api-guidelines.md>
- `app/src/commonMain/kotlin/xyz/junerver/composehooks/ui/component/DividerSpacer.kt:21` - `modifier` is now the first optional parameter. References: <https://developer.android.com/develop/ui/compose/api-guidelines>, <https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-component-api-guidelines.md>

## Remaining Non-Blocking Follow-Up

1. Remove the deprecated legacy `FormItem` overload in a future breaking release after external consumers have had a migration window. References: <https://developer.android.com/develop/ui/compose/state>, <https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-component-api-guidelines.md>
2. Keep cancellation propagation as a regression rule for future hooks: any new broad `catch (Exception)` / `catch (Throwable)` around suspend work should rethrow `CancellationException` first. References: <https://developer.android.com/kotlin/coroutines/coroutines-best-practices>

## Notes And Limits

- `COMPOSE-AUDIT-REPORT.md` was deleted before this audit and recreated with current findings.
- Prioritized remediation was applied after the audit: `useSse`, `useStateAsync`, polling, audited `modifier` APIs, mutable/demo lazy-list keys, `FormRef` internal mutation surfaces, and app `FormItemWithState` migration were fixed.
- The Gradle build with the audit init script completed successfully for `:hooks:compileReleaseKotlinAndroid`, `:ai:compileReleaseKotlinAndroid`, and `:app:compileReleaseKotlinAndroid` after the final form refactor.
- Latest compiler diagnostics: named restartable skippability remains `hooks` 46/46 and `ai` 3/3.
- Post-remediation verification completed successfully: `formatKotlin`, `lintKotlin`, `:hooks:compileReleaseKotlinAndroid`, `:app:compileReleaseKotlinAndroid`, `:hooks:compileTestKotlinDesktop`, `:hooks:desktopTest --tests xyz.junerver.compose.hooks.test.UseSseTest`, and the targeted form tests (`UseFormRefTest`, `UseFormInstanceTest`, `UseFormSubmitTest`, `UseFormTouchedDirtyTest`, `UseFormValidationTriggerTest`).
- A strict source search found no remaining direct assignments/removals through the old `FormRef` map properties in production or form tests, and no remaining actual `FormItem` calls under `app/src` or `ai/src`.
- Compiler diagnostics used: yes, but only `hooks` and `ai` produced files under `build/compose_audit`; `app` was audited from source because no `app/build/compose_audit` report was generated.
- Strong Skipping mode: on in measured modules (`featureFlags.StrongSkipping = true`).
- Weight choice: default 35/25/20/20.
- Renormalization: none.
- Adjacent coverage notes: this is a Compose Multiplatform/KMP repository with `commonMain`, Android, Desktop, and iOS source sets. Focus/keyboard and UI-test coverage were noted but not scored in this skill.
- Android Launch UX resources: no splash-screen theme item found; no static splash icon risk was scored or prioritized.

## Suggested Follow-Up

- `material-3` audit is optional; this skill did not score visual design or Material 3 token quality.
- Run `compose-agent focus on kmp` if platform-boundary quality is the next priority; this repo has significant CMP/KMP surface.
- Run `compose-agent focus on testing` if UI test, preview, and screenshot coverage should be assessed beyond the source-level audit.
- Run `compose-agent focus on focus` only if Desktop/keyboard behavior becomes a product requirement beyond the current demo coverage.
