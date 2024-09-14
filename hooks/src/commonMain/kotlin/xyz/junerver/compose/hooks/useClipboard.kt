package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString

/**
 * 快捷的使用剪切板：复制、粘贴
 */
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
