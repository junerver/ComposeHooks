package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import xyz.junerver.kotlin.Tuple2
import xyz.junerver.kotlin.tuple

/*
  Description: This hook function is used to conveniently call the soft keyboard controller.
  Author: Junerver
  Date: 2024/4/16-10:14
  Email: junerver@gmail.com
  Version: v1.0
*/
internal typealias HideKeyboardFn = () -> Unit
internal typealias ShowKeyboardFn = () -> Unit

@Composable
fun useKeyboard(): Tuple2<HideKeyboardFn, ShowKeyboardFn> {
    val keyboardController = LocalSoftwareKeyboardController.current
    return tuple(
        first = { keyboardController?.hide() },
        second = { keyboardController?.show() }
    )
}
