package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.junerver.compose.hooks.useControllable
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.useUndo
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description: Example demonstrating the useUndo hook
  Author: Junerver
  Date: 2024/3/11-9:33
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Example component demonstrating the useUndo hook
 */
@Composable
fun UseUndoExample() {
    ScrollColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "useUndo Examples",
            style = MaterialTheme.typography.headlineMedium,
        )

        Text(
            text = "This hook provides undo/redo functionality for state management, allowing you to track state history and navigate through previous values.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Interactive Demo
        InteractiveUndoDemo()

        // Basic usage example
        ExampleCard(title = "Basic Usage") {
            BasicUndoExample()
        }

        // Advanced usage example
        ExampleCard(title = "Advanced Usage: Text Editor with History") {
            TextEditorExample()
        }
    }
}

/**
 * Interactive demo component for useUndo hook
 */
@Composable
private fun InteractiveUndoDemo() {
    val (state, set, reset, undo, redo, canUndo, canRedo) = useUndo(initialPresent = "")
    val (input, setInput) = useControllable("")

    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Interactive Demo",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            Text(
                text = "Current Value: ${state.value}",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            OutlinedTextField(
                value = input.value,
                onValueChange = setInput,
                label = { Text("Enter text") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp),
            ) {
                TButton(
                    text = "Submit",
                    modifier = Modifier.weight(1f),
                ) {
                    set(input.value)
                    setInput("")
                }
                TButton(
                    text = "Reset",
                    modifier = Modifier.weight(1f),
                ) {
                    reset("")
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TButton(
                    text = "Undo",
                    enabled = canUndo.value,
                    modifier = Modifier.weight(1f),
                ) {
                    undo()
                }
                TButton(
                    text = "Redo",
                    enabled = canRedo.value,
                    modifier = Modifier.weight(1f),
                ) {
                    redo()
                }
            }
        }
    }
}

/**
 * Basic usage example for useUndo hook
 */
@Composable
private fun BasicUndoExample() {
    val (count, setCount, resetCount, undoCount, redoCount, canUndoCount, canRedoCount) = useUndo(initialPresent = 0)

    Column {
        Text(
            text = "Counter: ${count.value}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 8.dp),
        ) {
            TButton(
                text = "Increment",
                modifier = Modifier.weight(1f),
            ) {
                setCount(count.value.present + 1)
            }
            TButton(
                text = "Decrement",
                modifier = Modifier.weight(1f),
            ) {
                setCount(count.value.present - 1)
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TButton(
                text = "Undo",
                enabled = canUndoCount.value,
                modifier = Modifier.weight(1f),
            ) {
                undoCount()
            }
            TButton(
                text = "Redo",
                enabled = canRedoCount.value,
                modifier = Modifier.weight(1f),
            ) {
                redoCount()
            }
            TButton(
                text = "Reset",
                modifier = Modifier.weight(1f),
            ) {
                resetCount(0)
            }
        }
    }
}

/**
 * Advanced text editor example with undo/redo functionality
 */
@Composable
private fun TextEditorExample() {
    val (text, setText, resetText, undoText, redoText, canUndoText, canRedoText) = useUndo(initialPresent = "Start typing...")
    var wordCount by useState(0)

    Column {
        Text(
            text = "Text Editor with History",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Text(
            text = "Word count: $wordCount",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        OutlinedTextField(
            value = text.value.present,
            onValueChange = { newText ->
                setText(newText)
                wordCount = newText.split("\\s+".toRegex()).filter { it.isNotBlank() }.size
            },
            label = { Text("Document content") },
            modifier = Modifier.fillMaxWidth().height(120.dp).padding(bottom = 16.dp),
            maxLines = 5,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TButton(
                text = "Undo",
                enabled = canUndoText.value,
                modifier = Modifier.weight(1f),
            ) {
                undoText()
                wordCount = text.value.present.split("\\s+".toRegex()).filter { it.isNotBlank() }.size
            }
            TButton(
                text = "Redo",
                enabled = canRedoText.value,
                modifier = Modifier.weight(1f),
            ) {
                redoText()
                wordCount = text.value.present.split("\\s+".toRegex()).filter { it.isNotBlank() }.size
            }
            TButton(
                text = "Clear",
                modifier = Modifier.weight(1f),
            ) {
                resetText("")
                wordCount = 0
            }
        }
    }
}
