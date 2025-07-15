package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.random.Random
import xyz.junerver.compose.hooks.getValue
import xyz.junerver.compose.hooks.useCreation
import xyz.junerver.compose.hooks.useRef
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.useUpdate
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description: Example component for useCreation hook
  Author: Junerver
  Date: 2024/3/11-11:36
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Example data class that logs when it's instantiated
 */
data class Subject(val flag: String) {
    init {
        println("UseCreationExample Subject is be instantiated：$flag")
    }
}

/**
 * Example component demonstrating the useCreation hook
 */
@Composable
fun UseCreationExample() {
    ScrollColumn(
        modifier = Modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "useCreation Examples",
            style = MaterialTheme.typography.headlineMedium,
        )

        // Interactive Demo
        InteractiveCreationDemo()

        // Basic usage example
        ExampleCard(title = "Basic Usage") {
            BasicCreationExample()
        }

        // Comparison with useRef
        ExampleCard(title = "Comparison with useRef") {
            ComparisonExample()
        }

        // Practical application example
        ExampleCard(title = "Practical Application: Memoized Calculation") {
            MemoizedCalculationExample()
        }
    }
}

/**
 * Interactive demo showing how useCreation works with dependencies
 */
@Composable
private fun InteractiveCreationDemo() {
    var dependency by useState("Change me")
    var recomposeCounter by useState(0)
    
    // Create an object with useCreation that depends on the dependency value
    val createdObject by useCreation(dependency) {
        Subject("Created with dependency: $dependency")
    }
    
    // Update function to force recomposition
    val update = useUpdate()
    
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
            
            // Display current object
            Text(
                text = "Current Object:",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
            )
            
            Text(
                text = createdObject.flag,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp),
            )
            
            // Recompose counter
            Text(
                text = "Recompose Count: $recomposeCounter",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            
            // Dependency input
            Text(
                text = "Dependency Value:",
                style = MaterialTheme.typography.bodyMedium,
            )
            
            OutlinedTextField(
                value = dependency,
                onValueChange = { dependency = it },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            )
            
            // Control buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                TButton(
                    text = "Force Recompose",
                    onClick = { 
                        update()
                        recomposeCounter++
                    },
                    modifier = Modifier.weight(1f),
                )
                
                TButton(
                    text = "Change Dependency",
                    onClick = { 
                        dependency = "Changed ${Random.nextInt(100)}"
                        recomposeCounter++
                    },
                    modifier = Modifier.weight(1f),
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Note: Check the console log to see when the Subject is instantiated. It will only be created when the dependency changes, not on every recomposition.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

/**
 * Basic example showing how to use useCreation
 */
@Composable
private fun BasicCreationExample() {
    // Create an object with useCreation
    val createdObject by useCreation {
        Subject("Basic example object")
    }
    
    // Update function to force recomposition
    val update = useUpdate()
    
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Created Object: ${createdObject.flag}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp),
        )
        
        TButton(
            text = "Force Recompose",
            onClick = { update() },
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "The object is only created once and persists across recompositions.",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

/**
 * Example comparing useCreation with useRef
 */
@Composable
private fun ComparisonExample() {
    // Create objects with useRef and useCreation
    val refObject by useRef(Subject("useRef object ${Random.nextDouble()}"))
    val creationObject by useCreation {
        Subject("useCreation object ${Random.nextDouble()}")
    }
    
    // Update function to force recomposition
    val update = useUpdate()
    var recomposeCount by useState(0)
    
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "useRef Object: ${refObject.flag}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        
        Text(
            text = "useCreation Object: ${creationObject.flag}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp),
        )
        
        Text(
            text = "Recompose Count: $recomposeCount",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        
        TButton(
            text = "Force Recompose",
            onClick = { 
                update()
                recomposeCount++
            },
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Note: Check the console log. useRef creates a new Subject instance on every recomposition, while useCreation only creates it once.",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

/**
 * Example showing a practical application of useCreation for memoized calculations
 */
@Composable
private fun MemoizedCalculationExample() {
    var inputA by useState(5)
    var inputB by useState(10)
    
    // Use useCreation to memoize an expensive calculation
    val result by useCreation(inputA, inputB) {
        // Simulate an expensive calculation
        println("Performing expensive calculation with $inputA and $inputB")
        calculateExpensiveResult(inputA, inputB)
    }
    
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Result: $result",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp),
        )
        
        Text(
            text = "Input A: $inputA",
            style = MaterialTheme.typography.bodyMedium,
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 8.dp),
        ) {
            TButton(text = "+1", onClick = { inputA++ })
            TButton(text = "-1", onClick = { inputA-- })
        }
        
        Text(
            text = "Input B: $inputB",
            style = MaterialTheme.typography.bodyMedium,
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 8.dp),
        ) {
            TButton(text = "+1", onClick = { inputB++ })
            TButton(text = "-1", onClick = { inputB-- })
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "The expensive calculation is only performed when inputA or inputB changes, not on every recomposition.",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

/**
 * Simulates an expensive calculation
 */
private fun calculateExpensiveResult(a: Int, b: Int): Int {
    // In a real application, this would be a complex calculation
    return a * b
}
