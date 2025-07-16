package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.useAsync
import xyz.junerver.compose.hooks.useGetState
import xyz.junerver.compose.hooks.useList
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.LogCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.TButton
import xyz.junerver.composehooks.utils.now

/*
  Description: Example component for useGetState hook
  Author: Junerver
  Date: 2024/5/10-10:10
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Example component demonstrating the useGetState hook
 */
@Composable
fun UseGetStateExample() {
    Surface {
        ScrollColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "useGetState Examples",
                style = MaterialTheme.typography.headlineMedium,
            )

            // Basic usage example
            ExampleCard(title = "Basic Usage") {
                BasicUsageExample()
            }

            // Closure problem solution example
            ExampleCard(title = "Solving Closure Problems") {
                ClosureProblemExample()
            }

            // Rapid state updates example
            ExampleCard(title = "Handling Rapid State Updates") {
                RapidStateUpdatesExample()
            }
        }
    }
}

/**
 * Demonstrates the basic usage of useGetState hook
 */
@Composable
private fun BasicUsageExample() {
    val (state, setState, getState) = useGetState("Hello, useGetState!")

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Current state: ${state.value}",
            style = MaterialTheme.typography.bodyLarge,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TButton(text = "Update") {
                setState("Updated at ${now()}")
            }

            TButton(text = "Get Latest") {
                // Demonstrates getting the latest state value
                setState("Latest value was: ${getState()}")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "useGetState provides a getter function to access the latest state value",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Demonstrates how useGetState solves closure problems
 */
@Composable
private fun ClosureProblemExample() {
    val (state, setState) = useGetState("Initial state")
    val asyncRun = useAsync()
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Current state: ${state.value}",
            style = MaterialTheme.typography.bodyLarge,
        )

        Spacer(modifier = Modifier.height(8.dp))

        TButton(text = "Start Delayed Updates") {
            // This demonstrates how useGetState solves closure problems
            asyncRun {
                repeat(5) { index ->
                    delay(1.seconds)
                    setState { currentState -> "$currentState [$index]" }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "With useGetState, you don't need useLatestRef to access the latest state in closures",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Demonstrates how useGetState handles rapid state updates
 */
@Composable
private fun RapidStateUpdatesExample() {
    val (state, setState) = useGetState("Ready")
    val updateLog = useList<String>()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Current state: ${state.value}",
            style = MaterialTheme.typography.bodyLarge,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TButton(text = "Trigger Rapid Updates") {
                repeat(20) { index ->
                    setState("Update #$index")
                    val timestamp = now()
                    val logEntry = "State changed to: ${state.value} at $timestamp"
                    updateLog.add(logEntry)
                }
            }

            TButton(text = "Clear Log") {
                updateLog.clear()
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "useGetState properly handles rapid state updates without losing intermediate states",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Update log display
        LogCard(title = "Update Log:", logs = updateLog)
    }
}
