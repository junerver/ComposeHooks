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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.random.Random
import xyz.junerver.compose.hooks.useEffect
import xyz.junerver.compose.hooks.useList
import xyz.junerver.compose.hooks.useListReduce
import xyz.junerver.compose.hooks.useState
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.LogCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description: Example component for useList hook
  Author: Junerver
  Date: 2024/3/8-14:35
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Example component demonstrating the useList hook
 */
@Composable
fun UseListExample() {
    Surface {
        ScrollColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "useList Examples",
                style = MaterialTheme.typography.headlineMedium,
            )

            Text(
                text = "This hook provides a convenient way to create and manage a mutable list state that triggers recomposition when modified.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            // Interactive Demo
            InteractiveListDemo()

            // Basic usage example
            ExampleCard(title = "Basic Usage") {
                BasicListExample()
            }

            // List operations example
            ExampleCard(title = "List Operations") {
                ListOperationsExample()
            }

            // List reduce example
            ExampleCard(title = "List Reduce") {
                ListReduceExample()
            }

            // Practical application
            ExampleCard(title = "Practical Application: Dynamic List") {
                DynamicListExample()
            }
        }
    }
}

/**
 * Interactive demonstration of useList hook with real-time updates
 */
@Composable
private fun InteractiveListDemo() {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Interactive Demo",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Initialize list with some default values
            val listState = useList(1, 2, 3)
            val listSum by useListReduce(listState) { a, b -> a + b }

            // Log list changes
            val logs = useList<String>()
            useEffect(listState) {
                logs.add("List changed: ${listState.joinToString()}")
                if (logs.size > 10) logs.removeAt(0)
            }

            // Display current list state
            Text(
                text = "Current List: [${listState.joinToString()}]",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp),
            )

            Text(
                text = "Sum: $listSum",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            // List operations buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TButton(text = "Add Item") {
                    listState.add(listState.size + 1)
                }

                TButton(text = "Remove Last", enabled = listState.isNotEmpty()) {
                    listState.removeLast()
                }

                TButton(text = "Change Random", enabled = listState.size > 1) {
                    val index = Random.nextInt(listState.size)
                    listState[index] = Random.nextInt(100)
                }

                TButton(text = "Clear", enabled = listState.isNotEmpty()) {
                    listState.clear()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display operation logs
            LogCard(title = "Operation Logs:", logs = logs)
        }
    }
}

/**
 * Basic example of useList hook
 */
@Composable
private fun BasicListExample() {
    // Initialize list with default values
    val listState = useList(1, 2, 3)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Configuration: Initial values [1, 2, 3]",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Display current list
        Text(
            text = "Current list: [${listState.joinToString()}]",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Simple operations
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TButton(text = "Add") {
                listState.add(listState.size + 1)
            }

            TButton(text = "Remove", enabled = listState.isNotEmpty()) {
                if (listState.isNotEmpty()) {
                    listState.removeLast()
                }
            }
        }
    }
}

/**
 * Example demonstrating various list operations
 */
@Composable
private fun ListOperationsExample() {
    val listState = useList<String>()
    var operationCount by useState(0)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "List size: ${listState.size}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Display current list with random background to show recomposition
        LazyColumn(
            modifier = Modifier.height(120.dp),
        ) {
            items(listState) { item ->
                Row(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                    Text(text = item)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // List operation buttons
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TButton(text = "Add") {
                operationCount++
                listState.add("Item $operationCount")
            }

            TButton(text = "Insert", enabled = listState.isNotEmpty()) {
                if (listState.isNotEmpty()) {
                    operationCount++
                    val index = Random.nextInt(listState.size)
                    listState.add(index, "Inserted $operationCount")
                }
            }

            TButton(text = "Remove", enabled = listState.isNotEmpty()) {
                if (listState.isNotEmpty()) {
                    val index = Random.nextInt(listState.size)
                    listState.removeAt(index)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
            TButton(text = "Clear", enabled = listState.isNotEmpty()) {
                listState.clear()
            }

            TButton(text = "Shuffle", enabled = listState.size > 1) {
                listState.shuffle()
            }
        }

        Text(
            text = "The background color changes indicate that the component recomposes",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

/**
 * Example demonstrating useListReduce hook
 */
@Composable
private fun ListReduceExample() {
    val listState = useList(1, 2, 3, 4, 5)

    // Different reduction operations
    val sum by useListReduce(listState) { a, b -> a + b }
    val product by useListReduce(listState) { a, b -> a * b }
    val max by useListReduce(listState) { a, b -> maxOf(a, b) }
    val min by useListReduce(listState) { a, b -> minOf(a, b) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Configuration: Initial values [1, 2, 3, 4, 5]",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Display current list
        Text(
            text = "Current list: [${listState.joinToString()}]",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Display reduction results
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(text = "Sum: $sum")
            Text(text = "Product: $product")
            Text(text = "Maximum: $max")
            Text(text = "Minimum: $min")
        }

        // Modify list buttons
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TButton(text = "Add Random") {
                listState.add(Random.nextInt(1, 10))
            }

            TButton(text = "Remove Last", enabled = listState.isNotEmpty()) {
                if (listState.isNotEmpty()) {
                    listState.removeLast()
                }
            }
        }
    }
}

/**
 * Example showing a practical application of useList for a dynamic list
 */
@Composable
private fun DynamicListExample() {
    // Task list state
    val tasks = useList<String>()
    var newTask by useState("")

    Column(modifier = Modifier.fillMaxWidth()) {
        // Task input
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            androidx.compose.material3.TextField(
                value = newTask,
                onValueChange = { newTask = it },
                label = { Text("Enter a task") },
                modifier = Modifier.weight(1f),
            )

            TButton(
                text = "Add",
                enabled = newTask.isNotEmpty(),
                modifier = Modifier.padding(start = 8.dp),
            ) {
                if (newTask.isNotEmpty()) {
                    tasks.add(newTask)
                    newTask = ""
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Task list
        Text(
            text = "Tasks (${tasks.size}):",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        if (tasks.isEmpty()) {
            Text(
                text = "No tasks yet. Add some tasks above.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyColumn(
                modifier = Modifier.height(200.dp),
            ) {
                items(tasks.size) { index ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${index + 1}. ${tasks[index]}",
                            modifier = Modifier.weight(1f),
                        )

                        TButton(text = "Remove") {
                            tasks.removeAt(index)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TButton(text = "Clear All") {
                tasks.clear()
            }
        }
    }
}
