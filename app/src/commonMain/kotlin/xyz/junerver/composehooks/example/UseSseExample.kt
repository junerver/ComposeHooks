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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.usses.useSse
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description: Example component for useSse hook
  Author: Junerver
  Date: 2024/3/11-11:09
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Example component demonstrating the useSse hook
 */
@Composable
fun UseSseExample() {
    ScrollColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "useSse Examples",
            style = MaterialTheme.typography.headlineMedium,
        )

        // Interactive Demo
        InteractiveSseDemo()

        // Basic usage example
        ExampleCard(title = "Basic Usage") {
            BasicSseExample()
        }

        // Manual control example
        ExampleCard(title = "Manual Control") {
            ManualSseExample()
        }
    }
}

/**
 * Interactive demo showing how useSse works
 */
@Composable
private fun InteractiveSseDemo() {
    // Simulated SSE stream function
    val streamFn: suspend (String) -> kotlinx.coroutines.flow.Flow<String> = { url ->
        flow {
            for (i in 1..10) {
                delay(1000) // Simulate network delay
                emit("Event $i from $url")
            }
        }
    }

    val (lastEvent, isStreaming, error, params, send, cancel, refresh) = useSse(
        streamFn = streamFn,
        optionsOf = {
            defaultParams = "https://api.example.com/events"
            onEvent = { event ->
                println("Received event: $event")
            }
        },
    )

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

            // Status display
            Text(
                text = "Status: ${if (isStreaming.value) "Streaming" else "Idle"}",
                style = MaterialTheme.typography.bodyLarge,
            )

            Text(
                text = "Last Event: ${lastEvent.value ?: "None"}",
                style = MaterialTheme.typography.bodyMedium,
            )

            if (error.value != null) {
                Text(
                    text = "Error: ${error.value?.message}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Control buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TButton(
                    text = "Start Stream",
                    modifier = Modifier.weight(1f),
                ) {
                    send("https://api.example.com/events")
                }

                TButton(
                    text = "Cancel",
                    modifier = Modifier.weight(1f),
                ) {
                    cancel()
                }

                TButton(
                    text = "Refresh",
                    modifier = Modifier.weight(1f),
                ) {
                    refresh()
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Instructions
            Text(
                text = "Click 'Start Stream' to begin receiving simulated SSE events. " +
                    "Each event arrives after 1 second delay.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

/**
 * Basic example showing how to use useSse
 */
@Composable
private fun BasicSseExample() {
    // Simulated SSE stream function
    val streamFn: suspend (Unit) -> kotlinx.coroutines.flow.Flow<String> = {
        flow {
            for (i in 1..5) {
                delay(500)
                emit("Message $i")
            }
        }
    }

    val (lastEvent, isStreaming) = useSse(
        streamFn = streamFn,
        optionsOf = {
            defaultParams = Unit
        },
    )

    Column {
        Text(
            text = "This example demonstrates basic useSse usage with auto-start.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Text("Status: ${if (isStreaming.value) "Streaming" else "Idle"}")
        Text("Last Event: ${lastEvent.value ?: "None"}")
    }
}

/**
 * Manual control example showing how to manually start and stop SSE streams
 */
@Composable
private fun ManualSseExample() {
    var eventCount by useState(0)

    // Simulated SSE stream function
    val streamFn: suspend (String) -> kotlinx.coroutines.flow.Flow<String> = { url ->
        flow {
            for (i in 1..3) {
                delay(800)
                emit("Event $i from $url")
            }
        }
    }

    val (lastEvent, isStreaming, error, _, send, cancel) = useSse(
        streamFn = streamFn,
        optionsOf = {
            manual = true // Don't auto-start
            onEvent = { event ->
                eventCount++
            }
        },
    )

    Column {
        Text(
            text = "This example demonstrates manual control of SSE streams.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Text("Status: ${if (isStreaming.value) "Streaming" else "Idle"}")
        Text("Events Received: $eventCount")
        Text("Last Event: ${lastEvent.value ?: "None"}")

        if (error.value != null) {
            Text("Error: ${error.value?.message}")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TButton(
                text = "Start",
                modifier = Modifier.weight(1f),
            ) {
                send("https://api.example.com/manual")
            }

            TButton(
                text = "Stop",
                modifier = Modifier.weight(1f),
            ) {
                cancel()
            }
        }
    }
}