package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import xyz.junerver.compose.hooks.useTimeoutPoll

/**
 * useTimeoutPoll example
 * Demonstrates how to use the useTimeoutPoll function for polling operations
 */
@Composable
fun UseTimeoutPollExample() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("useTimeoutPoll Example")
            Spacer(modifier = Modifier.height(16.dp))

            // Basic usage example
            BasicTimeoutPollExample()

            Spacer(modifier = Modifier.height(24.dp))

            // Controlled polling example
            ControlledTimeoutPollExample()
        }
    }
}

/**
 * Basic useTimeoutPoll usage example
 */
@Composable
fun BasicTimeoutPollExample() {
    var count by remember { mutableStateOf(0) }

    // Use useTimeoutPoll to increment count every 2 seconds
    useTimeoutPoll(
        fn = {
            // Simulate async operation
            delay(500)
            count++
        },
        interval = 2.seconds,
        immediate = true
    )

    Column {
        Text("Basic usage: Auto count every 2 seconds")
        Spacer(modifier = Modifier.height(8.dp))
        Text("Current count: $count")
    }
}

/**
 * Controlled useTimeoutPoll usage example
 */
@Composable
fun ControlledTimeoutPollExample() {
    var pollCount by remember { mutableStateOf(0) }
    var lastPollTime by remember { mutableStateOf("") }

    // Use useTimeoutPoll and get control functions
    val timeoutPoll = useTimeoutPoll(
        fn = {
            // Simulate async operation
            delay(800)
            pollCount++
            lastPollTime = getCurrentTimeString()
        },
        interval = 3.seconds,
        optionsOf = {
            immediate = false // Don't start immediately
            immediateCallback = true // Execute callback immediately when resumed
        }
    )

    // Get polling status
    val isActive by timeoutPoll.isActive

    Column {
        Text("Controlled polling: Execute every 3 seconds")
        Spacer(modifier = Modifier.height(8.dp))

        Text("Polling status: ${if (isActive) "Running" else "Paused"}")
        Text("Poll count: $pollCount")
        Text("Last poll time: ${lastPollTime.ifEmpty { "None" }}")

        Spacer(modifier = Modifier.height(16.dp))

        // Control buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { timeoutPoll.resume() },
                enabled = !isActive
            ) {
                Text("Start Polling")
            }

            Button(
                onClick = { timeoutPoll.pause() },
                enabled = isActive
            ) {
                Text("Pause Polling")
            }
        }
    }
}

/**
 * Get current time string
 */
private fun getCurrentTimeString(): String {
    val now = Clock.System.now()
    val localDateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDateTime.hour}:${localDateTime.minute}:${localDateTime.second}"
}
