package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.Padding
import xyz.junerver.compose.hooks.useNow
import xyz.junerver.compose.hooks.useState
import xyz.junerver.composehooks.ui.component.DividerSpacer
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.TButton
import xyz.junerver.composehooks.utils.CHINESE_FULL
import xyz.junerver.composehooks.utils.toLocalDateTime
import xyz.junerver.composehooks.utils.tsMs

/*
  Description: Example demonstrating the useNow hook for displaying current time
  Author: Junerver
  Date: 2024/3/14-12:08
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Example component demonstrating the useNow hook
 */
@Composable
fun UseNowExample() {
    Surface {
        ScrollColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Page title
            Text(
                text = "useNow Examples",
                style = MaterialTheme.typography.headlineMedium,
            )

            // Introduction text
            Text(
                text = "This hook provides a way to display the current time that updates at regular intervals. It supports both default formatting and custom formatting through a format function.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            // Interactive Demo
            ExampleCard(title = "Interactive Demo") {
                InteractiveTimeDemo()
            }

            // Basic usage example
            ExampleCard(title = "Basic Usage") {
                BasicTimeExample()
            }

            // Custom format example
            ExampleCard(title = "Custom Formats") {
                CustomFormatExample()
            }

            // Interval control example
            ExampleCard(title = "Update Interval Control") {
                IntervalControlExample()
            }
        }
    }
}

/**
 * Interactive demonstration of the useNow hook
 *
 * Allows users to control time updates through pause and resume buttons
 */
@Composable
private fun InteractiveTimeDemo() {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Interactive Demo",
                style = MaterialTheme.typography.titleLarge,
            )

            // Using default configuration of useNow hook
            val now by useNow()

            // State to control time updates
            var isPaused by useState(false)

            // Value to store time when paused
            var pausedTime by useState("")

            Text(
                text = if (isPaused) "Time updates paused" else "Time updating in real-time",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isPaused) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp),
            )

            Text(
                text = if (isPaused) pausedTime else now,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(vertical = 16.dp),
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TButton(
                    text = if (isPaused) "Resume" else "Pause",
                    onClick = {
                        if (!isPaused) {
                            // Save current time when pausing
                            pausedTime = now
                        }
                        isPaused = !isPaused
                    },
                )
            }
        }
    }
}

/**
 * Basic time display example
 *
 * Demonstrates the basic usage of useNow hook with default formatting
 */
@Composable
private fun BasicTimeExample() {
    // Using default configuration of useNow hook
    val now by useNow()

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(
            text = "Default Format (YYYY-MM-DD HH:mm:ss):",
            style = MaterialTheme.typography.bodyMedium,
        )

        Text(
            text = now,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = 8.dp),
        )

        Text(
            text = "The useNow() hook updates the time once per second by default, using the standard date-time format.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Custom format example
 *
 * Demonstrates how to use the format option of useNow hook to customize time display
 */
@Composable
private fun CustomFormatExample() {
    // Using useNow hook with custom Chinese date format
    val customTime by useNow(
        optionsOf = {
            format = {
                it.tsMs
                    .toLocalDateTime()
                    .format(
                        LocalDateTime.Format {
                            year()
                            chars("年")
                            monthNumber()
                            chars("月")
                            day(padding = Padding.ZERO)
                            chars("日")
                            dayOfWeek(DayOfWeekNames.CHINESE_FULL)
                        },
                    )
            }
        },
    )

    // Another custom format showing time only
    val timeOnly by useNow(
        optionsOf = {
            format = {
                it.tsMs
                    .toLocalDateTime()
                    .format(
                        LocalDateTime.Format {
                            hour()
                            chars(":")
                            minute()
                            chars(":")
                            second()
                        },
                    )
            }
        },
    )

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(
            text = "Chinese Date Format:",
            style = MaterialTheme.typography.bodyMedium,
        )

        Text(
            text = customTime,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = 8.dp),
        )

        DividerSpacer()

        Text(
            text = "Time-Only Format:",
            style = MaterialTheme.typography.bodyMedium,
        )

        Text(
            text = timeOnly,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = 8.dp),
        )

        Text(
            text = "With the format option, you can completely customize how time is displayed, including using kotlinx.datetime formatters or your own formatting logic.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Update interval control example
 *
 * Demonstrates how to control the update interval of the useNow hook
 */
@Composable
private fun IntervalControlExample() {
    // Using useNow hook with different update intervals
    val fastUpdate by useNow(
        optionsOf = {
            interval = 100.milliseconds
        },
    )

    val slowUpdate by useNow(
        optionsOf = {
            interval = 5.seconds
        },
    )

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(
            text = "Fast Updates (100ms):",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        Text(
            text = fastUpdate,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = 8.dp),
        )

        DividerSpacer()

        Text(
            text = "Slow Updates (5s):",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
        )

        Text(
            text = slowUpdate,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = 8.dp),
        )

        Text(
            text = "With the interval option, you can control how frequently the time updates. Faster intervals provide more precise time display but consume more resources.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
