package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

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
fun useKeyboard(): Pair<HideKeyboardFn, ShowKeyboardFn> {
    val keyboardController = LocalSoftwareKeyboardController.current
    return remember {
        Pair(
            first = { keyboardController?.hide() },
            second = { keyboardController?.show() }
        )
    }
}
