package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString

internal typealias CopyFn = (String) -> Unit
internal typealias PasteFn = () -> String

@Composable
fun useClipboard(): Pair<CopyFn, PasteFn> {
    val clipboardManager = LocalClipboardManager.current
    return remember {
        Pair(
            first = { text: String -> clipboardManager.setText(AnnotatedString(text)) },
            second = { clipboardManager.getText()?.text ?: "" }
        )
    }
}
