package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.junerver.compose.hooks.useMemoizedFn
import xyz.junerver.compose.hooks.useState
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description: Example component for useMemoizedFn hook
  Author: Junerver
  Date: 2024/3/11-11:09
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Example component demonstrating the useMemoizedFn hook
 */
@Composable
fun UseMemoizedFnExample() {
    ScrollColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "useMemoizedFn Examples",
            style = MaterialTheme.typography.headlineMedium,
        )

        // Interactive Demo
        InteractiveMemoizedFnDemo()

        // Basic usage example
        ExampleCard(title = "Basic Usage") {
            BasicMemoizedFnExample()
        }

        // Performance example
        ExampleCard(title = "Performance Example") {
            PerformanceMemoizedFnExample()
        }
    }
}

/**
 * Interactive demo showing how useMemoizedFn works
 */
@Composable
private fun InteractiveMemoizedFnDemo() {
    var counter by useState(0)
    var callCount by useState(0)

    // Create a memoized recursive function that calculates factorial
    val factorial = useMemoizedFn<Int, Long> { n ->
        if (n <= 1) 1L
        else n * callRecursive(n - 1)
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

            Spacer(modifier = Modifier.height(16.dp))

            // Current values display
            Text(
                text = "Input: $counter",
                style = MaterialTheme.typography.bodyLarge,
            )

            Text(
                text = "Factorial: ${factorial(counter)}",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp),
            )

            Text(
                text = "Function calls: $callCount",
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Control buttons
            TButton(
                text = "Increment (+1)",
                modifier = Modifier.fillMaxWidth(),
            ) {
                counter++
                callCount++
            }

            Spacer(modifier = Modifier.height(8.dp))

            TButton(
                text = "Reset",
                modifier = Modifier.fillMaxWidth(),
            ) {
                counter = 0
                callCount = 0
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Instructions
            Text(
                text = "This demonstrates a memoized recursive function that calculates factorial. " +
                    "The function is memoized, meaning each calculation is cached.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

/**
 * Basic example showing how to use useMemoizedFn
 */
@Composable
private fun BasicMemoizedFnExample() {
    var input by useState(5)

    // Create a memoized recursive function that calculates Fibonacci numbers
    val fibonacci = useMemoizedFn<Int, Long> { n ->
        if (n <= 1) n.toLong()
        else callRecursive(n - 1) + callRecursive(n - 2)
    }

    Column {
        Text(
            text = "This hook creates a memoized recursive function.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Text("Input: $input")
        Text("Fibonacci: ${fibonacci(input)}")

        Spacer(modifier = Modifier.height(8.dp))

        TButton(
            text = "Increment Input",
            modifier = Modifier.fillMaxWidth(),
        ) {
            input++
        }
    }
}

/**
 * Performance example showing memoization benefits
 */
@Composable
private fun PerformanceMemoizedFnExample() {
    var input by useState(10)

    // Create a memoized recursive function
    val memoizedFib = useMemoizedFn<Int, Long> { n ->
        if (n <= 1) n.toLong()
        else callRecursive(n - 1) + callRecursive(n - 2)
    }

    Column {
        Text(
            text = "Performance Comparison",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Text("Input: $input")
        Text("Memoized Result: ${memoizedFib(input)}")

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Without memoization, calculating Fibonacci for large numbers would be very slow. " +
                "With memoization, each value is calculated only once.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        TButton(
            text = "Increment Input",
            modifier = Modifier.fillMaxWidth(),
        ) {
            input++
        }
    }
}