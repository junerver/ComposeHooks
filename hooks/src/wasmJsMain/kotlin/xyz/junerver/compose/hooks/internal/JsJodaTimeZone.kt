@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package xyz.junerver.compose.hooks.internal

import kotlin.js.JsModule

/*
  Description: Initializes the JS-Joda timezone database for Wasm/JS.
  Author: Junerver
  Date: 2026/7/17
  Email: junerver@gmail.com
  Version: v1.0
*/

@JsModule("@js-joda/timezone")
private external object JsJodaTimeZoneModule

@Suppress("unused")
private val jsJodaTimeZoneModule = JsJodaTimeZoneModule
