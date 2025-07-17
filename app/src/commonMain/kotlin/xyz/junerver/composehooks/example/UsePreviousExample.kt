package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.junerver.compose.hooks.useControllable
import xyz.junerver.compose.hooks.usePrevious
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.ScrollColumn

/*
  Description: Example component for usePrevious hook
  Author: Junerver
  Date: 2024/3/11-9:50
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Example component demonstrating the usePrevious hook
 */
@Composable
fun UsePreviousExample() {
    Surface {
        ScrollColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "usePrevious Examples",
                style = MaterialTheme.typography.headlineMedium,
            )

            Text(
                text = "This hook returns the previous value of a state or prop, allowing you to track changes over time.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            // Interactive Demo
            InteractivePreviousDemo()

            // Basic usage example
            ExampleCard(title = "Basic Usage") {
                BasicPreviousExample()
            }
        }
    }
}

/**
 * Interactive demo for usePrevious hook
 */
@Composable
private fun InteractivePreviousDemo() {
    ExampleCard(title = "Interactive Demo") {
        val (input, setInput) = useControllable("")
        val previous by usePrevious(present = input.value)

        Column(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = input.value,
                onValueChange = setInput,
                label = { Text("Type something...") },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Current: ",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = input.value.ifEmpty { "(empty)" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Previous: ",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = previous ?: "(no previous value)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
    }
}

/**
 * Basic example of usePrevious hook
 */
@Composable
private fun BasicPreviousExample() {
    val (input, setInput) = useControllable("Initial value")
    val previous by usePrevious(present = input.value)

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = input.value,
            onValueChange = setInput,
            label = { Text("Edit this text") },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Current value: ${input.value}",
            style = MaterialTheme.typography.bodyMedium,
        )

        Text(
            text = "Previous value: $previous",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
