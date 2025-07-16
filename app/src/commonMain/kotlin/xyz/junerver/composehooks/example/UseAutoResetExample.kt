package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import xyz.junerver.compose.hooks.useAutoReset
import xyz.junerver.compose.hooks.useState
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.ScrollColumn

/*
  Description: Example component for useAutoReset hook
  Author: Junerver
  Date: 2024/7/8-15:58
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Example component demonstrating the useAutoReset hook
 */
@Composable
fun UseAutoResetExample() {
    ScrollColumn(
        modifier = Modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "useAutoReset Examples",
            style = MaterialTheme.typography.headlineMedium,
        )

        // Interactive Demo - similar to VueUse demo
        InteractiveAutoResetDemo()

        // Basic usage example
        ExampleCard(title = "Basic Usage") {
            var state by useAutoReset(default = "default value", interval = 2.seconds)

            Column {
                Text(
                    text = "Current value: $state",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                Text(
                    text = "This value will reset to 'default value' after 2 seconds",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                Button(
                    onClick = { state = "Modified value!" },
                ) {
                    Text("Set New Value")
                }
            }
        }

        // Different data types example
        ExampleCard(title = "Different Data Types") {
            var stringState by useAutoReset(default = "Hello", interval = 3.seconds)
            var numberState by useAutoReset(default = 0, interval = 2.seconds)
            var booleanState by useAutoReset(default = false, interval = 1.5.seconds)

            Column {
                // String example
                Text(
                    text = "String: $stringState",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp),
                ) {
                    Button(
                        onClick = { stringState = "World!" },
                    ) {
                        Text("Change String")
                    }
                }

                // Number example
                Text(
                    text = "Number: $numberState",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp),
                ) {
                    Button(
                        onClick = { numberState = 42 },
                    ) {
                        Text("Set to 42")
                    }
                    Button(
                        onClick = { numberState = 100 },
                    ) {
                        Text("Set to 100")
                    }
                }

                // Boolean example
                Text(
                    text = "Boolean: $booleanState",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                Button(
                    onClick = { booleanState = true },
                ) {
                    Text("Set to True")
                }
            }
        }

        // Use case: Notification system
        ExampleCard(title = "Use Case: Notification System") {
            var notification by useAutoReset(default = "", interval = 4.seconds)
            var inputText by useState("")

            Column {
                Text(
                    text = "Simulate a notification that auto-dismisses:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text("Notification message") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                )

                Button(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            notification = inputText
                            inputText = ""
                        }
                    },
                    modifier = Modifier.padding(bottom = 16.dp),
                ) {
                    Text("Show Notification")
                }

                if (notification.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                    ) {
                        Text(
                            text = "📢 $notification",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                } else {
                    Text(
                        text = "No active notifications",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/**
 * Interactive demo component similar to VueUse demo
 */
@Composable
private fun InteractiveAutoResetDemo() {
    var customDefault by useState("Default Text")
    var selectedInterval by useState(2000L) // in milliseconds
    var demoState by useAutoReset(
        default = customDefault,
        interval = selectedInterval.milliseconds,
    )

    val intervalOptions = listOf(
        1000L to "1 second",
        2000L to "2 seconds",
        3000L to "3 seconds",
        5000L to "5 seconds",
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
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

            // Display current state with large, bold styling
            Text(
                text = "Current State: $demoState",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = MaterialTheme.colorScheme.primary,
                ),
                modifier = Modifier.padding(bottom = 16.dp),
            )

            // Default value editor
            Text(
                text = "Default Value:",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            OutlinedTextField(
                value = customDefault,
                onValueChange = { customDefault = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true,
                placeholder = { Text("Enter default value...") },
            )

            // Reset interval selection
            Text(
                text = "Reset Interval:",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            Column(
                modifier = Modifier.padding(bottom = 16.dp),
            ) {
                intervalOptions.forEach { (interval, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedInterval == interval,
                                onClick = { selectedInterval = interval },
                                role = Role.RadioButton,
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selectedInterval == interval,
                            onClick = { selectedInterval = interval },
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                }
            }

            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = { demoState = "Modified!" },
                ) {
                    Text("Modify State")
                }
                Button(
                    onClick = { demoState = "Another Value" },
                ) {
                    Text("Set Another Value")
                }
            }
        }
    }
}
