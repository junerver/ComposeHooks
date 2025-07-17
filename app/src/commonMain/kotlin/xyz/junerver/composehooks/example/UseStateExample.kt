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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import xyz.junerver.compose.hooks.Tuple2
import xyz.junerver.compose.hooks._useState
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.tuple
import xyz.junerver.compose.hooks.useBoolean
import xyz.junerver.compose.hooks.useGetState
import xyz.junerver.compose.hooks.useLatestRef
import xyz.junerver.compose.hooks.useList
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.useStateAsync
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.LogCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.SimpleContainer
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description: [useState]can make controlled components easier to create
  Author: Junerver
  Date: 2024/3/8-14:29
  Email: junerver@gmail.com
  Version: v1.0
*/
@Composable
fun UseStateExample() {
    Surface {
        ScrollColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "useState Examples",
                style = MaterialTheme.typography.headlineMedium,
            )

            // Interactive Demo
            InteractiveStateDemo()

            // Basic usage example
            ExampleCard(title = "Basic Usage") {
                BasicUsageExample()
            }

            // Closure problems example
            ExampleCard(title = "Closure Problems") {
                ClosureProblemsExample()
            }

            // Quick state updates example
            ExampleCard(title = "Quick State Updates") {
                QuickStateUpdatesExample()
            }

            // Solutions to closure problems
            ExampleCard(title = "Solutions to Closure Problems") {
                ClosureSolutionsExample()
            }

            // Computed properties example
            ExampleCard(title = "Computed Properties") {
                ComputedPropertiesExample()
            }

            // Async computed properties example
            ExampleCard(title = "Async Computed Properties") {
                AsyncComputedPropertiesExample()
            }
        }
    }
}

/**
 * Interactive demo for useState hook
 */
@Composable
private fun InteractiveStateDemo() {
    val (text, setText) = useState("")

    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Interactive Demo",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Current value: $text",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp),
            )

            Text(
                text = "This is a simple controlled component using useState hook.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            OutlinedTextField(
                value = text,
                onValueChange = setText,
                label = { Text("Type something...") },
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = "Important note: While useState is convenient, it has two potential issues:",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
            )

            Text(
                text = "1. Closure problems in functions (see examples below)",
                style = MaterialTheme.typography.bodySmall,
            )

            Text(
                text = "2. State loss during rapid updates (millisecond level)",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

/**
 * Basic usage example for useState hook
 */
@Composable
private fun BasicUsageExample() {
    val (state, setState) = useState("Hello, useState!")

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "This example shows the basic usage of useState hook:",
            style = MaterialTheme.typography.bodySmall,
        )

        OutlinedTextField(
            value = state,
            onValueChange = setState,
            label = { Text("Edit me") },
            modifier = Modifier.fillMaxWidth(),
        )

        Text("Current value: $state")
    }
}

/**
 * Example demonstrating closure problems with useState
 */
@Composable
private fun ClosureProblemsExample() {
    val (state, setState) = useState("destructure State")
    val directState = useState(default = "directly read and write the MutableState value")
    var byState by useState("by delegate")
    val (state2, setState2) = useGetState("useGetState")

    // State to track if the demo is running
    val (isRunning, toggle, _, _, setFalse) = useBoolean(false)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "This example demonstrates closure problems when using useState in coroutines:",
            style = MaterialTheme.typography.bodySmall,
        )

        Text(
            text = "When using destructuring declarations with useState, you need to pay special attention to coroutine scenarios.",
            style = MaterialTheme.typography.bodySmall,
        )

        Row(modifier = Modifier.padding(vertical = 8.dp)) {
            TButton(
                text = if (isRunning.value) "Stop Demo" else "Start Demo",
                onClick = { toggle() },
            )
        }

        // Run the demo when isRunning is true
        if (isRunning.value) {
            LaunchedEffect(key1 = Unit) {
                repeat(10) {
                    delay(1.seconds)
                    // Closure issues occur when using destructured state values in closure functions
                    setState("$state.")
                    // Directly using MutableState to access the state value will not cause closure problems
                    directState.value += "."
                    // by delegate, it will not cause closure problems
                    byState += "."
                    // useState + useLatestRef can avoid closure problems
                    setState2 { "$it." }
                }
                setFalse()
            }
        }

        Column(modifier = Modifier.padding(top = 8.dp)) {
            Text("1. Destructured state (closure problem):", style = MaterialTheme.typography.bodyMedium)
            Text(text = state, modifier = Modifier)

            Text("2. Direct MutableState access (no closure problem):", style = MaterialTheme.typography.bodyMedium)
            Text(text = directState.value, modifier = Modifier)

            Text("3. By delegate (no closure problem):", style = MaterialTheme.typography.bodyMedium)
            Text(text = byState, modifier = Modifier)

            Text("4. useGetState (no closure problem):", style = MaterialTheme.typography.bodyMedium)
            Text(text = state2.value, modifier = Modifier)
        }
    }
}

/**
 * Example demonstrating quick state updates issues
 */
@Composable
private fun QuickStateUpdatesExample() {
    // useLatestRef can avoid closure problems
    val (state, setState) = useState("destructure State")
    val stateRef = useLatestRef(state)

    val directState = useState(default = "directly read and write the MutableState value")
    var byState by useState("by delegate")
    val (state2, setState2) = useGetState("useGetState")

    // State to track if the demo is running
    val (isRunning, toggle, _, _, setFalse) = useBoolean(false)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "This example demonstrates state loss when updating state very quickly:",
            style = MaterialTheme.typography.bodySmall,
        )

        Text(
            text = "If you call the set function quickly (millisecond level), there will be a problem of state loss with destructured state.",
            style = MaterialTheme.typography.bodySmall,
        )

        Row(modifier = Modifier.padding(vertical = 8.dp)) {
            TButton(
                text = if (isRunning.value) "Stop Demo" else "Start Demo",
                onClick = { toggle() },
            )
        }

        // Run the demo when isRunning is true
        if (isRunning.value) {
            LaunchedEffect(key1 = Unit) {
                repeat(20) {
                    // If you call the set function quickly(millisecond level), there will be a problem of state loss.
                    setState("${stateRef.current}.")
                    directState.value += "."
                    // if use by delegate, can modify status correctly
                    byState += "."
                    setState2 { "$it." }
                }
                setFalse()
            }
        }

        Column(modifier = Modifier.padding(top = 8.dp)) {
            Text("1. Destructured state with useLatestRef:", style = MaterialTheme.typography.bodyMedium)
            Text(text = state, modifier = Modifier)

            Text("2. Direct MutableState access:", style = MaterialTheme.typography.bodyMedium)
            Text(text = directState.value, modifier = Modifier)

            Text("3. By delegate:", style = MaterialTheme.typography.bodyMedium)
            Text(text = byState, modifier = Modifier)

            Text("4. useGetState:", style = MaterialTheme.typography.bodyMedium)
            Text(text = state2.value, modifier = Modifier)
        }

        Text(
            text = "Note: This happens because recomposition is asynchronous. When we update state quickly, recomposition doesn't happen immediately. Only after recomposition occurs will the destructured state be updated to the latest value.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

/**
 * An example showing using destructuring declarations can lead to closure
 * problems in some cases
 *
 * @param default
 * @return
 */
@Composable
private fun useAddIncorrect(default: Int = 0): Tuple2<Int, () -> Unit> {
    val (state, setState) = useState(default)

    // This kind of code will cause closure problems,
    fun add() {
        setState(state + 1) // The value of state is always default
    }
    return tuple(
        first = state,
        second = ::add,
    )
}

@Composable
private fun useAddCorrect1(default: Int = 0): Tuple2<Int, () -> Unit> {
    var state by _useState(default)

    // Using the `by` delegate can avoid most closure problems
    fun add() {
        state += 1
    }
    return tuple(
        first = state,
        second = ::add,
    )
}

@Composable
private fun useAddCorrect2(default: Int = 0): Tuple2<Int, () -> Unit> {
    val (state, setState) = _useState(default)
    // Using lambda generally does not cause closure problems in simple scenarios,
    // but if it is a complex lambda like `useReducer` source code, it may also cause
    // closure problems. This situation can be circumvented by `useLatestRef`.
    val add = { setState(state + 1) }
    return tuple(
        first = state,
        second = add,
    )
}

/**
 * You can use [useGetState] to get the Ref of state using tuple's third
 * [get] fun
 *
 * @param default
 * @return
 */
@Composable
private fun useAddCorrect3(default: Int = 0): Tuple2<Int, () -> Unit> {
    val (state, setState) = useGetState(default)

    fun add() {
        setState { it + 1 }
    }
    return tuple(
        first = state.value,
        second = ::add,
    )
}

@Composable
private fun useAddCorrect4(default: Int = 0): Tuple2<Int, () -> Unit> {
    val (state, setState) = useState(default)
    val stateRef = useLatestRef(state)

    fun add() {
        setState(stateRef.current + 1)
    }
    return tuple(
        first = state,
        second = ::add,
    )
}

/**
 * Example demonstrating computed properties with useState
 */

/**
 * Example demonstrating different solutions to closure problems
 */
@Composable
private fun ClosureSolutionsExample() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "There are several ways to solve the closure problem:",
            style = MaterialTheme.typography.bodySmall,
        )

        // Example 1: useAddIncorrect - demonstrates the problem
        ExampleCard(title = "1. Incorrect Implementation (with closure problem)") {
            val (num, add) = useAddIncorrect()
            Column {
                Text(text = "Current: $num")
                TButton(text = "+1") {
                    add()
                }
                Text(
                    text = "Problem: In coroutines or callbacks, the destructured value is captured and won't update",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        // Example 2: useAddCorrect1 - using MutableState directly
        ExampleCard(title = "2. Solution: Use MutableState directly") {
            val (num, add) = useAddCorrect1()
            Column {
                Text(text = "Current: $num")
                TButton(text = "+1") {
                    add()
                }
                Text(
                    text = "Solution: Access the MutableState.value directly instead of destructuring",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        // Example 3: useAddCorrect2 - using by delegate
        ExampleCard(title = "3. Solution: Use 'by' delegate") {
            val (num, add) = useAddCorrect2()
            Column {
                Text(text = "Current: $num")
                TButton(text = "+1") {
                    add()
                }
                Text(
                    text = "Solution: Use 'by' delegate to automatically access the latest value",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        // Example 4: useAddCorrect3 - using useLatestRef
        ExampleCard(title = "4. Solution: Use useGetState") {
            val (num, add) = useAddCorrect3()
            Column {
                Text(text = "Current: $num")
                TButton(text = "+1") {
                    add()
                }
                Text(
                    text = "Solution: Use useGetState to get a function that always returns the latest value",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        // Example 5: useAddCorrect4 - using useGetState
        ExampleCard(title = "5. Solution: Use useLatestRef") {
            val (num, add) = useAddCorrect4()
            Column {
                Text(text = "Current: $num")
                TButton(text = "+1") {
                    add()
                }
                Text(
                    text = "Solution: Use useLatestRef to always access the latest value",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

/**
 * Example demonstrating basic computed properties with useState
 */
@Composable
private fun ComputedPropertiesExample() {
    val (state, setState) = useGetState(default = 0)
    val isBiggerThanFive = useState { state.value >= 5 }

    Column(modifier = Modifier.padding(bottom = 16.dp, start = 10.dp)) {
        Text(
            text = "This example demonstrates how computed properties work with useState.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        SimpleContainer {
            Row {
                TButton(text = "add1", onClick = { setState { it + 1 } })
                TButton(text = "add2", onClick = { setState { it + 2 } })
            }
        }
        SimpleContainer { Text("current: ${state.value}", modifier = Modifier) }
        SimpleContainer { Text("bigger than 5: ${isBiggerThanFive.value}", modifier = Modifier) }

        Text(
            text = "Note: When you click '+1', both child components will recompose (background colors change). When you click '+2', only the first component will recompose if the calculation result doesn't change (e.g., if state is already > 5).",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

/**
 * Example demonstrating async computed properties with useState
 * Shows the difference between lazy=true and lazy=false settings
 */
@Composable
private fun AsyncComputedPropertiesExample() {
    val (state, setState) = useGetState(default = 0)
    val (showLazy, toggleLazy) = useBoolean(default = false)
    val (showEager, toggleEager) = useBoolean(default = false)

    // Log to track when calculations happen
    val logs = useList<String>()
    val addLog = { message: String ->
        logs.add(message)
    }

    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            text = "This example demonstrates how async computed properties work with lazy loading.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        // Counter controls
        SimpleContainer {
            Row {
                TButton(
                    text = "Increment",
                    onClick = {
                        setState { it + 1 }
                        addLog("State incremented to ${state.value + 1}")
                    },
                )
                Spacer(modifier = Modifier.width(8.dp))
                TButton(
                    text = "Reset",
                    onClick = {
                        setState(0)
                        logs.clear()
                        addLog("State reset to 0")
                    },
                )
            }
        }

        SimpleContainer {
            Text(
                text = "Current value: ${state.value}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }

        // Configuration explanation
        Text(
            text = "Lazy vs Eager Loading:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
        )

        Text(
            text = "• Lazy (lazy=true): Calculation only happens when the value is first read",
            style = MaterialTheme.typography.bodySmall,
        )

        Text(
            text = "• Eager (lazy=false): Calculation happens immediately when dependencies change",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        // Lazy loading example
        SimpleContainer {
            Column {
                Row {
                    Text(
                        text = "Lazy Loading Example (lazy=true)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TButton(
                        text = if (showLazy.value) "Hide" else "Show",
                        onClick = {
                            toggleLazy()
                            if (!showLazy.value) {
                                addLog("Lazy value requested")
                            }
                        },
                    )
                }

                SimpleContainer {
                    val lazyComputed = useStateAsync(
                        optionsOf = {
                            lazy = true
                        },
                    ) {
                        addLog("Lazy calculation started with state=${state.value}")
                        delay(1.seconds)
                        addLog("Lazy calculation completed")
                        "Result after 1 second delay: ${state.value * 2}"
                    }
                    if (showLazy.value) {
                        Text(
                            text = lazyComputed.value ?: "Loading...",
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                        Text(
                            text = "Note: This calculation only happens when first shown or when explicitly read again.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }

        // Eager loading example
        SimpleContainer {
            Column {
                Row {
                    Text(
                        text = "Eager Loading Example (lazy=false)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TButton(
                        text = if (showEager.value) "Hide" else "Show",
                        onClick = {
                            toggleEager()
                        },
                    )
                }
                SimpleContainer {
                    // The eager computation happens regardless of whether we're showing it
                    val eagerComputed = useStateAsync(
                        state, // Adding state as a dependency
                        optionsOf = {
                            lazy = false
                            onError = { error ->
                                addLog("Error in eager calculation: ${error.message}")
                            }
                        },
                    ) {
                        addLog("Eager calculation started with state=${state.value}")
                        delay(1.seconds)
                        addLog("Eager calculation completed")
                        "Result after 1 second delay: ${state.value * 3}"
                    }
                    if (showEager.value) {
                        Text(
                            text = "Result: ${eagerComputed.value}",
                            modifier = Modifier.padding(vertical = 8.dp),
                        )

                        Text(
                            text = "Note: This calculation happens automatically whenever state changes, even if not shown.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }

        // Log display
        LogCard("Event Log:", logs = logs, limit = 20)
    }
}
