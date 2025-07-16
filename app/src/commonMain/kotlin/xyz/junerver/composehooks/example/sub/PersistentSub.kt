package xyz.junerver.composehooks.example.sub

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.junerver.compose.hooks.usePersistent
import xyz.junerver.composehooks.route.useNavigate
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description: Sub-screen demonstrating persistent state across navigation
  Author: Junerver
  Date: 2024/4/11-11:34
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * PersistentSub component
 *
 * Demonstrates how persistent state is maintained across navigation
 * Compares usePersistent hook with ViewModel for state persistence
 */
@Composable
fun PersistentSub() {
    val vsvm by usePersistent(key = "vsVm", "")
    val vm = viewModel { PersistentVm() }
    val vmstate by vm.vmState
    val nav = useNavigate()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Page title
            Text(
                text = "Persistent State Navigation Example",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
            )

            // Description
            Text(
                text = "This screen demonstrates how state is preserved when navigating between screens.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            // State display section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            ) {
                Text(
                    text = "State Values",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )

                Text(
                    text = "State from usePersistent: $vsvm",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                )

                Text(
                    text = "State from ViewModel: $vmstate",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }

            // Navigation button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                TButton(text = "Return to Examples") {
                    nav.popBackStack()
                }
            }
        }
    }
}
