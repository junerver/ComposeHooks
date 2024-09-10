package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString

internal typealias CopyFn = (String) -> Unit
internal typealias PasteFn = () -> String

@Composable
fun useClipboard(): Pair<CopyFn, PasteFn> {
    val clipboardManager = LocalClipboardManager.current
    val copy = { text: String -> clipboardManager.setText(AnnotatedString(text)) }
    val paste = { clipboardManager.getText()?.text ?: "" }
    return Pair(
        first = copy,
        second = paste
    )
}
