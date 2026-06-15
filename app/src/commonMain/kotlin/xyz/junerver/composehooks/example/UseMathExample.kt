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
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.junerver.compose.hooks.useAbs
import xyz.junerver.compose.hooks.useCeil
import xyz.junerver.compose.hooks.useFloor
import xyz.junerver.compose.hooks.useMax
import xyz.junerver.compose.hooks.useMin
import xyz.junerver.compose.hooks.usePow
import xyz.junerver.compose.hooks.useRound
import xyz.junerver.compose.hooks.useSqrt
import xyz.junerver.compose.hooks.useTrunc
import xyz.junerver.compose.hooks.useState
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.ScrollColumn

/*
  Description: Example component for useMath hooks
  Author: Junerver
  Date: 2024/3/11-11:09
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Example component demonstrating the useMath hooks
 */
@Composable
fun UseMathExample() {
    ScrollColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "useMath Examples",
            style = MaterialTheme.typography.headlineMedium,
        )

        // Interactive Demo
        InteractiveMathDemo()

        // Basic usage examples
        ExampleCard(title = "useAbs") {
            AbsExample()
        }

        ExampleCard(title = "useCeil & useFloor") {
            CeilFloorExample()
        }

        ExampleCard(title = "useRound & useTrunc") {
            RoundTruncExample()
        }

        ExampleCard(title = "useMin & useMax") {
            MinMaxExample()
        }

        ExampleCard(title = "usePow & useSqrt") {
            PowSqrtExample()
        }
    }
}

/**
 * Interactive demo showing various math hooks
 */
@Composable
private fun InteractiveMathDemo() {
    var number by useState(0.0)

    val absValue by useAbs(number)
    val ceilValue by useCeil(number)
    val floorValue by useFloor(number)
    val roundValue by useRound(number)
    val truncValue by useTrunc(number)
    val sqrtValue by useSqrt(number)

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

            // Input slider
            Text("Input Value: $number")
            Slider(
                value = number.toFloat(),
                onValueChange = { number = it.toDouble() },
                valueRange = -10f..10f,
                steps = 20,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Results
            Text("useAbs: $absValue")
            Text("useCeil: $ceilValue")
            Text("useFloor: $floorValue")
            Text("useRound: $roundValue")
            Text("useTrunc: $truncValue")
            Text("useSqrt: ${sqrtValue.takeIf { !it.isNaN() } ?: "NaN (negative input)"}")
        }
    }
}

/**
 * Example showing useAbs
 */
@Composable
private fun AbsExample() {
    var number by useState(-5)

    val absInt by useAbs(number)
    val absDouble by useAbs(number.toDouble())

    Column {
        Text(
            text = "Input: $number",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Text("Absolute value (Int): $absInt")
        Text("Absolute value (Double): $absDouble")

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = number.toFloat(),
            onValueChange = { number = it.toInt() },
            valueRange = -10f..10f,
            steps = 20,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * Example showing useCeil and useFloor
 */
@Composable
private fun CeilFloorExample() {
    var number by useState(3.7)

    val ceilValue by useCeil(number)
    val floorValue by useFloor(number)

    Column {
        Text(
            text = "Input: $number",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Text("Ceil: $ceilValue")
        Text("Floor: $floorValue")

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = number.toFloat(),
            onValueChange = { number = it.toDouble() },
            valueRange = -5f..5f,
            steps = 100,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * Example showing useRound and useTrunc
 */
@Composable
private fun RoundTruncExample() {
    var number by useState(3.7)

    val roundValue by useRound(number)
    val truncValue by useTrunc(number)

    Column {
        Text(
            text = "Input: $number",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Text("Round: $roundValue")
        Text("Trunc: $truncValue")

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = number.toFloat(),
            onValueChange = { number = it.toDouble() },
            valueRange = -5f..5f,
            steps = 100,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * Example showing useMin and useMax
 */
@Composable
private fun MinMaxExample() {
    var a by useState(5)
    var b by useState(10)

    val minValue by useMin(a, b)
    val maxValue by useMax(a, b)

    Column {
        Text(
            text = "a: $a, b: $b",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Text("Min: $minValue")
        Text("Max: $maxValue")

        Spacer(modifier = Modifier.height(8.dp))

        Text("Value a:")
        Slider(
            value = a.toFloat(),
            onValueChange = { a = it.toInt() },
            valueRange = 0f..20f,
            steps = 20,
            modifier = Modifier.fillMaxWidth(),
        )

        Text("Value b:")
        Slider(
            value = b.toFloat(),
            onValueChange = { b = it.toInt() },
            valueRange = 0f..20f,
            steps = 20,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * Example showing usePow and useSqrt
 */
@Composable
private fun PowSqrtExample() {
    var base by useState(2.0)
    var exponent by useState(3.0)

    val powValue by usePow(base, exponent)
    val sqrtValue by useSqrt(base)

    Column {
        Text(
            text = "Base: $base, Exponent: $exponent",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Text("Pow: $powValue")
        Text("Sqrt: ${sqrtValue.takeIf { !it.isNaN() } ?: "NaN (negative input)"}")

        Spacer(modifier = Modifier.height(8.dp))

        Text("Base:")
        Slider(
            value = base.toFloat(),
            onValueChange = { base = it.toDouble() },
            valueRange = 0f..10f,
            steps = 100,
            modifier = Modifier.fillMaxWidth(),
        )

        Text("Exponent:")
        Slider(
            value = exponent.toFloat(),
            onValueChange = { exponent = it.toDouble() },
            valueRange = 0f..5f,
            steps = 50,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}