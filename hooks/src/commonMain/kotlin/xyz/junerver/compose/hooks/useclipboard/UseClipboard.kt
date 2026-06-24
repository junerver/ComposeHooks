package xyz.junerver.compose.hooks.useclipboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import xyz.junerver.compose.hooks.CopyFn
import xyz.junerver.compose.hooks.PasteFn

/**
 * A composable function that provides access to clipboard operations
 * like copying and pasting text using the system clipboard manager.
 *
 * @return A `CopyPasteHolder` object that contains:
 *         - `copy`: A function that copies the provided text to the clipboard.
 *         - `paste`: A function that retrieves the text from the clipboard.
 */
@Composable
@Suppress("DEPRECATION")
fun useClipboardImpl(): CopyPasteHolder {
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
