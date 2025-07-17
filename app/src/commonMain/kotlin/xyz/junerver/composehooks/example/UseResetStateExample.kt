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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.useResetState
import xyz.junerver.compose.hooks.useState
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.TButton
import xyz.junerver.composehooks.utils.Timestamp

/*
  Description: Example component for useResetState hook
  Author: Junerver
  Date: 2024/7/9-14:24
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Example component demonstrating the useResetState hook
 */
@Composable
fun UseResetStateExample() {
    Surface {
        ScrollColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "useResetState Examples",
                style = MaterialTheme.typography.headlineMedium,
            )

            // Interactive Demo
            InteractiveResetStateDemo()

            // Basic usage example
            ExampleCard(title = "Basic Usage") {
                BasicResetStateExample()
            }

            // Advanced usage example
            ExampleCard(title = "Form Reset Example") {
                FormResetExample()
            }

            // Practical application
            ExampleCard(title = "Practical Application: Settings Panel") {
                SettingsPanelExample()
            }
        }
    }
}

/**
 * Interactive demo for useResetState hook
 */
@Composable
private fun InteractiveResetStateDemo() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Interactive Demo",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(8.dp))

            val (state, setState, _, reset) = useResetState("Initial value")

            Text(
                text = "Current value:",
                style = MaterialTheme.typography.bodyMedium,
            )

            Text(
                text = state.value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp),
            )

            var inputText by useState("")

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Enter new value") },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TButton(text = "Set Value", modifier = Modifier.weight(1f)) {
                    if (inputText.isNotEmpty()) {
                        setState(inputText)
                    }
                }

                TButton(text = "Reset", modifier = Modifier.weight(1f)) {
                    reset()
                    inputText = ""
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "The useResetState hook provides a state with reset capability, allowing you to restore the state to its initial value at any time.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

/**
 * Basic example of useResetState hook
 */
@Composable
private fun BasicResetStateExample() {
    val (state, setState, _, reset) = useResetState("default value")

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Current value: ${state.value}")

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TButton(text = "Set New Value") {
                setState("New value ${Timestamp.now()}")
            }

            TButton(text = "Reset") {
                reset()
            }
        }
    }
}

/**
 * Example showing form reset functionality
 */
@Composable
private fun FormResetExample() {
    val (nameState, setName, _, resetName) = useResetState("")
    val (emailState, setEmail, _, resetEmail) = useResetState("")

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Form with Reset Capability",
            style = MaterialTheme.typography.bodyLarge,
        )

        OutlinedTextField(
            value = nameState.value,
            onValueChange = { setName(it) },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = emailState.value,
            onValueChange = { setEmail(it) },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TButton(text = "Fill Sample Data") {
                setName("John Doe")
                setEmail("john@example.com")
            }

            TButton(text = "Reset Form") {
                resetName()
                resetEmail()
            }
        }
    }
}

/**
 * Practical application example: Settings panel with reset capability
 */
@Composable
private fun SettingsPanelExample() {
    // Default settings
    val (darkMode, setDarkMode, _, resetDarkMode) = useResetState(false)
    val (fontSize, setFontSize, _, resetFontSize) = useResetState("Medium")
    val (notifications, setNotifications, _, resetNotifications) = useResetState(true)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Settings Panel",
            style = MaterialTheme.typography.bodyLarge,
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Dark Mode: ${if (darkMode.value) "On" else "Off"}", modifier = Modifier.weight(1f))
            TButton(text = if (darkMode.value) "Turn Off" else "Turn On") {
                setDarkMode(!darkMode.value)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Font Size: ${fontSize.value}", modifier = Modifier.weight(1f))
            TButton(text = "Change") {
                setFontSize(
                    when (fontSize.value) {
                        "Small" -> "Medium"
                        "Medium" -> "Large"
                        else -> "Small"
                    },
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Notifications: ${if (notifications.value) "Enabled" else "Disabled"}", modifier = Modifier.weight(1f))
            TButton(text = if (notifications.value) "Disable" else "Enable") {
                setNotifications(!notifications.value)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TButton(text = "Reset All Settings") {
            resetDarkMode()
            resetFontSize()
            resetNotifications()
        }
    }
}
