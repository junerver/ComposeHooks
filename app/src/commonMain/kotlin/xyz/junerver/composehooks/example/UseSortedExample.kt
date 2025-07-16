package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.junerver.compose.hooks.useList
import xyz.junerver.compose.hooks.useSorted

/**
 * Example data class for demonstration
 */
data class Person(val name: String, val age: Int)

/**
 * Example component demonstrating the useSorted hook
 */
@Composable
fun UseSortedExample() {
    // Create a list of numbers
    val numbers = useList(10, 3, 5, 7, 2, 1, 8, 6, 9, 4)

    // Sort numbers using the default comparison function
    // Note: The default comparison function automatically detects numeric types and sorts them by value
    val sortedNumbers by useSorted(numbers)

    // Create a list of Person objects
    val people = useList(
        Person("John", 40),
        Person("Joe", 20),
        Person("Jane", 30),
        Person("Jenny", 22),
    )


    // Sort by age
    val sortedByAge by useSorted(people) { a, b -> a.age - b.age }

    // Sort by name
    val sortedByName by useSorted(people) { a, b -> a.name.compareTo(b.name) }

    // Using dirty mode (modifies the source array)
    val dirtyNumbers = useList(10, 3, 5, 7, 2, 1, 8, 6, 9, 4)
    val sortedDirtyNumbers by useSorted(
        dirtyNumbers,
        optionsOf = {
            dirty = true
        },
    )

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Original number list: ${numbers.joinToString()}")
        Text("Sorted number list: ${sortedNumbers.joinToString()}")

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(Modifier.fillMaxWidth(), DividerDefaults.Thickness, DividerDefaults.color)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Original people list:")
        people.forEach { person ->
            Text("${person.name}, ${person.age} years old")
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Sorted by age:")
        sortedByAge.forEach { person ->
            Text("${person.name}, ${person.age} years old")
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Sorted by name:")
        sortedByName.forEach { person ->
            Text("${person.name}, ${person.age} years old")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Divider(modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))

        Text("Dirty mode original list: ${dirtyNumbers.joinToString()}")
        Text("Dirty mode sorted list: ${sortedDirtyNumbers.joinToString()}")
        Text("Note: The original list has been modified")

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                // Reset number list
                numbers.clear()
                numbers.addAll(listOf(10, 3, 5, 7, 2, 1, 8, 6, 9, 4))

                // Reset dirty mode number list
                dirtyNumbers.clear()
                dirtyNumbers.addAll(listOf(10, 3, 5, 7, 2, 1, 8, 6, 9, 4))

                // Shuffle people list
                people.shuffle()
            },
        ) {
            Text("Reset Lists")
        }
    }
}
