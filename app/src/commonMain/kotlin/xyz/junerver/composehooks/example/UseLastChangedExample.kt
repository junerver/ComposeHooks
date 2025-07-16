package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import xyz.junerver.compose.hooks.DefaultChineseTimeAgoMessages
import xyz.junerver.compose.hooks.DefaultEnglishTimeAgoMessages
import xyz.junerver.compose.hooks.getValue
import xyz.junerver.compose.hooks.useControllable
import xyz.junerver.compose.hooks.useCreation
import xyz.junerver.compose.hooks.useLastChanged
import xyz.junerver.compose.hooks.useList
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.useTimeAgo
import xyz.junerver.compose.hooks.useUpdateEffect
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.LogCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.TButton
import xyz.junerver.composehooks.ui.component.randomBackground
import xyz.junerver.composehooks.utils.now

/*
  Description: Examples demonstrating the usage of useLastChanged and useTimeAgo hooks
  Author: Junerver
  Date: 2025/6/24-16:21
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun UseLastChangedExample() {
    ScrollColumn(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "useLastChanged & useTimeAgo",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Text(
            text = "These hooks are used to track the last change time of a value and display time differences in a human-readable format.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Basic usage example
        ExampleCard(title = "Basic Usage") {
            BasicUsageExample()
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Multi-language support example
        ExampleCard(title = "Multi-language Support") {
            MultiLanguageExample()
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Custom time point example
        ExampleCard(title = "Custom Time Point") {
            CustomTimePointExample()
        }
    }
}

/**
 * Basic Usage Example
 * Demonstrates the basic usage of useLastChanged and useTimeAgo
 */
@Composable
private fun BasicUsageExample() {
    val logs = useList<String>()
    val (text, setText) = useControllable("")
    val lastChange by useLastChanged(text)
    val timeAgo by useTimeAgo(lastChange) {
        messages = DefaultEnglishTimeAgoMessages
        updateInterval = 1.seconds // Update every second
    }

    useUpdateEffect(text) {
        logs.add("Text: ${text.value}, Time: $lastChange")
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        TextField(
            value = text.value,
            onValueChange = setText,
            label = { Text("Type anything...") },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Last changed: $timeAgo",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp),
        )

        Text(
            text = "Timestamp: ${lastChange.toEpochMilliseconds()}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(16.dp))

        LogCard(title = "Change History:", logs = logs)
    }
}

/**
 * Multi-language Support Example
 * Demonstrates the multi-language support feature of useTimeAgo
 */
@Composable
private fun MultiLanguageExample() {
    val (text, setText) = useControllable("")
    val lastChange by useLastChanged(text)

    // English time difference
    val englishTimeAgo by useTimeAgo(lastChange) {
        messages = DefaultEnglishTimeAgoMessages
        updateInterval = 1.seconds
    }

    // Chinese time difference
    val chineseTimeAgo by useTimeAgo(lastChange) {
        messages = DefaultChineseTimeAgoMessages
        updateInterval = 1.seconds
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = text.value,
            onValueChange = setText,
            label = { Text("Enter any content...") },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "English:",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.width(80.dp),
            )

            Text(
                text = englishTimeAgo,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.randomBackground().padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Chinese:",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.width(80.dp),
            )

            Text(
                text = chineseTimeAgo,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.randomBackground().padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}

/**
 * Custom Time Point Example
 * Demonstrates how to use custom time points and update intervals
 */
@Composable
private fun CustomTimePointExample() {
    // Create several fixed time points
    val now by useCreation { now() }
    val fiveMinutesAgo by useCreation { now - 5.minutes }
    val thirtyMinutesAgo by useCreation { now - 30.minutes }
    val oneHourAgo by useCreation { now - 60.minutes }

    // Currently selected time point
    var selectedTime by useState(now)

    // Use useTimeAgo to display time difference
    val timeAgo by useTimeAgo(selectedTime) {
        messages = DefaultEnglishTimeAgoMessages
        updateInterval = 1.seconds
        showSecond = true // Show second-level units
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Select a time point:",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Row(modifier = Modifier.padding(bottom = 16.dp)) {
            TButton(text = "Now", onClick = { selectedTime = now })
            Spacer(modifier = Modifier.width(8.dp))
            TButton(text = "5 min ago", onClick = { selectedTime = fiveMinutesAgo })
            Spacer(modifier = Modifier.width(8.dp))
            TButton(text = "30 min ago", onClick = { selectedTime = thirtyMinutesAgo })
            Spacer(modifier = Modifier.width(8.dp))
            TButton(text = "1 hour ago", onClick = { selectedTime = oneHourAgo })
        }

        Text(
            text = "Time ago: $timeAgo",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.randomBackground().padding(8.dp),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Selected timestamp: ${selectedTime.toEpochMilliseconds()}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
