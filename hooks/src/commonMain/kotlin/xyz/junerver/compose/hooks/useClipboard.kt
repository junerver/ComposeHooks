package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString

/**
 * 快捷的使用剪切板：复制、粘贴
 */
@Composable
fun useClipboard(): CopyPasteHolder {
    val clipboardManager = LocalClipboardManager.current
    return remember {
        CopyPasteHolder(
            copy = { text: String -> clipboardManager.setText(AnnotatedString(text)) },
            paste = { clipboardManager.getText()?.text ?: "" }
        )
    }
}

@Stable
data class CopyPasteHolder(
    val copy: CopyFn,
    val paste: PasteFn,
)
