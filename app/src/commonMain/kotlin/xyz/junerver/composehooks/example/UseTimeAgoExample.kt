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
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.pow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import xyz.junerver.compose.hooks.DefaultEnglishTimeAgoMessages
import xyz.junerver.compose.hooks.TimeAgoMessageFormatter
import xyz.junerver.compose.hooks.TimeAgoMessages
import xyz.junerver.compose.hooks.TimeUnitMessageFormatter
import xyz.junerver.compose.hooks.getValue
import xyz.junerver.compose.hooks.useControllable
import xyz.junerver.compose.hooks.useCreation
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.useTimeAgo
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.randomBackground
import xyz.junerver.composehooks.utils.now

/*
  Description: Example component for useTimeAgo hook
  Author: Junerver
  Date: 2025/6/24-16:21
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Example component demonstrating the useTimeAgo hook
 */
@Composable
fun UseTimeAgoExample() {
    ScrollColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "useTimeAgo Examples",
            style = MaterialTheme.typography.headlineMedium,
        )

        Text(
            text = "This hook converts a timestamp into a human-readable relative time string (e.g., '5 minutes ago', 'just now').",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Interactive Demo
        InteractiveTimeAgoDemo()

        // Basic Usage
        ExampleCard(title = "Basic Usage") {
            BasicTimeAgoExample()
        }

        // Custom Messages
        ExampleCard(title = "Custom Messages") {
            CustomMessagesExample()
        }

        // Practical Application
        ExampleCard(title = "Practical Application: Activity Feed") {
            ActivityFeedExample()
        }
    }
}

/**
 * Interactive demonstration of the useTimeAgo hook
 */
@Composable
private fun InteractiveTimeAgoDemo() {
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

            // Main interactive slider for time adjustment
            val (sliderPosition, setSliderPosition) = useControllable(0f)
            val time by useState {
                sliderPosition.value.pow(3).toLong()
            }
            val fromInstant by useState {
                now() + time.milliseconds
            }
            val timeAgo by useTimeAgo(fromInstant) {
                messages = DefaultEnglishTimeAgoMessages
            }

            // Display the time ago result prominently
            Text(
                text = timeAgo,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 16.dp),
            )

            Text(
                text = "Adjust the slider to see different time formats:",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            Slider(
                value = sliderPosition.value,
                onValueChange = setSliderPosition,
                valueRange = -3800f..3800f,
            )

            Text(
                text = "Time offset: $time ms",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Configuration: Using DefaultEnglishTimeAgoMessages",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

/**
 * Basic usage example of the useTimeAgo hook
 */
@Composable
private fun BasicTimeAgoExample() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Simple implementation with default settings:",
            style = MaterialTheme.typography.bodyMedium,
        )

        // Create timestamps for different time periods
        val now by useCreation { now() }
        val fiveMinutesAgo by useCreation { now - 5.minutes }
        val oneHourAgo by useCreation { now - 1.hours }
        val oneDayAgo by useCreation { now - 1.days }

        // Use the hook with default settings
        val justNowText by useTimeAgo(now)
        val fiveMinutesText by useTimeAgo(fiveMinutesAgo)
        val oneHourText by useTimeAgo(oneHourAgo)
        val oneDayText by useTimeAgo(oneDayAgo)

        // Display the results
        TimeAgoRow("Just now:", justNowText)
        TimeAgoRow("5 minutes ago:", fiveMinutesText)
        TimeAgoRow("1 hour ago:", oneHourText)
        TimeAgoRow("1 day ago:", oneDayText)

        Text(
            text = "Note: The hook automatically uses DefaultEnglishTimeAgoMessages when no messages are provided.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

/**
 * Helper component to display a time ago label and value
 */
@Composable
private fun TimeAgoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(0.4f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(0.6f).randomBackground().padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

/**
 * Example demonstrating custom messages with the useTimeAgo hook
 */
@Composable
private fun CustomMessagesExample() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Using custom messages for localization or different formatting:",
            style = MaterialTheme.typography.bodyMedium,
        )

        val customMessages = object : TimeAgoMessages {
            override val justNow: String = "刚刚"
            override val past: TimeAgoMessageFormatter = { unit -> "$unit 前" }
            override val future: TimeAgoMessageFormatter = { unit -> "$unit 后" }
            override val invalid: String = "无效时间"
            override val second: TimeUnitMessageFormatter = { n -> "$n 秒" }
            override val minute: TimeUnitMessageFormatter = { n -> "$n 分钟" }
            override val hour: TimeUnitMessageFormatter = { n -> "$n 小时" }
            override val day: TimeUnitMessageFormatter = { n -> "${n}天" }
            override val week: TimeUnitMessageFormatter = { n -> "${n}周" }
            override val month: TimeUnitMessageFormatter = { n -> "${n}个月" }
            override val year: TimeUnitMessageFormatter = { n -> "${n}年" }
        }

        // Create timestamps for different time periods
        val now by useCreation { now() }
        val fiveMinutesAgo by useCreation { now - 5.minutes }
        val oneHourAgo by useCreation { now - 1.hours }
        val oneDayAgo by useCreation { now - 1.days }

        // Use the hook with custom messages
        val justNowText by useTimeAgo(now) {
            messages = customMessages
            updateInterval = Duration.ZERO
        }
        val fiveMinutesText by useTimeAgo(fiveMinutesAgo) {
            messages = customMessages
            updateInterval = Duration.ZERO
        }
        val oneHourText by useTimeAgo(oneHourAgo) {
            messages = customMessages
            updateInterval = Duration.ZERO
        }
        val oneDayText by useTimeAgo(oneDayAgo) {
            messages = customMessages
            updateInterval = Duration.ZERO
        }

        // Display the results
        TimeAgoRow("Just now:", justNowText)
        TimeAgoRow("5 minutes ago:", fiveMinutesText)
        TimeAgoRow("1 hour ago:", oneHourText)
        TimeAgoRow("1 day ago:", oneDayText)

        Text(
            text = "Configuration: Using custom Chinese TimeAgoMessages, updateInterval = Duration.ZERO",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

/**
 * Data class representing an activity in a feed
 */
private data class Activity(
    val id: Int,
    val user: String,
    val action: String,
    val timestamp: Instant,
)

/**
 * Example demonstrating a practical application of useTimeAgo in an activity feed
 */
@Composable
private fun ActivityFeedExample() {
    // Sample activity data
    val currentTime by useCreation { now() }
    val activities by useCreation {
        listOf(
            Activity(1, "Alice", "posted a photo", currentTime - 2.minutes),
            Activity(2, "Bob", "commented on your post", currentTime - 15.minutes),
            Activity(3, "Charlie", "liked your comment", currentTime - 1.hours),
            Activity(4, "Diana", "shared your post", currentTime - 5.hours),
            Activity(5, "Evan", "sent you a friend request", currentTime - 1.days),
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Activity Feed Example:",
            style = MaterialTheme.typography.bodyMedium,
        )

        // Display each activity with relative time
        activities.forEach { activity ->
            ActivityItem(activity)
        }

        Text(
            text = "This example shows how useTimeAgo can be used in a social media activity feed to display when actions occurred.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

/**
 * Component to display a single activity item with relative time
 */
@Composable
private fun ActivityItem(activity: Activity) {
    val timeAgo by useTimeAgo(activity.timestamp)

    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.user,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                )
                Text(
                    text = activity.action,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Text(
                text = timeAgo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}
