package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import xyz.junerver.kotlin.Tuple2
import xyz.junerver.kotlin.tuple

typealias CopyFn = (String) -> Unit
typealias PasteFn = () -> String

@Composable
fun useClipboard(): Tuple2<CopyFn, PasteFn> {
    val clipboardManager = LocalClipboardManager.current
    val copy = { text: String -> clipboardManager.setText(AnnotatedString(text)) }
    val paste = { clipboardManager.getText()?.text ?: "" }
    return tuple(
        first = copy,
        second = paste
    )
}
