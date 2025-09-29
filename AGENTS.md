# Repository Guidelines

## Project Structure & Module Organization
ComposeHooks is split into the `hooks` Kotlin Multiplatform library and the `app` multiplatform showcase client. Library sources live in `hooks/src/*` with platform folders like `commonMain`, `commonTest`, `androidMain`, `desktopMain`, and the `ios*` targets. Example screens and demo scaffolding reside under `app/src/...`, mirroring the platform folders, while shared assets and docs sit in `art/` and `docs/`. Gradle wiring remains at the root (`build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`) alongside contributor resources.

## Build, Test, and Development Commands
- `./gradlew :hooks:build` compiles the library for every configured publication target and runs platform compilation checks.
- `./gradlew :app:run` launches the desktop sample; pass `-Pcompose.desktop.target` when you need a specific runtime.
- `./gradlew lintKotlin` executes kotlinter across modules; auto-fix formatting with `./gradlew formatKotlin`.
- `./gradlew :hooks:publishToMavenLocal` validates the Maven Central publication pipeline before cutting a release.

## Coding Style & Naming Conventions
Indent with four spaces and keep Kotlin lines under 140 characters as enforced by `.editorconfig`. Use Kotlin Official formatting with trailing commas enabled and never introduce wildcard imports. Hook results must expose an `XxxHolder` where stable `State<T>` values appear before helper lambdas, reusing existing hooks (`useState`, `useEffect`, `useRef`, `useCreation`) instead of raw Compose primitives. Include the standard header comment block in every new Kotlin file and favor descriptive, imperative function names.

## Testing Guidelines
Add shared coverage in `hooks/src/commonTest/kotlin` with `kotlin.test` and `kotlinx.coroutines.test`; desktop-only verifications belong in `hooks/src/desktopTest`. UI behavior should use Compose testing APIs in the relevant platform source set. Run `./gradlew :hooks:check` before opening a PR, and complement feature work with focused tests. Android instrumentation lives in `hooks/src/androidInstrumentedTest` and can be executed on a device or emulator via `./gradlew :hooks:connectedDebugAndroidTest`.

## Commit & Pull Request Guidelines
Prefix commits with a Gitmoji and module tag, such as `:sparkles: [Hooks]: Add useToggleHolder`, keeping the subject under 50 characters and in present tense. Expand on context in the body when behavior is non-trivial and reference issues after the first line. Pull requests should summarize the change, link related issues, note verification commands, and include screenshots or recordings for UI-facing updates. Keep PR scope tight, ensure CI passes, and request review only when the branch is green.
