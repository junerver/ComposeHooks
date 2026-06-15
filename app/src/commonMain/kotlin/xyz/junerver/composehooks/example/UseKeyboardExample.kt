package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import xyz.junerver.compose.hooks.useKeyboard
import xyz.junerver.compose.hooks.useState
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description: Example component for useKeyboard hook
  Author: Junerver
  Date: 2024/3/11-11:09
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Example component demonstrating the useKeyboard hook
 */
@Composable
fun UseKeyboardExample() {
    ScrollColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "useKeyboard Examples",
            style = MaterialTheme.typography.headlineMedium,
        )

        // Interactive Demo
        InteractiveKeyboardDemo()

        // Basic usage example
        ExampleCard(title = "Basic Usage") {
            BasicKeyboardExample()
        }
    }
}

/**
 * Interactive demo showing how useKeyboard works
 */
@Composable
private fun InteractiveKeyboardDemo() {
    val (hideKeyboard, showKeyboard) = useKeyboard()
    var text by useState("")

    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Interactive Demo",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Text field
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Type something...") },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Control buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TButton(
                    text = "Show Keyboard",
                    modifier = Modifier.weight(1f),
                ) {
                    showKeyboard()
                }

                TButton(
                    text = "Hide Keyboard",
                    modifier = Modifier.weight(1f),
                ) {
                    hideKeyboard()
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Instructions
            Text(
                text = "Click 'Show Keyboard' to open the soft keyboard, 'Hide Keyboard' to close it.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

/**
 * Basic example showing how to use useKeyboard
 */
@Composable
private fun BasicKeyboardExample() {
    val (hideKeyboard, showKeyboard) = useKeyboard()
    var text by useState("")

    Column {
        Text(
            text = "This hook provides a convenient way to control the software keyboard.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Type something...") },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TButton(
                text = "Show Keyboard",
                modifier = Modifier.weight(1f),
            ) {
                showKeyboard()
            }

            TButton(
                text = "Hide Keyboard",
                modifier = Modifier.weight(1f),
            ) {
                hideKeyboard()
            }
        }
    }
}