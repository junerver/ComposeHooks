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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.seconds
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.useList
import xyz.junerver.compose.hooks.useResetState
import xyz.junerver.compose.hooks.useTimeoutFn
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.LogCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.TButton
import xyz.junerver.composehooks.ui.component.randomBackground
import xyz.junerver.composehooks.utils.now

/*
  Description: Example component for useTimeoutFn hook
  Author: Junerver
  Date: 2025/6/25-10:10
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Example component demonstrating the useTimeoutFn hook
 *
 * useTimeoutFn is a hook that executes a callback after a specified delay,
 * but unlike useTimeout, it provides control functions to start, stop, and
 * check the status of the timeout.
 */
@Composable
fun UseTimeoutFnExample() {
    ScrollColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "useTimeoutFn Examples",
            style = MaterialTheme.typography.headlineMedium,
        )

        // Basic Usage
        ExampleCard(title = "Basic Usage") {
            BasicTimeoutFnExample()
        }

        // Interactive Example
        ExampleCard(title = "Interactive Control") {
            InteractiveTimeoutFnExample()
        }
    }
}

/**
 * Basic example demonstrating the simplest usage of useTimeoutFn hook
 *
 * This example shows that useTimeoutFn:
 * 1. Executes a callback after a specified delay (3 seconds)
 * 2. Provides a way to check if the timeout is pending
 * 3. Automatically starts when the component is mounted
 */
@Composable
private fun BasicTimeoutFnExample() {
    val logs = useList<String>()
    val (text, setText) = useResetState("Please wait for 3 seconds")

    // Add initial log entry
    LaunchedEffect(Unit) {
        logs.add("Component mounted at ${now()}")
        logs.add("Timeout scheduled for 3 seconds...")
    }

    // When component is mounted, execute this block after 3 seconds
    val (isPending, _, _) = useTimeoutFn(
        {
            setText("Done!")
            logs.add("Timeout completed at ${now()}")
        },
        3.seconds,
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        // Description of the example
        Text(
            text = "useTimeoutFn automatically starts when the component is mounted and executes the callback after the specified delay. The isPending state can be used to track the timeout status.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        // Display the current status with a colored background
        Text(
            text = text.value,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .randomBackground()
                .padding(16.dp),
        )

        // Display the pending status
        Text(
            text = "Status: ${if (isPending.value) "Pending..." else "Completed"}",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isPending.value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Log display
        LogCard(title = "Log:", logs = logs)
    }
}

/**
 * Interactive example demonstrating the control functions of useTimeoutFn hook
 *
 * This example shows how to:
 * 1. Start a timeout manually
 * 2. Stop a timeout before it completes
 * 3. Track the pending status of a timeout
 */
@Composable
private fun InteractiveTimeoutFnExample() {
    val logs = useList<String>()
    val (text, setText, _, reset) = useResetState("Click 'Start' to begin countdown")

    // Initialize useTimeoutFn with manual start
    val (isPending, start, stop) = useTimeoutFn(
        fn = {
            setText("Done!")
            logs.add("Timeout completed at ${now()}")
        },
        3.seconds,
        { immediate = false },
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        // Description of the example
        Text(
            text = "useTimeoutFn provides control functions to start and stop the timeout. This example demonstrates manual control of the timeout process.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        // Display the current status with a colored background
        Text(
            text = text.value,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .randomBackground()
                .padding(16.dp),
        )

        // Display the pending status
        Text(
            text = "Status: ${if (isPending.value) "Pending..." else "Ready"}",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isPending.value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        // Control buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TButton(
                text = "Start",
                enabled = !isPending.value,
                onClick = {
                    logs.add("Started timeout at ${now()}")
                    setText("Waiting for 3 seconds...")
                    start()
                },
                modifier = Modifier.weight(1f),
            )

            TButton(
                text = "Stop",
                enabled = isPending.value,
                onClick = {
                    logs.add("Stopped timeout at ${now()}")
                    setText("Timeout stopped")
                    stop()
                },
                modifier = Modifier.weight(1f),
            )

            TButton(
                text = "Reset",
                onClick = {
                    logs.add("Reset at ${now()}")
                    reset()
                    if (isPending.value) {
                        stop()
                    }
                },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Log display
        LogCard(title = "Log:", logs = logs)
    }
}
