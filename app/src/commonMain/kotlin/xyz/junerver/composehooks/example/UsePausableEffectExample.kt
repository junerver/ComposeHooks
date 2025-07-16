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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import xyz.junerver.compose.hooks.useList
import xyz.junerver.compose.hooks.useMount
import xyz.junerver.compose.hooks.usePausableEffect
import xyz.junerver.compose.hooks.useState
import xyz.junerver.composehooks.ui.component.DividerSpacer
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.LogCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description: Example demonstrating the usePausableEffect hook
  Author: Junerver
  Date: 2025/7/3-16:22
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Example component demonstrating the usePausableEffect hook
 */
@Composable
fun UsePausableEffectExample() {
    Surface {
        ScrollColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Page title
            Text(
                text = "usePausableEffect Examples",
                style = MaterialTheme.typography.headlineMedium,
            )

            // Introduction text
            Text(
                text = "This hook provides a way to create effects that can be paused, resumed, and stopped. " +
                    "It's useful for controlling side effects that need to be temporarily disabled or permanently terminated.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            // Interactive Demo
            ExampleCard(title = "Interactive Demo") {
                InteractivePausableEffectDemo()
            }

            // Basic usage example
            ExampleCard(title = "Basic Usage") {
                BasicPausableEffectExample()
            }

            // Advanced control example
            ExampleCard(title = "Advanced Control") {
                AdvancedControlExample()
            }
        }
    }
}

/**
 * Interactive demonstration of the usePausableEffect hook
 *
 * Allows users to control effect execution through text input and control buttons
 */
@Composable
private fun InteractivePausableEffectDemo() {
    val (source, setSource) = useState("")
    val effectLogs = useList<String>()

    // Create a pausable effect that responds to source changes
    val effectHolder = usePausableEffect(source) {
        if (source.isNotEmpty()) {
            effectLogs.add("Effect executed: Changed to \"$source\"")
        }
    }

    // Destructure the effect holder to get control functions
    val (stop, pause, resume) = effectHolder

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Type something below to trigger the effect",
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = source,
            onValueChange = setSource,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Enter text here...") },
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            TButton(
                text = "Pause",
                onClick = {
                    effectLogs.add("Effect paused")
                    pause()
                },
                modifier = Modifier.weight(1f),
            )

            TButton(
                text = "Resume",
                onClick = {
                    effectLogs.add("Effect resumed")
                    resume()
                },
                modifier = Modifier.weight(1f),
            )

            TButton(
                text = "Stop",
                onClick = {
                    effectLogs.add("Effect stopped permanently")
                    stop()
                },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        TButton(
            text = "Clear Log",
            onClick = { effectLogs.clear() },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Display effect execution log
        LogCard(title = "Effect Log:", logs = effectLogs)
    }
}

/**
 * Basic example of using the usePausableEffect hook
 */
@Composable
private fun BasicPausableEffectExample() {
    val (counter, setCounter) = useState(0)
    val logs = useList<String>()

    // Create a simple pausable effect that logs counter changes
    val effectHolder = usePausableEffect(counter) {
        logs.add("Counter changed to: $counter")
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Basic Pausable Effect Demo",
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            TButton(
                text = "Increment",
                onClick = { setCounter(counter + 1) },
                modifier = Modifier.weight(1f),
            )

            TButton(
                text = "Pause Effect",
                onClick = {
                    effectHolder.pause()
                    logs.add("Effect paused")
                },
                modifier = Modifier.weight(1f),
            )

            TButton(
                text = "Resume Effect",
                onClick = {
                    effectHolder.resume()
                    logs.add("Effect resumed")
                },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display effect execution log
        LogCard(title = "Effect Log:", logs = logs)
    }
}

/**
 * Advanced example showing more control options for the usePausableEffect hook
 *
 * Demonstrates using multiple dependencies and conditional effect execution
 */
@Composable
private fun AdvancedControlExample() {
    var count1 by useState(0)
    var count2 by useState(0)
    var isAutoIncrement by useState(false)
    val logs = useList<String>()

    // Create a pausable effect with multiple dependencies
    val effectHolder = usePausableEffect(count1, count2) {
        logs.add("Effect executed: count1=$count1, count2=$count2")
    }

    // Auto-increment effect
    useMount {
        while (true) {
            delay(1000) // Wait for 1 second
            if (isAutoIncrement) {
                count1++
                logs.add("Auto-incremented count1 to $count1")
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Advanced Control Example",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "This example demonstrates using multiple dependencies and conditional effect execution.",
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Display current values
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Count 1: $count1",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "Count 2: $count2",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Manual control buttons
        Text(
            text = "Manual Controls:",
            style = MaterialTheme.typography.bodyMedium,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            TButton(
                text = "Count1 +1",
                onClick = { count1++ },
                modifier = Modifier.weight(1f),
            )

            TButton(
                text = "Count2 +1",
                onClick = { count2++ },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Auto-increment control
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Auto-increment:",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )

            TButton(
                text = if (isAutoIncrement) "Stop" else "Start",
                onClick = { isAutoIncrement = !isAutoIncrement },
                modifier = Modifier.weight(1f),
            )
        }

        DividerSpacer()

        // Effect control buttons
        Text(
            text = "Effect Controls:",
            style = MaterialTheme.typography.bodyMedium,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            TButton(
                text = "Pause",
                onClick = {
                    effectHolder.pause()
                    logs.add("Main effect paused")
                },
                modifier = Modifier.weight(1f),
            )

            TButton(
                text = "Resume",
                onClick = {
                    effectHolder.resume()
                    logs.add("Main effect resumed")
                },
                modifier = Modifier.weight(1f),
            )

            TButton(
                text = "Stop",
                onClick = {
                    effectHolder.stop()
                    logs.add("Main effect stopped permanently")
                },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        TButton(
            text = "Clear Log",
            onClick = { logs.clear() },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Display effect execution log
        LogCard(title = "Effect Log:", logs = logs)
    }
}
