package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import xyz.junerver.kotlin.Tuple2
import xyz.junerver.kotlin.tuple

/**
 * Description: This hook function is used to conveniently call the soft keyboard controller.
 * @author Junerver
 * date: 2024/4/16-10:14
 * Email: junerver@gmail.com
 * Version: v1.0
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun useKeyboard(): Tuple2<() -> Unit, () -> Unit> {
    val keyboardController = LocalSoftwareKeyboardController.current
    return tuple(
        first = { keyboardController?.hide() },
        second = { keyboardController?.show() }
    )
}
