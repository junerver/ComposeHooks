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
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.useGetState
import xyz.junerver.compose.hooks.useList
import xyz.junerver.compose.hooks.useTimeout
import xyz.junerver.compose.hooks.useUpdate
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.LogCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.TButton
import xyz.junerver.composehooks.ui.component.randomBackground
import xyz.junerver.composehooks.utils.now

/*
  Description: Example component for useTimeout hook
  Author: Junerver
  Date: 2024/3/11-9:09
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Example component demonstrating the useTimeout hook
 *
 * useTimeout is a simple hook that executes a callback after a specified delay.
 * It only runs once when the component is mounted and won't re-execute on recomposition.
 *
 * Important: useTimeout can only be called directly within a @Composable function scope.
 * It cannot be called from regular functions, even if those functions are called from composables.
 */
@Composable
fun UseTimeoutExample() {
    ScrollColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "useTimeout Examples",
            style = MaterialTheme.typography.headlineMedium,
        )

        // Basic Usage
        ExampleCard(title = "Basic Usage") {
            BasicTimeoutExample()
        }

        // Practical Example
        ExampleCard(title = "Practical Example: Auto Dismiss Notification") {
            NotificationExample()
        }
    }
}

/**
 * Basic example demonstrating the simplest usage of useTimeout hook
 *
 * This example shows that useTimeout:
 * 1. Executes a callback after a specified delay (3 seconds)
 * 2. Only runs once when the component is mounted
 * 3. Won't re-execute on recomposition
 * 4. Must be called directly within a @Composable function scope
 */
@Composable
private fun BasicTimeoutExample() {
    val logs = useList<String>()
    val (text, setText) = useGetState("Please wait for 3 seconds")
    val update = useUpdate()

    // Add initial log entry
    LaunchedEffect(Unit) {
        logs.add("Component mounted at ${now()}")
        logs.add("Timeout scheduled for 3 seconds...")
    }

    // When component is mounted, execute this block after 3 seconds
    // This will only run ONCE when the component is first mounted
    useTimeout(3.seconds) {
        setText("Done!")
        logs.add("Timeout completed at ${now()}")
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Description of the example
        Text(
            text = "useTimeout only executes once when the component is first mounted and won't trigger again on recomposition. Click the button below to update the component and observe that the timeout task doesn't execute again.",
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

        // Update button to demonstrate that the timeout won't trigger again
        TButton(
            text = "Update Component",
            onClick = {
                // Even if the component is refreshed, the timeout won't trigger again
                update()
                logs.add("Component updated, but timeout won't trigger again")
            },
            modifier = Modifier.align(Alignment.End),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Log display
        LogCard(title = "Log:", logs = logs)
    }
}

/**
 * Practical example showing how useTimeout can be used for auto-dismissing notifications
 */
@Composable
private fun NotificationExample() {
    val logs = useList<String>()
    val notifications = useList<Notification>()

    // Data structure to track notifications that need auto-dismissal
    val autoDismissNotifications = useList<Pair<Notification, Duration>>()

    // Function to add a new notification
    fun addNotification(message: String, autoDismiss: Boolean = true, duration: Duration = 5.seconds) {
        val notification = Notification(
            id = now().toEpochMilliseconds().toString(),
            message = message,
            timestamp = now().toEpochMilliseconds(),
        )
        notifications.add(notification)
        logs.add("Added notification: $message")

        // Add to auto-dismiss list if enabled
        if (autoDismiss) {
            autoDismissNotifications.add(Pair(notification, duration))
        }
    }

    // Handle auto-dismissal of notifications
    for (notificationPair in autoDismissNotifications) {
        val notification = notificationPair.first
        val duration = notificationPair.second

        // Each notification gets its own useTimeout
        key(notification.id) {
            // Use key to ensure proper recomposition behavior
            useTimeout(duration) {
                notifications.removeAll { it.id == notification.id }
                autoDismissNotifications.removeAll { it.first.id == notification.id }
                logs.add("Auto-dismissed notification: ${notification.message}")
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Notification System with Auto-Dismiss",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Notification controls
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TButton(
                text = "Add (5s)",
                onClick = {
                    addNotification("Auto-dismiss after 5s")
                },
                modifier = Modifier.weight(1f),
            )

            TButton(
                text = "Add (3s)",
                onClick = {
                    addNotification("Auto-dismiss after 3s", duration = 3.seconds)
                },
                modifier = Modifier.weight(1f),
            )

            TButton(
                text = "Add (No Auto)",
                onClick = {
                    addNotification("Manual dismiss required", autoDismiss = false)
                },
                modifier = Modifier.weight(1f),
            )
        }

        // Display notifications
        if (notifications.isEmpty()) {
            Text(
                text = "No active notifications",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp),
            )
        } else {
            Text(
                text = "Active Notifications (${notifications.size}):",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            notifications.forEach { notification ->
                NotificationItem(
                    notification = notification,
                    onDismiss = {
                        notifications.removeAll { it.id == notification.id }
                        logs.add("Manually dismissed notification: ${notification.message}")
                    },
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Log display
        LogCard(title = "Notification Log:", logs = logs)
    }
}

/**
 * Data class representing a notification
 */
private data class Notification(
    val id: String,
    val message: String,
    val timestamp: Long,
)

/**
 * Component to display a single notification with dismiss button
 */
@Composable
private fun NotificationItem(notification: Notification, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .randomBackground()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "ID: ${notification.id.takeLast(4)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        TButton(
            text = "Dismiss",
            onClick = onDismiss,
        )
    }
}
