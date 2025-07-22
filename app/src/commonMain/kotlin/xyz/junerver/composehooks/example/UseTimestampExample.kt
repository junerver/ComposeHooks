package xyz.junerver.composehooks.example

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
import xyz.junerver.compose.hooks.useList
import xyz.junerver.compose.hooks.useTimestamp
import xyz.junerver.compose.hooks.useTimestampRef
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.LogCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.SimpleContainer
import xyz.junerver.composehooks.ui.component.TButton
import xyz.junerver.composehooks.ui.component.randomBackground

/*
  Description: Example component for useTimestamp hook
  Author: Junerver
  Date: 2024/3/14-10:24
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * UseTimestampExample demonstrates the usage of useTimestamp and useTimestampRef hooks.
 *
 * This example shows how to:
 * - Create a timestamp that updates at regular intervals
 * - Pause and resume timestamp updates
 * - Use timestamp references for accessing the latest timestamp value
 *
 * @author Junerver
 * @since 2024/3/14
 */
@Composable
fun UseTimestampExample() {
    Surface {
        ScrollColumn(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "Timestamp Hook Examples",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            ExampleCard(title = "Basic Timestamp Example") {
                BasicTimestampExample()
            }

            Spacer(modifier = Modifier.height(16.dp))

            ExampleCard(title = "Timestamp Reference Example") {
                TimestampRefExample()
            }

            Spacer(modifier = Modifier.height(16.dp))

            ExampleCard(title = "Comparison Example") {
                ComparisonExample()
            }
        }
    }
}

/**
 * BasicTimestampExample demonstrates the basic usage of useTimestamp hook.
 *
 * This example shows:
 * - How to initialize a timestamp with a specific interval
 * - How to pause and resume the timestamp updates
 * - How to track the active state of the timestamp
 */
@Composable
fun BasicTimestampExample() {
    // Keep track of logs for demonstration purposes
    val logs = useList<String>()

    val (timestamp, pause, resume, isActive) = useTimestamp(
        optionsOf = {
            interval = 1.seconds
            callback = { time ->
                logs.add("Timestamp updated: $time")
            }
        },
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        // Configuration info
        Text(
            text = "Configuration: interval = 1 second",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Current timestamp display
        Text(
            text = "Current timestamp: ${timestamp.value}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth().randomBackground().padding(8.dp),
        )

        // Status display
        Text(
            text = "Status: ${if (isActive.value) "Active" else "Paused"}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp),
        )

        // Control buttons
        Row {
            TButton(text = "Pause", enabled = isActive.value) {
                pause()
                logs.add("Timestamp paused")
            }
            TButton(text = "Resume", enabled = !isActive.value) {
                resume()
                logs.add("Timestamp resumed")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Log display
        LogCard(title = "Activity Log:", logs = logs)
    }
}

/**
 * TimestampRefExample demonstrates the usage of useTimestampRef hook.
 *
 * This example shows:
 * - How to use a timestamp reference to access the latest timestamp value
 * - How to access the timestamp value from event handlers
 */
@Composable
fun TimestampRefExample() {
    // Keep track of logs for demonstration purposes
    val logs = useList<String>()

    val (timestamp, pause, resume, isActive) = useTimestampRef(
        optionsOf = {
            interval = 1.seconds
            callback = { time ->
                // Optional callback if you want to track updates
                logs.add("Ref timestamp updated: $time")
            }
        },
        autoResume = true,
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        // Configuration info
        Text(
            text = "Configuration: interval = 1 second, autoResume = true",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Current timestamp display
        Text(
            text = "Current timestamp ref: ${timestamp.current}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth().randomBackground().padding(8.dp),
        )

        // Status display
        Text(
            text = "Status: ${if (isActive.value) "Active" else "Paused"}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp),
        )

        // Control buttons
        Row {
            TButton(text = "Get Current Time") {
                val currentTime = timestamp.current
                logs.add("Retrieved time: $currentTime")
            }

            TButton(text = "Pause", enabled = isActive.value) {
                pause()
                logs.add("Timestamp ref paused")
            }

            TButton(text = "Resume", enabled = !isActive.value) {
                resume()
                logs.add("Timestamp ref resumed")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Log display
        LogCard(title = "Activity Log:", logs = logs)
    }
}

/**
 * ComparisonExample demonstrates the differences between useTimestamp and useTimestampRef hooks.
 *
 * This example shows:
 * - Side-by-side comparison of both hooks
 * - When to use each hook type
 * - How they behave differently in various scenarios
 */
@Composable
private fun ComparisonExample() {
    // Keep track of logs for demonstration purposes
    val logs = useList<String>()

    // Initialize both timestamp hooks with the same interval
    val (stateTimestamp, statePause, stateResume, stateIsActive) = useTimestamp(
        optionsOf = {
            interval = 2.seconds
            callback = { time ->
                logs.add("State timestamp updated: $time")
            }
        },
    )

    val (refTimestamp, refPause, refResume, refIsActive) = useTimestampRef(
        optionsOf = {
            interval = 2.seconds
            callback = { time ->
                logs.add("Ref timestamp updated: $time")
            }
        },
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        // Explanation text
        Text(
            text = "This example compares useTimestamp (State-based) and useTimestampRef (Reference-based) hooks.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Text(
            text = "Configuration: Both hooks use 2 second intervals",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        // Comparison display
        Row(modifier = Modifier.fillMaxWidth()) {
            // State-based timestamp
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = "State-based",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 4.dp),
                )

                SimpleContainer {
                    Text(
                        text = "${stateTimestamp.value}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth().randomBackground().padding(8.dp),
                    )
                }

                Text(
                    text = "Status: ${if (stateIsActive.value) "Active" else "Paused"}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }

            // Reference-based timestamp
            Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                Text(
                    text = "Reference-based",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 4.dp),
                )

                SimpleContainer {
                    Text(
                        text = "${refTimestamp.current}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth().randomBackground().padding(8.dp),
                    )
                }

                Text(
                    text = "Status: ${if (refIsActive.value) "Active" else "Paused"}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Control buttons
        Row {
            TButton(text = "Capture Values") {
                val stateValue = stateTimestamp.value
                val refValue = refTimestamp.current
                logs.add("Captured - State: $stateValue, Ref: $refValue")
            }

            TButton(text = "Toggle Both") {
                if (stateIsActive.value) {
                    statePause()
                    refPause()
                    logs.add("Both timestamps paused")
                } else {
                    stateResume()
                    refResume()
                    logs.add("Both timestamps resumed")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Key differences explanation
        Text(
            text = "Key Differences:",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 4.dp),
        )

        Text(
            text = "• State-based (useTimestamp): Triggers recomposition when updated",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 2.dp),
        )

        Text(
            text = "• Reference-based (useTimestampRef): Access latest value without recomposition",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Log display
        LogCard(title = "Activity Log:", logs = logs)
    }
}
