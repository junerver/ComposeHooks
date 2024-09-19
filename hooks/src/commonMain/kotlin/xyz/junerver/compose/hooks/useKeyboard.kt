package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

/*
  Description: This hook function is used to conveniently call the soft keyboard controller.
  Author: Junerver
  Date: 2024/4/16-10:14
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun useKeyboard(): KeyboardHolder {
    val keyboardController = LocalSoftwareKeyboardController.current
    return remember {
        KeyboardHolder(
            hideKeyboard = { keyboardController?.hide() },
            showKeyboard = { keyboardController?.show() }
        )
    }
}

@Stable
data class KeyboardHolder(
    val hideKeyboard: HideKeyboardFn,
    val showKeyboard: ShowKeyboardFn,
)
