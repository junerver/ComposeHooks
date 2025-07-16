package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.junerver.compose.hooks.useBoolean
import xyz.junerver.compose.hooks.useState
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description: Example component for useBoolean hook
  Author: Junerver
  Date: 2024/3/8-10:08
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun UseBooleanExample() {
    ScrollColumn(
        modifier = Modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "useBoolean Examples",
            style = MaterialTheme.typography.headlineMedium,
        )

        // Interactive Demo
        InteractiveBooleanDemo()

        // Basic Usage
        ExampleCard(title = "Basic Usage") {
            BasicUsageExample()
        }

        // Toggle Controls
        ExampleCard(title = "Toggle Controls") {
            ToggleControlsExample()
        }

        // Real-world Usage
        ExampleCard(title = "Real-world Usage: Settings Panel") {
            SettingsPanelExample()
        }
    }
}

@Composable
private fun InteractiveBooleanDemo() {
    var defaultValue by useState(false)

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

            // Default value selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Default Value:")
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = defaultValue,
                    onCheckedChange = { defaultValue = it },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Demo with the selected default value
            val (state, toggle, setValue, setTrue, setFalse) = useBoolean(defaultValue)

            Column {
                Text(
                    text = "Current Value: ${state.value}",
                    style = MaterialTheme.typography.bodyLarge,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TButton(text = "Toggle", onClick = toggle)
                    TButton(text = "Set True", onClick = setTrue)
                    TButton(text = "Set False", onClick = setFalse)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Try changing the default value and see how it affects the initial state!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun BasicUsageExample() {
    val (state, toggle, _, _, _) = useBoolean(false)

    Column {
        Text(
            text = "The simplest way to use useBoolean is with the toggle function:",
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Value: ${state.value}")
            TButton(text = "Toggle", onClick = toggle)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Code: val (state, toggle, _, _, _) = useBoolean(false)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ToggleControlsExample() {
    val (state, toggle, setValue, setTrue, setFalse) = useBoolean(false)

    Column {
        Text(
            text = "useBoolean provides multiple ways to control the boolean state:",
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = state.value,
                onCheckedChange = setValue,
            )
            Text("Current value: ${state.value}")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TButton(text = "Toggle", onClick = toggle)
            TButton(text = "Set True", onClick = setTrue)
            TButton(text = "Set False", onClick = setFalse)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "The setValue function allows direct setting of any boolean value",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SettingsPanelExample() {
    val (darkMode, toggleDarkMode, _, _, _) = useBoolean(false)
    val (notifications, toggleNotifications, _, _, _) = useBoolean(true)
    val (autoSave, toggleAutoSave, _, _, _) = useBoolean(true)

    Column {
        Text(
            text = "useBoolean is perfect for toggle settings:",
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(modifier = Modifier.height(12.dp))

        SettingItem("Dark Mode", darkMode.value, toggleDarkMode)
        SettingItem("Notifications", notifications.value, toggleNotifications)
        SettingItem("Auto-Save", autoSave.value, toggleAutoSave)

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Each setting uses its own useBoolean hook instance",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SettingItem(title: String, checked: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = checked,
            onCheckedChange = { onToggle() },
        )
    }
}
