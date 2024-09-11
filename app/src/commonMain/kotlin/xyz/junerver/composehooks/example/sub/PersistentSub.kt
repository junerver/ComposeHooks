package xyz.junerver.composehooks.example.sub

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.junerver.compose.hooks.usePersistent

/*
  Description:
  Author: Junerver
  Date: 2024/4/11-11:34
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun PersistentSub() {
    val (vsvm, _) = usePersistent(key = "vsVm", "")
    val vm = viewModel { PersistentVm() }
    val vmstate by vm.vmState

    Surface {
        Column {
            Text(text = "PersistentSub component:", modifier = Modifier.padding(20.dp))
            Text(text = "state from persistent: $vsvm")
            Text(text = "state from vm: $vmstate")
        }
    }
}
