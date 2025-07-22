package xyz.junerver.composehooks.example

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.junerver.compose.hooks.useList
import xyz.junerver.compose.hooks.useToggle
import xyz.junerver.compose.hooks.useToggleEither
import xyz.junerver.compose.hooks.useToggleVisible
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.LogCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description: Example component for useToggle hook
  Author: Junerver
  Date: 2024/3/11-9:19
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * UseToggleExample demonstrates the usage of toggle-related hooks.
 *
 * This example shows how to:
 * - Toggle between two values using useToggle
 * - Toggle between two different types using useToggleEither
 * - Toggle visibility of a component using useToggleVisible
 *
 * @author Junerver
 * @since 2024/3/11
 */
@Composable
fun UseToggleExample() {
    Surface {
        ScrollColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "useToggle Examples",
                style = MaterialTheme.typography.headlineMedium,
            )

            Text(
                text = "These hooks provide convenient ways to toggle between values, types, or component visibility.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            // Interactive Demo
            InteractiveToggleDemo()

            // Basic usage example
            ExampleCard(
                title = "Basic Toggle Example",
                content = { BasicToggleExample() },
            )

            // Toggle Either example
            ExampleCard(
                title = "Toggle Either Example",
                content = { ToggleEitherExample() },
            )

            // Toggle Visibility example
            ExampleCard(
                title = "Toggle Visibility Example",
                content = { ToggleVisibilityExample() },
            )

            // Practical application example
            ExampleCard(
                title = "Practical Application: Collapsible Panel",
                content = { CollapsiblePanelExample() },
            )
        }
    }
}

/**
 * Interactive demo for the useToggle hooks
 */
@Composable
fun InteractiveToggleDemo() {
    val logs = useList<String>()

    // Basic toggle between two strings
    val (stringValue, toggleString) = useToggle("hello", "world")

    // Toggle between different types
    val (eitherValue, toggleEither) = useToggleEither("example", 3.14)

    // Toggle component visibility
    val (component, toggleVisible) = useToggleVisible {
        Text(
            text = "This component can be toggled visible/invisible",
            modifier = Modifier.padding(8.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }

    ExampleCard(title = "Interactive Demo") {
        Column(modifier = Modifier.fillMaxWidth().padding(8.dp).padding(8.dp)) {
            // String toggle section
            Text(
                text = "String Toggle: $stringValue",
                style = MaterialTheme.typography.bodyLarge,
            )

            TButton(text = "Toggle String") {
                toggleString()
                logs.add("Toggled string to $stringValue")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Either toggle section
            Text(
                text = "Either Toggle: ${
                    eitherValue.fold(
                        { "String: $it" },
                        { "Double: $it" },
                    )
                }",
                style = MaterialTheme.typography.bodyLarge,
            )

            TButton(text = "Toggle Either") {
                toggleEither()
                logs.add("Toggled either type")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Visibility toggle section
            Text(
                text = "Component Visibility Toggle:",
                style = MaterialTheme.typography.bodyLarge,
            )

            TButton(text = "Toggle Visibility") {
                toggleVisible()
                logs.add("Toggled component visibility")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // The component with toggled visibility
            component()

            Spacer(modifier = Modifier.height(16.dp))

            LogCard(title = "Toggle Operations:", logs = logs)
        }
    }
}

/**
 * BasicToggleExample demonstrates the basic usage of useToggle hook.
 *
 * The useToggle hook provides a boolean state and a function to toggle it.
 */
@Composable
fun BasicToggleExample() {
    // Keep track of toggle operations for logging
    val logs = useList<String>()

    // Use the useToggle hook to toggle between false and true
    val (value, toggle) = useToggle(defaultValue = false, reverseValue = true)

    Column(modifier = Modifier.fillMaxWidth().padding(8.dp).padding(8.dp)) {
        Text(
            text = "Current Value: $value",
            style = MaterialTheme.typography.bodyLarge,
        )

        Row(modifier = Modifier.padding(vertical = 8.dp)) {
            TButton(text = "Toggle") {
                toggle()
                logs.add("Toggled to $value")
            }

            Spacer(modifier = Modifier.weight(1f))

            TButton(text = "Set True") {
                // Note: useToggle's toggle function doesn't accept parameters
                // It only toggles between the two predefined values
                toggle()
                logs.add("Toggled value")
            }

            Spacer(modifier = Modifier.weight(0.2f))

            TButton(text = "Set False") {
                // Note: useToggle's toggle function doesn't accept parameters
                // It only toggles between the two predefined values
                toggle()
                logs.add("Toggled value")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LogCard(title = "Toggle Operations:", logs = logs)
    }
}

/**
 * ToggleEitherExample demonstrates the usage of useToggleEither hook.
 *
 * The useToggleEither hook allows toggling between two different types of values.
 * In this example, we toggle between a String and an Int.
 */
@Composable
fun ToggleEitherExample() {
    // Keep track of toggle operations for logging
    val logs = useList<String>()

    // Use the useToggleEither hook with initial String and Int values
    val (value, toggle) = useToggleEither("Hello World", 42)

    // Format the current value based on its type
    val displayValue = value.fold(
        { str -> "String: \"$str\"" },
        { num -> "Integer: $num" },
    )

    Column(modifier = Modifier.fillMaxWidth().padding(8.dp).padding(8.dp)) {
        Text(
            text = "Current Value: $displayValue",
            style = MaterialTheme.typography.bodyLarge,
        )

        Row(modifier = Modifier.padding(vertical = 8.dp)) {
            TButton(text = "Toggle") {
                // Note: useToggleEither's toggle function doesn't accept parameters
                // It only toggles between the two predefined values
                toggle()
                logs.add("Toggled between String and Integer")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LogCard(title = "Toggle Operations:", logs = logs)
    }
}

/**
 * ToggleVisibilityExample demonstrates the usage of useToggleVisible hook.
 *
 * The useToggleVisible hook provides a boolean state for visibility control
 * and a function to toggle it. It's particularly useful for showing/hiding UI elements.
 */
@Composable
fun ToggleVisibilityExample() {
    // Keep track of toggle operations for logging
    val logs = useList<String>()

    // Use the useToggleVisible hook to toggle between showing and hiding a component
    val (component, toggleVisible) = useToggleVisible(true) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "This is a toggleable component",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "The useToggleVisible hook makes it easy to control component visibility",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(8.dp).padding(8.dp)) {
        Text(
            text = "Toggle component visibility:",
            style = MaterialTheme.typography.bodyLarge,
        )

        Row(modifier = Modifier.padding(vertical = 8.dp)) {
            TButton(text = "Toggle Visibility") {
                toggleVisible()
                logs.add("Toggled component visibility")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // The component returned by useToggleVisible
        component()

        Spacer(modifier = Modifier.height(8.dp))

        LogCard(title = "Visibility Operations:", logs = logs)
    }
}

/**
 * CollapsiblePanelExample demonstrates a practical application of the useToggleVisible hook.
 *
 * This example shows how to create a collapsible panel component that can be expanded or collapsed
 * with a header that indicates the current state.
 */
@Composable
fun CollapsiblePanelExample() {
    // Keep track of toggle operations for logging
    val logs = useList<String>()

    Column(modifier = Modifier.fillMaxWidth().padding(8.dp).padding(8.dp)) {
        Text(
            text = "Collapsible Panels",
            style = MaterialTheme.typography.bodyLarge,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // First collapsible panel
        CollapsiblePanel(
            title = "Panel 1: Features Overview",
            initialExpanded = true,
            onToggle = { expanded -> logs.add("Panel 1 ${if (expanded) "expanded" else "collapsed"}") },
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Key Features of useToggle Hooks:",
                    style = MaterialTheme.typography.bodyLarge,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("• Simple boolean toggle with useToggle")
                Text("• Type switching with useToggleEither")
                Text("• Visibility control with useToggleVisible")
                Text("• All hooks provide controlled state management")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Second collapsible panel
        CollapsiblePanel(
            title = "Panel 2: Implementation Details",
            initialExpanded = false,
            onToggle = { expanded -> logs.add("Panel 2 ${if (expanded) "expanded" else "collapsed"}") },
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Implementation Notes:",
                    style = MaterialTheme.typography.bodyLarge,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("• Hooks are implemented using Compose's remember and mutableStateOf")
                Text("• They provide a convenient API for common UI patterns")
                Text("• Each hook returns both the current state and a function to update it")
                Text("• The implementation is lightweight and efficient")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Third collapsible panel
        CollapsiblePanel(
            title = "Panel 3: Usage Examples",
            initialExpanded = false,
            onToggle = { expanded -> logs.add("Panel 3 ${if (expanded) "expanded" else "collapsed"}") },
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Common Usage Patterns:",
                    style = MaterialTheme.typography.bodyLarge,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("• Toggle buttons and switches with useToggle")
                Text("• Theme switching (light/dark) with useToggle")
                Text("• Error/Success state handling with useToggleEither")
                Text("• Expandable sections with useToggleVisible")
                Text("• Modal dialogs and popovers with useToggleVisible")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LogCard(title = "Panel Operations:", logs = logs)
    }
}

/**
 * CollapsiblePanel is a reusable component that implements a collapsible panel.
 *
 * This component demonstrates the correct usage of useToggleVisible hook,
 * which returns a component function that can be called to render content conditionally.
 *
 * @param title The title to display in the panel header
 * @param initialExpanded Whether the panel should be initially expanded
 * @param onToggle Callback that is invoked when the panel is expanded or collapsed
 * @param content The content to display when the panel is expanded
 */
@Composable
fun CollapsiblePanel(
    title: String,
    initialExpanded: Boolean = false,
    onToggle: (Boolean) -> Unit = {},
    content: @Composable () -> Unit,
) {
    // Use the useToggleVisible hook to control the expanded/collapsed state
    // This returns a component function and a toggle function
    val (contentComponent, toggleContent) = useToggleVisible(initialExpanded, content)

    // Track the current state for the button text and callback
    val (isExpanded, toggleExpanded) = useToggle(initialExpanded, !initialExpanded)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column {
            // Panel header - always visible
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.weight(1f),
                    )

                    TButton(
                        text = if (isExpanded == true) "Collapse" else "Expand",
                        onClick = {
                            toggleContent() // Toggle the content visibility
                            toggleExpanded() // Toggle the state for button text
                            onToggle(isExpanded != true) // Call the callback with new state
                        },
                    )
                }
            }

            // Panel content - rendered by the component returned from useToggleVisible
            contentComponent()
        }
    }
}
