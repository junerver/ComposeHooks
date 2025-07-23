package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.dp
import xyz.junerver.compose.hooks.KeyPressDelegate
import xyz.junerver.compose.hooks.useKeyPress
import xyz.junerver.compose.hooks.useList
import xyz.junerver.compose.hooks.useState
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.LogCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.utils.now

/*
  Description: Example component for useKeyPress hook
  Author: Junerver
  Date: 2025/7/23-14:30
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * UseKeyPressExample demonstrates the usage of the useKeyPress hook.
 *
 * This example shows how to:
 * - Detect single key presses
 * - Detect combinations of keys (2, 3, or 4 keys)
 * - Implement keyboard shortcuts in a Compose application
 *
 * @author Junerver
 * @since 2025/7/23
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun UseKeyPressExample() {
    Surface {
        ScrollColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "useKeyPress Examples",
                style = MaterialTheme.typography.headlineMedium,
            )

            Text(
                text = "This hook allows you to detect keyboard key presses and combinations in your Compose application.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            // Basic usage example
            ExampleCard(
                title = "Single Key Press Example",
                content = { SingleKeyPressExample() },
            )

            // Two keys combination example
            ExampleCard(
                title = "Two Keys Combination Example",
                content = { TwoKeysCombinationExample() },
            )

            // Three keys combination example
            ExampleCard(
                title = "Three Keys Combination Example",
                content = { ThreeKeysCombinationExample() },
            )

            // Four keys combination example
            ExampleCard(
                title = "Four Keys Combination Example",
                content = { FourKeysCombinationExample() },
            )

            // Practical application example
            ExampleCard(
                title = "Practical Application: Keyboard Shortcuts",
                content = { KeyboardShortcutsExample() },
            )
        }
    }
}

/**
 * Example showing how to detect a single key press
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SingleKeyPressExample() {
    val logs = useList<String>()

    // Use the useKeyPress hook to detect key presses
    useKeyPress(Key.Spacebar) {
        logs.add("Space key was pressed at ${now()}")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .focusTarget()
            .onKeyEvent { event ->
                KeyPressDelegate.onKeyEvent(event)
                false
            },
    ) {
        Text(
            text = "Press the 'Space' key to trigger an action",
            style = MaterialTheme.typography.bodyMedium,
        )

        // 移除不必要的请求焦点按钮

        LogCard(
            logs = logs,
            limit = 3,
            modifier = Modifier.padding(top = 8.dp),
        )
    }

    // 移除不必要的请求焦点操作
}

/**
 * Example showing how to detect a combination of two keys
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TwoKeysCombinationExample() {
    val logs = useList<String>()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .focusTarget()
            .onKeyEvent { event ->
                KeyPressDelegate.onKeyEvent(event)
                false
            },
    ) {
        Text(
            text = "Press 'Ctrl+Z' to trigger an undo action",
            style = MaterialTheme.typography.bodyMedium,
        )

        // Detect when Ctrl+Z is pressed
        useKeyPress(Key.CtrlLeft, Key.Z) {
            logs.add("Ctrl+Z combination was pressed - Undo action triggered")
        }

        // 移除不必要的请求焦点按钮

        LogCard(
            logs = logs,
            limit = 3,
            modifier = Modifier.padding(top = 8.dp),
        )
    }

    // 移除不必要的请求焦点操作
}

/**
 * Example showing how to detect a combination of three keys
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ThreeKeysCombinationExample() {
    val logs = useList<String>()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .focusTarget()
            .onKeyEvent { event ->
                KeyPressDelegate.onKeyEvent(event)
                false
            },
    ) {
        Text(
            text = "Press 'Ctrl+Shift+S' to trigger a save-as action",
            style = MaterialTheme.typography.bodyMedium,
        )

        // Detect when Ctrl+Shift+S is pressed
        useKeyPress(Key.CtrlLeft, Key.ShiftLeft, Key.S) {
            logs.add("Ctrl+Shift+S combination was pressed - Save As action triggered")
        }

        // 移除不必要的请求焦点按钮

        LogCard(
            logs = logs,
            limit = 3,
            modifier = Modifier.padding(top = 8.dp),
        )
    }

    // 移除不必要的请求焦点操作
}

/**
 * Example showing how to detect a combination of four keys
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FourKeysCombinationExample() {
    val logs = useList<String>()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .focusTarget()
            .onKeyEvent { event ->
                KeyPressDelegate.onKeyEvent(event)
                false
            },
    ) {
        Text(
            text = "Press 'Ctrl+Alt+Shift+D' to trigger a debug action",
            style = MaterialTheme.typography.bodyMedium,
        )

        // Detect when Ctrl+Alt+Shift+D is pressed
        useKeyPress(Key.CtrlLeft, Key.AltLeft, Key.ShiftLeft, Key.D) {
            logs.add("Ctrl+Alt+Shift+D combination was pressed - Debug action triggered")
        }

        // 移除不必要的请求焦点按钮

        LogCard(
            logs = logs,
            limit = 3,
            modifier = Modifier.padding(top = 8.dp),
        )
    }

    // 移除不必要的请求焦点操作
}

/**
 * Example showing a practical application of keyboard shortcuts
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun KeyboardShortcutsExample() {
    val logs = useList<String>()
    var no by useState(0)

    // Use the useKeyPress hook to detect key combinations
    useKeyPress(Key.CtrlLeft, Key.N) {
        logs.add("New file action triggered")
        no = 1
    }

    useKeyPress(Key.CtrlLeft, Key.O) {
        logs.add("Open file action triggered")
        no = 2
    }

    useKeyPress(Key.CtrlLeft, Key.S) {
        logs.add("Save file action triggered")
        no = 3
    }

    useKeyPress(Key.CtrlLeft, Key.ShiftLeft, Key.S) {
        logs.add("Save as action triggered")
        no = 4
    }

    useKeyPress(Key.CtrlLeft, Key.P) {
        logs.add("Print action triggered")
        no = 5
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .focusTarget()
            .onKeyEvent { event ->
                KeyPressDelegate.onKeyEvent(event)
                false
            },
    ) {
        Text(
            text = "This example demonstrates how to implement keyboard shortcuts in a Compose application",
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Available shortcuts:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        Row {
            Text(text = "• Ctrl+N: New file")
            if (no == 1) {
                Text(text = " ✔", color = Color.Red, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Row {
            Text(text = "• Ctrl+O: Open file")
            if (no == 2) {
                Text(text = " ✔", color = Color.Red, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Row {
            Text(text = "• Ctrl+S: Save file")
            if (no == 3) {
                Text(text = " ✔", color = Color.Red, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Row {
            Text(text = "• Ctrl+Shift+S: Save as")
            if (no == 4) {
                Text(text = " ✔", color = Color.Red, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Row {
            Text(text = "• Ctrl+P: Print")
            if (no == 5) {
                Text(text = " ✔", color = Color.Red, style = MaterialTheme.typography.bodyMedium)
            }
        }

        // Note about keyboard shortcuts
        Text(
            text = "Try pressing the shortcuts to see them in action",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 8.dp),
        )

        // 移除不必要的请求焦点按钮

        LogCard(
            logs = logs,
            limit = 5,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
