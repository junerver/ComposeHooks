package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.junerver.compose.hooks.*
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.LogCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description: Example component for useEffect hook
  Author: Junerver
  Date: 2024/3/11-11:09
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Example component demonstrating the useEffect hook
 */
@Composable
fun UseEffectExample() {
    ScrollColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "useEffect Examples",
            style = MaterialTheme.typography.headlineMedium,
        )

        // Interactive Demo
        InteractiveEffectDemo()

        // Basic usage example
        ExampleCard(title = "Basic Usage: No Dependencies") {
            NoDepExample()
        }

        // State dependency example
        ExampleCard(title = "State Dependency") {
            StateDepExample()
        }

        // Ref dependency example
        ExampleCard(title = "Ref Dependency") {
            RefDepExample()
        }

        // Multiple dependencies example
        ExampleCard(title = "Multiple Dependencies") {
            MultipleDepExample()
        }
    }
}

/**
 * Interactive demo showing different useEffect scenarios
 */
@Composable
private fun InteractiveEffectDemo() {
    var mountCount by useState(0)
    var stateValue by useState(0)
    var refValue by useState(0)
    val effectLog = useList<String>()

    // Effect with no dependencies - runs only once
    useEffect {
        val message = "Mount effect executed (count: ${++mountCount})"
        effectLog.add(message)
        println(message)
    }

    // Effect with state dependency
    useEffect(stateValue) {
        val message = "State effect executed: stateValue = $stateValue"
        effectLog.add(message)
        println(message)
    }

    // Effect with ref dependency
    val ref = useRef(refValue)
    useEffect(ref) {
        val message = "Ref effect executed: ref.current = ${ref.current}"
        effectLog.add(message)
        println(message)
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
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color,
            )

            // Current values display
            Text(
                text = "Current Values:",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Text("State Value: $stateValue")
            Text("Ref Value: $refValue")
            Text("Mount Count: $mountCount")

            Spacer(modifier = Modifier.height(16.dp))

            // Control buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TButton(
                    text = "State +1",
                    modifier = Modifier.weight(1f),
                ) {
                    stateValue++
                }
                TButton(
                    text = "Ref +1",
                    modifier = Modifier.weight(1f),
                ) {
                    refValue++
                    ref.current = refValue
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TButton(
                text = "Clear Log",
                modifier = Modifier.fillMaxWidth(),
            ) {
                effectLog.clear()
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Effect log display
            LogCard(title = "Effect Log:", logs = effectLog)
        }
    }
}

/**
 * Basic example with no dependencies
 */
@Composable
private fun NoDepExample() {
    var executionCount by useState(0)

    useEffect {
        executionCount++
        println("useEffect with no dependencies executed: $executionCount")
    }

    Column {
        Text(
            text = "This effect runs only once when the component mounts.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Text("Execution count: $executionCount")
    }
}

/**
 * Example with state dependency
 */
@Composable
private fun StateDepExample() {
    val (state, setState, getState) = useGetState(0)
    var effectCount by useState(0)

    useEffect(state) {
        effectCount++
        println("useEffect with state dependency executed: state = ${getState()}, count = $effectCount")
    }

    Column {
        Text(
            text = "This effect runs whenever the state changes.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Text("State value: ${getState()}")
        Text("Effect execution count: $effectCount")
        Spacer(modifier = Modifier.height(8.dp))
        TButton(text = "Increment State") {
            setState { it + 1 }
        }
    }
}

/**
 * Example with ref dependency
 */
@Composable
private fun RefDepExample() {
    val ref = useRef(0)
    var effectCount by useState(0)

    useEffect(ref) {
        effectCount++
        println("useEffect with ref dependency executed: ref.current = ${ref.current}, count = $effectCount")
    }

    Column {
        Text(
            text = "This effect runs whenever the ref value changes.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Text("Ref value: ${ref.current}")
        Text("Effect execution count: $effectCount")
        Spacer(modifier = Modifier.height(8.dp))
        TButton(text = "Increment Ref") {
            ref.current += 1
        }
    }
}

/**
 * Example with multiple dependencies
 */
@Composable
private fun MultipleDepExample() {
    var count1 by useState(0)
    var count2 by useState(0)
    var effectCount by useState(0)

    useEffect(count1, count2) {
        effectCount++
        println("useEffect with multiple dependencies executed: count1 = $count1, count2 = $count2, effectCount = $effectCount")
    }

    Column {
        Text(
            text = "This effect runs when either count1 or count2 changes.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Text("Count 1: $count1")
        Text("Count 2: $count2")
        Text("Effect execution count: $effectCount")
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TButton(
                text = "Count1 +1",
                modifier = Modifier.weight(1f),
            ) {
                count1++
            }
            TButton(
                text = "Count2 +1",
                modifier = Modifier.weight(1f),
            ) {
                count2++
            }
        }
    }
}
