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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.junerver.compose.hooks.useControllable
import xyz.junerver.compose.hooks.useState
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description: Example component for useControllable hook
  Author: Junerver
  Date: 2024/3/11-11:09
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Example component demonstrating the useControllable hook
 */
@Composable
fun UseControllableExample() {
    ScrollColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "useControllable Examples",
            style = MaterialTheme.typography.headlineMedium,
        )

        // Interactive Demo
        InteractiveControllableDemo()

        // Basic usage example
        ExampleCard(title = "Basic Usage") {
            BasicControllableExample()
        }

        // Form example
        ExampleCard(title = "Form Example") {
            FormControllableExample()
        }
    }
}

/**
 * Interactive demo showing how useControllable works
 */
@Composable
private fun InteractiveControllableDemo() {
    val (state, setValue, getValue) = useControllable("Initial Value")
    var inputValue by useState("")

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

            // Current state display
            Text(
                text = "Current State: ${state.value}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            // Get value button
            TButton(
                text = "Get Current Value",
                modifier = Modifier.fillMaxWidth(),
            ) {
                inputValue = getValue()
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Input field
            OutlinedTextField(
                value = inputValue,
                onValueChange = { inputValue = it },
                label = { Text("New Value") },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Set value button
            TButton(
                text = "Set Value",
                modifier = Modifier.fillMaxWidth(),
            ) {
                setValue(inputValue)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Instructions
            Text(
                text = "Use 'Get Current Value' to read the state, then type a new value and click 'Set Value' to update it.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

/**
 * Basic example showing how to use useControllable
 */
@Composable
private fun BasicControllableExample() {
    val (state, setValue, getValue) = useControllable(0)

    Column {
        Text(
            text = "This hook provides a controllable state with getter and setter functions.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Text("Current value: ${state.value}")
        Text("Getter result: ${getValue()}")

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TButton(
                text = "Increment",
                modifier = Modifier.weight(1f),
            ) {
                setValue(state.value + 1)
            }

            TButton(
                text = "Reset",
                modifier = Modifier.weight(1f),
            ) {
                setValue(0)
            }
        }
    }
}

/**
 * Form example showing useControllable in a form context
 */
@Composable
private fun FormControllableExample() {
    val (name, setName, getName) = useControllable("")
    val (email, setEmail, getEmail) = useControllable("")
    var nameInput by useState("")
    var emailInput by useState("")

    Column {
        Text(
            text = "Form Example",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Name field
        OutlinedTextField(
            value = nameInput,
            onValueChange = { nameInput = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Email field
        OutlinedTextField(
            value = emailInput,
            onValueChange = { emailInput = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Submit button
        TButton(
            text = "Submit",
            modifier = Modifier.fillMaxWidth(),
        ) {
            setName(nameInput)
            setEmail(emailInput)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display submitted values
        if (name.value.isNotEmpty() || email.value.isNotEmpty()) {
            Text("Submitted Values:")
            Text("Name: ${name.value}")
            Text("Email: ${email.value}")
        }
    }
}