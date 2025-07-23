package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.junerver.compose.hooks.Tuple1
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.useUpdate
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.SimpleContainer
import xyz.junerver.composehooks.ui.component.TButton
import xyz.junerver.composehooks.ui.component.randomBackground
import xyz.junerver.composehooks.utils.Timestamp

/*
  Description: useUpdate Hook Examples
  Author: Junerver
  Date: 2024/3/8-12:03
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun UseUpdateExample() {
    ScrollColumn(
        modifier = Modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "useUpdate Hook Examples",
            style = MaterialTheme.typography.headlineMedium,
        )

        Text(
            text = "useUpdate provides a force update mechanism by leveraging Compose's recomposition system. It's not a true force update but a clever trick using state reading to trigger recomposition.",
            style = MaterialTheme.typography.bodyMedium,
        )

        // Basic usage example
        ExampleCard(title = "Basic Usage") {
            BasicUpdateExample()
        }

        // Inline expansion example
        ExampleCard(title = "Inline Expansion Demo") {
            InlineExpansionExample()
        }

        // Recomposition scope example
        ExampleCard(title = "Recomposition Scope") {
            RecompositionScopeExample()
        }

        // Practical application example
        ExampleCard(title = "Practical Application") {
            PracticalApplicationExample()
        }
    }
}

@Composable
fun BasicUpdateExample() {
    val update = useUpdate()

    Column {
        Text(
            text = "Current timestamp: ${Timestamp.now()}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Text(
            text = "Click the button to force update and see the timestamp change.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        TButton(text = "Force Update") {
            update()
        }
    }
}

@Composable
fun InlineExpansionExample() {
    // This demonstrates what useUpdate() does internally
    // Instead of val update = useUpdate(), we inline the implementation
    var state by useState(0)
    val (_) = Tuple1(state)
    val forceUpdate = { state += 1 }

    Column {
        Text(
            text = "Inline Implementation",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Text(
            text = "This example shows the inline expansion of useUpdate():",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        ) {
            Text(
                text = "var state by useState(0)\nval (single) = Tuple1(state)\nval forceUpdate = { state += 1 }",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(16.dp),
            )
        }

        Text(
            text = "Timestamp: ${Timestamp.now()}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        TButton(text = "Inline Force Update") {
            forceUpdate()
        }
    }
}

@Composable
fun RecompositionScopeExample() {
    val update = useUpdate()
    var clickCount by useState(0)

    Column {
        Text(
            text = "Recomposition Scope Demo",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Text(
            text = "The colored backgrounds show which parts recompose when force update is triggered.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        // This part will recompose when update() is called
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .randomBackground()
                .padding(16.dp),
        ) {
            Text(
                text = "This area recomposes on force update",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "Timestamp: ${Timestamp.now()}",
                style = MaterialTheme.typography.bodySmall,
            )
        }

        // This part only recomposes when clickCount changes
        SimpleContainer {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .randomBackground()
                    .padding(16.dp),
            ) {
                Text(
                    text = "This area only recomposes when click count changes",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "Click count: $clickCount",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 16.dp),
        ) {
            TButton(text = "Force Update") {
                update()
            }
            TButton(text = "Increment Count") {
                clickCount++
            }
        }
    }
}

@Composable
fun PracticalApplicationExample() {
    val update = useUpdate()
    var refreshCount by useState(0)

    Column {
        Text(
            text = "Practical Use Cases",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Text(
            text = "Common scenarios where force update might be useful:",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    text = "Data Refresh Simulation",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                Text(
                    text = "Last refresh: ${Timestamp.now()}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                Text(
                    text = "Refresh count: $refreshCount",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                TButton(
                    text = "Refresh Data",
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    refreshCount++
                    update() // Force update to refresh timestamp
                }
            }
        }

        Text(
            text = "Note: In real applications, prefer reactive state management over force updates.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
