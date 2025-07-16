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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import xyz.junerver.compose.hooks.useGetState
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.useThrottle
import xyz.junerver.compose.hooks.useThrottleEffect
import xyz.junerver.compose.hooks.useThrottleFn
import xyz.junerver.composehooks.net.NetApi
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.TButton
import xyz.junerver.composehooks.utils.subStringIf

/*
  Description: useThrottle hook examples
  Author: Junerver
  Date: 2024/3/8-14:13
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Interactive demo component for useThrottle hook
 */
@Composable
private fun InteractiveThrottleDemo() {
    var waitTime by useState(1000f) // milliseconds
    var leading by useState(false)
    var trailing by useState(true)
    val (counter, setCounter) = useState(0)
    val (clickCount, setClickCount) = useState(0)

    val throttledCounter by useThrottle(counter) {
        wait = waitTime.toInt().milliseconds
        this.leading = leading
        this.trailing = trailing
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Interactive Throttle Demo",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            HorizontalDivider()

            // Current values display
            Text(
                text = "Original: $counter | Throttled: $throttledCounter | Clicks: $clickCount",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Wait time slider
            Text("Wait Time: ${waitTime.toInt()}ms")
            Slider(
                value = waitTime,
                onValueChange = { waitTime = it },
                valueRange = 100f..3000f,
                modifier = Modifier.fillMaxWidth(),
            )

            // Leading/Trailing options
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = leading,
                    onClick = { leading = !leading },
                )
                Text("Leading")

                Spacer(modifier = Modifier.width(16.dp))

                RadioButton(
                    selected = trailing,
                    onClick = { trailing = !trailing },
                )
                Text("Trailing")
            }

            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TButton(
                    text = "Increment",
                    modifier = Modifier.weight(1f),
                ) {
                    setCounter(counter + 1)
                    setClickCount(clickCount + 1)
                }

                TButton(
                    text = "Reset",
                    modifier = Modifier.weight(1f),
                ) {
                    setCounter(0)
                    setClickCount(0)
                }
            }
        }
    }
}

/**
 * Basic usage example for useThrottle
 */
@Composable
private fun BasicThrottleExample() {
    val (state, setState) = useGetState(0)
    val throttledState by useThrottle(value = state.value) {
        wait = 1.seconds
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Original value: ${state.value}")
        Text("Throttled value: $throttledState")
        Text(
            text = "Configuration: leading=true, trailing=true, wait=1s",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 16.dp),
        )
        TButton(text = "Increment (+1)") {
            setState(state.value + 1)
        }
    }
}

/**
 * Example for useThrottleFn hook
 */
@Composable
private fun ThrottleFnExample() {
    val (stateFn, setStateFn) = useGetState(0)
    val (executionCount, setExecutionCount) = useState(0)

    val throttledFn = useThrottleFn(
        fn = {
            setStateFn(stateFn.value + 1)
        },
        optionsOf = {
            wait = 1.seconds
            leading = true
            trailing = true
        },
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Function executions: ${stateFn.value}")
        Text("Button clicks: $executionCount")
        Text(
            text = "Configuration: leading=true, trailing=true, wait=1s",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 16.dp),
        )
        TButton(text = "Throttled Function Call") {
            throttledFn()
            setExecutionCount(executionCount + 1)
        }
    }
}

/**
 * Example for useThrottleEffect hook
 */
@Composable
private fun ThrottleEffectExample() {
    val (stateEf, setStateEf) = useGetState(0)
    val (result, setResult) = useGetState("Ready")
    val (requestCount, setRequestCount) = useState(0)

    useThrottleEffect(stateEf.value) {
        setResult("Loading...")
        setRequestCount(requestCount + 1)
        try {
            val resp = NetApi.userInfo("junerver")
            setResult("Success: ${resp.toString().subStringIf()}")
        } catch (e: Exception) {
            setResult("Error: ${e.message}")
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Trigger count: ${stateEf.value}")
        Text("API calls made: $requestCount")
        Text("Result: ${result.value}")
        Text(
            text = "Configuration: leading=true, trailing=true, wait=1s",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 16.dp),
        )
        TButton(text = "Trigger Effect") {
            setStateEf(stateEf.value + 1)
        }
    }
}

/**
 * Advanced usage example with different configurations
 */
@Composable
private fun AdvancedThrottleExample() {
    val (counter, setCounter) = useState(0)

    // Leading only
    val leadingOnly by useThrottle(counter) {
        wait = 1.seconds
        leading = true
        trailing = false
    }

    // Trailing only
    val trailingOnly by useThrottle(counter) {
        wait = 1.seconds
        leading = false
        trailing = true
    }

    // Both leading and trailing
    val both by useThrottle(counter) {
        wait = 1.seconds
        leading = true
        trailing = true
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Original: $counter")
        Text("Leading only: $leadingOnly")
        Text("Trailing only: $trailingOnly")
        Text("Both: $both")

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TButton(
                text = "Increment",
                modifier = Modifier.weight(1f),
            ) {
                setCounter(counter + 1)
            }

            TButton(
                text = "Reset",
                modifier = Modifier.weight(1f),
            ) {
                setCounter(0)
            }
        }
    }
}

@Composable
fun UseThrottleExample() {
    ScrollColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "useThrottle Examples",
            style = MaterialTheme.typography.headlineMedium,
        )

        // Interactive Demo
        InteractiveThrottleDemo()

        // Basic Usage
        ExampleCard(title = "Basic Usage") {
            BasicThrottleExample()
        }

        // Throttled Function
        ExampleCard(title = "useThrottleFn") {
            ThrottleFnExample()
        }

        // Throttled Effect
        ExampleCard(title = "useThrottleEffect") {
            ThrottleEffectExample()
        }

        // Advanced Usage
        ExampleCard(title = "Advanced Configuration") {
            AdvancedThrottleExample()
        }
    }
}
