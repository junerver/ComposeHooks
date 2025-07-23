package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Arrangement
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
import kotlin.time.Instant
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.toLocalDateTime
import xyz.junerver.compose.hooks.useList
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.useTimeoutPoll
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.LogCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.TButton
import xyz.junerver.composehooks.ui.component.randomBackground
import xyz.junerver.composehooks.utils.formatToDecimalPlaces
import xyz.junerver.composehooks.utils.now

/*
  Description: Example component for useTimeoutPoll hook
  Author: Junerver
  Date: 2025/7/18-14:30
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * UseTimeoutPollExample demonstrates the useTimeoutPoll hook with various examples
 *
 * This screen showcases different ways to use the useTimeoutPoll hook:
 * 1. Basic usage with automatic polling
 * 2. Controlled usage with manual start/stop
 * 3. Real-time data collection simulation
 */
@Composable
fun UseTimeoutPollExample() {
    ScrollColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "useTimeoutPoll Examples",
            style = MaterialTheme.typography.headlineMedium,
        )

        // Basic Usage
        ExampleCard(title = "Basic useTimeoutPoll Example") {
            BasicTimeoutPollExample()
        }

        // Controlled Polling
        ExampleCard(title = "Controlled useTimeoutPoll Example") {
            ControlledTimeoutPollExample()
        }

        // Real-time Data Example
        ExampleCard(title = "Real-time Data useTimeoutPoll Example") {
            RealTimeDataExample()
        }
    }
}

/**
 * Basic example demonstrating the simplest usage of useTimeoutPoll hook
 *
 * This example shows that useTimeoutPoll:
 * 1. Automatically starts polling when the component is mounted
 * 2. Executes a callback function at regular intervals
 * 3. Can be used for simple auto-incrementing counters or data refresh
 */
@Composable
private fun BasicTimeoutPollExample() {
    var count by useState(0)
    val logs = useList<String>()

    // Add initial log entry
    LaunchedEffect(Unit) {
        logs.add("Component mounted at ${now()}")
        logs.add("Polling started with 2 second interval")
    }

    // Use useTimeoutPoll to increment count every 2 seconds
    useTimeoutPoll(
        fn = {
            // Simulate async operation
            delay(500)
            count++
            logs.add("Count incremented to $count at ${now()}")
        },
        interval = 2.seconds,
        immediate = true,
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        // Description of the example
        Text(
            text = "useTimeoutPoll automatically starts polling when the component is mounted and executes the callback at regular intervals.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        // Configuration information
        Text(
            text = "Configuration: interval=2s, immediate=true",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        // Display the current count with a colored background
        Text(
            text = "Current count: $count",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .randomBackground()
                .padding(16.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Log display
        LogCard(title = "Log:", logs = logs)
    }
}

/**
 * Interactive example demonstrating the control functions of useTimeoutPoll hook
 *
 * This example shows how to:
 * 1. Start polling manually using the resume() function
 * 2. Pause polling using the pause() function
 * 3. Configure polling with custom options
 * 4. Track the active status of polling
 */
@Composable
private fun ControlledTimeoutPollExample() {
    var pollCount by useState(0)
    var lastPollTime by useState("")
    val logs = useList<String>()

    // Add initial log entry
    LaunchedEffect(Unit) {
        logs.add("Component mounted at ${now()}")
        logs.add("Polling is initially paused (immediate=false)")
    }

    // Use useTimeoutPoll and get control functions
    val timeoutPoll = useTimeoutPoll(
        fn = {
            // Simulate async operation
            delay(800)
            pollCount++
            lastPollTime = formatTime()
            logs.add("Poll executed at ${now()}, count: $pollCount")
        },
        interval = 3.seconds,
        optionsOf = {
            immediate = false // Don't start immediately
            immediateCallback = true // Execute callback immediately when resumed
        },
    )

    // Get polling status
    val isActive by timeoutPoll.isActive

    Column(modifier = Modifier.fillMaxWidth()) {
        // Description of the example
        Text(
            text = "useTimeoutPoll provides control functions to pause and resume polling. This example demonstrates manual control of the polling process.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        // Configuration information
        Text(
            text = "Configuration: interval=3s, immediate=false, immediateCallback=true",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        // Display the current status with a colored background
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .randomBackground()
                .padding(16.dp),
        ) {
            Text(
                text = "Polling status: ${if (isActive) "Running" else "Paused"}",
                style = MaterialTheme.typography.titleMedium,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Poll count: $pollCount",
                style = MaterialTheme.typography.bodyLarge,
            )

            Text(
                text = "Last poll time: ${lastPollTime.ifEmpty { "None" }}",
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Control buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TButton(
                text = "Start Polling",
                enabled = !isActive,
                onClick = {
                    logs.add("Polling started at ${now()}")
                    timeoutPoll.resume()
                },
                modifier = Modifier.weight(1f),
            )

            TButton(
                text = "Pause Polling",
                enabled = isActive,
                onClick = {
                    logs.add("Polling paused at ${now()}")
                    timeoutPoll.pause()
                },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Log display
        LogCard(title = "Log:", logs = logs)
    }
}

/**
 * Real-time data example demonstrating useTimeoutPoll for simulating data updates
 *
 * This example shows how to:
 * 1. Use useTimeoutPoll for real-time data updates
 * 2. Simulate a data source that changes over time
 * 3. Display a history of data points
 */
@Composable
private fun RealTimeDataExample() {
    // Simulated data points
    val dataPoints = useList<DataPoint>()
    val logs = useList<String>()

    // Add initial log entry
    LaunchedEffect(Unit) {
        logs.add("Component mounted at ${now()}")
        logs.add("Data collection started with 2 second interval")
    }

    // Current value state
    var currentValue by useState(0.0)

    // Use useTimeoutPoll to fetch new data every 2 seconds
    val timeoutPoll = useTimeoutPoll(
        fn = {
            // Simulate data fetching with some delay
            delay(300)

            // Generate a new random value (simulating sensor data or stock price)
            val newValue = (currentValue + (-5..5).random() * 0.5).coerceIn(0.0, 100.0)
            currentValue = newValue

            // Add to data history
            val timestamp = now()
            dataPoints.add(DataPoint(timestamp, newValue))
            if (dataPoints.size > 10) {
                dataPoints.removeAt(0)
            }

            logs.add("New data point collected: $newValue at ${formatTime(timestamp)}")
        },
        interval = 2.seconds,
    )

    // Get polling status
    val isActive by timeoutPoll.isActive

    Column(modifier = Modifier.fillMaxWidth()) {
        // Description of the example
        Text(
            text = "This example demonstrates using useTimeoutPoll for real-time data collection, such as sensor readings, stock prices, or other time-series data.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        // Display the current value with a colored background
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .randomBackground()
                .padding(16.dp),
        ) {
            Text(
                text = "Current Value: ${currentValue.formatToDecimalPlaces(1)}",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
            )

            Text(
                text = "Status: ${if (isActive) "Collecting data..." else "Paused"}",
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Display data history
            Text(
                text = "Data History:",
                style = MaterialTheme.typography.titleSmall,
            )

            dataPoints.takeLast(5).forEach { dataPoint ->
                Text(
                    text = "${formatTime(dataPoint.timestamp)}: ${dataPoint.value.formatToDecimalPlaces(1)}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Control buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TButton(
                text = "Start Collection",
                enabled = !isActive,
                onClick = {
                    logs.add("Data collection started at ${now()}")
                    timeoutPoll.resume()
                },
                modifier = Modifier.weight(1f),
            )

            TButton(
                text = "Pause Collection",
                enabled = isActive,
                onClick = {
                    logs.add("Data collection paused at ${now()}")
                    timeoutPoll.pause()
                },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Log display
        LogCard(title = "Log:", logs = logs)
    }
}

/**
 * Data point class for storing time-series data
 */
private data class DataPoint(val timestamp: Instant, val value: Double)

/**
 * Format time from Instant to a readable string
 */
@OptIn(FormatStringsInDatetimeFormats::class)
private fun formatTime(time: Instant = now()): String = time.toLocalDateTime(TimeZone.currentSystemDefault()).format(
    LocalDateTime.Format {
        byUnicodePattern("HH:mm:ss")
    },
)
