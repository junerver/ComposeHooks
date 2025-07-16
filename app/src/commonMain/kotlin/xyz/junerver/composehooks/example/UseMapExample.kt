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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.random.Random
import xyz.junerver.compose.hooks.useList
import xyz.junerver.compose.hooks.useMap
import xyz.junerver.compose.hooks.useState
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.LogCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description: Examples demonstrating the usage of useMap hook
  Author: Junerver
  Date: 2024/3/8-14:47
  Email: junerver@gmail.com
  Version: v1.0
*/
@Composable
fun UseMapExample() {
    ScrollColumn(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "useMap Examples",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Text(
            text = "This hook provides a convenient way to create and manage a mutable map state that triggers recomposition when modified.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Interactive Demo
        ExampleCard(title = "Interactive Demo") {
            InteractiveDemo()
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Basic Usage Example
        ExampleCard(title = "Basic Usage") {
            BasicUsageExample()
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Advanced Usage Example
        ExampleCard(title = "Advanced Usage - Type Safety") {
            TypeSafetyExample()
        }
    }
}

/**
 * Basic usage example of useMap hook
 * Shows simple operations like get, set, and remove
 */
@Composable
private fun BasicUsageExample() {
    // Create a map with string keys and integer values
    val userScores = useMap(
        "Alice" to 85,
        "Bob" to 92,
        "Charlie" to 78,
    )

    val logs = useList<String>()

    Column(modifier = Modifier.fillMaxWidth()) {
        // Display current map
        Text(
            text = "User Scores:",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Column(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
        ) {
            userScores.forEach { (user, score) ->
                Text(
                    text = "$user: $score points",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 2.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TButton(
                text = "Add David",
                onClick = {
                    userScores["David"] = 88
                    logs.add("Added David with score 88")
                },
                modifier = Modifier.weight(1f),
            )

            TButton(
                text = "Improve Alice",
                onClick = {
                    val currentScore = userScores["Alice"] ?: 0
                    userScores["Alice"] = currentScore + 5
                    logs.add("Improved Alice's score to ${userScores["Alice"]}")
                },
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TButton(
                text = "Remove Bob",
                onClick = {
                    if (userScores.containsKey("Bob")) {
                        val score = userScores["Bob"]
                        userScores.remove("Bob")
                        logs.add("Removed Bob (score was $score)")
                    } else {
                        logs.add("Bob not found in scores")
                    }
                },
                modifier = Modifier.weight(1f),
            )

            TButton(
                text = "Reset Scores",
                onClick = {
                    userScores.clear()
                    userScores["Alice"] = 85
                    userScores["Bob"] = 92
                    userScores["Charlie"] = 78
                    logs.add("Reset all scores to initial values")
                },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Log display
        LogCard(title = "Operation Log:", logs = logs)
    }
}

/**
 * Advanced usage example demonstrating type safety with useMap
 * Shows how to use different types for keys and values
 */
@Suppress("LocalVariableName")
@Composable
private fun TypeSafetyExample() {
    // Create a map with enum keys and complex values
    data class UserInfo(val name: String, val age: Int, val isActive: Boolean)

    // Define enum outside of the composable function
    val ADMIN = "ADMIN"
    val MODERATOR = "MODERATOR"
    val MEMBER = "MEMBER"
    val GUEST = "GUEST"

    val userRoleMap = useMap(
        ADMIN to UserInfo("Admin User", 35, true),
        MODERATOR to UserInfo("Mod User", 28, true),
        MEMBER to UserInfo("Regular Member", 24, false),
    )

    val logs = useList<String>()
    var selectedRole by useState(ADMIN)

    Column(modifier = Modifier.fillMaxWidth()) {
        // Display current map
        Text(
            text = "User Roles and Information:",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Column(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
        ) {
            userRoleMap.forEach { (role, info) ->
                Text(
                    text = "$role: ${info.name}, ${info.age} years old, ${if (info.isActive) "Active" else "Inactive"}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 2.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Role selection buttons
        Text("Selected Role: $selectedRole")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf(ADMIN, MODERATOR, MEMBER, GUEST).forEach { role ->
                TButton(
                    text = role,
                    onClick = { selectedRole = role },
                    modifier = Modifier.weight(1f),
                    enabled = role != selectedRole,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TButton(
                text = "Toggle Active",
                onClick = {
                    userRoleMap[selectedRole]?.let { currentInfo ->
                        val updatedInfo = currentInfo.copy(isActive = !currentInfo.isActive)
                        userRoleMap[selectedRole] = updatedInfo
                        logs.add("$selectedRole: Set active status to ${updatedInfo.isActive}")
                    } ?: run {
                        logs.add("No user info found for $selectedRole")
                    }
                },
                modifier = Modifier.weight(1f),
            )

            TButton(
                text = "Add Guest",
                onClick = {
                    if (!userRoleMap.containsKey(GUEST)) {
                        userRoleMap[GUEST] = UserInfo("Guest User", 0, true)
                        logs.add("Added GUEST role with default info")
                    } else {
                        logs.add("GUEST role already exists")
                    }
                },
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TButton(
                text = "Remove Selected",
                onClick = {
                    if (userRoleMap.containsKey(selectedRole)) {
                        userRoleMap.remove(selectedRole)
                        logs.add("Removed $selectedRole role")
                    } else {
                        logs.add("$selectedRole role not found")
                    }
                },
                modifier = Modifier.weight(1f),
            )

            TButton(
                text = "Increment Age",
                onClick = {
                    userRoleMap[selectedRole]?.let { currentInfo ->
                        val updatedInfo = currentInfo.copy(age = currentInfo.age + 1)
                        userRoleMap[selectedRole] = updatedInfo
                        logs.add("$selectedRole: Incremented age to ${updatedInfo.age}")
                    } ?: run {
                        logs.add("No user info found for $selectedRole")
                    }
                },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Log display
        LogCard(title = "Operation Log:", logs = logs)
    }
}

/**
 * Interactive demonstration of useMap hook
 * Allows users to experiment with the hook in real-time
 */
@Composable
private fun InteractiveDemo() {
    // Create a map state with initial key-value pairs
    val mapState = useMap<Any, String>(
        1 to "first",
        2 to "second",
    )

    // State for new key and value inputs
    var newKey by useState("")
    var newValue by useState("")

    // Log of operations
    val logs = useList<String>()

    Column(modifier = Modifier.fillMaxWidth()) {
        // Display current map with a highlighted background
        Text(
            text = "Current Map:",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Column(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
        ) {
            if (mapState.isEmpty()) {
                Text(
                    text = "Map is empty",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                mapState.forEach { (key, value) ->
                    Text(
                        text = "$key : $value",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 2.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input fields for new key-value pair
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = newKey,
                onValueChange = { newKey = it },
                label = { Text("Key") },
                modifier = Modifier.weight(1f).padding(end = 8.dp),
            )

            OutlinedTextField(
                value = newValue,
                onValueChange = { newValue = it },
                label = { Text("Value") },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Control buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TButton(
                text = "Add Entry",
                onClick = {
                    if (newKey.isNotEmpty()) {
                        try {
                            val key = newKey.toIntOrNull() ?: newKey
                            mapState[key] = newValue.ifEmpty { "value-${Random.nextInt(100)}" }
                            logs.add("Added: $key -> ${mapState[key]}")
                            newKey = ""
                            newValue = ""
                        } catch (e: Exception) {
                            logs.add("Error: ${e.message}")
                        }
                    }
                },
                modifier = Modifier.weight(1f),
            )

            TButton(
                text = "Remove Last",
                onClick = {
                    if (mapState.isNotEmpty()) {
                        val lastKey = mapState.keys.last()
                        val lastValue = mapState[lastKey]
                        mapState.remove(lastKey)
                        logs.add("Removed: $lastKey -> $lastValue")
                    } else {
                        logs.add("Cannot remove: Map is empty")
                    }
                },
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TButton(
                text = "Update Random",
                onClick = {
                    if (mapState.isNotEmpty()) {
                        val randomKey = mapState.keys.random()
                        val oldValue = mapState[randomKey]
                        val newRandomValue = "updated-${Random.nextInt(100)}"
                        mapState[randomKey] = newRandomValue
                        logs.add("Updated: $randomKey -> $oldValue to $newRandomValue")
                    } else {
                        logs.add("Cannot update: Map is empty")
                    }
                },
                modifier = Modifier.weight(1f),
            )

            TButton(
                text = "Clear All",
                onClick = {
                    if (mapState.isNotEmpty()) {
                        mapState.clear()
                        logs.add("Cleared all entries")
                    } else {
                        logs.add("Map is already empty")
                    }
                },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Log display
        LogCard(title = "Operation Log:", logs = logs)
    }
}
