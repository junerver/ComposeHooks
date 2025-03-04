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

/**
 * A hook for managing the software keyboard in Compose.
 *
 * This hook provides a convenient way to control the software keyboard,
 * allowing you to show and hide it programmatically. It wraps the
 * [LocalSoftwareKeyboardController] functionality in a more ergonomic API.
 *
 * @return A [KeyboardHolder] containing functions to control the keyboard
 *
 * @example
 * ```kotlin
 * val (hideKeyboard, showKeyboard) = useKeyboard()
 * 
 * // Hide the keyboard
 * hideKeyboard()
 * 
 * // Show the keyboard
 * showKeyboard()
 * ```
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

/**
 * Holder class for keyboard control functions.
 *
 * @property hideKeyboard Function to hide the software keyboard
 * @property showKeyboard Function to show the software keyboard
 */
@Stable
data class KeyboardHolder(
    val hideKeyboard: HideKeyboardFn,
    val showKeyboard: ShowKeyboardFn,
)
