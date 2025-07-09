package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString

/**
 * A composable function that provides access to clipboard operations
 * like copying and pasting text using the system clipboard manager.
 *
 * This function allows you to interact with the device's clipboard. You can
 * easily copy text to the clipboard or retrieve the text from the clipboard
 * using this helper function.
 *
 * @return A `CopyPasteHolder` object that contains:
 *         - `copy`: A function that copies the provided text to the clipboard.
 *         - `paste`: A function that retrieves the text from the clipboard.
 *
 * Example usage:
 * ```kotlin
 * val (copy, paste) = useClipboard()
 * copy("Hello, world!") // Copies "Hello, world!" to the clipboard.
 * val pastedText = paste() // Retrieves text from the clipboard.
 * ```
 */
@Composable
fun useClipboard(): CopyPasteHolder {
    val clipboardManager = LocalClipboardManager.current
    return remember {
        CopyPasteHolder(
            copy = { text: String -> clipboardManager.setText(AnnotatedString(text)) },
            paste = { clipboardManager.getText()?.text ?: "" },
        )
    }
}

@Stable
data class CopyPasteHolder(
    val copy: CopyFn,
    val paste: PasteFn,
)
