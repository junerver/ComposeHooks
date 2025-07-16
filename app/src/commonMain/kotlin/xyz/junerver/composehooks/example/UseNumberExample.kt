package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.random.Random
import xyz.junerver.compose.hooks.useDouble
import xyz.junerver.compose.hooks.useFloat
import xyz.junerver.compose.hooks.useInt
import xyz.junerver.compose.hooks.useLong
import xyz.junerver.compose.hooks.useState
import xyz.junerver.composehooks.ui.component.DividerSpacer
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description: Example demonstrating the useState and useNumber hooks for numeric values
  Author: Junerver
  Date: 2024/3/11-8:34
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Example component demonstrating the useState and useNumber hooks
 */
@Composable
fun UseNumberExample() {
    Surface {
        ScrollColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Page title
            Text(
                text = "Number State Examples",
                style = MaterialTheme.typography.headlineMedium,
            )

            // Introduction text
            Text(
                text = "These examples demonstrate different ways to manage numeric state in Compose. The useState hook provides a generic way to handle state, while specialized hooks like useInt, useLong, useFloat, and useDouble offer optimized performance for specific number types.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            // Generic useState example
            ExampleCard(title = "Generic useState with Numbers") {
                GenericStateExample()
            }

            // Specialized number hooks example
            ExampleCard(title = "Specialized Number Hooks") {
                SpecializedNumberHooksExample()
            }

            // Performance comparison example
            ExampleCard(title = "Performance Comparison") {
                PerformanceComparisonExample()
            }
        }
    }
}

/**
 * Example demonstrating the generic useState hook with different number types
 */
@Composable
private fun GenericStateExample() {
    val (countInt, setCountInt) = useState(0)
    val (countLong, setCountLong) = useState(0L)
    val (countFloat, setCountFloat) = useState(0f)
    val (countDouble, setCountDouble) = useState(0.0)

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(
            text = "Generic useState with Different Number Types",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        Text(
            text = "The useState hook can handle any type of value, including different numeric types:",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp),
        )

        // Display current values
        Text(
            text = "Integer: $countInt",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp),
        )

        Text(
            text = "Long: $countLong",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp),
        )

        Text(
            text = "Float: $countFloat",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp),
        )

        Text(
            text = "Double: $countDouble",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
        )

        // Button to update all values
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TButton(
                text = "Add Random Values",
                onClick = {
                    setCountInt(countInt + Random.nextInt(1, 10))
                    setCountLong(countLong + Random.nextLong(1, 10))
                    setCountFloat(countFloat + Random.nextFloat())
                    setCountDouble(countDouble + Random.nextDouble(1.0, 10.0))
                },
            )

            TButton(
                text = "Reset",
                onClick = {
                    setCountInt(0)
                    setCountLong(0L)
                    setCountFloat(0f)
                    setCountDouble(0.0)
                },
            )
        }

        Text(
            text = "The useState hook provides a simple way to manage state with a getter and setter pattern, but it's not optimized specifically for numeric types.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

/**
 * Example demonstrating specialized number hooks for different numeric types
 */
@Composable
private fun SpecializedNumberHooksExample() {
    // Using specialized number hooks
    val intValue = useInt(0)
    val longValue = useLong(0L)
    val floatValue = useFloat(0f)
    val doubleValue = useDouble(0.0)

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(
            text = "Specialized Number Hooks",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        Text(
            text = "These specialized hooks are optimized for specific numeric types:",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp),
        )

        // Display current values
        Text(
            text = "Integer (useInt): ${intValue.intValue}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp),
        )

        Text(
            text = "Long (useLong): ${longValue.longValue}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp),
        )

        Text(
            text = "Float (useFloat): ${floatValue.floatValue}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp),
        )

        Text(
            text = "Double (useDouble): ${doubleValue.doubleValue}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
        )

        // Button to update all values
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TButton(
                text = "Add Random Values",
                onClick = {
                    intValue.intValue += Random.nextInt(1, 10)
                    longValue.longValue += Random.nextLong(1, 10)
                    floatValue.floatValue += Random.nextFloat()
                    doubleValue.doubleValue += Random.nextDouble(1.0, 10.0)
                },
            )

            TButton(
                text = "Reset",
                onClick = {
                    intValue.intValue = 0
                    longValue.longValue = 0L
                    floatValue.floatValue = 0f
                    doubleValue.doubleValue = 0.0
                },
            )
        }

        Text(
            text = "These specialized hooks provide direct access to the mutable state value, offering better performance than generic useState for numeric operations.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

/**
 * Example comparing performance aspects of different number state approaches
 */
@Composable
private fun PerformanceComparisonExample() {
    // Using both approaches for comparison
    val (genericInt, setGenericInt) = useState(0)
    val specializedInt = useInt(0)

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(
            text = "Performance Comparison",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        Text(
            text = "Compare the performance characteristics of generic vs. specialized number hooks:",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp),
        )

        // Generic useState approach
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Generic useState<Int>",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.secondary,
                )

                Text(
                    text = "Current value: $genericInt",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 8.dp),
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TButton(
                        text = "Increment",
                        onClick = { setGenericInt(genericInt + 1) },
                    )

                    TButton(
                        text = "Reset",
                        onClick = { setGenericInt(0) },
                    )
                }

                Text(
                    text = "Characteristics: Requires destructuring, uses a setter function, creates a new State object for each value type.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        DividerSpacer()

        // Specialized hook approach
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Specialized useInt",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.secondary,
                )

                Text(
                    text = "Current value: ${specializedInt.intValue}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 8.dp),
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TButton(
                        text = "Increment",
                        onClick = { specializedInt.intValue++ },
                    )

                    TButton(
                        text = "Reset",
                        onClick = { specializedInt.intValue = 0 },
                    )
                }

                Text(
                    text = "Characteristics: Direct property access, no destructuring needed, optimized for the specific number type, more efficient for frequent updates.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        Text(
            text = "When to use which approach:\n\n• Use specialized hooks (useInt, useLong, etc.) when you need optimal performance for numeric operations, especially with frequent updates.\n\n• Use generic useState when you need more flexibility or when working with complex state that might change type.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}
