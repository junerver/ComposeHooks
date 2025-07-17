package xyz.junerver.composehooks.example

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.junerver.compose.hooks.PersistentContext
import xyz.junerver.compose.hooks.useControllable
import xyz.junerver.compose.hooks.useKeyboard
import xyz.junerver.compose.hooks.usePersistent
import xyz.junerver.composehooks.example.sub.PersistentVm
import xyz.junerver.composehooks.mmkvClear
import xyz.junerver.composehooks.mmkvGet
import xyz.junerver.composehooks.mmkvSave
import xyz.junerver.composehooks.route.useNavigate
import xyz.junerver.composehooks.ui.component.DividerSpacer
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description: Example demonstrating the usePersistent hook for state persistence
  Author: Junerver
  Date: 2024/4/10-14:44
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Example component demonstrating the usePersistent hook
 */
@Composable
fun UsePersistentExample() {
    Surface {
        ScrollColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Page title
            Text(
                text = "usePersistent Examples",
                style = MaterialTheme.typography.headlineMedium,
            )

            // Introduction text
            Text(
                text = "This hook provides a way to create and manage state that persists across component recompositions and app restarts. It supports both memory-based and custom persistence solutions.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            // Default persistence example
            ExampleCard(title = "Memory Persistence") {
                DefaultPersistent()
            }

            // MMKV persistence example
            ExampleCard(title = "Custom Persistence (MMKV)") {
                MMKVPersistent()
            }

            // Comparison with ViewModel
            ExampleCard(title = "Comparison with ViewModel") {
                VsViewModel()
            }
        }
    }
}

/**
 * Basic memory persistence example
 *
 * Demonstrates the basic usage of usePersistent hook with in-memory storage
 */
@Composable
private fun DefaultPersistent() {
    var count by usePersistent(key = "count", -1)

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(
            text = "Memory Persistence Example",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )

        Text(
            text = "This example uses the default in-memory persistence. State will be preserved during the app session but lost when the app is closed.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 8.dp),
        )

        Text(
            text = "Current count: $count",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = 8.dp),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TButton(text = "+1") {
                count += 1
            }

            TButton(text = "Reset") {
                count = -1
            }
        }

        DividerSpacer()

        SubShowCount()
    }
}

/**
 * Demonstrates accessing the same persistent state from a child component
 */
@Composable
private fun SubShowCount() {
    val (countState, _, clear) = usePersistent(key = "count", -1)

    Column {
        Text(
            text = "Child Component Access",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.secondary,
        )

        Text(
            text = "This demonstrates how the same persistent state can be accessed from different components.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 4.dp),
        )

        Text(
            text = "Persistent count from child: ${countState.value}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 4.dp).clickable { clear() },
        )

        Text(
            text = "(Click the text above to clear the count, when persistence is cleared, the default value will be displayed)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Custom persistence example using MMKV
 *
 * Demonstrates how to use a custom persistence solution with usePersistent hook
 */
@Composable
private fun MMKVPersistent() {
    // Using PersistentContext.Provider to override the default persistence implementation
    // with MMKV-based persistence functions for get, save, and clear operations
    PersistentContext.Provider(
        value = Triple(
            first = ::mmkvGet, // Custom get function
            second = ::mmkvSave, // Custom save function
            third = ::mmkvClear, // Custom clear function
        ),
    ) {
        val (hideKeyboard) = useKeyboard()
        var token by usePersistent(key = "token", "123")
        val (state, setState) = useControllable("")

        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = "MMKV Persistence Example",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )

            Text(
                text = "This example uses MMKV for persistent storage. State will be preserved even when the app is closed and reopened.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp),
            )

            Text(
                text = "Current token: $token",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 8.dp),
            )

            OutlinedTextField(
                value = state.value,
                onValueChange = setState,
                label = { Text("Enter new token") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TButton(text = "Save Token") {
                    hideKeyboard()
                    token = state.value
                    setState("")
                }

                TButton(text = "Reset") {
                    token = "123"
                    setState("")
                }
            }

            Text(
                text = "Note: You can exit and reopen the app, and your token will still be preserved.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp),
            )

            DividerSpacer()

            MMKVPersistentSub()
        }
    }
}

/**
 * Demonstrates accessing the same MMKV-persisted state from a child component
 * Also shows the use of forceUseMemory parameter
 */
@Composable
private fun MMKVPersistentSub() {
    val (token, _, clear) = usePersistent(key = "token", "321")
    var clearCount by usePersistent(key = "clearCount", 0, forceUseMemory = true)

    Column {
        Text(
            text = "Child Component with Mixed Persistence",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.secondary,
        )

        Text(
            text = "This demonstrates accessing the same token from a child component, plus a memory-only counter.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 4.dp),
        )

        Text(
            text = "MMKV-persisted token: ${token.value}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 4.dp).clickable {
                clear()
                clearCount += 1
            },
        )

        Text(
            text = "Memory-only clear count: $clearCount (using forceUseMemory=true)",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 4.dp),
        )

        Text(
            text = "(Click the token text above to clear it)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Comparison between usePersistent and ViewModel
 *
 * Demonstrates the differences and similarities between usePersistent hook and ViewModel
 */
@Composable
private fun VsViewModel() {
    var vsvm by usePersistent(key = "vsVm", "")
    val (state, setState) = useControllable("")
    val vm = viewModel { PersistentVm() }
    var vmstate by vm.vmState
    val nav = useNavigate()
    val (hideKeyboard) = useKeyboard()

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(
            text = "Comparing usePersistent with ViewModel",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )

        Text(
            text = "This example compares how state is managed and shared between components using usePersistent vs. ViewModel.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 8.dp),
        )

        Text(
            text = "State from usePersistent: $vsvm",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 4.dp),
        )

        Text(
            text = "State from ViewModel: $vmstate",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 4.dp),
        )

        OutlinedTextField(
            value = state.value,
            onValueChange = setState,
            label = { Text("Enter new state value") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TButton(text = "Update Both") {
                hideKeyboard()
                vsvm = state.value
                vmstate = state.value
                setState("")
            }

            TButton(text = "Navigate to Sub") {
                nav.navigate("PersistentSub")
            }
        }

        Text(
            text = "Note: Navigate to the sub-screen to see how state is preserved across navigation.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 8.dp),
        )

        DividerSpacer()

        VsViewModelSub()
    }
}

/**
 * Child component demonstrating state access from both usePersistent and ViewModel
 */
@Composable
private fun VsViewModelSub() {
    val vsvm by usePersistent(key = "vsVm", "")
    val vm = viewModel { PersistentVm() }
    val vmstate by vm.vmState

    Column {
        Text(
            text = "Child Component State Access",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.secondary,
        )

        Text(
            text = "This demonstrates how state from both usePersistent and ViewModel can be accessed from child components.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 4.dp),
        )

        Text(
            text = "State from usePersistent: $vsvm",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 4.dp),
        )

        Text(
            text = "State from ViewModel: $vmstate",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 4.dp),
        )

        Text(
            text = "Both approaches allow state sharing between components, but usePersistent offers more flexibility with custom persistence solutions.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
