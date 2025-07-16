package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import xyz.junerver.compose.hooks.useLatestRef
import xyz.junerver.compose.hooks.useList
import xyz.junerver.compose.hooks.useState
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.LogCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.TButton
import xyz.junerver.composehooks.ui.component.randomBackground

/*
  Description: Examples demonstrating the usage of useLatest hook
  Author: Junerver
  Date: 2024/3/8-13:38
  Email: junerver@gmail.com
  Version: v1.0
*/
@Composable
fun UseLatestExample() {
    ScrollColumn(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "useLatest Examples",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Text(
            text = "This hook is used to get the latest value of a variable, solving closure problems in asynchronous contexts.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Interactive Demo
        ExampleCard(title = "Interactive Demo") {
            InteractiveDemo()
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Comparison Example
        ExampleCard(title = "Closure Problem Comparison") {
            ClosureProblemComparison()
        }
    }
}

/**
 * Interactive demonstration of useLatest hook
 * Allows users to experiment with the hook in real-time
 */
@Composable
private fun InteractiveDemo() {
    // State for the counter
    val (count, setCount) = useState(0)
    // Create a latest reference to the count
    val latestRef = useLatestRef(value = count)
    // State to track if the counter is running
    val (isRunning, setIsRunning) = useState(false)
    // Log of operations
    val logs = useList<String>()

    // Effect to increment the counter when running
    LaunchedEffect(isRunning) {
        if (isRunning) {
            // Use a safer approach with repeat instead of while(true)
            while (isRunning) {
                delay(1.seconds)
                val newValue = latestRef.current + 1
                setCount(newValue)
                logs.add("Incremented to $newValue")
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Display current count with a highlighted background
        Text(
            text = "Current count: $count",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.randomBackground().padding(8.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Configuration explanation
        Text(
            text = "Configuration: Using useLatestRef to access the most recent count value",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        // Control buttons
        Row(modifier = Modifier.padding(bottom = 16.dp)) {
            TButton(
                text = if (isRunning) "Stop" else "Start",
                onClick = { setIsRunning(!isRunning) },
            )
            Spacer(modifier = Modifier.weight(1f))
            TButton(
                text = "Reset",
                onClick = {
                    setCount(0)
                    setIsRunning(false)
                    logs.clear()
                    logs.add("Counter reset to 0")
                },
            )
        }

        // Manual increment button
        TButton(
            text = "Increment Manually",
            onClick = {
                setCount(count + 1)
                logs.add("Manually incremented to ${count + 1}")
                if (logs.size > 10) logs.removeAt(0)
            },
            modifier = Modifier.padding(bottom = 16.dp),
        )

        // Log display
        LogCard(title = "Operation Log:", logs = logs)
    }
}

/**
 * Comparison between different approaches to handling state in asynchronous contexts
 * Demonstrates the closure problem and how useLatest solves it
 */
@Composable
private fun ClosureProblemComparison() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "This example demonstrates the closure problem in asynchronous contexts and how useLatest solves it.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Text(
            text = "Note: useLatestRef updates the 'current' value inside the container only when the component recomposes. It only updates after recomposition occurs (when the parameter to useLatestRef changes).",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        // Example 1: Using delegate (works correctly)
        ExampleCard(title = "1. Using Delegate (works correctly)") {
            NormalExample()
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Example 2: Using useState without useLatest (closure problem)
        ExampleCard(title = "2. Using useState without useLatest (closure problem)") {
            UseStateWithoutLatestExample()
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Example 3: Using useState with useLatest (solution)
        ExampleCard(title = "3. Using useState with useLatest (solution)") {
            UseStateWithLatestExample()
        }
    }
}

/**
 * Example using delegate syntax (works correctly)
 */
@Composable
private fun NormalExample() {
    var count by useState(0)
    LaunchedEffect(key1 = Unit) {
        repeat(10) {
            delay(1.seconds)
            count += 1
        }
    }
    Text(
        text = "Count: $count",
        modifier = Modifier.padding(vertical = 8.dp),
    )
    Text(
        text = "Using delegate syntax works correctly because the compiler handles the reference for us.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/**
 * Example using useState without useLatest (demonstrates closure problem)
 */
@Composable
private fun UseStateWithoutLatestExample() {
    val (count, setCount) = useState(0)
    LaunchedEffect(key1 = Unit) {
        repeat(10) {
            delay(1.seconds)
            // Closure problem: count is captured at the time LaunchedEffect runs
            // and doesn't update with each iteration
            setCount(count + 1)
        }
    }
    Text(
        text = "Count: $count",
        modifier = Modifier.padding(vertical = 8.dp),
    )
    Text(
        text = "This example demonstrates the closure problem. The count will only increment to 1 because the captured value doesn't update.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/**
 * Example using useState with useLatest (solves closure problem)
 */
@Composable
private fun UseStateWithLatestExample() {
    val (count, setCount) = useState(0)
    val latestRef = useLatestRef(value = count)
    LaunchedEffect(key1 = Unit) {
        repeat(10) {
            delay(1.seconds)
            // Using latestRef.current always gives us the latest value
            setCount(latestRef.current + 1)
        }
    }
    Text(
        text = "Count: $count",
        modifier = Modifier.padding(vertical = 8.dp),
    )
    Text(
        text = "Using useLatest solves the closure problem by providing a reference that always contains the current value.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
