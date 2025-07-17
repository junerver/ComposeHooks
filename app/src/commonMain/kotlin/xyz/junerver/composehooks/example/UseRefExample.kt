package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.junerver.compose.hooks.Ref
import xyz.junerver.compose.hooks.observeAsState
import xyz.junerver.compose.hooks.useEffect
import xyz.junerver.compose.hooks.useList
import xyz.junerver.compose.hooks.useRef
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.useUpdate
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.LogCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.TButton
import xyz.junerver.composehooks.ui.component.randomBackground

/*
  Description: Examples demonstrating the usage of useRef hook in Compose
  Author: Junerver
  Date: 2024/3/8-11:16
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Example demonstrating the use of useRef hook and its advanced applications
 */
@Composable
fun UseRefExample() {
    Surface {
        ScrollColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "useRef Examples",
                style = MaterialTheme.typography.headlineMedium,
            )

            Text(
                text = "The useRef hook provides a mutable reference that persists across renders without causing recomposition when its value changes.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            InteractiveRefDemo()

            ExampleCard(title = "Ref Non-Reactivity Feature") {
                NonReactivityExample()
            }

            ExampleCard(title = "Converting Ref to State") {
                RefToStateExample()
            }
        }
    }
}

/**
 * Interactive demonstration of useRef with real-time updates
 *
 * This component shows how refs don't trigger recomposition when changed,
 * but can be observed as state when needed.
 */
@Composable
private fun InteractiveRefDemo() {
    val countRef = useRef(0)
    val update = useUpdate()
    val logs = useList<String>()

    // Log ref changes using useEffect
    useEffect(countRef) {
        logs.add("Ref changed to: ${countRef.current}")
    }

    ExampleCard(title = "Interactive Demo") {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Display current ref value
            Text(
                text = "Current ref value: ${countRef.current}",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp),
            )

            Text(
                text = "Notice: Reading the ref value directly doesn't cause recomposition when it changes.",
                style = MaterialTheme.typography.bodySmall,
            )

            // Control buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TButton(
                    text = "Increment Ref",
                    onClick = {
                        countRef.current += 1
                        logs.add("Added '1' to ref (UI won't update yet)")
                    },
                    modifier = Modifier.weight(1f),
                )

                TButton(
                    text = "Force Update",
                    onClick = {
                        update()
                        logs.add("Forced UI update")
                    },
                    modifier = Modifier.weight(1f),
                )

                TButton(
                    text = "Reset",
                    onClick = {
                        countRef.current = 0
                        update()
                        logs.add("Reset ref to '0'")
                    },
                    modifier = Modifier.weight(1f),
                )
            }

            // Observed state demonstration
            ObservedRefDemo(countRef)

            // Log display
            LogCard(logs = logs)
        }
    }
}

/**
 * Demonstrates the non-reactivity of refs compared to state
 *
 * This example shows how changing a ref doesn't trigger recomposition,
 * while changing a state does.
 */
@Composable
private fun NonReactivityExample() {
    // Create a ref and a state with the same initial value
    val counterRef = useRef(0)
    var counterState by useState(0)
    val logs = useList<String>()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "This example demonstrates how refs don't trigger recomposition when changed, while states do.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Display both values with random backgrounds to visualize recomposition
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Ref Value",
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = "${counterRef.current}",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.randomBackground().padding(8.dp),
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "State Value",
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = "$counterState",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.randomBackground().padding(8.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Control buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TButton(
                text = "Increment Ref",
                onClick = {
                    counterRef.current++
                    logs.add("Ref incremented to ${counterRef.current} (UI won't update)")
                },
                modifier = Modifier.weight(1f),
            )

            TButton(
                text = "Increment State",
                onClick = {
                    counterState++
                    logs.add("State incremented to $counterState (UI will update)")
                },
                modifier = Modifier.weight(1f),
            )
        }

        Text(
            text = "Notice: The background color of the State value changes on each update, indicating recomposition. The Ref value's background only changes when the State changes.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(vertical = 8.dp),
        )

        // Log display
        LogCard(logs = logs)
    }
}

/**
 * Demonstrates how to convert a ref to an observable state
 *
 * @param ref The reference to observe
 */
@Composable
private fun ObservedRefDemo(ref: Ref<Int>) {
    // Convert ref to state using observeAsState
    val refState by ref.observeAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Text(
            text = "Observed as State: $refState",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.randomBackground(),
        )

        Text(
            text = "This component automatically updates when the ref changes because it uses observeAsState()",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

/**
 * Demonstrates how to convert refs to reactive states using observeAsState
 *
 * This example shows how to make refs reactive by converting them to states,
 * allowing automatic UI updates when the ref value changes.
 */
@Composable
private fun RefToStateExample() {
    // Create a ref
    val counterRef = useRef(0)
    val logs = useList<String>()

    // Convert the ref to a state
    val counterState by counterRef.observeAsState()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "This example demonstrates how to make refs reactive using observeAsState()",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Display the observed state value
        Text(
            text = "Observed Ref Value: $counterState",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.randomBackground().padding(8.dp),
        )

        Text(
            text = "The background color changes on each update, showing that observeAsState() makes the UI reactive to ref changes.",
            style = MaterialTheme.typography.bodySmall,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Control buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TButton(
                text = "Increment Ref",
                onClick = {
                    counterRef.current++
                    logs.add("Ref incremented to ${counterRef.current} (UI will update automatically)")
                },
                modifier = Modifier.weight(1f),
            )

            TButton(
                text = "Decrement Ref",
                onClick = {
                    counterRef.current--
                    logs.add("Ref decremented to ${counterRef.current} (UI will update automatically)")
                },
                modifier = Modifier.weight(1f),
            )
        }

        // Multiple observers demonstration
        Text(
            text = "Multiple Components Observing the Same Ref:",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
        )

        // Create multiple observer components for the same ref
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(3) { index ->
                RefObserver(
                    ref = counterRef,
                    label = "Observer ${index + 1}",
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Log display
        LogCard(logs = logs)
    }
}

/**
 * A component that observes a ref and displays its value
 *
 * @param ref The reference to observe
 * @param label The label to display
 * @param modifier The modifier to apply to the component
 */
@Composable
private fun RefObserver(ref: Ref<Int>, label: String, modifier: Modifier = Modifier) {
    // Convert ref to state
    val value by ref.observeAsState()

    Column(
        modifier = modifier
            .randomBackground()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            text = "$value",
            style = MaterialTheme.typography.titleLarge,
        )
    }
}
