package xyz.junerver.compose.hooks.usekeyboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import xyz.junerver.compose.hooks.HideKeyboardFn
import xyz.junerver.compose.hooks.ShowKeyboardFn

/*
  Description: This hook function is used to conveniently call the soft keyboard controller.
  Author: Junerver
  Date: 2024/4/16-10:14
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun useKeyboardImpl(): KeyboardHolder {
    val keyboardController = LocalSoftwareKeyboardController.current
    return remember {
        KeyboardHolder(
            hideKeyboard = { keyboardController?.hide() },
            showKeyboard = { keyboardController?.show() },
        )
    }
}

@Stable
data class KeyboardHolder(
    val hideKeyboard: HideKeyboardFn,
    val showKeyboard: ShowKeyboardFn,
)
