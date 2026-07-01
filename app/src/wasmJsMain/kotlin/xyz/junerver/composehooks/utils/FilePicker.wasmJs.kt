/*
 * Copyright (c) 2024. ComposeHooks project
 *
 * Description: wasmJs file picker actual backed by an HTML <input type="file">.
 * Author: Junerver
 * Date: 2026/07/01
 * Email: junerver@gmail.com
 */
@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package xyz.junerver.composehooks.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.browser.document
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.files.File as JsFile
import org.w3c.files.FileReader

/**
 * wasmJs actual for [rememberFilePickerLauncher].
 *
 * The browser has no native file dialog API; the portable approach is to synthesize a hidden
 * `<input type="file">` element, click it programmatically, and read the selected file via
 * [FileReader.readAsDataURL]. The result is delivered as a base64 data URL, matching the
 * contract of the desktop/android pickers (which also surface base64 content to the AI examples).
 */
@Composable
actual fun rememberFilePickerLauncher(onFilePicked: (PickedFile) -> Unit): FilePickerLauncher =
    remember(onFilePicked) {
        WasmJsFilePickerLauncher(onFilePicked)
    }

private class WasmJsFilePickerLauncher(
    private val onFilePicked: (PickedFile) -> Unit,
) : FilePickerLauncher {
    override fun launch() {
        val input = document.createElement("input") as HTMLInputElement
        input.type = "file"
        input.accept = "image/*"
        input.style.display = "none"

        input.onchange = changeListener@{ _: Event ->
            val file = input.files?.item(0) ?: return@changeListener
            readPickedFile(file) { picked -> onFilePicked(picked) }
        }

        document.body?.appendChild(input)
        input.click()
        // Clean up the element after the dialog closes to avoid leaking DOM nodes.
        input.onfocus = {
            document.body?.removeChild(input)
        }
    }
}

/**
 * Reads [file] as a base64 data URL via [FileReader.readAsDataURL], then strips the
 * `data:<mime>;base64,` prefix and delivers a [PickedFile] to [callback].
 *
 * The DOM `FileReader` result is reached through a small JS interop shim because the
 * `EventTarget.result` property is not visible from Kotlin's typed DOM bindings.
 */
private fun readPickedFile(file: JsFile, callback: (PickedFile) -> Unit) {
    val reader = FileReader()
    reader.onload = { event ->
        val result = fileReaderResultString(event)
        if (result != null) {
            // result looks like "data:<mime>;base64,<payload>"
            val headerEnd = result.indexOf(";base64,")
            if (headerEnd >= 0) {
                val mimeType = result.substring(5, headerEnd).ifEmpty { "application/octet-stream" }
                val base64 = result.substring(headerEnd + ";base64,".length)
                callback(PickedFile(name = file.name, mimeType = mimeType, base64Content = base64))
            }
        }
    }
    reader.readAsDataURL(file)
}

/**
 * Returns `event.target.result` as a String (or null) for a FileReader load event.
 *
 * In Kotlin/Wasm, `js("...")` must be a single top-level expression; its body receives the
 * function arguments under the names `_arg0`, `_arg1`, … so we read the result property here.
 */
private fun fileReaderResultString(event: Event): String? = js("{ const r = _arg0.target && _arg0.target.result; return typeof r === 'string' ? r : null; }")


