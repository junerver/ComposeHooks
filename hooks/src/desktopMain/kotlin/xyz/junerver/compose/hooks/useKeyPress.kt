package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type

/*
  Description: A hook for detecting keyboard key press events in Compose applications
  Author: Junerver
  Date: 2025/7/23-12:00
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * A composable hook that detects when a specific key is pressed and executes the provided event handler.
 *
 * This hook monitors keyboard events and triggers the event handler when the specified key is pressed.
 * It ensures that only the exact key combination is detected (single key in this case).
 *
 * @param key The key to detect
 * @param eventHandler The function to execute when the key is pressed
 */
@Composable
fun useKeyPress(key: Key, eventHandler: () -> Unit) {
    useEffect(KeyPressDelegate.keyPress) {
        if (key in KeyPressDelegate.keyPress && KeyPressDelegate.keyPress.size == 1) {
            eventHandler()
        }
    }
}

/**
 * A composable hook that detects when a specific combination of two keys is pressed simultaneously
 * and executes the provided event handler.
 *
 * This hook monitors keyboard events and triggers the event handler when both specified keys are pressed
 * at the same time. It ensures that only the exact key combination is detected (exactly two keys).
 *
 * @param key1 The first key in the combination
 * @param key2 The second key in the combination
 * @param eventHandler The function to execute when the key combination is pressed
 */
@Composable
fun useKeyPress(key1: Key, key2: Key, eventHandler: () -> Unit) {
    useEffect(KeyPressDelegate.keyPress) {
        if (key1 in KeyPressDelegate.keyPress && key2 in KeyPressDelegate.keyPress && KeyPressDelegate.keyPress.size == 2) {
            eventHandler()
        }
    }
}

/**
 * A composable hook that detects when a specific combination of three keys is pressed simultaneously
 * and executes the provided event handler.
 *
 * This hook monitors keyboard events and triggers the event handler when all three specified keys are pressed
 * at the same time. It ensures that only the exact key combination is detected (exactly three keys).
 *
 * @param key1 The first key in the combination
 * @param key2 The second key in the combination
 * @param key3 The third key in the combination
 * @param eventHandler The function to execute when the key combination is pressed
 */
@Composable
fun useKeyPress(
    key1: Key,
    key2: Key,
    key3: Key,
    eventHandler: () -> Unit,
) {
    useEffect(KeyPressDelegate.keyPress) {
        if (key1 in KeyPressDelegate.keyPress && key2 in KeyPressDelegate.keyPress && key3 in KeyPressDelegate.keyPress && KeyPressDelegate.keyPress.size == 3) {
            eventHandler()
        }
    }
}

/**
 * A composable hook that detects when a specific combination of four keys is pressed simultaneously
 * and executes the provided event handler.
 *
 * This hook monitors keyboard events and triggers the event handler when all four specified keys are pressed
 * at the same time. It ensures that only the exact key combination is detected (exactly four keys).
 *
 * @param key1 The first key in the combination
 * @param key2 The second key in the combination
 * @param key3 The third key in the combination
 * @param key4 The fourth key in the combination
 * @param eventHandler The function to execute when the key combination is pressed
 */
@Composable
fun useKeyPress(
    key1: Key,
    key2: Key,
    key3: Key,
    key4: Key,
    eventHandler: () -> Unit,
) {
    useEffect(KeyPressDelegate.keyPress) {
        if (key1 in KeyPressDelegate.keyPress && key2 in KeyPressDelegate.keyPress && key3 in KeyPressDelegate.keyPress && key4 in KeyPressDelegate.keyPress && KeyPressDelegate.keyPress.size == 4) {
            eventHandler()
        }
    }
}

/**
 * Delegate object that manages key press state and handles keyboard events.
 *
 * This object maintains a map of currently pressed keys and provides a method to process
 * keyboard events. It's used internally by the useKeyPress hook to track key states.
 */
object KeyPressDelegate {
    /**
     * A mutable state map that tracks currently pressed keys.
     * The key is the keyboard key, and the value is a boolean indicating the key is pressed.
     */
    internal val keyPress = mutableStateMapOf<Key, Boolean>()

    /**
     * Processes keyboard events and updates the keyPress map accordingly.
     *
     * This function should be called from a KeyEventHandler to track key press states.
     * It handles both key down (press) and key up (release) events.
     *
     * @param event The keyboard event to process
     * @return Always returns false to allow the event to continue propagating
     */
    fun onKeyEvent(event: KeyEvent): Boolean {
        when (event.type) {
            KeyEventType.KeyDown -> {
                keyPress[event.key] = true
            }

            KeyEventType.KeyUp -> {
                keyPress.remove(event.key)
            }

            else -> {}
        }
        return false
    }
}
