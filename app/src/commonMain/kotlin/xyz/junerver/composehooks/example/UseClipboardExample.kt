package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.useClipboard
import xyz.junerver.compose.hooks.useControllable
import xyz.junerver.compose.hooks.useGetState
import xyz.junerver.compose.hooks.useState
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.ScrollColumn

/*
  Description: Example component for useClipboard hook
  Author: Junerver
  Date: 2024/4/2-11:15
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun UseClipboardExample() {
    ScrollColumn(
        modifier = Modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "useClipboard Examples",
            style = MaterialTheme.typography.headlineMedium,
        )

        // 交互式演示
        InteractiveClipboardDemo()

        // 基本用法示例
        ExampleCard(title = "Basic Usage") {
            BasicUsageExample()
        }

        // 复制粘贴操作示例
        ExampleCard(title = "Copy & Paste Operations") {
            CopyPasteExample()
        }

        // 文本编辑器示例
        ExampleCard(title = "Text Editor Example") {
            TextEditorExample()
        }
    }
}

@Composable
private fun InteractiveClipboardDemo() {
    var inputText by useState("")
    var outputText by useState("")
    val (copy, paste) = useClipboard()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Interactive Demo",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color,
            )

            // Input section
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Enter text to copy") },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = { copy(inputText) },
                    modifier = Modifier.weight(1f),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy to Clipboard")
                    }
                }

                Button(
                    onClick = { outputText = paste() },
                    modifier = Modifier.weight(1f),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ContentPaste, contentDescription = "Paste")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Paste from Clipboard")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Output section
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Clipboard Content:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = outputText.ifEmpty { "(Empty clipboard or no paste action yet)" },
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Try typing text above, copying it to clipboard, then pasting it back!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BasicUsageExample() {
    val (copy, paste) = useClipboard()

    Column {
        Text(
            text = "The useClipboard hook provides two functions:",
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "• copy(text: String): Copies text to the clipboard",
            style = MaterialTheme.typography.bodyMedium,
        )

        Text(
            text = "• paste(): Retrieves text from the clipboard",
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Code: val (copy, paste) = useClipboard()",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CopyPasteExample() {
    // Copy section
    val (copyState, setCopyState) = useControllable("Hello, Compose Hooks!")
    val (copy, _) = useClipboard()

    // Paste section
    val (pasteState, setPasteState) = useGetState("")
    val (_, paste) = useClipboard()

    Column {
        // Copy section
        Text(
            text = "Copy Operation:",
            style = MaterialTheme.typography.titleSmall,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = copyState.value,
                onValueChange = setCopyState,
                label = { Text("Text to copy") },
                modifier = Modifier.weight(1f),
            )

            IconButton(onClick = { copy(copyState.value) }) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Paste section
        Text(
            text = "Paste Operation:",
            style = MaterialTheme.typography.titleSmall,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = pasteState.value.ifEmpty { "(Paste content will appear here)" },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )

            IconButton(onClick = { setPasteState(paste()) }) {
                Icon(Icons.Default.ContentPaste, contentDescription = "Paste")
            }
        }
    }
}

@Composable
private fun TextEditorExample() {
    var editorContent by useState("")
    val (copy, paste) = useClipboard()

    Column {
        Text(
            text = "A simple text editor with clipboard functionality:",
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Text editor
        OutlinedTextField(
            value = editorContent,
            onValueChange = { editorContent = it },
            label = { Text("Editor") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Clipboard actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = { copy(editorContent) },
                modifier = Modifier.weight(1f),
            ) {
                Text("Copy All")
            }

            Button(
                onClick = { editorContent = paste() },
                modifier = Modifier.weight(1f),
            ) {
                Text("Paste")
            }

            Button(
                onClick = { editorContent = "" },
                modifier = Modifier.weight(1f),
            ) {
                Text("Clear")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "This example demonstrates how useClipboard can be integrated into a text editor application.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
