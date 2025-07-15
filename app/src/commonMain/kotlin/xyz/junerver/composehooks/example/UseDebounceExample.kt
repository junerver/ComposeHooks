package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.useDebounce
import xyz.junerver.compose.hooks.useDebounceEffect
import xyz.junerver.compose.hooks.useDebounceFn
import xyz.junerver.compose.hooks.useGetState
import xyz.junerver.compose.hooks.useState
import xyz.junerver.composehooks.net.NetApi
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.TButton
import xyz.junerver.composehooks.utils.subStringIf

/*
  Description: Example component for useDebounce hook
  Author: Junerver
  Date: 2024/3/8-14:13
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Example component demonstrating the useDebounce, useDebounceFn, and useDebounceEffect hooks
 */
@Composable
fun UseDebounceExample() {
    ScrollColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "useDebounce Examples",
            style = MaterialTheme.typography.headlineMedium,
        )

        // Interactive Demo
        InteractiveDebounceDemo()

        // Basic usage example
        ExampleCard(title = "Basic Usage") {
            BasicDebounceExample()
        }

        // Debounced function example
        ExampleCard(title = "Debounced Function") {
            DebouncedFunctionExample()
        }

        // Debounced effect example
        ExampleCard(title = "Debounced Effect") {
            DebouncedEffectExample()
        }

        // Advanced configuration example
        ExampleCard(title = "Advanced Configuration") {
            AdvancedConfigurationExample()
        }
    }
}

/**
 * Interactive demo showing how useDebounce works with configurable options
 */
@Composable
private fun InteractiveDebounceDemo() {
    var inputValue by useState("")
    var waitTime by useState(500)
    var leading by useState(false)
    var trailing by useState(true)

    val debouncedValue by useDebounce(
        value = inputValue,
        optionsOf = {
            wait = waitTime.milliseconds
            this.leading = leading
            this.trailing = trailing
        },
    )
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

            // Display the debounced value
            Text(
                text = "Debounced Value:",
                style = MaterialTheme.typography.bodyMedium,
            )

            Text(
                text = debouncedValue,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input field
            OutlinedTextField(
                value = inputValue,
                onValueChange = { inputValue = it },
                label = { Text("Type something...") },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Wait time slider
            Text(
                text = "Wait Time: ${waitTime}ms",
                style = MaterialTheme.typography.bodyMedium,
            )

            Slider(
                value = waitTime.toFloat(),
                onValueChange = { waitTime = it.toInt() },
                valueRange = 100f..2000f,
                steps = 19,
                modifier = Modifier.padding(vertical = 8.dp),
            )

            // Options
            Row(verticalAlignment = Alignment.CenterVertically) {
                TButton(
                    text = if (leading) "Leading: ON" else "Leading: OFF",
                    onClick = { leading = !leading },
                    modifier = Modifier.weight(1f),
                )

                Spacer(modifier = Modifier.width(8.dp))

                TButton(
                    text = if (trailing) "Trailing: ON" else "Trailing: OFF",
                    onClick = { trailing = !trailing },
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                TButton(
                    text = "Reset",
                    onClick = {
                        inputValue = ""
                        waitTime = 500
                        leading = false
                        trailing = true
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/**
 * Basic example showing how to use useDebounce
 */
@Composable
private fun BasicDebounceExample() {
    val (state, setState) = useGetState(0)
    val debouncedState by useDebounce(value = state.value)

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Current Value: ${state.value}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Text(
            text = "Debounced Value: $debouncedState",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp),
        )
        Text(
            text = "Configuration: leading=false, trailing=true, wait=1s",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        TButton(text = "Increment Value") {
            setState(state.value + 1)
        }
    }
}

/**
 * Example showing how to use useDebounceFn
 */
@Composable
private fun DebouncedFunctionExample() {
    val (state, setState) = useGetState(0)
    val debouncedFn = useDebounceFn(
        fn = { setState { it + 1 } },
        optionsOf = {
            wait = 500.milliseconds
        },
    )

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Current Value: ${state.value}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        Text(
            text = "Click the button rapidly - the counter will only increment once per 500ms",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 16.dp),
        )
        Text(
            text = "Configuration: leading=false, trailing=true, wait=500ms",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        TButton(text = "Debounced Increment") {
            // Manual import required: import xyz.junerver.compose.hooks.invoke
            debouncedFn()
        }
    }
}

/**
 * Example showing how to use useDebounceEffect
 */
@Composable
private fun DebouncedEffectExample() {
    val (state, setState) = useGetState(0)
    val (result, setResult) = useGetState("")

    useDebounceEffect(state.value) {
        setResult("Loading user data...")
        val userInfo = NetApi.userInfo("junerver")
        setResult(userInfo.toString().subStringIf(200))
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Dependency Value: ${state.value}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp),
        )
        Text(
            text = "Configuration: leading=false, trailing=true, wait=1s",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 16.dp),
        )
        TButton(text = "Change Dependency") {
            setState(state.value + 1)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "API Result:",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Text(
            text = result.value,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

/**
 * Example showing advanced configuration options for useDebounce
 */
@Composable
private fun AdvancedConfigurationExample() {
    var counter by useState(0)
    var maxWaitEnabled by useState(false)

    // Configure debounce with leading edge trigger and optional max wait time
    val debouncedCounter by useDebounce(
        value = counter,
        optionsOf = {
            wait = 2.seconds
            leading = true
            trailing = true
            maxWait = if (maxWaitEnabled) 3.seconds else 0.seconds
        },
    )

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Counter: $counter",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Text(
            text = "Debounced Counter: $debouncedCounter",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        Text(
            text = "Configuration: leading=true, trailing=true, wait=2s${if (maxWaitEnabled) ", maxWait=3s" else ""}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            TButton(
                text = "Increment",
                onClick = { counter++ },
                modifier = Modifier.weight(1f),
            )

            Spacer(modifier = Modifier.width(8.dp))

            TButton(
                text = if (maxWaitEnabled) "Disable MaxWait" else "Enable MaxWait",
                onClick = { maxWaitEnabled = !maxWaitEnabled },
                modifier = Modifier.weight(1f),
            )
        }
    }
}
