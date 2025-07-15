package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import xyz.junerver.compose.hooks.useCycleList
import xyz.junerver.compose.hooks.useState
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description: Example component for useCycleList hook
  Author: Junerver
  Date: 2025/7/3-17:50
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Example component demonstrating the useCycleList hook
 */
@Composable
fun UseCycleListExample() {
    ScrollColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "useCycleList Examples",
            style = MaterialTheme.typography.headlineMedium,
        )

        // Interactive Demo
        InteractiveCycleListDemo()

        // Basic usage example
        ExampleCard(title = "Basic Usage") {
            BasicCycleListExample()
        }

        // Custom fallback example
        ExampleCard(title = "Custom Fallback") {
            CustomFallbackExample()
        }

        // Practical application example
        ExampleCard(title = "Practical Application: Carousel") {
            CarouselExample()
        }
    }
}

/**
 * Interactive demo showing how useCycleList works with different options
 */
@Composable
private fun InteractiveCycleListDemo() {
    var fallbackIndex by useState(0)
    var initialItemIndex by useState(2)

    val items = persistentListOf(
        "Dog",
        "Cat",
        "Lizard",
        "Shark",
        "Whale",
        "Dolphin",
        "Octopus",
        "Seal",
    )

    val initialValue = if (initialItemIndex < items.size) items[initialItemIndex] else "Unknown"

    val (state, index, next, prev, go) = useCycleList(items) {
        this.initialValue = initialValue
        this.fallbackIndex = fallbackIndex
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

            // Current state display
            Text(
                text = "Current Item: ${state.value}",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp),
            )

            Text(
                text = "Current Index: ${index.value}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            // Navigation buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                TButton(
                    text = "Previous",
                    onClick = { prev() },
                    modifier = Modifier.weight(1f),
                )

                TButton(
                    text = "Next",
                    onClick = { next() },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Configuration options
            Text(
                text = "Configuration",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(vertical = 8.dp),
            )

            // Initial item index slider
            Text(
                text = "Initial Item Index: $initialItemIndex",
                style = MaterialTheme.typography.bodyMedium,
            )

            Slider(
                value = initialItemIndex.toFloat(),
                onValueChange = { initialItemIndex = it.toInt() },
                valueRange = 0f..items.size.toFloat(),
                steps = items.size - 1,
                modifier = Modifier.padding(vertical = 8.dp),
            )

            // Fallback index slider
            Text(
                text = "Fallback Index: $fallbackIndex",
                style = MaterialTheme.typography.bodyMedium,
            )

            Slider(
                value = fallbackIndex.toFloat(),
                onValueChange = { fallbackIndex = it.toInt() },
                valueRange = 0f..items.size.toFloat(),
                steps = items.size - 1,
                modifier = Modifier.padding(vertical = 8.dp),
            )

            // Direct navigation
            Text(
                text = "Go to specific index:",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                for (i in 0 until minOf(5, items.size)) {
                    TButton(
                        text = "$i",
                        onClick = { go(i) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

/**
 * Basic example showing how to use useCycleList
 */
@Composable
private fun BasicCycleListExample() {
    val items = persistentListOf("Red", "Green", "Blue", "Yellow", "Purple")

    val (state, index, next, prev) = useCycleList(items)

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Current Color: ${state.value}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        Text(
            text = "Position: ${index.value + 1} of ${items.size}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TButton(text = "Previous", onClick = { prev() })
            TButton(text = "Next", onClick = { next() })
        }
    }
}

/**
 * Example showing how to use useCycleList with custom fallback
 */
@Composable
private fun CustomFallbackExample() {
    val items = persistentListOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")

    val (state, _, next, prev) = useCycleList(items) {
        initialValue = "Sunday" // Not in the list
        fallbackIndex = 0 // Will use this index when initialValue is not found
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Current Day: ${state.value}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        Text(
            text = "Note: We started with 'Sunday' which is not in the list, so fallbackIndex (0) was used.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TButton(text = "Previous", onClick = { prev() })
            TButton(text = "Next", onClick = { next() })
        }
    }
}

/**
 * Example showing a practical application of useCycleList for a simple carousel
 */
@Composable
private fun CarouselExample() {
    val images = persistentListOf(
        "Mountain View",
        "Beach Sunset",
        "Forest Path",
        "City Skyline",
        "Desert Landscape"
    )

    val (state, index, next, prev, go) = useCycleList(images)

    Column(modifier = Modifier.padding(16.dp)) {
        // Image placeholder
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = state.value,
                    style = MaterialTheme.typography.headlineSmall,
                )

                Text(
                    text = "Image ${index.value + 1} of ${images.size}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation controls
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            TButton(text = "Previous", onClick = { prev() })

            // Indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (i in images.indices) {
                    TButton(
                        text = "${i + 1}",
                        onClick = { go(i) },
                        enabled = i != index.value,
                    )
                }
            }

            TButton(text = "Next", onClick = { next() })
        }
    }
}
