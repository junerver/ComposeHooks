@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package xyz.junerver.compose.ai

/**
 * Get current timestamp in milliseconds.
 * wasmJs implementation: delegates to the browser's `Date.now()` via JS interop,
 * which is the native high-resolution wall clock available in the browser sandbox.
 *
 * We deliberately avoid `kotlinx.datetime.Clock.System` here: its companion
 * accessor does not resolve cleanly across every wasmJs/datetime combination,
 * whereas `Date.now()` is the most direct, dependency-free source of epoch millis.
 */
internal actual fun currentTimestamp(): Long = jsDateNow()

/** Thin JS interop shim around `Date.now()`. */
private fun jsDateNow(): Long = js("Date.now()")
