package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.useCounter
import xyz.junerver.compose.hooks.useState
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.TButton
import xyz.junerver.composehooks.utils.formatToDecimalPlaces

/*
  Description: Example component for useCounter hook
  Author: Junerver
  Date: 2024/7/8-13:53
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Example component demonstrating the useCounter hook
 */
@Composable
fun UseCounterExample() {
    ScrollColumn(
        modifier = Modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "useCounter Examples",
            style = MaterialTheme.typography.headlineMedium,
        )

        // Interactive Demo
        InteractiveCounterDemo()

        // Basic usage example
        ExampleCard(title = "Basic Usage") {
            BasicCounterExample()
        }

        // Advanced usage example
        ExampleCard(title = "Advanced Usage: Custom Min/Max") {
            CustomBoundsExample()
        }

        // Practical application example
        ExampleCard(title = "Practical Application: Quantity Selector") {
            QuantitySelectorExample()
        }
    }
}

/**
 * Basic counter example demonstrating simple increment and decrement
 */
@Composable
private fun BasicCounterExample() {
    val (count, increment, decrement) = useCounter(
        initialValue = 0,
        optionsOf = {
            min = 0
            max = 10
        },
    )

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Count: ${count.value}",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TButton(text = "+", onClick = { increment(1) })
            TButton(text = "-", onClick = { decrement(1) })
        }
    }
}

/**
 * Example demonstrating custom min and max bounds
 */
@Composable
private fun CustomBoundsExample() {
    val counterHolder = useCounter(
        initialValue = 5,
        optionsOf = {
            min = 0
            max = 10
        },
    )
    val (count, increment, decrement, set) = counterHolder

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Count: ${count.value} (Range: 0-10)",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TButton(text = "+2", onClick = { increment(2) })
            TButton(text = "-2", onClick = { decrement(2) })
            TButton(text = "Set to 8", onClick = { set(8) })
        }
    }
}

/**
 * Practical example: A quantity selector for a shopping cart
 */
@Composable
private fun QuantitySelectorExample() {
    val (quantity, increment, decrement, set) = useCounter(
        initialValue = 1,
        optionsOf = {
            min = 1
            max = 99
        },
    )

    var price by useState(19.99)
    var productName by useState("Premium Widget")

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = productName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            Text(
                text = "$$price each",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 4.dp),
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Quantity:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )

                TButton(
                    text = "-",
                    onClick = { decrement(1) },
                    modifier = Modifier.padding(end = 8.dp),
                )

                OutlinedTextField(
                    value = quantity.value.toString(),
                    onValueChange = { newValue ->
                        newValue.toIntOrNull()?.let {
                            if (it in 1..99) set(it)
                        }
                    },
                    modifier = Modifier.weight(1f),
                )

                TButton(
                    text = "+",
                    onClick = { increment(1) },
                    modifier = Modifier.padding(start = 8.dp),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Total: $${(price * quantity.value).formatToDecimalPlaces(2)}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

/**
 * Interactive demo component for useCounter hook
 */
@Composable
private fun InteractiveCounterDemo() {
    var initialValue by useState(5)
    var minValue by useState(0)
    var maxValue by useState(10)
    var incrementAmount by useState(1)

    val (current, inc, dec, set, reset) = useCounter(
        initialValue = initialValue,
        optionsOf = {
            min = minValue
            max = maxValue
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
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color,
            )

            // Current counter value display
            Text(
                text = "Current Value:",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
            )

            Text(
                text = "${current.value}",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            // Configuration options
            Text(
                text = "Configuration:",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            // Min value slider
            Text(
                text = "Min Value: $minValue",
                style = MaterialTheme.typography.bodyMedium,
            )
            Slider(
                value = minValue.toFloat(),
                onValueChange = { minValue = it.toInt() },
                valueRange = 0f..maxValue.toFloat(),
                steps = maxValue,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            // Max value slider
            Text(
                text = "Max Value: $maxValue",
                style = MaterialTheme.typography.bodyMedium,
            )
            Slider(
                value = maxValue.toFloat(),
                onValueChange = { maxValue = it.toInt() },
                valueRange = minValue.toFloat()..100f,
                steps = 100 - minValue,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            // Increment amount
            Text(
                text = "Increment Amount: $incrementAmount",
                style = MaterialTheme.typography.bodyMedium,
            )
            Slider(
                value = incrementAmount.toFloat(),
                onValueChange = { incrementAmount = it.toInt() },
                valueRange = 1f..5f,
                steps = 4,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            // Control buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                TButton(
                    text = "Increment",
                    onClick = { inc(incrementAmount) },
                    modifier = Modifier.weight(1f),
                )

                TButton(
                    text = "Decrement",
                    onClick = { dec(incrementAmount) },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                TButton(
                    text = "Set to 5",
                    onClick = { set(5) },
                    modifier = Modifier.weight(1f),
                )

                TButton(
                    text = "Double",
                    onClick = { set { it * 2 } },
                    modifier = Modifier.weight(1f),
                )

                TButton(
                    text = "Reset",
                    onClick = { reset() },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
