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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.random.Random
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.useEventPublish
import xyz.junerver.compose.hooks.useEventSubscribe
import xyz.junerver.compose.hooks.useGetState
import xyz.junerver.compose.hooks.useState
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.TButton
import xyz.junerver.composehooks.utils.formatToDecimalPlaces

/*
  Description: Example component for useEvent hook
  Author: Junerver
  Date: 2024/3/13-9:13
  Email: junerver@gmail.com
  Version: v1.0
 */

/**
 * Example component demonstrating the useEvent hook
 */
@Composable
fun UseEventExample() {
    ScrollColumn(
        modifier = Modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "useEvent Examples",
            style = MaterialTheme.typography.headlineMedium,
        )

        // Interactive Demo
        InteractiveEventDemo()

        // Basic usage example
        ExampleCard(title = "Basic Usage") {
            BasicEventExample()
        }

        // Practical application example
        ExampleCard(title = "Practical Application") {
            PracticalEventExample()
        }
    }
}

/**
 * Interactive demo for useEvent hook
 */
@Composable
private fun InteractiveEventDemo() {
    val post = useEventPublish<Unit>()
    var refreshCount by useState(0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Interactive Demo",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
            )

            Text(
                text = "This demo shows how events can be published to multiple subscribers.",
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Global refresh count: $refreshCount",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                )

                TButton("Refresh All") {
                    refreshCount++
                    post(Unit)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display a few subscriber components
            (0..3).forEach { index ->
                EventSubscriberComponent(index = index)
            }
        }
    }
}

/**
 * A component that subscribes to events
 */
@Composable
private fun EventSubscriberComponent(index: Int) {
    val (state, setState) = useGetState(0.0)

    fun refresh() {
        setState(Random.nextDouble())
    }

    useEventSubscribe { _: Unit ->
        refresh()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Component $index: ${state.value.formatToDecimalPlaces(4)}",
            modifier = Modifier.weight(1f),
        )
        TButton(text = "Refresh") {
            refresh()
        }
    }
}

/**
 * Basic example of useEvent hook
 */
@Composable
private fun BasicEventExample() {
    val post = useEventPublish<String>()
    val (message, setMessage) = useGetState("No message received")

    useEventSubscribe { msg: String ->
        setMessage("Received: $msg")
    }

    Column {
        Text("Current message: ${message.value}")
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            TButton("Send Hello") { post("Hello") }
            TButton("Send World") { post("World") }
        }
    }
}

/**
 * Practical application of useEvent hook
 */
@Composable
private fun PracticalEventExample() {
    val notifyThemeChange = useEventPublish<String>()
    val (currentTheme, setCurrentTheme) = useGetState("Light")

    Column {
        Text("Current theme: ${currentTheme.value}")
        Spacer(modifier = Modifier.height(8.dp))

        // Theme selector
        Row {
            TButton("Light Theme") {
                setCurrentTheme("Light")
                notifyThemeChange("Light")
            }
            TButton("Dark Theme") {
                setCurrentTheme("Dark")
                notifyThemeChange("Dark")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Components that react to theme changes
        ThemeAwareComponent("Header")
        ThemeAwareComponent("Content")
        ThemeAwareComponent("Footer")
    }
}

/**
 * A component that reacts to theme changes
 */
@Composable
private fun ThemeAwareComponent(name: String) {
    val (theme, setTheme) = useGetState("Light")

    useEventSubscribe { newTheme: String ->
        setTheme(newTheme)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$name component (Theme: ${theme.value})",
            style = if (theme.value == "Dark") {
                MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary)
            } else {
                MaterialTheme.typography.bodyMedium
            },
        )
    }
}
