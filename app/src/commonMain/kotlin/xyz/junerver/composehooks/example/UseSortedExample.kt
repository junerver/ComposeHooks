package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.junerver.compose.hooks.useList
import xyz.junerver.compose.hooks.useSorted
import xyz.junerver.compose.hooks.useState
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.LogCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description: Example component for useSorted hook
  Author: Junerver
  Date: 2024/7/18-16:30
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Example data class for demonstration
 */
data class Person(val name: String, val age: Int)

/**
 * Example component demonstrating the useSorted hook
 */
@Composable
fun UseSortedExample() {
    Surface {
        ScrollColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "useSorted Examples",
                style = MaterialTheme.typography.headlineMedium,
            )

            // Interactive Demo
            InteractiveSortedDemo()

            // Basic usage example
            ExampleCard(title = "Basic Usage") {
                BasicSortedExample()
            }

            // Custom comparator example
            ExampleCard(title = "Custom Comparator") {
                CustomComparatorExample()
            }

            // Dirty mode example
            ExampleCard(title = "Dirty Mode (Modifies Source)") {
                DirtyModeExample()
            }

            // Practical application
            ExampleCard(title = "Practical Application: Data Table") {
                DataTableExample()
            }
        }
    }
}

/**
 * Interactive demonstration of useSorted with real-time updates
 */
@Composable
private fun InteractiveSortedDemo() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Interactive Demo",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Create a list of numbers
            val numbers = useList(10, 3, 5, 7, 2, 1, 8, 6, 9, 4)
            val logs = useList<String>()

            // Sort options
            var useDirtyMode by useState(false)
            var sortDirection by useState("ascending")

            // Sort the list based on current options
            val sortedNumbers by if (sortDirection == "ascending") {
                useSorted(numbers, optionsOf = { this.dirty = useDirtyMode })
            } else {
                useSorted(
                    numbers,
                    optionsOf = {
                        this.compareFn = { a, b -> b - a }
                        this.dirty = useDirtyMode
                    },
                )
            }

            // Display original and sorted lists
            Text(
                text = "Original: ${numbers.joinToString()}",
                style = MaterialTheme.typography.bodyMedium,
            )

            Text(
                text = "Sorted: ${sortedNumbers.joinToString()}",
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sort direction options
            Text("Sort Direction:", style = MaterialTheme.typography.bodyMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = sortDirection == "ascending",
                    onClick = {
                        sortDirection = "ascending"
                        logs.add("Changed to ascending sort")
                    },
                )
                Text("Ascending")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(
                    selected = sortDirection == "descending",
                    onClick = {
                        sortDirection = "descending"
                        logs.add("Changed to descending sort")
                    },
                )
                Text("Descending")
            }

            // Dirty mode option
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = useDirtyMode,
                    onCheckedChange = {
                        useDirtyMode = it
                        logs.add("Dirty mode ${if (it) "enabled" else "disabled"}")
                    },
                )
                Text("Use Dirty Mode (modifies source array)")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Control buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TButton(
                    text = "Add Random",
                    onClick = {
                        val random = (1..100).random()
                        numbers.add(random)
                        logs.add("Added $random to list")
                    },
                    modifier = Modifier.weight(1f),
                )

                TButton(
                    text = "Shuffle",
                    onClick = {
                        numbers.shuffle()
                        logs.add("Shuffled list")
                    },
                    modifier = Modifier.weight(1f),
                )

                TButton(
                    text = "Reset",
                    onClick = {
                        numbers.clear()
                        numbers.addAll(listOf(10, 3, 5, 7, 2, 1, 8, 6, 9, 4))
                        logs.add("Reset list to original values")
                    },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Log display
            LogCard(logs = logs)
        }
    }
}

/**
 * Basic example of useSorted hook
 */
@Composable
private fun BasicSortedExample() {
    // Create a list of numbers
    val numbers = useList(10, 3, 5, 7, 2, 1, 8, 6, 9, 4)

    // Sort numbers using the default comparison function
    val sortedNumbers by useSorted(numbers)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "The useSorted hook creates a sorted version of a list that updates when the source list changes.",
            style = MaterialTheme.typography.bodySmall,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Original list: ${numbers.joinToString()}")
        Text("Sorted list: ${sortedNumbers.joinToString()}")

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TButton(
                text = "Shuffle",
                onClick = { numbers.shuffle() },
            )

            TButton(
                text = "Add Random",
                onClick = { numbers.add((1..100).random()) },
            )
        }
    }
}

/**
 * Example showing custom comparator usage
 */
@Composable
private fun CustomComparatorExample() {
    // Create a list of Person objects
    val people = useList(
        Person("John", 40),
        Person("Joe", 20),
        Person("Jane", 30),
        Person("Jenny", 22),
    )

    // Sort options
    var sortBy by useState("age")

    // Sort by selected field
    val sortedPeople by if (sortBy == "age") {
        useSorted(people) { a, b -> a.age - b.age }
    } else {
        useSorted(people) { a, b -> a.name.compareTo(b.name) }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "You can provide a custom comparator function to sort by any property.",
            style = MaterialTheme.typography.bodySmall,
        )

        // Sort options
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Sort by: ")
            RadioButton(
                selected = sortBy == "age",
                onClick = { sortBy = "age" },
            )
            Text("Age")
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(
                selected = sortBy == "name",
                onClick = { sortBy = "name" },
            )
            Text("Name")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Display people list
        Text("Original people list:")
        people.forEach { person ->
            Text("${person.name}, ${person.age} years old")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text("Sorted people list:")
        sortedPeople.forEach { person ->
            Text("${person.name}, ${person.age} years old")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TButton(
            text = "Shuffle",
            onClick = { people.shuffle() },
        )
    }
}

/**
 * Example showing dirty mode (modifies source array)
 */
@Composable
private fun DirtyModeExample() {
    // Using dirty mode (modifies the source array)
    val dirtyNumbers = useList(10, 3, 5, 7, 2, 1, 8, 6, 9, 4)
    val sortedDirtyNumbers by useSorted(
        dirtyNumbers,
        optionsOf = {
            dirty = true
        },
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "In dirty mode, the source array is modified in-place instead of creating a new sorted array.",
            style = MaterialTheme.typography.bodySmall,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Original list reference: ${dirtyNumbers.joinToString()}")
        Text("Sorted list reference: ${sortedDirtyNumbers.joinToString()}")
        Text("Note: The original list has been modified in-place")

        Spacer(modifier = Modifier.height(8.dp))

        TButton(
            text = "Reset & Shuffle",
            onClick = {
                dirtyNumbers.clear()
                dirtyNumbers.addAll(listOf(10, 3, 5, 7, 2, 1, 8, 6, 9, 4))
                dirtyNumbers.shuffle()
            },
        )
    }
}

/**
 * Practical application example: Data table with sortable columns
 */
@Composable
private fun DataTableExample() {
    // Sample data
    val users = useList(
        Person("Alice", 28),
        Person("Bob", 34),
        Person("Charlie", 22),
        Person("Diana", 41),
        Person("Evan", 19),
    )

    // Sort state
    var sortField by useState("name")
    var sortAscending by useState(true)

    // Sort users based on current sort settings
    val sortedUsers by if (sortField == "name") {
        if (sortAscending) {
            useSorted(users) { a, b -> a.name.compareTo(b.name) }
        } else {
            useSorted(users) { a, b -> b.name.compareTo(a.name) }
        }
    } else {
        if (sortAscending) {
            useSorted(users) { a, b -> a.age - b.age }
        } else {
            useSorted(users) { a, b -> b.age - a.age }
        }
    }

    // New user form
    var newName by useState("")
    var newAge by useState("")

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "This example demonstrates a practical application of useSorted for a sortable data table.",
            style = MaterialTheme.typography.bodySmall,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Table header
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TButton(
                text = "Name ${if (sortField == "name") {
                    if (sortAscending) {
                        "↑"
                    } else {
                        "↓"
                    }
                } else {
                    ""
                }}",
                onClick = {
                    if (sortField == "name") {
                        sortAscending = !sortAscending
                    } else {
                        sortField = "name"
                        sortAscending = true
                    }
                },
                modifier = Modifier.weight(1f),
            )

            TButton(
                text = "Age ${if (sortField == "age") {
                    if (sortAscending) {
                        "↑"
                    } else {
                        "↓"
                    }
                } else {
                    ""
                }}",
                onClick = {
                    if (sortField == "age") {
                        sortAscending = !sortAscending
                    } else {
                        sortField = "age"
                        sortAscending = true
                    }
                },
                modifier = Modifier.weight(1f),
            )
        }

        // Table rows
        sortedUsers.forEach { user ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(user.name, modifier = Modifier.weight(1f))
                Text(user.age.toString(), modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Add new user form
        Text("Add New User", style = MaterialTheme.typography.titleSmall)

        OutlinedTextField(
            value = newName,
            onValueChange = { newName = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = newAge,
            onValueChange = { newAge = it },
            label = { Text("Age") },
            modifier = Modifier.fillMaxWidth(),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TButton(
                text = "Add User",
                onClick = {
                    val age = newAge.toIntOrNull()
                    if (newName.isNotBlank() && age != null) {
                        users.add(Person(newName, age))
                        newName = ""
                        newAge = ""
                    }
                },
                enabled = newName.isNotBlank() && newAge.toIntOrNull() != null,
            )

            TButton(
                text = "Reset Table",
                onClick = {
                    users.clear()
                    users.addAll(
                        listOf(
                            Person("Alice", 28),
                            Person("Bob", 34),
                            Person("Charlie", 22),
                            Person("Diana", 41),
                            Person("Evan", 19),
                        ),
                    )
                },
            )
        }
    }
}
