package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.useGetState
import xyz.junerver.compose.hooks.useList
import xyz.junerver.compose.hooks.useUpdateEffect
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.LogCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.TButton
import xyz.junerver.composehooks.utils.now

/*
  Description: useUpdateEffect Hook 示例
  Author: Junerver
  Date: 2024/3/11-11:09
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun UseUpdateEffectExample() {
    ScrollColumn(
        modifier = Modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "useUpdateEffect Hook Examples",
            style = MaterialTheme.typography.headlineMedium,
        )

        Text(
            text = "useUpdateEffect is a hook similar to useEffect but skips running the effect for the first time.",
            style = MaterialTheme.typography.bodyMedium,
        )

        // Basic usage example
        ExampleCard(title = "Basic Usage") {
            BasicUpdateEffectExample()
        }

        // Multiple dependencies example
        ExampleCard(title = "Multiple Dependencies") {
            MultiDepsUpdateEffectExample()
        }

        // Logging example
        ExampleCard(title = "Logging Example") {
            LoggingUpdateEffectExample()
        }
    }
}

@Composable
fun BasicUpdateEffectExample() {
    val (count, setCount) = useGetState(0)
    val logs = useList<String>()

    useUpdateEffect(count) {
        // Skip first render, only execute when dependencies change
        val newLog = "useUpdateEffect triggered: count = ${count.value}"
        logs.add(newLog)
    }

    Column {
        Text(
            text = "Counter: ${count.value}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TButton(text = "+1") {
                setCount { it + 1 }
            }
            TButton(text = "Reset") {
                setCount { 0 }
                logs.clear()
            }
        }

        if (logs.isNotEmpty()) {
            LogCard(
                title = "Execution Logs",
                logs = logs,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
fun MultiDepsUpdateEffectExample() {
    val (name, setName) = useGetState("John")
    val (age, setAge) = useGetState(25)
    val logs = useList<String>()

    useUpdateEffect(name, age) {
        // Listen to changes in multiple dependencies
        val newLog = "User info updated: ${name.value}, ${age.value} years old"
        logs.add(newLog)
    }

    Column {
        Text(
            text = "User Info: ${name.value}, ${age.value} years old",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 8.dp),
        ) {
            TButton(text = "Change Name") {
                setName { if (it == "John") "Jane" else "John" }
            }
            TButton(text = "Age +1") {
                setAge { it + 1 }
            }
        }

        TButton(
            text = "Reset",
            modifier = Modifier.fillMaxWidth(),
        ) {
            setName { "John" }
            setAge { 25 }
            logs.clear()
        }

        if (logs.isNotEmpty()) {
            LogCard(
                title = "Update Logs",
                logs = logs,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
fun LoggingUpdateEffectExample() {
    val (status, setStatus) = useGetState("Initial State")
    val logs = useList<String>()

    useUpdateEffect(status) {
        // Record status change logs
        val timestamp = now()
        val newLog = "[$timestamp] Status changed to: ${status.value}"
        logs.add(newLog)
        println("useUpdateEffect: ${status.value}")
    }

    Column {
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        ) {
            Text(
                text = "Current Status: ${status.value}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp),
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 8.dp),
        ) {
            TButton(text = "Loading") {
                setStatus { "Loading..." }
            }
            TButton(text = "Success") {
                setStatus { "Load Success" }
            }
            TButton(text = "Error") {
                setStatus { "Load Failed" }
            }
        }

        TButton(
            text = "Clear Logs",
            modifier = Modifier.fillMaxWidth(),
        ) {
            logs.clear()
        }

        if (logs.isNotEmpty()) {
            LogCard(
                title = "Status Change Logs",
                logs = logs.takeLast(5), // Show only last 5 entries
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}
