package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.useBackToFrontEffect
import xyz.junerver.compose.hooks.useFrontToBackEffect
import xyz.junerver.compose.hooks.useGetState
import xyz.junerver.compose.hooks.useState
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description: Example component for useBackToFrontEffect and useFrontToBackEffect hooks
  Author: Junerver
  Date: 2024/3/11-11:09
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Returns the current local time as "HH:mm:ss", the cross-platform replacement for the
 * JVM-only `java.time.LocalTime.now()` previously used here. Uses `kotlin.time.Clock.System`
 * (stdlib, available on every target including wasmJs) rather than `kotlinx.datetime.Clock`,
 * whose `System` accessor is not resolvable on wasmJs.
 */
private fun nowLocalTimeString(): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    return "${now.hour.toString().padStart(2, '0')}:" +
        "${now.minute.toString().padStart(2, '0')}:" +
        "${now.second.toString().padStart(2, '0')}"
}

/**
 * Example component demonstrating the useBackToFrontEffect and useFrontToBackEffect hooks
 */
@Composable
fun UseBackFrontExample() {
    ScrollColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "useBackToFrontEffect & useFrontToBackEffect Examples",
            style = MaterialTheme.typography.headlineMedium,
        )

        // Interactive Demo
        InteractiveBackFrontDemo()

        // Basic usage example
        ExampleCard(title = "useBackToFrontEffect") {
            BackToFrontExample()
        }

        // Advanced configuration example
        ExampleCard(title = "useFrontToBackEffect") {
            FrontToBackExample()
        }
    }
}

/**
 * Interactive demo showing how useBackToFrontEffect and useFrontToBackEffect work
 */
@Composable
private fun InteractiveBackFrontDemo() {
    var backToFrontCount by useState(0)
    var frontToBackCount by useState(0)
    val (log, setLog) = useGetState("")

    useBackToFrontEffect {
        backToFrontCount++
        setLog("useBackToFrontEffect executed: $backToFrontCount")
    }

    useFrontToBackEffect {
        frontToBackCount++
        setLog("useFrontToBackEffect executed: $frontToBackCount")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Interactive Demo",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Display the counts
            Text(
                text = "Back to Front Count: $backToFrontCount",
                style = MaterialTheme.typography.bodyLarge,
            )

            Text(
                text = "Front to Back Count: $frontToBackCount",
                style = MaterialTheme.typography.bodyLarge,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Display the log
            Text(
                text = "Last Effect: ${log.value}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Instructions
            Text(
                text = "Switch to another app and come back to see useBackToFrontEffect execute.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            Text(
                text = "Switch to another app to see useFrontToBackEffect execute.",
                style = MaterialTheme.typography.bodySmall,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Reset button
            TButton(
                text = "Reset Counts",
                modifier = Modifier.fillMaxWidth(),
            ) {
                backToFrontCount = 0
                frontToBackCount = 0
                setLog("")
            }
        }
    }
}

/**
 * Example showing useBackToFrontEffect
 */
@Composable
private fun BackToFrontExample() {
    var executionCount by useState(0)
    val (lastExecution, setLastExecution) = useGetState("Never")

    useBackToFrontEffect {
        executionCount++
        setLastExecution("Executed at ${nowLocalTimeString()}")
    }

    Column {
        Text(
            text = "This effect runs when the app returns to foreground.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Text("Execution count: $executionCount")
        Text("Last execution: ${lastExecution.value}")
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Switch to another app and come back to test.",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

/**
 * Example showing useFrontToBackEffect
 */
@Composable
private fun FrontToBackExample() {
    var executionCount by useState(0)
    val (lastExecution, setLastExecution) = useGetState("Never")

    useFrontToBackEffect {
        executionCount++
        setLastExecution("Executed at ${nowLocalTimeString()}")
    }

    Column {
        Text(
            text = "This effect runs when the app enters background.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Text("Execution count: $executionCount")
        Text("Last execution: ${lastExecution.value}")
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Switch to another app to test.",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}