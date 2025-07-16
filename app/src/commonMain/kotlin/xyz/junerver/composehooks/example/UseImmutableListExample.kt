package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.random.Random
import xyz.junerver.compose.hooks.useEffect
import xyz.junerver.compose.hooks.useImmutableList
import xyz.junerver.compose.hooks.useImmutableListReduce
import xyz.junerver.compose.hooks.useList
import xyz.junerver.compose.hooks.useState
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.TButton
import xyz.junerver.composehooks.ui.component.randomBackground

/*
  Description: Example component for useImmutableList hook
  Author: Junerver
  Date: 2024/9/27-19:44
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Example component demonstrating the useImmutableList hook
 */
@Composable
fun UseImmutableListExample() {
    Surface {
        ScrollColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "useImmutableList Examples",
                style = MaterialTheme.typography.headlineMedium,
            )

            // Basic usage example
            ExampleCard(title = "Basic Usage") {
                BasicUsageExample()
            }

            // List operations example
            ExampleCard(title = "List Operations") {
                ListOperationsExample()
            }

            // List reduce example
            ExampleCard(title = "List Reduce") {
                ListReduceExample()
            }
        }
    }
}

/**
 * Demonstrates the basic usage of useImmutableList hook
 */
@Composable
private fun BasicUsageExample() {
    val immutableListHolder = useImmutableList(1, 2, 3)
    val immutableList by immutableListHolder.list
    val logEntries = useList<String>()

    // Monitor list changes
    useEffect(immutableList) {
        logEntries.add("List changed to: $immutableList")
        if (logEntries.size > 5) {
            logEntries.removeAt(0)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Current list: $immutableList",
            style = MaterialTheme.typography.bodyLarge,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TButton(text = "Add Item") {
                immutableListHolder.mutate {
                    it.add(immutableList.size + 1)
                }
            }

            TButton(text = "Remove Last") {
                if (immutableList.isNotEmpty()) {
                    immutableListHolder.mutate {
                        it.removeLast()
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "useImmutableList provides a way to work with immutable lists in a reactive way",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Log display
        Text(
            text = "Change Log:",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                if (logEntries.isEmpty()) {
                    Text(
                        text = "No changes recorded yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    logEntries.forEach { log ->
                        Text(
                            text = "• $log",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 2.dp),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Demonstrates various operations on immutable lists
 */
@Composable
private fun ListOperationsExample() {
    val immutableListHolder = useImmutableList(1, 2, 3, 4, 5)
    val immutableList by immutableListHolder.list

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Current list: $immutableList",
            style = MaterialTheme.typography.bodyLarge,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TButton(text = "Replace Random") {
                if (immutableList.isNotEmpty()) {
                    val index = Random.nextInt(immutableList.size)
                    immutableListHolder.mutate {
                        it[index] = Random.nextInt(100)
                    }
                }
            }

            TButton(text = "Clear") {
                immutableListHolder.mutate {
                    it.clear()
                }
            }

            TButton(text = "Reset") {
                immutableListHolder.mutate {
                    it.clear()
                    it.addAll(listOf(1, 2, 3, 4, 5))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "List Items:",
            style = MaterialTheme.typography.bodyLarge,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (immutableList.isEmpty()) {
                Text(
                    text = "List is empty",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.height(200.dp).padding(8.dp),
                ) {
                    items(immutableList) {
                        RandomItem(it)
                    }
                }
            }
        }
    }
}

/**
 * Demonstrates using useImmutableListReduce with immutable lists
 */
@Composable
private fun ListReduceExample() {
    val immutableListHolder = useImmutableList(1, 2, 3, 4, 5)
    val immutableList by immutableListHolder.list

    // Different reduce operations
    val sum by useImmutableListReduce(immutableList) { acc, value -> acc + value }
    val product by useState(immutableList) {
        if (immutableList.isEmpty()) 0 else immutableList.reduce { acc, value -> acc * value }
    }
    val average by useState(immutableList) {
        if (immutableList.isEmpty()) 0.0 else immutableList.average()
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Current list: $immutableList",
            style = MaterialTheme.typography.bodyLarge,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TButton(text = "Add Random") {
                immutableListHolder.mutate {
                    it.add(Random.nextInt(1, 10))
                }
            }

            TButton(text = "Remove Last") {
                if (immutableList.isNotEmpty()) {
                    immutableListHolder.mutate {
                        it.removeLast()
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Reduce Operations:",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Sum: $sum")
                Text(text = "Product: $product")
                Text(text = "Average: $average")

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "useImmutableListReduce automatically recalculates when the list changes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * A list item with random background color
 */
@Composable
private fun RandomItem(content: Int) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .randomBackground(),
    ) {
        Text(
            text = "Item value: $content",
            modifier = Modifier.padding(8.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
