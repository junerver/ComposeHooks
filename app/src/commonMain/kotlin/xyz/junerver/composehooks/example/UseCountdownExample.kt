package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.seconds
import kotlinx.datetime.FixedOffsetTimeZone
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.toInstant
import xyz.junerver.compose.hooks.useCountdown
import xyz.junerver.compose.hooks.useState
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description: Example component for useCountdown hook
  Author: Junerver
  Date: 2024/7/8-15:11
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Example component demonstrating the useCountdown hook
 */
@Composable
fun UseCountdownExample() {
    ScrollColumn(
        modifier = Modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "useCountdown Examples",
            style = MaterialTheme.typography.headlineMedium,
        )

        // Interactive Demo
        InteractiveCountdownDemo()

        // Basic usage example - Duration-based countdown
        ExampleCard(title = "Basic Usage: Duration-based Countdown") {
            SimpleOne()
        }

        // Target date example
        ExampleCard(title = "Target Date Countdown") {
            SimpleTwo()
        }

        // Practical application example
        ExampleCard(title = "Practical Application: Event Timer") {
            EventTimerExample()
        }
    }
}

/**
 * Interactive demo component for useCountdown hook
 */
@Composable
private fun InteractiveCountdownDemo() {
    var durationValue by useState("10")
    var isRunning by useState(false)
    var showCompletedMessage by useState(false)

    // Parse the duration input (with fallback to 10 seconds)
    val duration = durationValue.toIntOrNull()?.seconds ?: 10.seconds

    // Use the countdown hook when running
    val countdownHolder = if (isRunning) {
        useCountdown(
            optionsOf = {
                leftTime = duration
                onEnd = {
                    isRunning = false
                    showCompletedMessage = true
                }
            },
        )
    } else {
        null
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
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color,
            )

            // Current countdown display
            if (isRunning && countdownHolder != null) {
                val (leftTime, formattedRes) = countdownHolder
                val formatted = formattedRes.value

                Text(
                    text = "Time Remaining:",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                )

                Text(
                    text = "${leftTime.value.inWholeSeconds} seconds",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                Text(
                    text = "${formatted.minutes}m ${formatted.seconds}s ${formatted.milliseconds}ms",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            } else if (showCompletedMessage) {
                Text(
                    text = "Countdown Completed!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.Green,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            } else {
                Text(
                    text = "Set a countdown duration and start the timer",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            }

            // Duration input field
            if (!isRunning) {
                OutlinedTextField(
                    value = durationValue,
                    onValueChange = { durationValue = it },
                    label = { Text("Duration (seconds)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    singleLine = true,
                )
            }

            // Control buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (isRunning) {
                    Button(
                        onClick = { isRunning = false },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Cancel")
                    }
                } else {
                    Button(
                        onClick = {
                            isRunning = true
                            showCompletedMessage = false
                        },
                        modifier = Modifier.weight(1f),
                        enabled = durationValue.toIntOrNull() != null && durationValue.toIntOrNull()!! > 0,
                    ) {
                        Text("Start Countdown")
                    }

                    Button(
                        onClick = { durationValue = "10" },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Reset to 10s")
                    }
                }
            }
        }
    }
}

/**
 * Basic example of duration-based countdown
 */
@Composable
private fun SimpleOne() {
    var show by useState(default = false)
    val (leftTime, formattedRes) = useCountdown(
        optionsOf = {
            leftTime = 10.seconds
            onEnd = {
                show = true
            }
        },
    )
    Column(modifier = Modifier.padding(8.dp)) {
        Text(
            text = "Countdown from 10 seconds:",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Text(
            text = "Seconds remaining: ${leftTime.value.inWholeSeconds}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        Text(
            text = "Formatted: ${formattedRes.value}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        if (show) {
            Text(
                text = "Countdown completed!",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Green,
            )
        }
    }
}

/**
 * Example of target date-based countdown
 */
@Composable
private fun SimpleTwo() {
    val chineseNewYear = LocalDateTime.parse("2026-02-17T00:00:00").toInstant(TimeZone.of("UTC+8"))
    val (leftTime, formattedRes) = useCountdown(
        optionsOf = {
            targetDate = chineseNewYear
            interval = 1.seconds
        },
    )
    val formatted by formattedRes

    Column(modifier = Modifier.padding(8.dp)) {
        Text(
            text = "Countdown to Chinese New Year (2026-02-17):",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Text(
            text = "Days remaining: ${leftTime.value.inWholeDays}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 4.dp),
        )

        Text(
            text = "Full countdown: ${formatted.days}d ${formatted.hours}h ${formatted.minutes}m ${formatted.seconds}s",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 4.dp),
        )

        // Progress visualization
        val totalDays = 365 // Approximate days in a year
        val daysLeft = leftTime.value.inWholeDays.toInt()
        val progress = 1f - (daysLeft.toFloat() / totalDays)

        Text(
            text = "Approximately ${(progress * 100).toInt()}% of the year has passed",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

/**
 * Practical application example: Event timer
 */
@Composable
private fun EventTimerExample() {
    var eventName by useState("Product Launch")
    var eventDate by useState("2025-12-31T00:00:00")
    var isEditing by useState(false)

    // Parse the event date (with fallback to New Year's Eve)
    var targetDate by useState(LocalDateTime.parse("2025-12-31T00:00:00").toInstant(TimeZone.of("UTC+8")))

    // Use the countdown hook
    val (leftTime, formattedRes) = useCountdown(
        optionsOf = {
            this.targetDate = targetDate
            interval = 1.seconds
        },
    )
    val formatted = formattedRes.value

    Column(modifier = Modifier.padding(8.dp)) {
        if (isEditing) {
            // Edit mode
            Text(
                text = "Configure Event:",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            OutlinedTextField(
                value = eventName,
                onValueChange = { eventName = it },
                label = { Text("Event Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                singleLine = true,
            )

            OutlinedTextField(
                value = eventDate,
                onValueChange = { eventDate = it },
                label = { Text("Event Date (YYYY-MM-DDTHH:MM:SS)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TButton(
                    text = "Save",
                    onClick = {
                        isEditing = false
                        targetDate = LocalDateTime.parse(eventDate).toInstant(FixedOffsetTimeZone(UtcOffset(8)))
                    },
                    modifier = Modifier.weight(1f),
                )

                TButton(
                    text = "Cancel",
                    onClick = { isEditing = false },
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            // Display mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Countdown to: $eventName",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )

                TButton(
                    text = "Edit",
                    onClick = { isEditing = true },
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = DividerDefaults.Thickness.div(2),
                color = DividerDefaults.color.copy(alpha = 0.5f),
            )

            // Countdown display
            if (leftTime.value.isPositive()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    ),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "${formatted.days} days ${formatted.hours} hours",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )

                        Text(
                            text = "${formatted.minutes} minutes ${formatted.seconds} seconds",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }

                // Additional information
                Text(
                    text = "Event Date: ${eventDate.replace('T', ' ')}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
                ) {
                    Text(
                        text = "Event has already occurred!",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }
    }
}

/**
 * Card component for displaying examples
 */
